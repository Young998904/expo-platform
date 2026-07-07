package com.expo.repository;

import com.expo.domain.Reservation;
import com.expo.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 예약 저장소. 목록은 연관 엔티티를 fetch join 해 N+1을 피한다.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    long countByExpoId(Long expoId);

    Optional<Reservation> findByReservationNo(String reservationNo);

    Optional<Reservation> findByCheckinToken(String checkinToken);

    // 특정 박람회의 점유 좌석 수(CONFIRMED·CHECKED_IN 인원 합)
    @Query("""
            select coalesce(sum(r.headcount), 0)
            from Reservation r
            where r.expo.id = :expoId and r.status in :statuses
            """)
    int sumHeadcount(@Param("expoId") Long expoId, @Param("statuses") Collection<ReservationStatus> statuses);

    // 고객의 내 예약(행사 정보 fetch join)
    @Query("""
            select r from Reservation r
            join fetch r.expo
            where r.customer.id = :customerId
            order by r.createdAt desc
            """)
    List<Reservation> findByCustomerWithExpo(@Param("customerId") Long customerId);

    // 박람회 관리자 예약자 명단(고객 fetch join)
    @Query("""
            select r from Reservation r
            join fetch r.customer
            where r.expo.id = :expoId
            order by r.createdAt desc
            """)
    List<Reservation> findByExpoWithCustomer(@Param("expoId") Long expoId);

    // 매출 집계(전체 / 박람회별) — CONFIRMED·CHECKED_IN 기준
    @Query("select coalesce(sum(r.amount), 0) from Reservation r where r.status in :statuses")
    int sumAmount(@Param("statuses") Collection<ReservationStatus> statuses);

    @Query("select coalesce(sum(r.amount), 0) from Reservation r where r.expo.id = :expoId and r.status in :statuses")
    int sumAmountByExpo(@Param("expoId") Long expoId, @Param("statuses") Collection<ReservationStatus> statuses);

    // 인원 집계(전체) — 체크인율 계산용
    @Query("select coalesce(sum(r.headcount), 0) from Reservation r where r.status in :statuses")
    int sumHeadcountAll(@Param("statuses") Collection<ReservationStatus> statuses);
}
