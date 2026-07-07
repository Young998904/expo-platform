package com.expo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 교육 배정/수강 기록. 교육↔관리자 조인 엔티티이며 시청률을 비정규화 저장한다.
 * 동일 교육을 같은 대상에게 중복 배정하지 않는다.
 */
@Entity
@Table(name = "training_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"training_id", "assignee_id"}))
@Getter
@Setter
public class TrainingAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignee_id", nullable = false)
    private AdminUser assignee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus status = TrainingStatus.NOT_STARTED;

    // 시청률(%). 되감기·시킹 무시, 최대 도달 위치 기준.
    @Column(nullable = false)
    private double progressRate = 0;

    private int lastPositionSec;

    private int durationSec;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
