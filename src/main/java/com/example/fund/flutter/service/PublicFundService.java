package com.example.fund.flutter.service;

import com.example.fund.flutter.dto.FundSimpleDTO;
import com.example.fund.fund.entity.FundPublic;
import com.example.fund.fund.repository.FundPublicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicFundService {

    private final FundPublicRepository pubRepo;

    public Page<FundSimpleDTO> getPublicFunds(int page, int size) {
        int safePage = Math.max(1, page); // 최소 1 보장
        Pageable pageable = PageRequest.of(safePage - 1, size, Sort.by("pubId").descending());
        return pubRepo.findByActiveTrue(pageable)
                .map(FundSimpleDTO::from);
    }

    public FundSimpleDTO getFundDetail(Long originId) {
        FundPublic pub = pubRepo
                .findTopByOriginFundIdAndActiveTrueOrderByVersionNoDesc(originId)
                .orElseThrow(() -> new IllegalArgumentException("펀드 없음"));
        return FundSimpleDTO.from(pub);
    }
}