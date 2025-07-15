package com.example.fund.fund.service;

import com.example.fund.fund.dto.FundResponseDTO;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.repository.FundDailyPriceRepository;
import com.example.fund.fund.repository.FundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundService {

    private final FundRepository fundRepository;
    private final FundDailyPriceService fundDailyPriceService;

    /** 투자 성향에 따른 펀드 목록 조회 - pagination */
    public Page<Fund> findByInvestType(Integer investType, Pageable pageable) {
        // 투자 성향 → 위험 등급 범위 계산
        int startRiskLevel;
        int endRiskLevel = 6;

        switch (investType) {
            case 1 -> startRiskLevel = 6; // 안정형
            case 2 -> startRiskLevel = 5; // 안정 추구형
            case 3 -> startRiskLevel = 4; // 위험 중립형
            case 4 -> startRiskLevel = 3; // 적극 투자형
            case 5 -> startRiskLevel = 1; // 공격 투자형
            default -> throw new IllegalArgumentException("올바르지 않은 투자 성향입니다.");
        }

        Page<Fund> fundPage = fundRepository.findByRiskLevelBetween(startRiskLevel, endRiskLevel, pageable);

        Page<FundResponseDTO> fundResponsePage = FundResponseDTO.builder();
    }

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
                fund.setClassType(updatedFund.getClassType());
                fund.setInvestmentRegion(updatedFund.getInvestmentRegion());
                fund.setEstablishDate(updatedFund.getEstablishDate());
                fund.setRiskLevel(updatedFund.getRiskLevel());
                fund.setFundStatus(updatedFund.getFundStatus());
                fund.setManagementCompany(updatedFund.getManagementCompany());
                fund.setBaseCurrency(updatedFund.getBaseCurrency());
                return fundRepository.save(fund);
            }).orElseThrow(() -> new RuntimeException("Fund not found"));
    }

    /** 특정 펀드 삭제  */
    public void delete(Long id) {
        fundRepository.deleteById(id);
    }
}
