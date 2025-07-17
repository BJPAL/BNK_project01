package com.example.fund.fund.service;

import com.example.fund.fund.dto.*;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.FundAsset;
import com.example.fund.fund.entity.FundDailyPrice;
import com.example.fund.fund.entity.FundReturn;
import com.example.fund.fund.repository.FundAssetRepository;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.fund.repository.FundReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundService {

    private final FundRepository fundRepository;
    private final FundReturnRepository fundReturnRepository;
    private final FundAssetRepository fundAssetRepository;

    /** 투자 성향에 따른 펀드 목록 조회 - pagination */
    public Page<FundResponseDTO> findByInvestType(Integer investType, Pageable pageable) {
        // 투자 성향 → 위험 등급 범위 계산
        int startRiskLevel;
        int endRiskLevel = 6;
        // String investTypeName = "";

        switch (investType) {
            case 1 -> startRiskLevel = 6; // 안정형
            case 2 -> startRiskLevel = 5; // 안정 추구형
            case 3 -> startRiskLevel = 4; // 위험 중립형
            case 4 -> startRiskLevel = 3; // 적극 투자형
            case 5 -> startRiskLevel = 1; // 공격 투자형
            default -> throw new IllegalArgumentException("올바르지 않은 투자 성향입니다.");
        }

        Page<Fund> fundPage = fundRepository.findByRiskLevelBetween(startRiskLevel, endRiskLevel, pageable);

        // ✅ fundPage → fundResponsePage 변환
        Page<FundResponseDTO> fundResponsePage = fundPage.map(fund -> {
            FundReturn fundReturn = fundReturnRepository.findByFundId(fund.getFundId());

            return FundResponseDTO.builder()
                    .fundId(fund.getFundId())
                    .fundName(fund.getFundName())
                    .fundType(fund.getFundType())
                    .investmentRegion(fund.getInvestmentRegion())
                    .establishDate(fund.getEstablishDate())
                    .launchDate(fund.getLaunchDate())
                    .nav(fund.getNav())
                    .aum(fund.getAum())
                    .totalExpenseRatio(fund.getTotalExpenseRatio())
                    .riskLevel(fund.getRiskLevel())
                    .managementCompany(fund.getManagementCompany())
                    .return1m(fundReturn.getReturn1m())
                    .return3m(fundReturn.getReturn3m())
                    .return6m(fundReturn.getReturn6m())
                    .return12m(fundReturn.getReturn12m())
                    .returnSince(fundReturn.getReturnSince())
                    .build();
        });

        return fundResponsePage;
    }



    // ========

    /** 모든 펀드 목록 조회 */
    public List<Fund> findAll() {
        return fundRepository.findAll();
    }

    /** 특정 펀드 ID 조회 */
    public Optional<Fund> findById(Long id) {
        return fundRepository.findById(id);
    }

    /** 펀드 등록 */
    public Fund save(Fund fund) {
        return fundRepository.save(fund);
    }

    /** 펀드 정보 수정 */
    public Fund update(Long id, Fund updatedFund) {
        return fundRepository.findById(id)
            .map(fund -> {
                fund.setFundName(updatedFund.getFundName());
                fund.setFundType(updatedFund.getFundType());
                fund.setInvestmentRegion(updatedFund.getInvestmentRegion());
                fund.setEstablishDate(updatedFund.getEstablishDate());
                fund.setLaunchDate(updatedFund.getLaunchDate());
                fund.setNav(updatedFund.getNav());
                fund.setAum(updatedFund.getAum());
                fund.setTotalExpenseRatio(updatedFund.getTotalExpenseRatio());
                fund.setRiskLevel(updatedFund.getRiskLevel());
                fund.setManagementCompany(updatedFund.getManagementCompany());
                return fundRepository.save(fund);
            }).orElseThrow(() -> new RuntimeException("Fund not found"));
    }

    /** 특정 펀드 삭제  */
    public void delete(Long id) {
        fundRepository.deleteById(id);
    }
}
