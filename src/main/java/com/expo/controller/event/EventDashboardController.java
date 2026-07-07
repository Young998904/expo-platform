package com.expo.controller.event;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 박람회 관리자 대시보드(스캐폴딩 단계 자리표시). 이후 예약/체크인 지표로 확장한다.
 */
@Controller
@RequestMapping("/event")
public class EventDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "박람회 관리자 대시보드");
        return "event/dashboard";
    }
}
