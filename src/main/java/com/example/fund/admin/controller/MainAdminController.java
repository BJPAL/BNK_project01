package com.example.fund.admin.controller;

import com.example.fund.admin.dto.AdminDTO;
import com.example.fund.admin.entity.Admin;
import com.example.fund.admin.service.AdminService_A;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class MainAdminController {

    @Autowired
    AdminService_A adminService_a;

    @GetMapping("/")
    public String root(){
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(AdminDTO adminDTO, HttpServletRequest request, RedirectAttributes rttr){
        HttpSession session = request.getSession();

        if(adminService_a.login(adminDTO)){
            Admin admin = adminService_a.searchAdmin(adminDTO);
            AdminDTO sessionAdmin = new AdminDTO(); //세션등록을 위한 DTO (role, name, admin_Id만 세션에 저장)
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

    @PostMapping("/adminRegist") //관리자 등록 처리
    public String adminRegist(AdminDTO adminDTO, RedirectAttributes rttr){
        adminService_a.adminRegist(adminDTO);
        String msg = "관리자 등록을 완료하였습니다.";
        rttr.addFlashAttribute("msg", msg);
        return "admin/main";
    }
}
