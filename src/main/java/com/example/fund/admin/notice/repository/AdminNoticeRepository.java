package com.example.fund.admin.notice.repository;

import com.example.fund.admin.notice.entity.AdminNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminNoticeRepository extends JpaRepository<AdminNotice, Long> {
    // 최신 공지 5건 조회 메서드
    List<AdminNotice> findTop5ByOrderByCreatedAtDesc();
}