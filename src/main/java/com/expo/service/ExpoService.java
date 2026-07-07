package com.expo.service;

import com.expo.domain.Expo;
import com.expo.domain.ExpoStatus;
import com.expo.dto.ExpoForm;
import com.expo.repository.ExpoRepository;
import com.expo.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 박람회 개설/수정/삭제 및 조회. 개설 시 식별코드를 자동 발급한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExpoService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<Expo> listAll() {
        return expoRepository.findAllByOrderByStartAtAsc();
    }

    @Transactional(readOnly = true)
    public List<Expo> listOpen(String keyword) {
        return expoRepository.findByStatusAndTitleContainingOrderByStartAtAsc(
                ExpoStatus.OPEN, keyword == null ? "" : keyword.trim());
    }

    @Transactional(readOnly = true)
    public Expo getById(Long id) {
        return expoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("박람회를 찾을 수 없습니다."));
    }

    public Long create(ExpoForm form) {
        Expo expo = new Expo();
        apply(expo, form);
        expo.setCode(generateCode());
        return expoRepository.save(expo).getId();
    }

    public void update(Long id, ExpoForm form) {
        Expo expo = getById(id);
        apply(expo, form);
    }

    public void delete(Long id) {
        if (reservationRepository.countByExpoId(id) > 0) {
            throw new IllegalStateException("예약이 있는 박람회는 삭제할 수 없습니다. 상태를 마감으로 변경하세요.");
        }
        expoRepository.deleteById(id);
    }

    private void apply(Expo expo, ExpoForm form) {
        if (form.getStartAt() != null && form.getEndAt() != null
                && !form.getStartAt().isBefore(form.getEndAt())) {
            throw new IllegalArgumentException("종료 일시는 시작 일시보다 뒤여야 합니다.");
        }
        if (form.getPrice() < 0 || form.getCapacity() < 0) {
            throw new IllegalArgumentException("가격과 좌석수는 0 이상이어야 합니다.");
        }
        expo.setTitle(form.getTitle());
        expo.setDescription(form.getDescription());
        expo.setCategory(form.getCategory());
        expo.setVenue(form.getVenue());
        expo.setStartAt(form.getStartAt());
        expo.setEndAt(form.getEndAt());
        expo.setPrice(form.getPrice());
        expo.setCapacity(form.getCapacity());
        expo.setStatus(form.getStatus() == null ? ExpoStatus.DRAFT : form.getStatus());
        expo.setThumbnailUrl(form.getThumbnailUrl());
    }

    // EXPO-{연도}-{영문대문자+숫자 6자리}. 충돌 시 재생성.
    private String generateCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("EXPO-").append(Year.now()).append('-');
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(ThreadLocalRandom.current().nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (expoRepository.existsByCode(code));
        return code;
    }
}
