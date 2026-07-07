package com.expo.controller.event;

import com.expo.domain.AdminUser;
import com.expo.domain.Reservation;
import com.expo.domain.Role;
import com.expo.repository.AdminUserRepository;
import com.expo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * 현장 체크인. 박람회 관리자는 자기 담당 박람회 예약만, 전체 관리자는 전체를 체크인할 수 있다.
 */
@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventCheckinController {

    private final ReservationService reservationService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping("/checkin")
    public String form() {
        return "event/checkin";
    }

    @PostMapping("/checkin")
    public String checkin(@RequestParam String code, Principal principal, RedirectAttributes ra) {
        AdminUser me = adminUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 확인할 수 없습니다."));
        Long managedExpoId = me.getRole() == Role.SUPER_ADMIN ? null : me.getManagedExpoId();
        try {
            Reservation r = reservationService.checkIn(code, managedExpoId);
            ra.addFlashAttribute("message", r.getReservationNo() + " 체크인 완료 · " + r.getExpo().getTitle()
                    + " · " + r.getHeadcount() + "명");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/event/checkin";
    }
}
