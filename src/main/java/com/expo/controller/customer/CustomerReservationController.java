package com.expo.controller.customer;

import com.expo.domain.Reservation;
import com.expo.security.CustomerSession;
import com.expo.service.PaymentService;
import com.expo.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 고객 예약 생성·조회·결제. 결제 성공 시 예약이 확정되고 QR 입장 배지가 발급된다.
 */
@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerReservationController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

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
        if (!ownedBy(r, session)) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/customer/reservations";
        }
        model.addAttribute("reservation", r);
        model.addAttribute("portoneEnabled", paymentService.isPortOneEnabled());
        model.addAttribute("storeId", paymentService.getStoreId());
        model.addAttribute("channelKey", paymentService.getChannelKey());
        return "customer/reservation-detail";
    }

    /** Mock 결제(PortOne 키 미설정 시). */
    @PostMapping("/reservations/{id}/pay")
    public String pay(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Reservation r = reservationService.getDetail(id);
        if (!ownedBy(r, session)) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/customer/reservations";
        }
        try {
            paymentService.payByMock(id);
            ra.addFlashAttribute("message", "결제가 완료되었습니다. 예약이 확정되고 입장 배지가 발급되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/reservations/" + id;
    }

    /** PortOne(카카오페이) 결제창 성공 후 복귀 → 서버 검증·확정. */
    @GetMapping("/reservations/{id}/pay/complete")
    public String payComplete(@PathVariable Long id, @RequestParam String paymentId,
                              HttpSession session, RedirectAttributes ra) {
        Reservation r = reservationService.getDetail(id);
        if (!ownedBy(r, session)) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/customer/reservations";
        }
        try {
            paymentService.verifyAndConfirm(id, paymentId);
            ra.addFlashAttribute("message", "결제가 완료되었습니다. 예약이 확정되고 입장 배지가 발급되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "결제 확인 실패: " + e.getMessage());
        }
        return "redirect:/customer/reservations/" + id;
    }

    private boolean ownedBy(Reservation r, HttpSession session) {
        return r.getCustomer().getId().equals(CustomerSession.getId(session));
    }
}
