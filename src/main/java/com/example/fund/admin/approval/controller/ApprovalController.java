package com.example.fund.admin.approval.controller;

import com.example.fund.admin.approval.entity.Approval;
import com.example.fund.admin.approval.entity.ApprovalLog;
import com.example.fund.admin.approval.service.ApprovalLogService;
import com.example.fund.admin.approval.service.ApprovalService;
import com.example.fund.admin.dto.AdminDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
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

        //커튼
        var pending  = approvalService.getApprovalsByStatus("결재대기", pendingPage);
        var waiting  = approvalService.getApprovalsByStatus("배포대기", readyPage);
        var rejected = approvalService.getApprovalsByStatus("반려",     rejectedPage);
        model.addAttribute("pendingPage",  pending);
        model.addAttribute("readyPage",    waiting);
        model.addAttribute("rejectedPage", rejected);
        /* 추가 – 요약바에 쓸 건수 */
        model.addAttribute("pendingTotal",  pending.getTotalElements());
        model.addAttribute("waitingTotal",  waiting.getTotalElements());
        model.addAttribute("rejectedTotal", rejected.getTotalElements());

        // 로그인 관리자 역할
        model.addAttribute("adminRole", admin.getRole());

        return "admin/approval/manage";
    }

    // 승인 사유란 기입 + 간단한 예외 처리
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id,
                          @RequestParam(required=false) String reason,
                          HttpSession session,
                          RedirectAttributes redirect) {

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        /* 1차 컨트롤러 권한 체크 */
        if (!List.of("super", "approver", "planner")
                .contains(admin.getRole())) {
            redirect.addFlashAttribute("alertMessage", "승인 권한이 없습니다.");
            return "redirect:/admin/approval/manage";
        }

        try {
            /* ✏️ 두 번째 파라미터를 admin.getRole() 으로 전달 */
            approvalService.approve(id, admin.getRole(), reason);
            redirect.addFlashAttribute("alertMessage", "승인 처리되었습니다.");
        } catch (SecurityException ex) {
            redirect.addFlashAttribute("alertMessage", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace(); // 로그 확인용
            redirect.addFlashAttribute("alertMessage", "승인 중 시스템 오류가 발생했습니다.");
        }
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
    public String publish(@PathVariable("id") Long id,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/admin/";
        }

        // 배포 권한 체크 (예: planner만 가능)
        if (!"planner".equals(admin.getRole())) {
            redirectAttributes.addFlashAttribute("alertMessage", "배포 권한이 없습니다.");
            return "redirect:/admin/approval/manage";
        }

        // 정상 배포 처리
        approvalService.publish(id, admin.getAdminname());
        redirectAttributes.addFlashAttribute("successMessage", "성공적으로 배포되었습니다.");
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

        var pending  = approvalService.getApprovalsByStatus(adminname,"결재대기", pendingPage);
        var waiting  = approvalService.getApprovalsByStatus(adminname,"배포대기", waitingPage);
        var rejected = approvalService.getApprovalsByStatus(adminname,"반려",     rejectedPage);
        var published= approvalService.getApprovalsByStatus(adminname,"배포",     publishedPage);

        model.addAttribute("pendingPage",  pending);
        model.addAttribute("waitingPage",  waiting);
        model.addAttribute("rejectedPage", rejected);
        model.addAttribute("publishedPage",published);
        model.addAttribute("adminRole", admin.getRole());

        /* ★ 요약바 숫자 */
        model.addAttribute("pendingTotal",  pending.getTotalElements());
        model.addAttribute("waitingTotal",  waiting.getTotalElements());
        model.addAttribute("rejectedTotal", rejected.getTotalElements());
        model.addAttribute("publishedTotal",published.getTotalElements());

        return "admin/approval/list";
    }

    @GetMapping("/form")
    public String showForm(HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !"planner".equals(admin.getRole())) return "redirect:/admin/";

        return "admin/approval/form";
    }

    @PostMapping("/register")
    public String register(@RequestParam("title") String title,
                           @RequestParam("content") String content,
                           HttpSession session,
                           RedirectAttributes redirect) {

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !"planner".equals(admin.getRole())) {
            throw new SecurityException("결재 요청 권한이 없습니다.");
        }

        Long id = approvalService.createApproval(title, content, admin.getAdmin_id());
        redirect.addFlashAttribute("successMessage", "결재 요청이 완료되었습니다!");
        redirect.addFlashAttribute("highlightId", id);

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
    public String editForm(@PathVariable("id") Long id, HttpSession session, Model model) {
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
    public String updateApproval(@PathVariable("id") Long id,
                                 @RequestParam("title") String title,
                                 @RequestParam("content") String content,
                                 HttpSession session) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        approvalService.updateApproval(id, title, content, admin.getAdminname());
        return "redirect:/admin/approval/my-list";
    }

}