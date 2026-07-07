package com.expo.repository;

import com.expo.domain.Expo;
import com.expo.domain.ExpoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 박람회 저장소.
 */
public interface ExpoRepository extends JpaRepository<Expo, Long> {

    boolean existsByCode(String code);

    List<Expo> findAllByOrderByStartAtAsc();

    // 고객 노출용: 예약중(OPEN) 행사만 임박순으로
    List<Expo> findByStatusAndTitleContainingOrderByStartAtAsc(ExpoStatus status, String keyword);
}
