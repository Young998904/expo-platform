package com.expo.dto;

import java.util.List;

/**
 * 전체 관리자 대시보드 집계. 매출·좌석은 Reservation(CONFIRMED·CHECKED_IN) 기준.
 */
public record AdminDashboard(
        long totalExpos,
        long totalReservations,
        int totalRevenue,
        int avgCheckinRate,
        List<ExpoStat> expoStats
) {
    /** 박람회별 예약/좌석 현황. */
    public record ExpoStat(String title, long reservations, int occupied, int capacity, int usageRate) {
    }
}
