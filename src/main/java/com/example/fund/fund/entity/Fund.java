package com.example.fund.fund.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.fund.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fund extends BaseEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fund_id")
    private Long fundId;

    @Column(name = "fund_name", length = 200)
    private String fundName;

    @Column(name = "fund_type", length = 50)
    private String fundType;

    @Column(name = "class_type", length = 20)
    private String classType;

    @Column(name = "investment_region", length = 50)
    private String investmentRegion;

    @Column(name = "establish_date")
    private LocalDate establishDate;

    @Column(name = "total_expense_ratio", precision = 10, scale = 4)
    private BigDecimal totalExpenseRatio;

    @Column(name = "risk_level", length = 3)
    private Integer riskLevel;

    @Column(name = "fund_status", length = 10)
    private String fundStatus;

    @Column(name = "management_company", length = 100)
    private String managementCompany;

    @Column(name = "base_currency", length = 10)
    private String baseCurrency;

    @JoinColumn(name = "fund_policy_id")
    @OneToOne(mappedBy = "fund", fetch = FetchType.LAZY)
    private FundPolicy fundPolicy;
}
