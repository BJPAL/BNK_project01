package com.example.fund.fund.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundResponseDTO {
    private Long fundId;    // 펀드 ID
    private String fundName;    // 펀드명
    private String fundType;    // 펀드 유형
    private String investmentRegion;    // 투자 지역    
    private String managementCompany;   // 운용사
    private Integer riskLevel;          // 위험도
    private Integer aum;        // 순자산 규모 (aum) - 아직 안됨
    private BigDecimal basePrice;  // 기준금
    private Integer fundScale;  // 설정액
    private LocalDate establishDate;    // 설정일
    private BigDecimal totalExpenseRatio;   // 총 보수율

    // 수익률
    private BigDecimal month1Return;    // 1개월
    private BigDecimal month3Return;    // 3개월
    private BigDecimal month6Return;    // 6개월
    private BigDecimal month12Return;   // 12개월
}

/*
추가 고려 필드
*/