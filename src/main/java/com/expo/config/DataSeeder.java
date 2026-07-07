package com.expo.config;

import com.expo.domain.AdminUser;
import com.expo.domain.Role;
import com.expo.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 초기 시연용 시드 데이터. 전체 관리자 계정이 없으면 하나 생성한다.
 * 로그인: admin / admin123
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.findByUsername("admin").isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("전체 관리자");
            admin.setRole(Role.SUPER_ADMIN);
            admin.setActive(true);
            adminUserRepository.save(admin);
        }
    }
}
