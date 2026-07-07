package com.expo.repository;

import com.expo.domain.AdminUser;
import com.expo.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 계정 저장소.
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    List<AdminUser> findByRoleOrderByNameAsc(Role role);
}
