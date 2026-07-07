package com.expo.service;

import com.expo.domain.Banner;
import com.expo.dto.BannerForm;
import com.expo.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VIP 배너 등록/토글/삭제 및 노출 조회.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<Banner> listAll() {
        return bannerRepository.findAllByOrderByPriorityAsc();
    }

    /** 고객 노출용: 활성 + 노출기간 내 배너를 priority 오름차순으로. */
    @Transactional(readOnly = true)
    public List<Banner> listActive() {
        LocalDateTime now = LocalDateTime.now();
        return bannerRepository.findByActiveTrueOrderByPriorityAsc().stream()
                .filter(b -> (b.getStartAt() == null || !b.getStartAt().isAfter(now))
                        && (b.getEndAt() == null || !b.getEndAt().isBefore(now)))
                .toList();
    }

    public void create(BannerForm form) {
        Banner b = new Banner();
        b.setTitle(form.getTitle());
        b.setImageUrl(form.getImageUrl());
        b.setLinkUrl(form.getLinkUrl());
        b.setPriority(form.getPriority());
        b.setActive(form.isActive());
        b.setStartAt(form.getStartAt());
        b.setEndAt(form.getEndAt());
        bannerRepository.save(b);
    }

    public void toggle(Long id) {
        Banner b = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
        b.setActive(!b.isActive());
    }

    public void delete(Long id) {
        bannerRepository.deleteById(id);
    }
}
