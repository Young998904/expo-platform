package com.expo.controller.event;

import com.expo.domain.AdminUser;
import com.expo.domain.TrainingAssignment;
import com.expo.repository.AdminUserRepository;
import com.expo.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

/**
 * 박람회 관리자의 교육 수강. YouTube 재생 진행률을 받아 시청률/이수를 갱신한다.
 */
@Controller
@RequestMapping("/event/trainings")
@RequiredArgsConstructor
public class LmsEventController {

    private final TrainingService trainingService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping
    public String list(Principal principal, Model model) {
        model.addAttribute("assignments", trainingService.assignmentsForAssignee(me(principal).getId()));
        return "event/trainings/list";
    }

    @GetMapping("/{id}")
    public String watch(@PathVariable Long id, Principal principal, Model model, RedirectAttributes ra) {
        TrainingAssignment a = trainingService.getAssignmentForWatch(id);
        if (!a.getAssignee().getId().equals(me(principal).getId())) {
            ra.addFlashAttribute("error", "접근 권한이 없습니다.");
            return "redirect:/event/trainings";
        }
        model.addAttribute("assignment", a);
        model.addAttribute("training", a.getTraining());
        return "event/trainings/watch";
    }

    /** 재생 진행률 수신(Ajax). 본인 배정분만 갱신한다. */
    @PostMapping("/{id}/progress")
    @ResponseBody
    public Map<String, Object> progress(@PathVariable Long id, @RequestParam int positionSec,
                                        @RequestParam int durationSec, Principal principal) {
        TrainingAssignment a = trainingService.getAssignmentForWatch(id);
        if (!a.getAssignee().getId().equals(me(principal).getId())) {
            return Map.of("status", "FORBIDDEN");
        }
        TrainingAssignment updated = trainingService.updateProgress(id, positionSec, durationSec);
        return Map.of(
                "status", updated.getStatus().name(),
                "label", updated.getStatus().getLabel(),
                "rate", Math.round(updated.getProgressRate() * 10) / 10.0);
    }

    private AdminUser me(Principal principal) {
        return adminUserRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("로그인 정보를 확인할 수 없습니다."));
    }
}
