package com.expo.config;

import com.expo.domain.*;
import com.expo.repository.AdminUserRepository;
import com.expo.repository.BannerRepository;
import com.expo.repository.CustomerRepository;
import com.expo.repository.ExpoRepository;
import com.expo.repository.ReservationRepository;
import com.expo.repository.TrainingAssignmentRepository;
import com.expo.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * QA/시연용 더미 데이터 시드. 모든 역할·박람회 상태·예약 상태·결제·배너·LMS 수강 상태를
 * 한 번에 채워 전체 기능을 바로 점검할 수 있게 한다. 계정이 하나라도 있으면(=이미 시드됨) 건너뛴다.
 *
 * 로그인 —
 *   전체 관리자      admin  / admin123
 *   박람회 관리자    event1 / event123 (리빙 디자인 페어 담당)
 *                    event2 / event123 (커피 엑스포 담당)
 *                    event3 / event123 (비활성 계정 · 로그인 차단 확인용)
 *   고객            01012345678 / PIN 1234 (김참가, 예약 전 상태 보유)
 *                    01023456789 · 01034567890 · 01045678901 · 01056789012 · 01067890123 (PIN 모두 1234)
 *
 * 현장 체크인 코드는 예약번호(RSV-####) 또는 체크인 토큰(CHK-####) 모두 사용 가능.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final ExpoRepository expoRepository;
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final BannerRepository bannerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingAssignmentRepository trainingAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    // 예약번호·결제식별자를 사람이 읽기 쉬운 순번으로 발급하기 위한 카운터
    private int resSeq = 1000;
    private int paySeq = 100000;

    @Override
    public void run(ApplicationArguments args) {
        // 이미 시드된 DB에는 손대지 않는다(중복·꼬임 방지). 처음부터 다시 채우려면 H2 파일을 지우고 재기동한다.
        if (adminUserRepository.count() > 0) {
            return;
        }

        // ── 박람회(모든 상태 커버) ─────────────────────────────────────────────
        Expo living = expo("2026 서울 리빙 디자인 페어", "생활·인테리어", "코엑스 A홀",
                15000, 200, LocalDateTime.now().plusDays(20), ExpoStatus.OPEN);
        Expo coffee = expo("2026 국제 커피 엑스포", "식음료", "벡스코 제2전시장",
                12000, 150, LocalDateTime.now().plusDays(35), ExpoStatus.OPEN);
        Expo pet = expo("2026 반려동물 페스타(좌석 임박)", "라이프", "세택(SETEC) 1관",
                8000, 10, LocalDateTime.now().plusDays(10), ExpoStatus.OPEN);
        Expo smart = expo("2026 스마트테크 위크(준비중)", "IT·가전", "킨텍스 1홀",
                0, 300, LocalDateTime.now().plusDays(60), ExpoStatus.DRAFT);
        Expo past = expo("2025 가을 리빙 페어(마감)", "생활·인테리어", "코엑스 B홀",
                10000, 100, LocalDateTime.now().minusDays(30), ExpoStatus.CLOSED);
        expo("2025 프리미엄 아트페어(비공개)", "문화·예술", "예술의전당",
                20000, 50, LocalDateTime.now().minusDays(10), ExpoStatus.HIDDEN);

        // ── 관리자 계정(역할·활성 여부) ────────────────────────────────────────
        admin("admin", "admin123", "전체 관리자", Role.SUPER_ADMIN, true, null);
        admin("event1", "event123", "리빙페어 운영", Role.EVENT_ADMIN, true, living.getId());
        admin("event2", "event123", "커피엑스포 운영", Role.EVENT_ADMIN, true, coffee.getId());
        admin("event3", "event123", "퇴사자(비활성)", Role.EVENT_ADMIN, false, past.getId());

        // ── 고객 ──────────────────────────────────────────────────────────────
        Customer c1 = customer("김참가", "01012345678");
        Customer c2 = customer("이수민", "01023456789");
        Customer c3 = customer("박도윤", "01034567890");
        Customer c4 = customer("최지아", "01045678901");
        Customer c5 = customer("정하준", "01056789012");
        Customer c6 = customer("강서연", "01067890123");

        // ── 예약(모든 상태 · 결제수단 혼합) ────────────────────────────────────
        // 리빙 디자인 페어: 입장완료 → 예약확정 → 결제대기 → 취소요청 → 취소됨 골고루
        reservation(living, c2, 2, ReservationStatus.CHECKED_IN, PaymentProvider.PORTONE, 5);
        reservation(living, c3, 1, ReservationStatus.CHECKED_IN, PaymentProvider.MOCK, 4);
        reservation(living, c1, 1, ReservationStatus.CONFIRMED, PaymentProvider.PORTONE, 3); // 김참가: QR·체크인 확인용
        reservation(living, c4, 2, ReservationStatus.CONFIRMED, PaymentProvider.MOCK, 2);
        reservation(living, c5, 1, ReservationStatus.CONFIRMED, PaymentProvider.PORTONE, 2);
        reservation(living, c1, 2, ReservationStatus.PENDING, PaymentProvider.MOCK, 1);      // 김참가: 이어결제 확인용
        reservation(living, c6, 1, ReservationStatus.PENDING, PaymentProvider.MOCK, 1);
        reservation(living, c4, 2, ReservationStatus.CANCEL_REQUESTED, PaymentProvider.PORTONE, 3);
        reservation(living, c1, 1, ReservationStatus.CANCELLED, PaymentProvider.MOCK, 6);    // 김참가: 취소 이력

        // 커피 엑스포(event2 담당 명단용)
        reservation(coffee, c5, 1, ReservationStatus.CHECKED_IN, PaymentProvider.PORTONE, 4);
        reservation(coffee, c1, 1, ReservationStatus.CONFIRMED, PaymentProvider.PORTONE, 2); // 김참가: 다른 행사 예약
        reservation(coffee, c2, 2, ReservationStatus.CONFIRMED, PaymentProvider.MOCK, 2);
        reservation(coffee, c3, 1, ReservationStatus.PENDING, PaymentProvider.MOCK, 1);

        // 반려동물 페스타(좌석 10석 중 9석 점유 → 잔여 1석, 좌석 부족 예약 시도 확인용)
        reservation(pet, c3, 3, ReservationStatus.CONFIRMED, PaymentProvider.MOCK, 2);
        reservation(pet, c4, 3, ReservationStatus.CONFIRMED, PaymentProvider.PORTONE, 2);
        reservation(pet, c5, 3, ReservationStatus.CHECKED_IN, PaymentProvider.MOCK, 3);

        // ── VIP 배너(활성/비활성/노출기간) ─────────────────────────────────────
        banner("2026 서울 리빙 디자인 페어 · 사전예약 오픈", 10, true, null, null);
        banner("반려동물 페스타 · 마감 임박, 서두르세요", 15, true,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7));
        banner("국제 커피 엑스포 · 얼리버드 특가", 20, true, null, null);
        banner("(종료) 봄맞이 프로모션", 30, false, null, null); // 비활성 → 홈 미노출 확인용

        // ── LMS: 교육 2종 + 수강 상태 골고루 ───────────────────────────────────
        AdminUser adminUser = adminUserRepository.findByUsername("admin").orElseThrow();
        AdminUser ev1 = adminUserRepository.findByUsername("event1").orElseThrow();
        AdminUser ev2 = adminUserRepository.findByUsername("event2").orElseThrow();

        Training t1 = training("박람회 운영 · 현장 체크인 절차 교육",
                "입장 배지 확인과 현장 체크인 절차를 안내합니다. 영상을 끝까지 시청하면 이수됩니다.",
                "https://www.youtube.com/watch?v=aqz-KE-bpKQ", "aqz-KE-bpKQ", adminUser);
        Training t2 = training("박람회 안전관리 및 고객 응대 교육",
                "현장 안전 수칙과 고객 응대 요령을 다룹니다. 순서대로 끝까지 시청해야 이수 처리됩니다.",
                "https://www.youtube.com/watch?v=M7lc1UVf-VE", "M7lc1UVf-VE", adminUser);

        // event1: 수강중(이어보기·앞으로가기 제지 확인) + 미시작
        assignment(t1, ev1, TrainingStatus.IN_PROGRESS, 42.0, 130, 310, null);
        assignment(t2, ev1, TrainingStatus.NOT_STARTED, 0.0, 0, 0, null);
        // event2: 이수완료 + 수강중 (관리자 수강율 화면 분포 확인)
        assignment(t1, ev2, TrainingStatus.COMPLETED, 100.0, 310, 310, LocalDateTime.now().minusDays(1));
        assignment(t2, ev2, TrainingStatus.IN_PROGRESS, 60.0, 198, 330, null);
    }

    // ── 헬퍼 ───────────────────────────────────────────────────────────────────

    private Expo expo(String title, String category, String venue,
                      int price, int capacity, LocalDateTime start, ExpoStatus status) {
        Expo e = new Expo();
        e.setCode("EXPO-" + Long.toString(System.nanoTime(), 36).toUpperCase());
        e.setTitle(title);
        e.setDescription(title + " · 사전 예약제로 운영되는 박람회입니다.");
        e.setCategory(category);
        e.setVenue(venue);
        e.setStartAt(start);
        e.setEndAt(start.plusDays(3));
        e.setPrice(price);
        e.setCapacity(capacity);
        e.setStatus(status);
        return expoRepository.save(e);
    }

    private void admin(String username, String rawPw, String name, Role role, boolean active, Long managedExpoId) {
        AdminUser u = new AdminUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPw));
        u.setName(name);
        u.setRole(role);
        u.setActive(active);
        u.setManagedExpoId(managedExpoId);
        adminUserRepository.save(u);
    }

    private Customer customer(String name, String phone) {
        Customer c = new Customer();
        c.setName(name);
        c.setPhone(phone);
        c.setPin(passwordEncoder.encode("1234"));
        return customerRepository.save(c);
    }

    /**
     * 예약 1건을 상태에 맞게 생성한다. 결제까지 진행된 상태(확정·입장·취소요청)에는 체크인 토큰과
     * 결제(PAID) 기록을, 결제 후 취소(CANCELLED)에는 취소된 결제 기록을 남긴다.
     */
    private void reservation(Expo expo, Customer cust, int headcount,
                             ReservationStatus status, PaymentProvider provider, int daysAgo) {
        resSeq++;
        LocalDateTime created = LocalDateTime.now().minusDays(daysAgo);

        Reservation r = new Reservation();
        r.setExpo(expo);
        r.setCustomer(cust);
        r.setHeadcount(headcount);
        r.setContactPhone(cust.getPhone());
        r.setAmount(expo.getPrice() * headcount);
        r.setStatus(status);
        r.setReservationNo(String.format("RSV-%04d", resSeq));
        r.setCreatedAt(created);

        boolean paidThrough = status == ReservationStatus.CONFIRMED
                || status == ReservationStatus.CHECKED_IN
                || status == ReservationStatus.CANCEL_REQUESTED;
        if (paidThrough) {
            r.setCheckinToken(String.format("CHK-%04d", resSeq));
            r.setPaidAt(created.plusMinutes(5));
            r.addPayment(payment(provider, r.getAmount(), PaymentStatus.PAID, r.getPaidAt()));
        }
        if (status == ReservationStatus.CHECKED_IN) {
            // 행사 당일 입장했다고 가정(생성일 다음 날)
            r.setCheckedInAt(created.plusDays(1));
        }
        if (status == ReservationStatus.CANCELLED) {
            r.setPaidAt(created.plusMinutes(5));
            r.addPayment(payment(provider, r.getAmount(), PaymentStatus.CANCELLED, null));
        }
        reservationRepository.save(r); // 결제는 cascade로 함께 저장
    }

    private Payment payment(PaymentProvider provider, int amount, PaymentStatus status, LocalDateTime paidAt) {
        paySeq++;
        Payment p = new Payment();
        p.setProvider(provider);
        p.setPaymentId(String.format("PAY-%06d", paySeq));
        p.setAmount(amount);
        p.setStatus(status);
        p.setPaidAt(paidAt);
        return p;
    }

    private void banner(String title, int priority, boolean active, LocalDateTime startAt, LocalDateTime endAt) {
        Banner b = new Banner();
        b.setTitle(title);
        b.setPriority(priority);
        b.setActive(active);
        b.setStartAt(startAt);
        b.setEndAt(endAt);
        bannerRepository.save(b);
    }

    private Training training(String title, String description, String url, String videoId, AdminUser creator) {
        Training t = new Training();
        t.setTitle(title);
        t.setDescription(description);
        t.setYoutubeUrl(url);
        t.setYoutubeVideoId(videoId);
        t.setCreatedBy(creator);
        return trainingRepository.save(t);
    }

    private void assignment(Training training, AdminUser assignee, TrainingStatus status,
                            double rate, int lastPos, int duration, LocalDateTime completedAt) {
        TrainingAssignment a = new TrainingAssignment();
        a.setTraining(training);
        a.setAssignee(assignee);
        a.setStatus(status);
        a.setProgressRate(rate);
        a.setLastPositionSec(lastPos);
        a.setDurationSec(duration);
        a.setCompletedAt(completedAt);
        trainingAssignmentRepository.save(a);
    }
}
