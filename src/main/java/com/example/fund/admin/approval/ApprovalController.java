package com.example.fund.admin.approval;

import com.example.fund.admin.dto.AdminDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

//    @GetMapping("/manage")
//    public String getApprovalManage(@RequestParam(defaultValue = "0") int page,
//                                    @RequestParam(required = false) String status,
//                                    HttpSession session, Model model) {
//
//        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
//        if (admin == null) {
//            return "redirect:/admin/";
//        }
//
//        Pageable pageable = PageRequest.of(page, 10, Sort.by("regDate").descending());
//        Page<Approval> approvals = approvalService.getAllApprovals(status, pageable);
//
//        model.addAttribute("approvalList", approvals);
//        model.addAttribute("isSuper", "SUPER".equals(admin.getRole()));
//        model.addAttribute("admin", admin);
//        model.addAttribute("status", status);
//        return "approval_manage";
//    }

    @GetMapping("/manage")
    public String manageApprovals(HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        // 권한 확인
        if (!"SUPER".equals(admin.getRole()) && !"APPROVER".equals(admin.getRole())) {
            model.addAttribute("msg", "승인 권한이 없습니다.");
            return "error";
        }

        List<Approval> pendingList = approvalService.getApprovalsByStatus("결재대기");
        List<Approval> readyList = approvalService.getApprovalsByStatus("배포대기");
        List<Approval> rejectedList = approvalService.getApprovalsByStatus("반려");

        model.addAttribute("pendingList", pendingList);
        model.addAttribute("readyList", readyList);
        model.addAttribute("rejectedList", rejectedList);

        return "approval_manage";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable("id") Long id, HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/";
        }
        System.out.println(">>> 현재 로그인 관리자 role: " + admin.getRole());
        approvalService.approve(id, admin.getRole());
        return "redirect:/admin/approval/manage";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable("id") Long id,
                         @RequestParam("reason") String reason,
                         HttpSession session,
                         RedirectAttributes rttr) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        try {
            approvalService.reject(id, reason, admin.getRole());
            rttr.addFlashAttribute("msg", "반려 완료");
        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "반려 실패: " + e.getMessage());
        }

        return "redirect:/admin/approval/manage";
    }

    @PostMapping("/publish/{id}")
    public String publish(@PathVariable("id") Long id, HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/";
        }
        approvalService.publish(id, admin.getAdminname());
        return "redirect:/admin/approval/my-list";
    }

//    @GetMapping("/my-list")
//    public String getMyApprovals(@RequestParam(defaultValue = "0") int page,
//                                 HttpSession session, Model model) {
//        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
//        if (admin == null) return "redirect:/admin/";
//
//        System.out.println(">>> 현재 로그인 adminname: " + admin.getAdminname());
//
//        Pageable pageable = PageRequest.of(page, 10, Sort.by("regDate").descending());
//        Page<Approval> approvals = approvalService.getMyApprovals(admin.getAdminname(), pageable);
//
//        System.out.println(">>> 가져온 결재 수: " + approvals.getTotalElements());
//
//        model.addAttribute("approvalList", approvals);
//        model.addAttribute("admin", admin);
//        return "approval_list";
//    }

    @GetMapping("/my-list")
    public String getMyApprovals(HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        String adminname = admin.getAdminname();

        model.addAttribute("approvalsPending", approvalService.getApprovalsByStatus(adminname, "결재대기"));
        model.addAttribute("approvalsWaiting", approvalService.getApprovalsByStatus(adminname, "배포대기"));
        model.addAttribute("approvalsRejected", approvalService.getApprovalsByStatus(adminname, "반려"));
        model.addAttribute("approvalsPublished", approvalService.getApprovalsByStatus(adminname, "배포"));

        return "approval_list";
    }

    // 등록 폼 보여주기
    @GetMapping("/form")
    public String showForm(HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !"PLANNER".equals(admin.getRole())) {
            return "redirect:/admin/";
        }
        return "approval_form";
    }

    // 등록 처리
    @PostMapping("/register")
    public String register(@RequestParam String title,
                           @RequestParam String content,
                           HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !"PLANNER".equals(admin.getRole())) {
            throw new SecurityException("결재 요청 권한이 없습니다.");
        }

        approvalService.createApproval(title, content, admin.getAdmin_id());
        return "redirect:/admin/approval/my-list";
    }
}