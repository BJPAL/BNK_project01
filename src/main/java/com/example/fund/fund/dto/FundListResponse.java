package com.example.fund.fund.dto;

import com.example.fund.fund.entity.FundPublic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundListResponse {
    private List<FundPolicyResponseDTO> funds;
    private Integer investType;
    private String investTypeName;

    //임시 테이블 생성용
    private Long originFundId;
    private String fundName;
    private String fundBrief;
    private BigDecimal nav;
    private Integer     aum;
    private LocalDate establishDate;
    private String      investmentRegion;
    private String      managementCompany;
    private Integer     riskLevel;

    public FundListResponse(FundPublic pub) {
        this.originFundId = pub.getOriginFundId();
        this.fundName     = pub.getFundName();
        this.fundBrief    = pub.getFundBrief();
        this.nav               = pub.getNav();
        this.aum               = pub.getAum();
        this.establishDate     = pub.getEstablishDate();
        this.investmentRegion  = pub.getInvestmentRegion();
        this.managementCompany = pub.getManagementCompany();
        this.riskLevel         = pub.getRiskLevel();
    }
}
