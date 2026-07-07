package com.expo.domain;

/**
 * 교육 수강 상태.
 */
public enum TrainingStatus {
    NOT_STARTED("미시작"),
    IN_PROGRESS("수강중"),
    COMPLETED("이수완료");

    private final String label;

    TrainingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
