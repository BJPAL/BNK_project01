package com.example.fund.fund.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.fund.fund.entity.Fund;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.fund.fund.entity.Fund;

public interface FundRepository extends JpaRepository<Fund, Long> {
    // 펀드 기본 정보 조회
    Optional<Fund> findByFundId(Long fundId);

    /** 위험 등급에 따른 펀드 목록 */
    Page<Fund> findByRiskLevelBetween(int start, int end, Pageable pageable);

    /*이름으로 펀드 검색*/ 
    List<Fund> findByFundNameContainingIgnoreCase(String name);

    /**
     * 필터링 조건을 적용한 펀드 목록 조회
     * - 투자성향에 따른 위험등급 범위 + 추가 필터들을 모두 적용
     *
     * @param startRiskLevel 최소 위험등급 (투자성향 기반)
     * @param endRiskLevel 최대 위험등급 (투자성향 기반)
     * @param riskLevels 사용자가 선택한 위험등급 리스트 (null 가능)
     * @param fundTypes 사용자가 선택한 펀드유형 리스트 (null 가능)
     * @param regions 사용자가 선택한 투자지역 리스트 (null 가능)
     * @param pageable 페이지네이션 정보
     * @return 조건에 맞는 펀드 페이지
     */
    @Query("SELECT f FROM Fund f WHERE " +
            "f.riskLevel BETWEEN :startRiskLevel AND :endRiskLevel " +
            "AND (:riskLevels IS NULL OR f.riskLevel IN :riskLevels) " +
            "AND (:fundTypes IS NULL OR f.fundType IN :fundTypes) " +
            "AND (:regions IS NULL OR f.investmentRegion IN :regions)")
    Page<Fund> findWithFilters(
            @Param("startRiskLevel") int startRiskLevel,
            @Param("endRiskLevel") int endRiskLevel,
            @Param("riskLevels") List<Integer> riskLevels,
            @Param("fundTypes") List<String> fundTypes,
            @Param("regions") List<String> regions,
            Pageable pageable
    );
}