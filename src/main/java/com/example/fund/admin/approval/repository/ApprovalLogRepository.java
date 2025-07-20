package com.example.fund.admin.approval.repository;

import com.example.fund.admin.approval.entity.ApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApprovalLogRepository extends JpaRepository<ApprovalLog, Integer> {
    List<ApprovalLog> findByApproval_ApprovalIdOrderByChangedAtDesc(Integer approvalId);

    List<ApprovalLog> findByStatusOrderByChangedAtDesc(String status);

    @Query("SELECT l FROM ApprovalLog l WHERE l.status = :status AND l.approval.writer.adminname = :writer")
    List<ApprovalLog> findAllByNewStatusAndWriter(@Param("status") String status,
                                                  @Param("writer") String writer);
}