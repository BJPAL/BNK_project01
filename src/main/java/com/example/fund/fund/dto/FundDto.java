package com.example.fund.fund.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundDto {
    private Long fundId;
    private String fundName;
    private String fundType;
    private String classType;
    private String investmentRegion;
    private LocalDate establishDate;
    private String riskLevel;
    private String fundStatus;
    private String managementCompany;
    private String baseCurrency;
}
