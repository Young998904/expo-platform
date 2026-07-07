package com.expo.controller.admin;

import com.expo.domain.Expo;
import com.expo.domain.ExpoStatus;
import com.expo.dto.ExpoForm;
import com.expo.service.ExpoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 전체 관리자의 박람회 개설/수정/삭제.
 */
@Controller
@RequestMapping("/admin/expos")
@RequiredArgsConstructor
public class ExpoController {

    private final ExpoService expoService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("expos", expoService.listAll());
        return "admin/expos/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new ExpoForm());
        model.addAttribute("statuses", ExpoStatus.values());
        model.addAttribute("mode", "new");
        return "admin/expos/form";
    }

    @PostMapping
    public String create(@ModelAttribute ExpoForm form, RedirectAttributes ra) {
        try {
            expoService.create(form);
            ra.addFlashAttribute("message", "박람회가 개설되었습니다.");
            return "redirect:/admin/expos";
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/expos/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Expo e = expoService.getById(id);
        ExpoForm form = new ExpoForm();
        form.setTitle(e.getTitle());
        form.setDescription(e.getDescription());
        form.setCategory(e.getCategory());
        form.setVenue(e.getVenue());
        form.setStartAt(e.getStartAt());
        form.setEndAt(e.getEndAt());
        form.setPrice(e.getPrice());
        form.setCapacity(e.getCapacity());
        form.setStatus(e.getStatus());
        form.setThumbnailUrl(e.getThumbnailUrl());
        model.addAttribute("form", form);
        model.addAttribute("statuses", ExpoStatus.values());
        model.addAttribute("mode", "edit");
        model.addAttribute("expo", e);
        return "admin/expos/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute ExpoForm form, RedirectAttributes ra) {
        try {
            expoService.update(id, form);
            ra.addFlashAttribute("message", "박람회가 수정되었습니다.");
            return "redirect:/admin/expos";
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/expos/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            expoService.delete(id);
            ra.addFlashAttribute("message", "박람회가 삭제되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/expos";
    }
}
