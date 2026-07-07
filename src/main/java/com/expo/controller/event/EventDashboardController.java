package com.expo.controller.event;

import com.expo.domain.AdminUser;
import com.expo.repository.AdminUserRepository;
import com.expo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/**
 * 박람회 관리자 대시보드(담당 박람회 지표).
 */
@Controller
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventDashboardController {

    private final DashboardService dashboardService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        AdminUser me = adminUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 확인할 수 없습니다."));
        model.addAttribute("title", "박람회 관리자 대시보드");
        model.addAttribute("dash", dashboardService.eventSummary(me.getManagedExpoId()));
        return "event/dashboard";
    }
}
