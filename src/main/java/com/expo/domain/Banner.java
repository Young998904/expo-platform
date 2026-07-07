package com.expo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * VIP 배너(고객 홈 상단 노출). 과금·클릭추적은 범위 밖이며, priority 오름차순으로 노출한다.
 */
@Entity
@Table(name = "banner")
@Getter
@Setter
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String imageUrl;

    private String linkUrl;

    // 노출 우선순위(작을수록 먼저)
    @Column(nullable = false)
    private int priority = 100;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
