package com.expo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 박람회(행사). 개설 시 식별코드(code)를 자동 발급한다.
 */
@Entity
@Table(name = "expo")
@Getter
@Setter
public class Expo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String category;

    private String venue;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    // 1인 참가 가격(원)
    @Column(nullable = false)
    private int price;

    // 총 좌석 수
    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpoStatus status = ExpoStatus.DRAFT;

    private String thumbnailUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
