package com.example.fund.admin.approval.repository;

import com.example.fund.admin.approval.entity.ApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalLogRepository extends JpaRepository<ApprovalLog, Integer> {
    List<ApprovalLog> findByApproval_ApprovalIdOrderByChangedAtDesc(Integer approvalId);

    List<ApprovalLog> findByStatusOrderByChangedAtDesc(String status);
}