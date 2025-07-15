package com.example.fund.fund.controller;

import com.example.fund.favorite.service.FundFavoriteService;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.InvestProfileResult;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.fund.repository.InvestProfileHistoryRepository;
import com.example.fund.fund.repository.InvestProfileResultRepository;
import com.example.fund.fund.service.FundService;
import com.example.fund.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fund")
public class FundController {

    private final FundRepository fundRepo;
    private final FundService fundService;
    private final FundFavoriteService favoriteService;

    // 최신 투자 성향 결과
    private final InvestProfileResultRepository investProfileResultRepository;
    // 사용자 투자 성향 기록
    private final InvestProfileHistoryRepository investProfileHistoryRepository;

    /** 투자 성향에 따른 펀드 목록 */
    @GetMapping("/list")
    public String list(
            HttpSession session,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        User user = (User) session.getAttribute("user");
        
        // 사용자 세션 여부
        if (user == null) {
            System.out.println("no have user");
            return "redirect:/auth/login";      // 로그인 필요
        }

        System.out.println(user);
        Integer userId = user.getUserId();

        // 투자 성향 존재 여부 확인
        Optional<InvestProfileResult> investResult = investProfileResultRepository
                .findByUser_UserId(userId);

        if (investResult.isPresent()) {
            Integer investType = investResult.get().getType().getTypeId().intValue();
            System.out.println("user invest type = " + investType);

            Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("fundId").descending());
            Page<Fund> fundPage = fundService.findByInvestType(investType, pageable);

            // Model에 데이터 저장
            model.addAttribute("fundList", fundPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", fundPage.getTotalPages());

            return "fundList";
        } else {
            // 투자 성향 검사 필요
            return "redirect:/profile";
        }
    }

    /** 펀드 상세 페이지 */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         HttpSession session,
                         Model model) {

        Fund fund = fundRepo.findById(id).orElseThrow();
        model.addAttribute("fund", fund);

        User user = (User) session.getAttribute("user");
        boolean isFav = favoriteService.isFavorite(user.getUserId(), id);
        model.addAttribute("isFavorite", isFav);


        return "mypage/fund-detail";
    }

    /** 보유 펀드 목록 */
    @GetMapping("/my")
    public String myFundHoldings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";

        // (보유 펀드 조회 로직 생략됨)
        return "mypage/my-fund-list";
    }
}
