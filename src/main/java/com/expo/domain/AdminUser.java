package com.expo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 관리자 계정(전체 관리자 / 박람회 관리자). 비밀번호는 BCrypt로 저장한다.
 */
@Entity
@Table(name = "admin_user")
@Getter
@Setter
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 비활성 계정은 로그인 차단(삭제 대신 보존)
    @Column(nullable = false)
    private boolean active = true;

    // 담당 박람회(EVENT_ADMIN만). Expo 엔티티 도입 전까지 식별자만 보관한다.
    private Long managedExpoId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
