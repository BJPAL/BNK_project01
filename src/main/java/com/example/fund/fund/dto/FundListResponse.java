package com.example.fund.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundListResponse {
    private List<FundPolicyResponseDTO> funds;
    private Integer investType;
    private String investTypeName;
}