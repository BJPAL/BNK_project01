package com.example.fund.flutter.dto;


import com.example.fund.fund.entity.FundPublic;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FundSimpleDTO {
    private Long originFundId;
    private String fundName;
    private String fundType;
    private String investmentRegion;
    private LocalDate establishDate;
    private BigDecimal nav;
    private Integer aum;
    private Integer riskLevel;
    private String managementCompany;
    private String status;

    public static FundSimpleDTO from(FundPublic pub) {
        return new FundSimpleDTO(
                pub.getOriginFundId(),
                pub.getFundName(),
                pub.getFundType(),
                pub.getInvestmentRegion(),
                pub.getEstablishDate(),
                pub.getNav(),
                pub.getAum(),
                pub.getRiskLevel(),
                pub.getManagementCompany(),
                pub.getActive() ? "판매중" : "중단"
        );
    }
}