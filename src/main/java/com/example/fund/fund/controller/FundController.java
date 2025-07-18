package com.example.fund.fund.controller;

import com.example.fund.favorite.service.FundFavoriteService;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.FundHolding;
import com.example.fund.fund.repository.FundHoldingRepository;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fund")
@CrossOrigin(origins = "*")
public class FundController {

    private final FundRepository fundRepo;
    private final FundFavoriteService favoriteService;
    private final FundHoldingRepository fundHoldingRepository;

    /** 펀드 상세 페이지 */
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {
        Fund fund = fundRepo.findById(id).orElse(null);
        if (fund == null) return "redirect:/error/404";

        model.addAttribute("fund", fund);

        User user = (User) session.getAttribute("user");
        boolean isFav = false;
        if (user != null) {
            isFav = favoriteService.isFavorite(user.getUserId(), id);
        }
        model.addAttribute("isFavorite", isFav);

        return "fund/detail";  // detail.html 경로 꼭 확인
    }

    /** 보유 펀드 목록 */
    @GetMapping("/my")
    public String myFundHoldings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";

        List<FundHolding> holdings =
                fundHoldingRepository.findByUser_UserIdOrderByJoinedAtDesc(Long.valueOf(user.getUserId()));
        model.addAttribute("holdings", holdings);
        return "mypage/my-fund-list";
    }
    @GetMapping
    public String fundRedirect(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        return "redirect:/fund/my";
    }

}
