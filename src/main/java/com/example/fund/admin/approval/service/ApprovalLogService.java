package com.example.fund.admin.approval.service;

import com.example.fund.admin.approval.entity.Approval;
import com.example.fund.admin.approval.entity.ApprovalLog;
import com.example.fund.admin.approval.repository.ApprovalLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalLogService {

    private final ApprovalLogRepository approvalLogRepository;

    public void saveLog(Approval approval, String changerId, String status, String reason) {
        ApprovalLog log = new ApprovalLog();
        log.setApproval(approval);
        log.setChangerId(changerId);
        log.setStatus(status);
        log.setReason(reason);
        approvalLogRepository.save(log);
    }

    public List<ApprovalLog> findAllByNewStatus(String newStatus) {
        return approvalLogRepository.findByStatusOrderByChangedAtDesc(newStatus);
    }

    public List<ApprovalLog> findAllByNewStatusAndWriter(String newStatus, String writer) {
        return approvalLogRepository.findAllByNewStatusAndWriter(newStatus, writer);
    }

    public List<ApprovalLog> getLogsByApprovalId(Integer approvalId) {
        return approvalLogRepository.findByApproval_ApprovalIdOrderByChangedAtDesc(approvalId.intValue());
    }
}