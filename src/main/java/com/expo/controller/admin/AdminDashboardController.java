package com.expo.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 전체 관리자 대시보드(스캐폴딩 단계 자리표시). 이후 KPI/통계로 확장한다.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "전체 관리자 대시보드");
        return "admin/dashboard";
    }
}
