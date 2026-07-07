package com.expo.controller.customer;

import com.expo.security.CustomerSession;
import com.expo.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 고객 로그인/로그아웃(전화번호 + PIN).
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerService customerService;

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (CustomerSession.isLoggedIn(session)) {
            return "redirect:/customer/home";
        }
        return "customer/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String phone, @RequestParam String pin,
                        @RequestParam(required = false) String name,
                        HttpSession session, RedirectAttributes ra) {
        try {
            Long id = customerService.loginOrRegister(phone, pin, name);
            CustomerSession.set(session, id);
            return "redirect:/customer/home";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        CustomerSession.clear(session);
        return "redirect:/customer/login";
    }
}
