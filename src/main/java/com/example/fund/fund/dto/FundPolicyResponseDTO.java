package com.example.fund.fund.dto;

import com.example.fund.fund.entity.FundPublic;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 목록 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPolicyResponseDTO {
    private Long fundId;    // 펀드 ID
    private String fundName;    // 펀드명
    private String fundType;    // 펀드 유형
    private String investmentRegion;    // 투자 지역
    private LocalDate establishDate;    // 설정일
    private LocalDate fundRelease;           // 개시일 - fund_Release 변경
    private LocalDate launchDate;           // 출범일 - deprecated
    private BigDecimal nav;                 // 기준금
    private Integer aum;                    // 순자산
    private BigDecimal totalExpenseRatio;   // 총 보수율
    private Integer riskLevel;          // 위험 등급
    private String managementCompany;   // 운용사

    // Fund_Policy 관련
    private String fundTheme;               // 펀드 테마

    // Fund_Return 관련
    @JsonProperty("return1m")     // 프런트 키와 맞춤
    private BigDecimal return1m;

    @JsonProperty("return3m")
    private BigDecimal return3m;

    @JsonProperty("return6m")
    private BigDecimal return6m;

    @JsonProperty("return12m")
    private BigDecimal return12m;

    @JsonProperty("returnSince")
    private BigDecimal returnSince;

    /** ▶ NEW: FundPublic 엔티티를 받아서 DTO로 변환하는 생성자 */
    public FundPolicyResponseDTO(FundPublic pub) {
        this.fundId             = pub.getOriginFundId();
        this.fundName           = pub.getFundName();
        this.fundType           = pub.getFundType();
        this.investmentRegion   = pub.getInvestmentRegion();
        this.establishDate      = pub.getEstablishDate();
        this.fundRelease        = pub.getLaunchDate();        // 필요시 변경
        this.launchDate         = pub.getLaunchDate();
        this.nav                = pub.getNav();
        this.aum                = pub.getAum();
        this.totalExpenseRatio  = pub.getTotalExpenseRatio();
        this.riskLevel          = pub.getRiskLevel();
        this.managementCompany  = pub.getManagementCompany();

        // 아래 필드들은 FundPublic에 없으므로 null 또는 기본값으로 둡니다.
        this.fundTheme          = null;
        this.return1m           = null;
        this.return3m           = null;
        this.return6m           = null;
        this.return12m          = null;
        this.returnSince        = null;
    }
}