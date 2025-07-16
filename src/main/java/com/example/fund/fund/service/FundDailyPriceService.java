package com.example.fund.fund.service;

import com.example.fund.fund.dto.FundReturnDTO;
import com.example.fund.fund.entity.FundDailyPrice;
import com.example.fund.fund.repository.FundDailyPriceRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundDailyPriceService {
    private final FundDailyPriceRepository fundDailyPriceRepository;

    /** 펀드 수익률 데이터 반환 메서드 */
    public FundReturnDTO getFundReturns(Long fundId) {
        return fundDailyPriceRepository.getFundReturnByFundId(fundId);
    }

    /** 최신 기준가 데이터 조회 */
    public Optional<FundDailyPrice> getLatestDailyPrice(Long fundId) {
        return fundDailyPriceRepository.findTopByFundFundIdOrderByBaseDateDesc(fundId);
    }
}
