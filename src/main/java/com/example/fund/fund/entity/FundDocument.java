package com.example.fund.fund.entity;

import java.time.LocalDate;

import com.example.fund.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fund_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long docId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "doc_type", length = 30)
    private String docType;

    @Column(name = "file_name", length = 100)
    private String fileName;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(name = "upload_date")
    private LocalDate uploadDate;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "doc_version", length = 10)
    private String docVersion;

    @Column(name = "is_active")
    private Boolean isActive;
}