package com.example.fund.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import com.example.fund.fund.dto.FundDetailResponse;
import com.example.fund.fund.entity.FundPolicy;
import com.example.fund.fund.repository.FundPolicyRepository;
import com.example.fund.fund.service.FundService;
import com.example.fund.qna.service.QnaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class MainAdminController {

    private final AdminService_A adminService_a;

    /* 1) /admin/ → 세션 O : 대시보드로 / 세션 X : 로그인 */
    @GetMapping({ "/", "" })
    public String root(HttpSession session) {
        return (session.getAttribute("admin") != null)
                ? "redirect:/admin/dashboard" // ★ 지표 있는 화면으로 보냄
                : "admin/login";

    }

    @Autowired
    QnaService qnaService;

    @Autowired
    FundService fundService;

    @Autowired
    FundPolicyRepository fundPolicyRepository;

    /* 2) /admin/main → 과거 주소로 들어와도 대시보드로 보냄 */
    @GetMapping("/main")
    public String legacyMain() {
        return "redirect:/admin/dashboard"; // ★ 한 줄로 끝
    }

    // 기존 2번
    // @GetMapping("/main")
    // public String main(HttpSession session, Model model) {
    // AdminDTO admin = (AdminDTO) session.getAttribute("admin");
    //
    // if (admin != null && "cs".equals(admin.getRole())) {
    // int unanswered = qnaService.countUnanwseQna();
    // model.addAttribute("unansweredCount", unanswered);
    // }
    // return "admin/main";
    // }

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

    /* ------------- 이하 기존 코드(로그아웃·관리자 CRUD 등) 그대로 -------------- */

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

    // @GetMapping("/list")
    // public String getAdminList(@RequestParam(required = false) String role,
    // Model model) {
    // List<AdminDTO> admins = (role == null || role.isEmpty())
    // ? adminService_a.getAllAdmins()
    // : adminService_a.getAdminsByRole(role);
    // model.addAttribute("adminList", admins);
    // return "admin/super/adminList :: admin-list-content";
    // }

    // 관리자 리스트 컨트롤러(role 파라미터는 필수값 X)
    // @GetMapping("/list")
    // public String getAdminList(@RequestParam(required = false) String role, Model
    // model){
    // List<AdminDTO> admins = new ArrayList<>();
    // if (role == null || role.isEmpty()) {
    // admins = adminService_a.getAllAdmins();
    // } else {
    // admins = adminService_a.getAdminsByRole(role); // 역할별 조회
    // }
    // model.addAttribute("adminList", admins);
    // return "admin/super/adminList :: admin-list-content";
    // }

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
    
    //펀드 등록 폼으로 이동
    @GetMapping("/fund/new")
    public String newFundForm() {

        return "fund/fundRegister";
    }

    @GetMapping("/fund/list")
    public String fundListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
        Page<FundPolicy> policyPage = fundPolicyRepository.findAllWithFund(pageable);
        model.addAttribute("policyPage", policyPage);
        return "fund/fundRegistList";
    }

    //상세보기 페이지로 이동
    @GetMapping("/fund/view/{id}")
    public String viewFundDetail(@PathVariable("id") Long id,
                                @RequestParam(name = "includePolicy", defaultValue = "true") boolean includePolicy,
                                Model model) {
        FundDetailResponse fund = includePolicy
                ? fundService.getFundDetailWithPolicy(id)
                : fundService.getFundDetailBasic(id);

        model.addAttribute("fund", fund);
        return "fund/fundRegistDetail"; // 🔁 템플릿 경로에 맞게 파일명 확인
    }

    //수정하기 페이지로 이동
    @GetMapping("/fund/edit/{id}")
    public String editPage(@PathVariable("id") Long id,
                        @RequestParam(name = "includePolicy", defaultValue = "false") boolean includePolicy,
                        Model model) {
        FundDetailResponse fund = includePolicy
                ? fundService.getFundDetailWithPolicy(id)
                : fundService.getFundDetailBasic(id);

        model.addAttribute("fund", fund);
        return "fund/fundRegistEdit";
    }
}
