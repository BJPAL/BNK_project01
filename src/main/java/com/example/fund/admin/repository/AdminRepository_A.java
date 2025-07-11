package com.example.fund.admin.repository;

import com.example.fund.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository_A extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByAdminnameAndPassword(String adminname, String password); //로그인

    boolean existsByAdminname(String adminname); //아이디 중복체크

}
