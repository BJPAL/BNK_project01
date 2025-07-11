package com.example.fund.admin.service;

import com.example.fund.admin.converter.AdminConverter;
import com.example.fund.admin.dto.AdminDTO;
import com.example.fund.admin.entity.Admin;
import com.example.fund.admin.repository.AdminRepository_A;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService_A {

    @Autowired
    AdminRepository_A adminRepository_a;

    @Autowired
    AdminConverter adminConverter;


    public boolean login(AdminDTO adminDTO){
        Optional<Admin> admin = adminRepository_a.findByAdminnameAndPassword(adminDTO.getAdminname(), adminDTO.getPassword());
        return admin.isPresent();
    }

    public Admin searchAdmin(AdminDTO adminDTO){
        Optional<Admin> optionalAdmin = adminRepository_a.findByAdminnameAndPassword(adminDTO.getAdminname(), adminDTO.getPassword());
        Admin admin = null;
        if(optionalAdmin.isPresent()){
            admin = optionalAdmin.get();
        }

        return admin;
    }

    public boolean check_id(String adminname){
        return adminRepository_a.existsByAdminname(adminname);
    }

    public void adminRegist(AdminDTO adminDTO){
        adminDTO.setPassword("1234");//초기 등록시 비밀번호 1234로 고정
        Admin admin = adminConverter.toAdminEntity(adminDTO);

        adminRepository_a.save(admin);
    }
}
