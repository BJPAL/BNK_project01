package com.example.fund.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 가격 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundPriceInfo {
    private LocalDate baseDate;         // 기준일
    private BigDecimal basePrice;       // 기준가격
    private BigDecimal changeAmount;    // 변동금액
    private BigDecimal changeRate;      // 변동률 (계산)
    private Integer aum;                // 순자산 (단위: 원)
    private Integer fundScale;          // 설정액 (단위: 원)
    private String aumFormatted;        // 포맷된 순자산 (예: "500억원")
    private String fundScaleFormatted;  // 포맷된 설정액 (예: "450억원")
}