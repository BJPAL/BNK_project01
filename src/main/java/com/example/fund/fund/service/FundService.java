package com.example.fund.fund.service;

import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.repository.FundRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FundService {

    private final FundRepository fundRepository;

    public FundService(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    public List<Fund> findAll() {
        return fundRepository.findAll();
    }

    public Optional<Fund> findById(Long id) {
        return fundRepository.findById(id);
    }

    public Fund save(Fund fund) {
        return fundRepository.save(fund);
    }

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

    public void delete(Long id) {
        fundRepository.deleteById(id);
    }
}
