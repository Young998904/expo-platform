package com.expo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 예약. 좌석 점유·매출 집계는 모두 이 엔티티(CONFIRMED·CHECKED_IN) 기준으로 한다.
 */
@Entity
@Table(name = "reservation")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private int headcount;

    private String contactPhone;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(nullable = false, unique = true)
    private String reservationNo;

    // 예약 확정 시 발급되는 입장 확인용 토큰(QR payload 구성)
    private String checkinToken;

    private LocalDateTime paidAt;

    private LocalDateTime checkedInAt;

    // 결제 기록(애그리거트 하위). 예약을 통해 cascade로 저장된다.
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 결제 기록을 예약에 추가한다(양방향 연관 설정). */
    public void addPayment(Payment payment) {
        payment.setReservation(this);
        payments.add(payment);
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
