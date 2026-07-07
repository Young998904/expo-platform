package com.expo.service;

import com.expo.domain.*;
import com.expo.repository.CustomerRepository;
import com.expo.repository.ExpoRepository;
import com.expo.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
