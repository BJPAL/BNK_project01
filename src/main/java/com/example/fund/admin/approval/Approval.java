package com.example.fund.admin.approval;

import com.example.fund.admin.Admin;
import com.example.fund.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_approval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "writer_id", referencedColumnName = "admin_id")
    private Admin writer;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @Column(length = 200)
    private String rejectReason; // 반려 사유
}
