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

    @GetMapping("/manage")
    public String manageApprovals(HttpSession session, Model model,
                                  @RequestParam(defaultValue = "0") int pendingPage,
                                  @RequestParam(defaultValue = "0") int readyPage,
                                  @RequestParam(defaultValue = "0") int rejectedPage) {

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        if (!"SUPER".equals(admin.getRole()) && !"APPROVER".equals(admin.getRole())) {
            model.addAttribute("msg", "승인 권한이 없습니다.");
        }

        model.addAttribute("pendingPage", approvalService.getApprovalsByStatus("결재대기", pendingPage));
        model.addAttribute("readyPage", approvalService.getApprovalsByStatus("배포대기", readyPage));
        model.addAttribute("rejectedPage", approvalService.getApprovalsByStatus("반려", rejectedPage));

        return "admin/approval/manage";
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

    @GetMapping("/my-list")
    public String getMyApprovals(HttpSession session, Model model,
                                 @RequestParam(defaultValue = "0") int pendingPage,
                                 @RequestParam(defaultValue = "0") int waitingPage,
                                 @RequestParam(defaultValue = "0") int rejectedPage,
                                 @RequestParam(defaultValue = "0") int publishedPage) {

        System.out.println(">>> [Controller] /my-list 진입 성공");

        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        String adminname = admin.getAdminname();

        model.addAttribute("pendingPage", approvalService.getApprovalsByStatus(adminname, "결재대기", pendingPage));
        model.addAttribute("waitingPage", approvalService.getApprovalsByStatus(adminname, "배포대기", waitingPage));
        model.addAttribute("rejectedPage", approvalService.getApprovalsByStatus(adminname, "반려", rejectedPage));
        model.addAttribute("publishedPage", approvalService.getApprovalsByStatus(adminname, "배포", publishedPage));

        return "admin/approval/list";
    }

    // 등록 폼 보여주기
    @GetMapping("/form")
    public String showForm(HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null || !"PLANNER".equals(admin.getRole())) {
            return "redirect:/admin/";
        }
        return "admin/approval/form";
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

    //요청 상세 페이지
    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable("id") Long id, HttpSession session, Model model) {
        AdminDTO admin = (AdminDTO) session.getAttribute("admin");
        if (admin == null) return "redirect:/admin/";

        Approval approval = approvalService.findById(id);
        if (approval == null) {
            model.addAttribute("msg", "존재하지 않는 결재 요청입니다.");
            return "redirect:/admin/approval/my-list";
        }

        model.addAttribute("approval", approval);
        model.addAttribute("admin", admin); // 승인자 판단을 위해 필요
        return "admin/approval/detail";
    }

    //작성자 상태별로 리스트 가져오기
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

}