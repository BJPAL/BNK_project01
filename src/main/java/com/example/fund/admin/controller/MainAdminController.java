package com.example.fund.admin.controller;

import java.util.List;

import com.example.fund.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fund.admin.dto.AdminDTO;
import com.example.fund.admin.entity.Admin;
import com.example.fund.admin.service.AdminService_A;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class MainAdminController {

    private final AdminService_A adminService_a;
    private final QnaService qnaService;

    /* 1) /admin/ → 세션 O : 대시보드로 / 세션 X : 로그인 */
    @GetMapping({ "/", "" })
    public String root(HttpSession session) {
        return (session.getAttribute("admin") != null)
                ? "redirect:/admin/dashboard"
                : "admin/login";
    }

    /* 2) /admin/main → 과거 주소로 들어와도 대시보드로 보냄 */
    @GetMapping("/main")
    public String legacyMain() {
        return "redirect:/admin/dashboard";
    }

    /* 3) 로그인 성공 후 → /admin/dashboard */
    @PostMapping("/login")
    public String login(AdminDTO adminDTO,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        if (!adminService_a.login(adminDTO)) {
            rttr.addFlashAttribute("msg", "아이디 또는 비밀번호를 확인하세요");
            return "redirect:/admin/";
        }

        Admin adminEntity = adminService_a.searchAdmin(adminDTO);

        AdminDTO sess = new AdminDTO();
        sess.setRole(adminEntity.getRole());
        sess.setAdmin_id(adminEntity.getAdmin_id());
        sess.setName(adminEntity.getName());
        sess.setAdminname(adminEntity.getAdminname());

        request.getSession().setAttribute("admin", sess);

        return "redirect:/admin/dashboard"; // ★ 대시보드로 이동
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes rttr) {
        request.getSession().removeAttribute("admin");
        rttr.addFlashAttribute("logoutMsg", "로그아웃");
        return "redirect:/admin/";
    }

    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkDuplicateAdminname(@RequestParam String adminname) {
        return ResponseEntity.ok(adminService_a.check_id(adminname));
    }

    @GetMapping("/adminRegistForm")
    public String adminRegistForm() {
        return "admin/super/admin_register";
    }

    @GetMapping("/adminSetting")
    public String adminSetting() {
        return "admin/super/adminSetting";
    }

    @PostMapping("/adminRegist")
    public String adminRegist(AdminDTO adminDTO, RedirectAttributes rttr) {
        adminService_a.adminRegist(adminDTO);
        rttr.addFlashAttribute("msg", "관리자 등록을 완료하였습니다");
        return "admin/super/adminSetting";
    }

    // 관리자 리스트 컨트롤러(role 파라미터는 필수값 X) + 페이지네이션
    @GetMapping("/list")
    public String getAdminList(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminDTO> adminPage = (role == null || role.isEmpty())
                ? adminService_a.getAllAdmins(pageable)
                : adminService_a.getAdminsByRole(role, pageable);

        model.addAttribute("adminPage", adminPage);
        model.addAttribute("adminList", adminPage.getContent());
        return "admin/super/adminList :: admin-list-content";
    }

    // 관리자 리스트 >> 상세정보 조회
    @GetMapping("/detail/{id}")
    public String getAdminDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("admin", adminService_a.findById(id));
        return "admin/super/adminList :: admin-modify-modal";
    }

    @PostMapping("/updateRole")
    @ResponseBody
    public String updateRole(@RequestBody AdminDTO adminDTO) {
        adminService_a.updateRole(adminDTO.getAdmin_id(), adminDTO.getRole());
        return "success";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteAdmin(@PathVariable Integer id) {
        adminService_a.deleteAdmin(id);
        return "success";
    }

    @GetMapping("/qnaList")
    public String qnaList() {
        return "admin/cs/qnaSetting";
    }
}
