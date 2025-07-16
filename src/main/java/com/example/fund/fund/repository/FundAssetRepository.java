package com.example.fund.fund.repository;

import com.example.fund.fund.entity.FundAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundAssetRepository extends JpaRepository<FundAsset, Long> {
    // fundId로 1:1 매칭되는 자산 정보 조회
    Optional<FundAsset> findByFund_FundId(Long fundId);
}