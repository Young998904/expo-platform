package com.expo.controller.admin;

import com.expo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 전체 관리자 대시보드(플랫폼 KPI + 박람회별 현황).
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "전체 관리자 대시보드");
        model.addAttribute("dash", dashboardService.adminSummary());
        return "admin/dashboard";
    }
}
