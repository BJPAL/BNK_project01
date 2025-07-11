package com.example.fund.admin.approval;

import com.example.fund.admin.entity.Admin;
import com.example.fund.admin.faq.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalRepository approvalRepository;
    private final AdminRepository adminRepository;

    public Page<Approval> getMyApprovals(String adminname, Pageable pageable) {
        List<String> excludeStatuses = List.of("배포", "반려");
        return approvalRepository.findByWriterAdminnameAndStatusNotIn(adminname, excludeStatuses, pageable);
    }

    public Page<Approval> getAllApprovals(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return approvalRepository.findByStatus(status, pageable);
        }
        return approvalRepository.findAll(pageable);
    }

    public void approve(Long approvalId, String role) {
        if (!"SUPER".equals(role) && !"APPROVER".equals(role)) {
            throw new SecurityException("승인 권한이 없습니다.");
        }

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("결재 요청 없음"));

        if (!"결재대기".equals(approval.getStatus() != null ? approval.getStatus().trim() : "")) {
            throw new IllegalStateException("현재 상태에서 배포대기로 변경할 수 없습니다.");
        }

        approval.setStatus("배포대기");
        approvalRepository.save(approval);
    }

    public void reject(Long id, String reason, String role) {
        if (!"SUPER".equals(role) && !"APPROVER".equals(role)) {
            throw new SecurityException("반려 권한이 없습니다.");
        }

        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결재 없음"));

        if (!"결재대기".equals(approval.getStatus())) {
            throw new IllegalStateException("현재 상태에서 반려할 수 없습니다.");
        }

        approval.setStatus("반려");
        approval.setRejectReason(reason);
        approvalRepository.save(approval);
    }

    public void publish(Long id, String adminname) {
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결재 없음"));

        if (approval.getWriter() == null || !adminname.equals(approval.getWriter().getAdminname())) {
            throw new SecurityException("배포 권한 없음");
        }

        if (!"배포대기".equals(approval.getStatus())) {
            throw new IllegalStateException("배포 가능한 상태가 아닙니다.");
        }

        approval.setStatus("배포");
        approvalRepository.save(approval);

        // TODO: 실제 펀드 등록 로직 호출 (예: fundService.register(approval))
    }

    public void createApproval(String title, String content, Integer adminId) {
        Admin writer = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보 없음"));

        Approval approval = Approval.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .status("결재대기")
                .build();

        approvalRepository.save(approval);
        System.out.println("결재 요청 저장됨: " + approval.getTitle());
    }

    // 요청자: adminname + 상태
    public List<Approval> getApprovalsByStatus(String adminname, String status) {
        return approvalRepository.findByWriterAdminnameAndStatus(adminname, status);
    }

    // 승인자: 상태만
    public List<Approval> getApprovalsByStatus(String status) {
        return approvalRepository.findByStatus(status);
    }

}
