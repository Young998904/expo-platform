package com.expo.repository;

import com.expo.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * VIP 배너 저장소.
 */
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findAllByOrderByPriorityAsc();

    List<Banner> findByActiveTrueOrderByPriorityAsc();
}
