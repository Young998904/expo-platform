package com.expo.repository;

import com.expo.domain.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 교육 저장소.
 */
public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findAllByOrderByCreatedAtDesc();
}
