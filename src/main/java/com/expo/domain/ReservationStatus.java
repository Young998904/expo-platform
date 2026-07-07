package com.expo.domain;

/**
 * 예약 상태 흐름: PENDING → CONFIRMED → CHECKED_IN,
 * 그리고 CONFIRMED → CANCEL_REQUESTED → CANCELLED.
 * 좌석은 CONFIRMED·CHECKED_IN 인원만 점유한다(PENDING은 미점유).
 */
public enum ReservationStatus {
    PENDING("결제대기"),
    CONFIRMED("예약확정"),
    CHECKED_IN("입장완료"),
    CANCEL_REQUESTED("취소요청"),
    CANCELLED("취소됨");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 좌석을 점유하는 상태인지 여부. */
    public boolean occupiesSeat() {
        return this == CONFIRMED || this == CHECKED_IN;
    }
}
