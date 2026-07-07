package com.expo.controller.customer;

import com.expo.domain.Reservation;
import com.expo.security.CustomerSession;
import com.expo.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 고객 예약 생성 및 내 예약 조회. 결제(확정)·QR은 M3에서 붙는다.
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerReservationController {

    private final ReservationService reservationService;

    @PostMapping("/expos/{id}/reserve")
    public String reserve(@PathVariable Long id, @RequestParam int headcount,
                          @RequestParam(required = false) String contactPhone,
                          HttpSession session, RedirectAttributes ra) {
        try {
            Long rid = reservationService.create(CustomerSession.getId(session), id, headcount, contactPhone);
            ra.addFlashAttribute("message", "예약이 접수되었습니다. 결제를 진행해 주세요.");
            return "redirect:/customer/reservations/" + rid;
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/expos/" + id;
        }
    }

    @GetMapping("/reservations")
    public String myList(HttpSession session, Model model) {
        model.addAttribute("reservations", reservationService.listByCustomer(CustomerSession.getId(session)));
        return "customer/reservations";
    }

    @GetMapping("/reservations/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        Reservation r = reservationService.getDetail(id);
        if (!r.getCustomer().getId().equals(CustomerSession.getId(session))) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/customer/reservations";
        }
        model.addAttribute("reservation", r);
        return "customer/reservation-detail";
    }
}
