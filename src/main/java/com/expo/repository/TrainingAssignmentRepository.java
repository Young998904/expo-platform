package com.expo.repository;

import com.expo.domain.TrainingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 교육 배정/수강 저장소. 목록은 연관 엔티티를 fetch join 해 N+1을 피한다.
 */
public interface TrainingAssignmentRepository extends JpaRepository<TrainingAssignment, Long> {

    boolean existsByTrainingIdAndAssigneeId(Long trainingId, Long assigneeId);

    // 수강율 조회(교육별 대상자)
    @Query("""
            select a from TrainingAssignment a
            join fetch a.assignee
            where a.training.id = :trainingId
            order by a.assignee.name asc
            """)
    List<TrainingAssignment> findByTrainingWithAssignee(@Param("trainingId") Long trainingId);

    // 박람회 관리자의 내 교육
    @Query("""
            select a from TrainingAssignment a
            join fetch a.training
            where a.assignee.id = :assigneeId
            order by a.createdAt desc
            """)
    List<TrainingAssignment> findByAssigneeWithTraining(@Param("assigneeId") Long assigneeId);

    // 수강 화면/진행률 갱신용(교육 fetch join)
    @Query("""
            select a from TrainingAssignment a
            join fetch a.training
            where a.id = :id
            """)
    Optional<TrainingAssignment> findByIdWithTraining(@Param("id") Long id);
}
