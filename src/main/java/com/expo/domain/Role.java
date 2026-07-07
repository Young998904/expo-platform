package com.expo.domain;

/**
 * 관리자 역할. 고객(USER)은 별도 경량 세션으로 관리하므로 여기 포함하지 않는다.
 */
public enum Role {
    SUPER_ADMIN("전체 관리자"),
    EVENT_ADMIN("박람회 관리자");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
