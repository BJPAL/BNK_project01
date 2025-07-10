package com.example.fund.admin.loginTest;

import com.example.fund.admin.Admin;

import com.example.fund.admin.faq.AdminRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminLoginController {

    private final AdminRepository adminRepository;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "adminLogin";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest,
                        HttpSession session,
                        Model model) {

        Admin admin = adminRepository.findByAdminname(loginRequest.getUsername())
                .filter(a -> a.getPassword().equals(loginRequest.getPassword()))
                .orElse(null);

        if (admin == null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 잘못되었습니다.");
            model.addAttribute("loginRequest", loginRequest);
            return "adminLogin";
        }

        session.setAttribute("loginAdmin", admin);
        return "redirect:/admin/faq/list"; // 로그인 후 FAQ 리스트로 이동
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}