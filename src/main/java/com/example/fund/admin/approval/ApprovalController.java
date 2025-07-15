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

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ApprovalLogService approvalLogService;

    @GetMapping("/manage")
    public String manageApprovals(HttpSession session, Model model,
                                  @RequestParam(defaultValue = "0") int pendingPage,
                                  @RequestParam(defaultValue = "0") int readyPage,
                                  @RequestParam(defaultValue = "0") int rejectedPage) {

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        if (!"super".equals(admin.getRole()) && !"approver".equals(admin.getRole())) {
            model.addAttribute("msg", "승인 권한이 없습니다.");
        }

        model.addAttribute("pendingPage", approvalService.getApprovalsByStatus("결재대기", pendingPage));
        model.addAttribute("readyPage", approvalService.getApprovalsByStatus("배포대기", readyPage));
        model.addAttribute("rejectedPage", approvalService.getApprovalsByStatus("반려", rejectedPage));

        return "admin/approval/manage";
    }

//    @PostMapping("/approve/{id}")
//    public String approve(@PathVariable("id") Long id, HttpSession session) {
//        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
//        if (admin == null) return "redirect:/admin/";
//
//        approvalService.approve(id, admin.getRole());
//        return "redirect:/admin/approval/manage";
//    }

    //아래로 승인 사유란 기입 가능하도록 수정
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable("id") Long id,
                          @RequestParam(value = "reason", required = false) String reason,
                          HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        approvalService.approve(id, admin.getRole(), reason); // 사유 전달
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
            approvalService.reject(id, reason, admin.getRole(), LocalDateTime.now());
            rttr.addFlashAttribute("msg", "반려 완료");
        } catch (Exception e) {
            rttr.addFlashAttribute("msg", "반려 실패: " + e.getMessage());
        }

        return "redirect:/admin/approval/manage";
    }

    @PostMapping("/publish/{id}")
    public String publish(@PathVariable("id") Long id, HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        approvalService.publish(id, admin.getAdminname(), LocalDateTime.now());
        return "redirect:/admin/approval/my-list";
    }

    @GetMapping("/my-list")
    public String getMyApprovals(HttpSession session, Model model,
                                 @RequestParam(defaultValue = "0") int pendingPage,
                                 @RequestParam(defaultValue = "0") int waitingPage,
                                 @RequestParam(defaultValue = "0") int rejectedPage,
                                 @RequestParam(defaultValue = "0") int publishedPage) {

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        String adminname = admin.getAdminname();

        model.addAttribute("pendingPage", approvalService.getApprovalsByStatus(adminname, "결재대기", pendingPage));
        model.addAttribute("waitingPage", approvalService.getApprovalsByStatus(adminname, "배포대기", waitingPage));
        model.addAttribute("rejectedPage", approvalService.getApprovalsByStatus(adminname, "반려", rejectedPage));
        model.addAttribute("publishedPage", approvalService.getApprovalsByStatus(adminname, "배포", publishedPage));

        return "admin/approval/list";
    }

    @GetMapping("/form")
    public String showForm(HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !"planner".equals(admin.getRole())) return "redirect:/admin/";

        return "admin/approval/form";
    }

    @PostMapping("/register")
    public String register(@RequestParam String title,
                           @RequestParam String content,
                           HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !"planner".equals(admin.getRole())) {
            throw new SecurityException("결재 요청 권한이 없습니다.");
        }

        approvalService.createApproval(title, content, admin.getAdmin_id());
        return "redirect:/admin/approval/my-list";
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable("id") Long id, HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        Approval approval = approvalService.findById(id);
        if (approval == null) {
            model.addAttribute("msg", "존재하지 않는 결재 요청입니다.");
            return "redirect:/admin/approval/my-list";
        }

        List<ApprovalLog> logs = approvalLogService.getLogsByApprovalId(id.intValue()); // 로그 조회

        model.addAttribute("approval", approval);
        model.addAttribute("logs", logs); // 모델 추가
        model.addAttribute("admin", admin);
        return "admin/approval/detail";
    }

    @GetMapping("/writer/{adminname}")
    public String viewByWriter(@PathVariable("adminname") String adminname,
                               @RequestParam(defaultValue = "0") int pendingPage,
                               @RequestParam(defaultValue = "0") int waitingPage,
                               @RequestParam(defaultValue = "0") int rejectedPage,
                               @RequestParam(defaultValue = "0") int publishedPage,
                               HttpSession session, Model model) {

        AdminDTO viewer = (AdminDTO) session.getAttribute("admin");
        if (viewer == null) return "redirect:/admin/";

        model.addAttribute("pendingPage", approvalService.getApprovalsByStatus(adminname, "결재대기", pendingPage));
        model.addAttribute("waitingPage", approvalService.getApprovalsByStatus(adminname, "배포대기", waitingPage));
        model.addAttribute("rejectedPage", approvalService.getApprovalsByStatus(adminname, "반려", rejectedPage));
        model.addAttribute("publishedPage", approvalService.getApprovalsByStatus(adminname, "배포", publishedPage));

        model.addAttribute("writerName", adminname);
        return "admin/approval/writer-list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        Approval approval = approvalService.findById(id);
        if (approval == null || !admin.getAdminname().equals(approval.getWriter().getAdminname())) {
            model.addAttribute("msg", "권한 없음");
            return "redirect:/admin/approval/my-list";
        }

        if (!"반려".equals(approval.getStatus())) {
            model.addAttribute("msg", "재기안 가능한 상태가 아닙니다.");
            return "redirect:/admin/approval/my-list";
        }

        model.addAttribute("approval", approval);
        return "admin/approval/form";
    }

    @PostMapping("/update/{id}")
    public String updateApproval(@PathVariable Long id,
                                 @RequestParam String title,
                                 @RequestParam String content,
                                 HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        approvalService.updateApproval(id, title, content, admin.getAdminname(), LocalDateTime.now());
        return "redirect:/admin/approval/my-list";
    }

}