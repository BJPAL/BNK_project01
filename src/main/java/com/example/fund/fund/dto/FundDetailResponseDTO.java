package com.example.fund.fund.dto;

import com.example.fund.fund.entity.FundPublic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 상세 정보 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundDetailResponseDTO {
    // 펀드 기본 정보
    private Long fundId;
    private String fundName;
    private String fundType;
    private String investmentRegion;
    private LocalDate establishDate;
    private LocalDate launchDate;
    private BigDecimal nav;
    private Integer aum;
    private BigDecimal totalExpenseRatio;
    private Integer riskLevel;
    private String managementCompany;

    // 펀드 수익률 정보
    private BigDecimal return1m;
    private BigDecimal return3m;
    private BigDecimal return6m;
    private BigDecimal return12m;
    private BigDecimal returnSince;

    // 펀드 포트폴리오 정보
    private BigDecimal domesticStock;
    private BigDecimal overseasStock;
    private BigDecimal domesticBond;
    private BigDecimal overseasBond;
    private BigDecimal fundInvestment;
    private BigDecimal liquidity;

    // 접근 권한 정보
    private boolean accessAllowed;
    private String accessMessage;
    private Integer userInvestType;
    private Integer requiredMinRiskLevel;

    /** ── FundPublic 엔티티를 DTO로 변환하는 생성자 ── */
    public FundDetailResponseDTO(FundPublic pub) {
        this.fundId               = pub.getOriginFundId();
        this.fundName             = pub.getFundName();
        // pub에 매핑된 필드가 추가됐다면 여기에 더 채우기
        // 예를 들어 pub.getFundBrief() → fundBrief라면 해당 필드에 매핑
        // 나머지 필드는 필요에 따라 수익률, 포트폴리오 값 등을 세팅하거나
        // FundPublic에 담긴 JSON/서브객체를 파싱해서 세팅합니다.
    }
}
