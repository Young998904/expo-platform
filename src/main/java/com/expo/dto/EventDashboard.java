package com.expo.dto;

/**
 * 박람회 관리자 대시보드 집계(담당 박람회 1건 기준).
 */
public record EventDashboard(
        boolean hasExpo,
        String expoTitle,
        long reservationCount,
        int paidAmount,
        int checkinRate,
        int seatUsageRate,
        int remaining,
        int capacity
) {
}
