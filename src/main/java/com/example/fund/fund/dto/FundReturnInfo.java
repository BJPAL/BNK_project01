package com.example.fund.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 펀드 수익률 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundReturnInfo {
    private BigDecimal month1Return;    // 1개월
    private BigDecimal month3Return;    // 3개월
    private BigDecimal month6Return;    // 6개월
    private BigDecimal month12Return;   // 12개월
    private BigDecimal totalReturn;     // 총 수익률
}
