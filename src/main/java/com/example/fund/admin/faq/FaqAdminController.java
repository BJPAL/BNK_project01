package com.example.fund.admin.controllerTest;


import com.example.fund.admin.Admin;
import com.example.fund.admin.serviceTest.FaqAdminService;
import com.example.fund.faq.entity.Faq;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/faq")
public class FaqAdminController {

    private final FaqAdminService faqAdminService;

    @GetMapping("/list")
    public String faqList(@RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(defaultValue = "0") int page,
                          Model model, HttpSession session) {

        Admin admin = (Admin) session.getAttribute("loginAdmin");
        model.addAttribute("admin", admin);

        Pageable pageable = PageRequest.of(page, 10, Sort.by("faqId").descending());
        Page<Faq> faqPage;

        if (keyword != null && !keyword.isEmpty()) {
            faqPage = faqAdminService.search(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            faqPage = new PageImpl<>(faqAdminService.findAll(), pageable, faqAdminService.findAll().size());
        }

        model.addAttribute("faqList", faqPage.getContent());
        model.addAttribute("totalPages", faqPage.getTotalPages());
        model.addAttribute("currentPage", faqPage.getNumber());

        return "adminFaqList";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session,Model model){
        Admin admin = (Admin) session.getAttribute("loginAdmin");
        if(!"CS".equals(admin.getRole())){
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 등록 가능합니다.");
//            return "adminFaqList";
        }
        return "adminFaqAdd";
    }

    @PostMapping("/add")
    public String addFaq(@RequestParam("question") String question,
                         @RequestParam("answer") String answer,
                         @RequestParam(value = "active", required = false) String active,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        Admin admin = (Admin) session.getAttribute("loginAdmin");
        if (admin == null || !"CS".equals(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 등록 가능합니다.");
            return "adminFaqAdd";
        }

        Faq faq = new Faq();
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setActive(active == null || active.equals("on"));

        faqAdminService.save(faq);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ가 등록되었습니다.");
        return "redirect:/admin/faq/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id")Integer id, Model model, HttpSession session){
        Admin admin = (Admin) session.getAttribute("loginAdmin");

        if (admin == null || !"CS".equals(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 접근 가능합니다.");
            return "adminFaqList";
        }

        Faq faq = faqAdminService.findById(id);
        if (faq == null) {
            model.addAttribute("errorMessage", "해당 FAQ가 존재하지 않습니다.");
            return "adminFaqList";
        }

        model.addAttribute("faq", faq);
        return "adminFaqEdit";
    }

    @PostMapping("/edit/{id}")
    public String editFaq(@PathVariable("id") Integer id,
                          @RequestParam("question") String question,
                          @RequestParam("answer") String answer,
                          @RequestParam(value = "active", required = false) String active,
                          HttpSession session,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        Admin admin = (Admin) session.getAttribute("loginAdmin");
        if (admin == null || !"CS".equals(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 수정 가능합니다.");
            return "adminFaqEdit";
        }

        Faq existing = faqAdminService.findById(id);
        if (existing == null) {
            model.addAttribute("errorMessage", "FAQ가 존재하지 않습니다.");
            return "adminFaqEdit";
        }

        existing.setQuestion(question);
        existing.setAnswer(answer);
        existing.setActive(active == null || active.equals("on")); // 체크박스가 null이 아니면 true

        faqAdminService.save(existing);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ가 수정되었습니다.");
        return "redirect:/admin/faq/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteFaq(@PathVariable("id") Integer id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        Admin admin = (Admin) session.getAttribute("loginAdmin");

        if (admin == null || !"CS".equals(admin.getRole())) {
            model.addAttribute("errorMessage", "CS 권한이 있는 관리자만 삭제 가능합니다.");
            return "adminFaqList";
        }

        faqAdminService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "FAQ가 성공적으로 삭제되었습니다.");
        return "redirect:/admin/faq/list";
    }

}
