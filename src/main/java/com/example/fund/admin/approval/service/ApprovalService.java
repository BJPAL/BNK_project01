package com.example.fund.admin.approval.service;

import com.example.fund.admin.approval.entity.Approval;
import com.example.fund.admin.approval.repository.ApprovalRepository;
import com.example.fund.admin.entity.Admin;
import com.example.fund.admin.repository.AdminRepository_A;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final AdminRepository_A  adminRepository;
    private final ApprovalLogService approvalLogService;   // 결재 로그 기록용

    /* 승인/반려 가능한 역할 */
    private static final List<String> APPROVER_ROLES =
            List.of("super", "approver", "planner");

    /* ───── 1. 내 결재 목록 (요청자) ───── */
    public Page<Approval> getMyApprovals(String adminname, Pageable pageable) {
        List<String> exclude = List.of("배포", "반려");
        return approvalRepository.findByWriterAdminnameAndStatusNotIn(
                adminname, exclude, pageable);
    }

    /* 모든 결재 목록 (승인자) */
    public Page<Approval> getAllApprovals(String status, Pageable pageable) {
        return (status != null && !status.isBlank())
                ? approvalRepository.findByStatus(status, pageable)
                : approvalRepository.findAll(pageable);
    }

    /* ───── 2. 승인 ───── */
    public void approve(Long approvalId, String role, String reason) {

        if (!APPROVER_ROLES.contains(role))
            throw new SecurityException("승인 권한이 없습니다.");

        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("결재 요청 없음"));

        if (!"결재대기".equals(
                approval.getStatus() != null ? approval.getStatus().trim() : ""))
            throw new IllegalStateException("현재 상태에서 배포대기로 변경할 수 없습니다.");

        approval.setStatus("배포대기");
        approvalRepository.save(approval);
        approvalLogService.saveLog(approval, role, "배포대기", reason);
    }

    /* ───── 3. 반려 ───── */
    public void reject(Long id, String reason, String role) {

        if (!APPROVER_ROLES.contains(role))
            throw new SecurityException("반려 권한이 없습니다.");

        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결재 없음"));

        if (!"결재대기".equals(approval.getStatus()))
            throw new IllegalStateException("현재 상태에서 반려할 수 없습니다.");

        approval.setStatus("반려");
        approval.setRejectReason(reason);
        approvalRepository.save(approval);
        approvalLogService.saveLog(approval, role, "반려", reason);
    }

    /* ───── 4. 배포 (요청자 본인) ───── */
    public void publish(Long id, String adminname) {

        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("결재 없음"));

        if (approval.getWriter() == null ||
                !adminname.equals(approval.getWriter().getAdminname()))
            throw new SecurityException("배포 권한이 없습니다.");

        if (!"배포대기".equals(approval.getStatus()))
            throw new IllegalStateException("배포 가능한 상태가 아닙니다.");

        approval.setStatus("배포");
        approvalRepository.save(approval);
        approvalLogService.saveLog(approval, adminname, "배포", null);
        // TODO: fundService.register(approval) 등 실제 펀드 등록 로직 호출
    }

    /* ───── 5. 결재 요청 등록 (요청자) ───── */
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
    }

    /* ───── 6. 목록 조회 (요청자·승인자) ───── */
    // 요청자
    public Page<Approval> getApprovalsByStatus(String adminname,
                                               String status, int page) {
        Pageable pageable = PageRequest.of(page, 10,
                Sort.by("regDate").descending());
        return approvalRepository.findByWriterAdminnameAndStatus(
                adminname, status, pageable);
    }
    // 승인자
    public Page<Approval> getApprovalsByStatus(String status, int page) {
        Pageable pageable = PageRequest.of(page, 10,
                Sort.by("regDate").descending());
        return approvalRepository.findByStatus(status, pageable);
    }

    /* ───── 7. 상세/재기안 등 기타 ───── */
    public Approval findById(Long id) {
        return approvalRepository.findById(id).orElse(null);
    }

    public List<Approval> getApprovalsByWriter(String adminname) {
        return approvalRepository
                .findByWriterAdminname(adminname, Pageable.unpaged())
                .getContent();
    }

    public void updateApproval(Long id, String title, String content,
                               String adminname) {

        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 결재 요청 없음"));

        if (!adminname.equals(approval.getWriter().getAdminname()))
            throw new SecurityException("수정 권한이 없습니다.");

        if (!"반려".equals(approval.getStatus()))
            throw new IllegalStateException("반려 상태만 수정 가능");

        approval.setTitle(title);
        approval.setContent(content);
        approval.setStatus("결재대기");
        approval.setRejectReason(null);
        approvalRepository.save(approval);

        approvalLogService.saveLog(approval, adminname, "결재대기", "재기안");
    }
}