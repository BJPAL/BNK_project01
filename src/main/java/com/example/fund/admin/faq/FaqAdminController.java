package com.example.fund.admin.faq;

import com.example.fund.admin.dto.AdminDTO;
import com.example.fund.admin.entity.Admin;
import com.example.fund.faq.entity.Faq;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/faq")
public class FaqAdminController {

    private final FaqAdminService faqAdminService;

    @GetMapping("/list")
    public String faqList(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(defaultValue = "0") int page,
                          Model model, HttpSession session) {

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        model.addAttribute("admin", admin);

        Pageable pageable = PageRequest.of(page, 10, Sort.by("faqId").descending());
        Page<Faq> faqPage;

        if (keyword != null && !keyword.isEmpty()) {
            faqPage = faqAdminService.search(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            faqPage = faqAdminService.findAllWithPaging(pageable);
        }

        model.addAttribute("faqPage", faqPage);
        return "admin/faq/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !List.of("CS", "SUPER").contains(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 등록 가능합니다.");
            return "admin/faq/list";
        }
        return "admin/faq/add";
    }

    @PostMapping("/add")
    public String addFaq(@RequestParam("question") String question,
                         @RequestParam("answer") String answer,
                         @RequestParam(value = "active", required = false) String active,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !List.of("CS", "SUPER").contains(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 등록 가능합니다.");
            return "admin/faq/add";
        }

        Faq faq = new Faq();
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setActive(active != null && active.equals("on")); // 수정된 부분

        faqAdminService.save(faq);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ가 등록되었습니다.");
        return "redirect:/admin/faq/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model, HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");

        if (admin == null || !List.of("CS", "SUPER").contains(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 접근 가능합니다.");
            return "admin/faq/list";
        }

        Faq faq = faqAdminService.findById(id);
        if (faq == null) {
            model.addAttribute("errorMessage", "해당 FAQ가 존재하지 않습니다.");
            return "admin/faq/list";
        }

        model.addAttribute("faq", faq);
        return "admin/faq/edit";
    }

    @PostMapping("/edit/{id}")
    public String editFaq(@PathVariable("id") Integer id,
                          @RequestParam("question") String question,
                          @RequestParam("answer") String answer,
                          @RequestParam(value = "active", required = false) String active,
                          HttpSession session,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !List.of("CS", "SUPER").contains(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 수정 가능합니다.");
            return "admin/faq/edit";
        }

        Faq existing = faqAdminService.findById(id);
        if (existing == null) {
            model.addAttribute("errorMessage", "FAQ가 존재하지 않습니다.");
            return "admin/faq/edit";
        }

        existing.setQuestion(question);
        existing.setAnswer(answer);
        existing.setActive(active != null && active.equals("on")); // 수정된 부분

        faqAdminService.save(existing);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ가 수정되었습니다.");
        return "redirect:/admin/faq/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteFaq(@PathVariable("id") Integer id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");

        if (admin == null || !List.of("CS", "SUPER").contains(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 삭제 가능합니다.");
            return "admin/faq/list";
        }

        faqAdminService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ가 성공적으로 삭제되었습니다.");
        return "redirect:/admin/faq/list";
    }
}