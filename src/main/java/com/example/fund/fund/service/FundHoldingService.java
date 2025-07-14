package com.example.fund.fund.service;

import com.example.fund.fund.entity.FundHolding;
import com.example.fund.fund.repository.FundHoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundHoldingService {

    private final FundHoldingRepository fundHoldingRepository;

    public List<FundHolding> getHoldingsByUserId(int userId) {
        return fundHoldingRepository.findByUser_UserIdOrderByJoinedAtDesc(userId);
    }
}
