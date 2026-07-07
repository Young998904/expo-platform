package com.expo.config;

import com.expo.domain.*;
import com.expo.repository.AdminUserRepository;
import com.expo.repository.CustomerRepository;
import com.expo.repository.ExpoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 초기 시연용 시드. 계정이 없을 때만 관리자·박람회 관리자·박람회·고객 샘플을 만든다.
 * 로그인 — 전체 관리자: admin/admin123 · 박람회 관리자: event1/event123 · 고객: 01012345678 / PIN 1234
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final ExpoRepository expoRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.findByUsername("admin").isEmpty()) {
            save("admin", "admin123", "전체 관리자", Role.SUPER_ADMIN, null);
        }

        if (expoRepository.count() == 0) {
            Expo a = sampleExpo("2026 서울 리빙 디자인 페어", "생활·인테리어",
                    "코엑스 A홀", 15000, 200, LocalDateTime.now().plusDays(20));
            Expo b = sampleExpo("2026 국제 커피 엑스포", "식음료",
                    "벡스코 제2전시장", 12000, 150, LocalDateTime.now().plusDays(35));
            Expo c = sampleExpo("2026 스마트테크 위크(준비중)", "IT·가전",
                    "킨텍스 1홀", 0, 300, LocalDateTime.now().plusDays(60));
            c.setStatus(ExpoStatus.DRAFT);
            expoRepository.save(a);
            expoRepository.save(b);
            expoRepository.save(c);

            // 박람회 관리자(첫 박람회 담당)
            if (adminUserRepository.findByUsername("event1").isEmpty()) {
                save("event1", "event123", "리빙페어 운영", Role.EVENT_ADMIN, a.getId());
            }
        }

        if (customerRepository.findByPhone("01012345678").isEmpty()) {
            Customer c = new Customer();
            c.setName("김참가");
            c.setPhone("01012345678");
            c.setPin(passwordEncoder.encode("1234"));
            customerRepository.save(c);
        }
    }

    private void save(String username, String rawPw, String name, Role role, Long managedExpoId) {
        AdminUser u = new AdminUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPw));
        u.setName(name);
        u.setRole(role);
        u.setActive(true);
        u.setManagedExpoId(managedExpoId);
        adminUserRepository.save(u);
    }

    private Expo sampleExpo(String title, String category, String venue, int price, int capacity, LocalDateTime start) {
        Expo e = new Expo();
        e.setCode("EXPO-SEED-" + Math.abs(title.hashCode() % 100000));
        e.setTitle(title);
        e.setDescription(title + " · 사전 예약제로 운영되는 박람회입니다.");
        e.setCategory(category);
        e.setVenue(venue);
        e.setStartAt(start);
        e.setEndAt(start.plusDays(3));
        e.setPrice(price);
        e.setCapacity(capacity);
        e.setStatus(ExpoStatus.OPEN);
        return e;
    }
}
