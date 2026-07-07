package com.expo.controller.admin;

import com.expo.domain.Role;
import com.expo.repository.AdminUserRepository;
import com.expo.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * 전체 관리자의 교육 생성·배정·수강율 조회.
 */
@Controller
@RequestMapping("/admin/trainings")
@RequiredArgsConstructor
public class LmsAdminController {

    private final TrainingService trainingService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("trainings", trainingService.listTrainings());
        return "admin/trainings/list";
    }

    @GetMapping("/new")
    public String newForm() {
        return "admin/trainings/form";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam(required = false) String description,
                         @RequestParam String youtubeUrl, Principal principal, RedirectAttributes ra) {
        try {
            Long id = trainingService.create(title, description, youtubeUrl, principal.getName());
            ra.addFlashAttribute("message", "교육을 생성했습니다.");
            return "redirect:/admin/trainings/" + id;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/trainings/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("training", trainingService.getTraining(id));
        model.addAttribute("assignments", trainingService.assignmentsForTraining(id));
        model.addAttribute("candidates", adminUserRepository.findByRoleOrderByNameAsc(Role.EVENT_ADMIN));
        return "admin/trainings/detail";
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Long assigneeId, RedirectAttributes ra) {
        try {
            trainingService.assign(id, assigneeId);
            ra.addFlashAttribute("message", "교육을 배정했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/trainings/" + id;
    }
}
