package com.example.fund.fund.controller;

import com.example.fund.fund.entity.InvestProfileResult;
import com.example.fund.fund.repository.InvestProfileResultRepository;
import com.example.fund.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/fund")
public class FundViewController {

    private final InvestProfileResultRepository investProfileResultRepository;

    /**
     * 투자 성향에 따른 펀드 목록
     */
    @GetMapping("/list")
    public String listPage(HttpSession session, Model model) {
        log.debug("펀드 목록 페이지 접근 요청");
        User user = (User) session.getAttribute("user");

        // 사용자 세션 여부
        if (user == null) {
            log.warn("미인증 사용자의 펀드 목록 페이지 접근 시도");
            return "redirect:/auth/login";      // 로그인 필요
        }

        // 투자 성향 존재 여부 확인
        Integer userId = user.getUserId();
        Optional<InvestProfileResult> investResult = investProfileResultRepository.findByUser_UserId(userId);

        if (investResult.isPresent()) {
            InvestProfileResult result = investResult.get();
            Integer investType = result.getType().getTypeId().intValue();

            model.addAttribute("userId", userId);
            model.addAttribute("investType", investType);

            return "fund/fundList";
        } else {
            // 투자 성향 검사 필요
            return "redirect:/profile";
        }
    }

    @GetMapping("/{fundId}")  // 상세 페이지
    public String detailPage(
            @PathVariable Long fundId,
            HttpSession session,
            Model model
    ) {
        log.debug("펀드 상세 페이지 접근 요청");
        User user = (User) session.getAttribute("user");

        // 사용자 세션 여부
        if (user == null) {
            log.warn("미인증 사용자의 펀드 상세 페이지 접근 시도");
            return "redirect:/auth/login";      // 로그인 필요
        }

        return "fund/fundDetail";
    }

}
