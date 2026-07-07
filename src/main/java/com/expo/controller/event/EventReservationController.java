package com.expo.controller.event;

import com.expo.domain.AdminUser;
import com.expo.domain.Reservation;
import com.expo.domain.ReservationStatus;
import com.expo.domain.Role;
import com.expo.repository.AdminUserRepository;
import com.expo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

/**
 * 박람회 관리자 예약자 명단(검색·상태변경·CSV). 담당 박람회 범위로 제한된다.
 */
@Controller
@RequestMapping("/event/reservations")
@RequiredArgsConstructor
public class EventReservationController {

    private final ReservationService reservationService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) ReservationStatus status,
                       Principal principal, Model model) {
        AdminUser me = me(principal);
        Long expoId = me.getManagedExpoId();
        model.addAttribute("reservations", reservationService.listForExpo(expoId, keyword, status));
        model.addAttribute("statuses", ReservationStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("hasExpo", expoId != null);
        return "event/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        AdminUser me = me(principal);
        Long managed = me.getRole() == Role.SUPER_ADMIN ? null : me.getManagedExpoId();
        try {
            reservationService.cancel(id, managed);
            ra.addFlashAttribute("message", "예약을 취소했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/event/reservations";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(Principal principal) {
        AdminUser me = me(principal);
        List<Reservation> list = reservationService.listForExpo(me.getManagedExpoId(), null, null);
        StringBuilder sb = new StringBuilder("예약번호,이름,전화,인원,상태,금액\n");
        for (Reservation r : list) {
            sb.append(r.getReservationNo()).append(',')
                    .append(r.getCustomer().getName()).append(',')
                    .append(r.getCustomer().getPhone()).append(',')
                    .append(r.getHeadcount()).append(',')
                    .append(r.getStatus().getLabel()).append(',')
                    .append(r.getAmount()).append('\n');
        }
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] text = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[bom.length + text.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(text, 0, out, bom.length, text.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reservations.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(out);
    }

    private AdminUser me(Principal principal) {
        return adminUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 확인할 수 없습니다."));
    }
}
