package com.expo.service;

import com.expo.domain.*;
import com.expo.repository.CustomerRepository;
import com.expo.repository.ExpoRepository;
import com.expo.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 예약 생성·조회. 좌석 잔여는 CONFIRMED·CHECKED_IN 인원 합 기준으로 계산한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private static final List<ReservationStatus> OCCUPYING =
            List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);

    private final ReservationRepository reservationRepository;
    private final ExpoRepository expoRepository;
    private final CustomerRepository customerRepository;

    /** 남은 좌석 = 총 좌석 − 점유(CONFIRMED·CHECKED_IN) 인원 합. */
    @Transactional(readOnly = true)
    public int remainingSeats(Expo expo) {
        int occupied = reservationRepository.sumHeadcount(expo.getId(), OCCUPYING);
        return Math.max(0, expo.getCapacity() - occupied);
    }

    @Transactional(readOnly = true)
    public List<Reservation> listByCustomer(Long customerId) {
        return reservationRepository.findByCustomerWithExpo(customerId);
    }

    /** 박람회 예약자 명단(이름/전화/예약번호 검색 + 상태 필터). */
    @Transactional(readOnly = true)
    public List<Reservation> listForExpo(Long expoId, String keyword, ReservationStatus status) {
        if (expoId == null) {
            return List.of();
        }
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        return reservationRepository.findByExpoWithCustomer(expoId).stream()
                .filter(r -> status == null || r.getStatus() == status)
                .filter(r -> kw.isEmpty()
                        || r.getCustomer().getName().toLowerCase().contains(kw)
                        || (r.getCustomer().getPhone() != null && r.getCustomer().getPhone().contains(kw))
                        || r.getReservationNo().toLowerCase().contains(kw))
                .toList();
    }

    /** 예약 취소(관리자). 입장 완료 건은 취소할 수 없다. */
    public void cancel(Long id, Long managedExpoId) {
        Reservation r = getDetail(id);
        if (managedExpoId != null && !r.getExpo().getId().equals(managedExpoId)) {
            throw new IllegalStateException("담당 박람회의 예약이 아닙니다.");
        }
        if (r.getStatus() == ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("입장 완료된 예약은 취소할 수 없습니다.");
        }
        r.setStatus(ReservationStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public Reservation getDetail(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    }

    /**
     * 예약 생성(결제대기). 남은 좌석을 확인하되, 좌석 점유는 결제 확정 시점에 이뤄진다.
     */
    public Long create(Long customerId, Long expoId, int headcount, String contactPhone) {
        if (headcount < 1) {
            throw new IllegalArgumentException("인원은 1명 이상이어야 합니다.");
        }
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new IllegalArgumentException("박람회를 찾을 수 없습니다."));
        if (expo.getStatus() != ExpoStatus.OPEN) {
            throw new IllegalStateException("예약할 수 없는 행사입니다.");
        }
        if (headcount > remainingSeats(expo)) {
            throw new IllegalStateException("남은 좌석이 부족합니다.");
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객 정보를 확인할 수 없습니다."));

        Reservation reservation = new Reservation();
        reservation.setExpo(expo);
        reservation.setCustomer(customer);
        reservation.setHeadcount(headcount);
        reservation.setContactPhone(contactPhone);
        reservation.setAmount(expo.getPrice() * headcount);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setReservationNo(generateReservationNo());
        return reservationRepository.save(reservation).getId();
    }

    private String generateReservationNo() {
        return "RSV-" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
    }

    /**
     * 결제 확정. PENDING → CONFIRMED로 전이하며 좌석을 최종 재검증하고 QR용 체크인 토큰을 발급한다.
     * 이미 확정된 예약이면 아무 것도 하지 않는다(콜백 재진입 멱등성).
     */
    public void confirmPayment(Long reservationId, PaymentProvider provider, String paymentId) {
        Reservation r = getDetail(reservationId);
        if (r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.CHECKED_IN) {
            return;
        }
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("결제할 수 없는 예약 상태입니다.");
        }
        if (r.getHeadcount() > remainingSeats(r.getExpo())) {
            throw new IllegalStateException("좌석이 마감되어 결제를 완료할 수 없습니다.");
        }
        r.setStatus(ReservationStatus.CONFIRMED);
        r.setPaidAt(LocalDateTime.now());
        r.setCheckinToken(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());

        Payment payment = new Payment();
        payment.setProvider(provider);
        payment.setPaymentId(paymentId);
        payment.setAmount(r.getAmount());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(r.getPaidAt());
        r.addPayment(payment);
    }

    /**
     * 현장 체크인. 체크인 코드 또는 예약번호로 예약을 찾아 CHECKED_IN으로 전이한다.
     * @param managedExpoId 담당 박람회 제한(null이면 전체 허용, 전체 관리자)
     */
    public Reservation checkIn(String code, Long managedExpoId) {
        String key = code == null ? "" : code.trim();
        Reservation r = reservationRepository.findByCheckinToken(key)
                .or(() -> reservationRepository.findByReservationNo(key))
                .orElseThrow(() -> new IllegalArgumentException("해당 코드의 예약을 찾을 수 없습니다."));
        if (managedExpoId != null && !r.getExpo().getId().equals(managedExpoId)) {
            throw new IllegalStateException("담당 박람회의 예약이 아닙니다.");
        }
        if (r.getStatus() == ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("이미 체크인된 예약입니다.");
        }
        if (r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 예약만 체크인할 수 있습니다.");
        }
        r.setStatus(ReservationStatus.CHECKED_IN);
        r.setCheckedInAt(LocalDateTime.now());
        return r;
    }
}
