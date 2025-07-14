package com.example.fund.fund.controller;

import com.example.fund.fund.service.FundHoldingService;
import com.example.fund.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fund")
public class FundController {

    private final FundHoldingService fundHoldingService;
    private static final String SESSION_KEY = "user";

    @GetMapping("/my")
    public String myFundHoldings(HttpSession session, Model model) {
        User user = (User) session.getAttribute(SESSION_KEY);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("holdings", fundHoldingService.getHoldingsByUserId(user.getUserId()));
        return "mypage/my-fund-list"; // ★ templates/mypage/my-fund-list.html

    }
}
