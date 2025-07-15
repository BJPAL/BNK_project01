package com.example.fund.fund.service;

import com.example.fund.fund.dto.FundReturnDTO;
import com.example.fund.fund.repository.FundDailyPriceRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundDailyPriceService {
    private final FundDailyPriceRepository fundDailyPriceRepository;

    public FundReturnDTO getFundReturns(Long fundId) {
        return fundDailyPriceRepository.getFundReturnByFundId(fundId);
    }

}
