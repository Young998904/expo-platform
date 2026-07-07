package com.expo.service;

import com.expo.domain.Expo;
import com.expo.domain.ReservationStatus;
import com.expo.dto.AdminDashboard;
import com.expo.dto.EventDashboard;
import com.expo.repository.ExpoRepository;
import com.expo.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 대시보드/통계 집계(읽기 전용). 엔티티 대신 집계값을 DTO로 반환한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<ReservationStatus> OCCUPYING =
            List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
    private static final List<ReservationStatus> CHECKED =
            List.of(ReservationStatus.CHECKED_IN);

    private final ExpoRepository expoRepository;
    private final ReservationRepository reservationRepository;

    public AdminDashboard adminSummary() {
        long totalExpos = expoRepository.count();
        long totalReservations = reservationRepository.count();
        int revenue = reservationRepository.sumAmount(OCCUPYING);
        int occupied = reservationRepository.sumHeadcountAll(OCCUPYING);
        int checkedIn = reservationRepository.sumHeadcountAll(CHECKED);
        int avgCheckinRate = occupied > 0 ? checkedIn * 100 / occupied : 0;

        List<AdminDashboard.ExpoStat> stats = new ArrayList<>();
        for (Expo e : expoRepository.findAllByOrderByStartAtAsc()) {
            long cnt = reservationRepository.countByExpoId(e.getId());
            int occ = reservationRepository.sumHeadcount(e.getId(), OCCUPYING);
            int usage = e.getCapacity() > 0 ? occ * 100 / e.getCapacity() : 0;
            stats.add(new AdminDashboard.ExpoStat(e.getTitle(), cnt, occ, e.getCapacity(), usage));
        }
        return new AdminDashboard(totalExpos, totalReservations, revenue, avgCheckinRate, stats);
    }

    public EventDashboard eventSummary(Long expoId) {
        Expo e = expoId == null ? null : expoRepository.findById(expoId).orElse(null);
        if (e == null) {
            return new EventDashboard(false, "—", 0, 0, 0, 0, 0, 0);
        }
        long cnt = reservationRepository.countByExpoId(expoId);
        int paid = reservationRepository.sumAmountByExpo(expoId, OCCUPYING);
        int occupied = reservationRepository.sumHeadcount(expoId, OCCUPYING);
        int checkedIn = reservationRepository.sumHeadcount(expoId, CHECKED);
        int checkinRate = occupied > 0 ? checkedIn * 100 / occupied : 0;
        int usage = e.getCapacity() > 0 ? occupied * 100 / e.getCapacity() : 0;
        int remaining = Math.max(0, e.getCapacity() - occupied);
        return new EventDashboard(true, e.getTitle(), cnt, paid, checkinRate, usage, remaining, e.getCapacity());
    }
}
