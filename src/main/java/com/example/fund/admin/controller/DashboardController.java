package com.example.fund.admin.controller;

import com.example.fund.admin.approval.service.ApprovalService;
import com.example.fund.admin.dto.AdminDTO;
import com.example.fund.fund.service.FundService;
import com.example.fund.qna.service.QnaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final FundService     fundService;     // findAll() 만 사용
    private final QnaService      qnaService;      // countUnanwseQna() 사용
    private final ApprovalService approvalService; // getApprovalsByStatus() 사용

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        /* 1) 로그인 세션 확인 */
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        String role = admin.getRole();
        model.addAttribute("role", role);

        /* 2) 공통 지표 – 숫자 없으면 일단 0 */
        model.addAttribute("aum",        0);   // TODO: 자산 합계 로직 필요시 채우기
        model.addAttribute("todayPnL",   0);
        model.addAttribute("targetRate", 0);

        /* 3) 역할별 지표 – 기존 서비스 메서드만 활용 */
        if ("cs".equals(role) || "super".equals(role)) {
            model.addAttribute("unansweredCount",
                    qnaService.countUnanwseQna());
        }
        if ("planner".equals(role) || "super".equals(role)) {
            Page<?> myPage = approvalService
                    .getApprovalsByStatus(admin.getAdminname(), "결재대기", 0);
            model.addAttribute("myPendingCount", myPage.getTotalElements());
        }
        if ("approver".equals(role) || "super".equals(role)) {
            Page<?> waitPage = approvalService
                    .getApprovalsByStatus("결재대기", 0);
            model.addAttribute("waitingApproveCount",
                    waitPage.getTotalElements());
        }

        /* 4) 빠른 액세스 링크 */
        model.addAttribute("quickLinks", buildQuickLinks(role));

        return "admin/main";   // 뷰
    }

    /* -------- 링크 빌더 -------- */
    private List<QuickLink> buildQuickLinks(String role) {
        List<QuickLink> links = new ArrayList<>();
        links.add(new QuickLink("/admin/report/daily", "fas fa-file-alt", "일간 보고서"));
        switch (role) {
            case "cs" -> {
                links.add(new QuickLink("/admin/qnaList", "far fa-comment-dots", "문의 목록"));
                links.add(new QuickLink("/admin/faq/list", "fas fa-question", "FAQ 관리"));
            }
            case "planner" -> {
                links.add(new QuickLink("/admin/approval/list", "fas fa-file-signature", "내 결재 요청"));
                links.add(new QuickLink("/admin/approval/form", "fas fa-sitemap", "펀드 초안 등록"));
            }
            case "approver" -> {
                links.add(new QuickLink("/admin/approval/manage", "fas fa-gavel", "승인 관리"));
            }
            case "super" -> {
                links.add(new QuickLink("/admin/fund/list", "fas fa-sitemap", "펀드 목록"));
                links.add(new QuickLink("/admin/approval/manage", "fas fa-check-double", "결재 승인"));
                links.add(new QuickLink("/admin/qnaList", "far fa-comment-dots", "1:1 문의"));
                links.add(new QuickLink("/admin/faq/list", "fas fa-question", "FAQ 관리"));
            }
        }
        return links;
    }

    /* 작은 DTO */
    public record QuickLink(String url, String icon, String label) {}
}