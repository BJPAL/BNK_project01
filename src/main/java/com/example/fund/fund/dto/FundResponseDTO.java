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
    private LocalDate establishDate;    // 설정일
    private LocalDate launchDate;           // 출범일
    private BigDecimal nav;                 // 기준금
    private Integer aum;                    // 순자산
    private BigDecimal totalExpenseRatio;   // 총 보수율
    private Integer riskLevel;          // 위험 등급
    private String managementCompany;   // 운용사
    private BigDecimal return1m;    // 1개월
    private BigDecimal return3m;    // 3개월
    private BigDecimal return6m;    // 6개월
    private BigDecimal return12m;   // 12개월
    private BigDecimal returnSince; // 누적
}