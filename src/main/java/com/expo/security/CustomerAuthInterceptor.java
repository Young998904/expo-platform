package com.expo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 고객 포털 접근 보호. 로그인하지 않은 요청은 고객 로그인으로 보낸다.
 */
@Component
public class CustomerAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (CustomerSession.isLoggedIn(request.getSession())) {
            return true;
        }
        response.sendRedirect("/customer/login");
        return false;
    }
}
