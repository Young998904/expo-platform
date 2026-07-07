package com.expo.security;

import jakarta.servlet.http.HttpSession;

/**
 * 고객 경량 세션 헬퍼. 로그인한 고객 id를 세션에 보관한다.
 */
public final class CustomerSession {

    private static final String KEY = "CUSTOMER_ID";

    private CustomerSession() {
    }

    public static void set(HttpSession session, Long customerId) {
        session.setAttribute(KEY, customerId);
    }

    public static Long getId(HttpSession session) {
        return (Long) session.getAttribute(KEY);
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getId(session) != null;
    }

    public static void clear(HttpSession session) {
        session.removeAttribute(KEY);
    }
}
