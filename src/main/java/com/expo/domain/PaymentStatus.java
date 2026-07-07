package com.expo.domain;

/**
 * 결제 상태.
 */
public enum PaymentStatus {
    PAID("결제완료"),
    FAILED("실패"),
    CANCELLED("취소");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
