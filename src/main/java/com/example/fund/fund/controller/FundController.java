package com.example.fund.fund.controller;

import com.example.fund.favorite.service.FundFavoriteService;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fund")
@CrossOrigin(origins = "*")
public class FundController {

    private final FundRepository fundRepo;
    private final FundFavoriteService favoriteService;


    /**
     * 펀드 상세 페이지 - legacy
     */
    /*
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {
        // 잠시 주석 처리
        Fund fund = fundRepo.findById(id).orElseThrow();
        model.addAttribute("fund", fund);

        User user = (User) session.getAttribute("user");
        boolean isFav = favoriteService.isFavorite(user.getUserId(), id);
        model.addAttribute("isFavorite", isFav);


        return "mypage/fund-detail";
    }
    */

    /**
     * 보유 펀드 목록
     */
    @GetMapping("/my")
    public String myFundHoldings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";

        // (보유 펀드 조회 로직 생략됨)
        return "mypage/my-fund-list";
    }
}
