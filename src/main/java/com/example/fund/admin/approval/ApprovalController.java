package com.example.fund.admin.approval;


import com.example.fund.admin.Admin;
import com.example.fund.admin.AdminRole;
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
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping("/manage")
    public String getApprovalManage(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(required = false) ApprovalStatus status,
                                    HttpSession session, Model model) {

        Admin admin = (Admin) session.getAttribute("loginAdmin");

        Pageable pageable = PageRequest.of(page, 10, Sort.by("regDate").descending());
        Page<Approval> approvals = approvalService.getAllApprovals(Optional.ofNullable(status), pageable);

        model.addAttribute("approvalList", approvals);
        model.addAttribute("isSuper", admin.getRole() == AdminRole.SUPER);

        return "approval_manage"; // templates/approval_manage.html
    }

    // 승인 처리
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable("id") Long id, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loginAdmin");
        approvalService.approve(id, admin.getRole());
        return "redirect:/admin/approval/manage";
    }

    // 반려 처리
    @PostMapping("/reject/{id}")
    public String reject(@PathVariable("id") Long id,
                         @RequestParam("reason") String reason,
                         HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loginAdmin");
        approvalService.reject(id, reason, admin.getRole());
        return "redirect:/admin/approval/manage";
    }

    // 요청자가 배포 버튼 클릭
    @PostMapping("/publish/{id}")
    public String publish(@PathVariable("id") Long id, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loginAdmin");
        approvalService.publish(id, admin.getAdminname());
        return "redirect:/admin/approval/my-list";
    }

    // 요청자 결재 요청 내역
    @GetMapping("/my-list")
    public String getMyApprovals(@RequestParam(defaultValue = "0") int page,
                                 HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("loginAdmin");

        Pageable pageable = PageRequest.of(page, 10, Sort.by("regDate").descending());
        Page<Approval> approvals = approvalService.getMyApprovals(admin.getAdminname(), pageable);

        model.addAttribute("approvalList", approvals);
        return "approval_list";
    }
}