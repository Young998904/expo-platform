package com.expo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 진입/로그인 라우팅. 로그인 성공 후 역할에 따라 대시보드로 보낸다.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        boolean superAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        return superAdmin ? "redirect:/admin/dashboard" : "redirect:/event/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
