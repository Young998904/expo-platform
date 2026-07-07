package com.expo.controller.admin;

import com.expo.dto.BannerForm;
import com.expo.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 전체 관리자의 VIP 배너 등록/토글/삭제.
 */
@Controller
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerService.listAll());
        return "admin/banners/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new BannerForm());
        return "admin/banners/form";
    }

    @PostMapping
    public String create(@ModelAttribute BannerForm form, RedirectAttributes ra) {
        bannerService.create(form);
        ra.addFlashAttribute("message", "배너를 등록했습니다.");
        return "redirect:/admin/banners";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        bannerService.toggle(id);
        ra.addFlashAttribute("message", "노출 상태를 변경했습니다.");
        return "redirect:/admin/banners";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        bannerService.delete(id);
        ra.addFlashAttribute("message", "배너를 삭제했습니다.");
        return "redirect:/admin/banners";
    }
}
