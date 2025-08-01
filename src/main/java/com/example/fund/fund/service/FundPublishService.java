package com.example.fund.fund.service;

import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.FundPublic;
import com.example.fund.fund.repository.FundPublicRepository;
import com.example.fund.fund.repository.FundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundPublishService {
    private final FundRepository fundRepo;      // 기존 Draft(Fund) repo
    private final FundPublicRepository pubRepo;

    @Transactional
    public void publishFromDraft(Long draftFundId) {
        // 1) Draft Fund 조회
        Fund draft = fundRepo.findById(draftFundId)
                .orElseThrow(() -> new IllegalArgumentException("Draft Fund not found: " + draftFundId));

        // 2) 기존 공개본이 있으면 비활성화
        pubRepo.findTopByOriginFundIdAndActiveTrueOrderByVersionNoDesc(draft.getFundId())
                .ifPresent(old -> {
                    old.setActive(false);
                    pubRepo.save(old);
                });

        // 3) 새 공개본 생성 & 필드 복사
        FundPublic pub = new FundPublic();
        pub.setOriginFundId(draft.getFundId());
        pub.setActive(true);

        // === 여기부터 실제 Draft Fund 게터 사용 ===
        pub.setFundName(draft.getFundName());
        pub.setFundType(draft.getFundType());
        pub.setInvestmentRegion(draft.getInvestmentRegion());
        pub.setEstablishDate(draft.getEstablishDate());
        pub.setLaunchDate(draft.getLaunchDate());
        pub.setNav(draft.getNav());
        pub.setAum(draft.getAum());
        pub.setTotalExpenseRatio(draft.getTotalExpenseRatio());
        pub.setRiskLevel(draft.getRiskLevel());
        pub.setManagementCompany(draft.getManagementCompany());
        // 필요시 추가 필드 복사...

        pubRepo.save(pub);
    }
}