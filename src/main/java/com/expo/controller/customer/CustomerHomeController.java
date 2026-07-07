package com.expo.controller.customer;

import com.expo.domain.Expo;
import com.expo.security.CustomerSession;
import com.expo.service.CustomerService;
import com.expo.service.ExpoService;
import com.expo.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 고객 포털 홈(행사 목록/검색)과 행사 상세.
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerHomeController {

    private final ExpoService expoService;
    private final ReservationService reservationService;
    private final CustomerService customerService;

    @GetMapping({"", "/home"})
    public String home(@RequestParam(required = false) String keyword, HttpSession session, Model model) {
        List<Expo> expos = expoService.listOpen(keyword);
        // 카드에 표시할 행사별 남은 좌석
        Map<Long, Integer> remaining = new LinkedHashMap<>();
        for (Expo e : expos) {
            remaining.put(e.getId(), reservationService.remainingSeats(e));
        }
        model.addAttribute("expos", expos);
        model.addAttribute("remaining", remaining);
        model.addAttribute("keyword", keyword);
        model.addAttribute("customer", customerService.getById(CustomerSession.getId(session)));
        return "customer/home";
    }

    @GetMapping("/expos/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Expo expo = expoService.getById(id);
        model.addAttribute("expo", expo);
        model.addAttribute("remaining", reservationService.remainingSeats(expo));
        return "customer/detail";
    }
}
