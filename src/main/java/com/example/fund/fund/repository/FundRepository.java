package com.example.fund.fund.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.fund.fund.entity.Fund;

import java.util.List;
import java.util.Optional;

public interface FundRepository extends JpaRepository<Fund, Long> {
    // 펀드 기본 정보 조회
    Optional<Fund> findByFundId(Long fundId);

    /** 위험 등급에 따른 펀드 목록 */
    Page<Fund> findByRiskLevelBetween(int start, int end, Pageable pageable);
}