package com.example.fund.admin.approval;

import com.example.fund.admin.entity.Admin;
import com.example.fund.admin.repository.AdminRepository_A;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalRepository approvalRepository;
    private final AdminRepository_A adminRepository;
    private final ApprovalLogService approvalLogService; //로그 서비스 주입

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

    public void approve(Long approvalId, String role, String reason) {
        if (!"super".equals(role) && !"approver".equals(role)) {
            throw new SecurityException("승인 권한이 없습니다.");
        }

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("결재 요청 없음"));

        if (!"결재대기".equals(approval.getStatus() != null ? approval.getStatus().trim() : "")) {
            throw new IllegalStateException("현재 상태에서 배포대기로 변경할 수 없습니다.");
        }

        approval.setStatus("배포대기");
        approvalRepository.save(approval);

        // 로그 저장
        approvalLogService.saveLog(approval, role, "배포대기", reason);
    }

    public void reject(Long id, String reason, String role, LocalDateTime now) {
        if (!"super".equals(role) && !"approver".equals(role)) {
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

        // 로그 저장
        approvalLogService.saveLog(approval, role, "반려", reason);
    }

    public void publish(Long id, String adminname, LocalDateTime now) {
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

        // 로그 저장
        approvalLogService.saveLog(approval, adminname, "배포", null);

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
    public Page<Approval> getApprovalsByStatus(String adminname, String status, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("regDate").descending());
        System.out.println(">>> 요청자용 호출됨: adminname=" + adminname + ", status=" + status + ", page=" + page);
        return approvalRepository.findByWriterAdminnameAndStatus(adminname, status, pageable);
    }

    // 승인자: 상태만
    public Page<Approval> getApprovalsByStatus(String status, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("regDate").descending());
        System.out.println(">>> 승인자용 호출됨: status=" + status + ", page=" + page);
        return approvalRepository.findByStatus(status, pageable);
    }

    //상세 페이지용
    public Approval findById(Long id) {
        return approvalRepository.findById(id).orElse(null);
    }

    //작성자별 찾기용
    public List<Approval> getApprovalsByWriter(String adminname) {
        Pageable pageable = Pageable.unpaged();
        return approvalRepository.findByWriterAdminname(adminname, pageable).getContent();
    }

    //재기안을 위한 메서드
    public void updateApproval(Long id, String title, String content, String adminname, LocalDateTime now) {
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 결재 요청 없음"));

        if (!adminname.equals(approval.getWriter().getAdminname())) {
            throw new SecurityException("수정 권한 없음");
        }

        if (!"반려".equals(approval.getStatus())) {
            throw new IllegalStateException("반려 상태만 수정 가능");
        }

        approval.setTitle(title);
        approval.setContent(content);
        approval.setStatus("결재대기"); // 다시 결재대기로 변경
        approval.setRejectReason(null); // 기존 반려 사유 제거
        approvalRepository.save(approval);

        // 로그 저장
        approvalLogService.saveLog(approval, adminname, "결재대기", "재기안");
    }
}
