package com.example.fund.fund.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

//공개용 엔티티
@Entity
@Table(name="tbl_fund_public")
@Data
public class FundPublic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pub_id")
    private Long pubId;

    @Column(name = "origin_fund_id", nullable = false)
    private Long originFundId;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "fund_name", length = 200, nullable = false)
    private String fundName;

    @Column(name = "fund_type", length = 50)
    private String fundType;

    @Column(name = "investment_region", length = 50)
    private String investmentRegion;

    @Column(name = "establish_date")
    private LocalDate establishDate;

    @Column(name = "launch_date")
    private LocalDate launchDate;

    @Column(name = "nav", precision = 10, scale = 2)
    private BigDecimal nav;

    @Column(name = "aum", length = 3)
    private Integer aum;

    @Column(name = "total_expense_ratio", precision = 10, scale = 4)
    private BigDecimal totalExpenseRatio;

    @Column(name = "risk_level", length = 3)
    private Integer riskLevel;

    @Column(name = "management_company", length = 100)
    private String managementCompany;

    @Column(name = "fund_brief", columnDefinition = "TEXT")
    private String fundBrief;

    private Integer versionNo;

    private String status;
}