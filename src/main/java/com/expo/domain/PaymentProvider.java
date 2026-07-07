package com.expo.domain;

/**
 * 결제 수단. 키가 없으면 MOCK으로 폴백한다.
 */
public enum PaymentProvider {
    PORTONE("카카오페이"),
    MOCK("모의결제");

    private final String label;

    PaymentProvider(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
