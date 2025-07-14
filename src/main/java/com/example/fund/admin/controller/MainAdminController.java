package com.example.fund.admin.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
public class MainAdminController {

    @Autowired
    AdminService_A adminService_a;

    @GetMapping("/")
    public String root(){
        return "admin/login";
    }

    @GetMapping("/main")
    public String main(){
        return "admin/main";
    }

    @PostMapping("/login")
    public String login(AdminDTO adminDTO, HttpServletRequest request, RedirectAttributes rttr){
        HttpSession session = request.getSession();

        if(adminService_a.login(adminDTO)){
            Admin admin = adminService_a.searchAdmin(adminDTO);
            AdminDTO sessionAdmin = new AdminDTO(); //세션등록을 위한 DTO (role, name, admin_Id, adminname만 세션에 저장)
            sessionAdmin.setRole(admin.getRole());
            sessionAdmin.setAdmin_id(admin.getAdmin_id());
            sessionAdmin.setName(admin.getName());
            sessionAdmin.setAdminname(admin.getAdminname());

            session.setAttribute("admin", sessionAdmin); //sessionAdmin으로 session등록
            return "admin/main";
        }
        String msg = "아이디 또는 비밀번호를 확인하세요";
        rttr.addFlashAttribute("msg", msg);
        return "redirect:/admin/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.invalidate();
        return "admin/login";
    }

    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkDuplicateAdminname(@RequestParam String adminname){
        boolean exists = adminService_a.check_id(adminname);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/adminRegistForm")
    public String adminRegistForm(){ //관리자 등록 폼으로 이동
        return "admin/super/admin_register";
    }

    @GetMapping("/adminSetting")
    public String adminSetting(){
        return "admin/super/adminSetting";
    }

    //관리자 등록 처리
    @PostMapping("/adminRegist")
    public String adminRegist(AdminDTO adminDTO, RedirectAttributes rttr){
        adminService_a.adminRegist(adminDTO);
        String msg = "관리자 등록을 완료하였습니다.";
        rttr.addFlashAttribute("msg", msg);
        return "admin/super/adminSetting";
    }

    //관리자 리스트 컨트롤러(role 파라미터는 필수값 X)
    @GetMapping("/list")
    public String getAdminList(@RequestParam(required = false) String role, Model model){
        List<AdminDTO> admins = new ArrayList<>();
        if (role == null || role.isEmpty()) {
            admins = adminService_a.getAllAdmins();
        } else {
            admins = adminService_a.getAdminsByRole(role); // 역할별 조회
        }
        model.addAttribute("adminList", admins);
        return "admin/super/adminList :: admin-list-content";
    }

    //관리자 리스트 >> 상세정보 조회
    @GetMapping("/detail/{id}")
    public String getAdminDetail(@PathVariable("id")Integer id, Model model){
        AdminDTO admin = adminService_a.findById(id);
        model.addAttribute("admin", admin);

        return "admin/super/adminList :: admin-modify-modal";
    }

    //관리자 리스트 >> 상세정보 조회 >> 수정작업
    @PostMapping("/updateRole")
    @ResponseBody
    public String updateRole(@RequestBody AdminDTO adminDTO){
        System.out.println(adminDTO);
        adminService_a.updateRole(adminDTO.getAdmin_id(), adminDTO.getRole());
        return "success";
    }

    //관리자 리스트 >> 상세정보 조회 >> 삭제작업
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public String deleteAdmin(@PathVariable("id")Integer id){
        adminService_a.deleteAdmin(id);
        return "success";
    }
}
