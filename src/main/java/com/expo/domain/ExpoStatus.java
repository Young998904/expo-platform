package com.expo.domain;

/**
 * 박람회 상태. 고객 목록에는 OPEN만 노출한다.
 */
public enum ExpoStatus {
    DRAFT("작성중"),
    OPEN("예약중"),
    CLOSED("마감"),
    HIDDEN("숨김");

    private final String label;

    ExpoStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
