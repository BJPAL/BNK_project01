package com.example.fund.admin.approval;

import com.example.fund.admin.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;

    public Page<Approval> getMyApprovals(String adminname, Pageable pageable) {
        return approvalRepository.findByWriterAdminname(adminname, pageable);
    }

    public Page<Approval> getAllApprovals(Optional<ApprovalStatus> status, Pageable pageable) {
        return status.map(s -> approvalRepository.findByStatus(s, pageable))
                .orElse(approvalRepository.findAll(pageable));
    }

    public void approve(Long approvalId, AdminRole role) {
        if (role != AdminRole.SUPER) {
            throw new SecurityException("승인 권한이 없습니다.");
        }

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("결재 요청 없음"));

        approval.setStatus(ApprovalStatus.배포대기);
        approvalRepository.save(approval);
    }

    public void reject(Long id, String reason, AdminRole role) {
        if (role != AdminRole.SUPER) throw new SecurityException("반려 권한 없음");

        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결재 없음"));

        approval.setStatus(ApprovalStatus.반려);
        approval.setRejectReason(reason);
        approvalRepository.save(approval);
    }

    public void publish(Long id, String adminname) {
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결재 없음"));
        if (!approval.getWriter().getAdminname().equals(adminname)) {
            throw new SecurityException("배포 권한 없음");
        }
        if (!approval.getStatus().equals(ApprovalStatus.배포대기)) {
            throw new IllegalStateException("배포 가능한 상태가 아님");
        }
        approval.setStatus(ApprovalStatus.배포);
        approvalRepository.save(approval);
    }
}
