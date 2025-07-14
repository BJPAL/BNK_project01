package com.example.fund.auth.controller;

import com.example.fund.auth.dto.UserUpdateRequest;
import com.example.fund.auth.service.UserService;
import com.example.fund.user.entity.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final UserService service;
    private static final String SESSION_KEY = "user";

    // GET: 마이페이지 폼
    @GetMapping
    public String form(HttpSession session, Model m) {
        User user = (User) session.getAttribute(SESSION_KEY);
        if (user == null) return "redirect:/auth/login";

        UserUpdateRequest dto = UserUpdateRequest.builder()
                .username(user.getUsername())     // ✅ 아이디 포함
                .name(user.getName())
                .phone(user.getPhone())
                .build();

        m.addAttribute("updateRequest", dto);
        return "mypage/form";
    }

    // POST: 수정 처리
    @PostMapping
    public String update(@Valid @ModelAttribute("updateRequest") UserUpdateRequest dto,
                         BindingResult br,
                         HttpSession session,
                         Model m) {

        User user = (User) session.getAttribute(SESSION_KEY);
        if (user == null) return "redirect:/auth/login";

        // 비밀번호 일치 검증
        if (dto.isChangingPassword() && !dto.newPwMatches()) {
            br.rejectValue("confirmNewPassword", "nomatch", "새 비밀번호가 서로 다릅니다.");
        }

        if (br.hasErrors()) return "mypage/form";

        try {
            User updated = service.updateProfile(user.getUserId(), dto);
            session.setAttribute(SESSION_KEY, updated);
            m.addAttribute("success", "변경되었습니다.");
        } catch (IllegalStateException e) {
            m.addAttribute("updateError", e.getMessage());
        }

        return "mypage/form";
    }
}
