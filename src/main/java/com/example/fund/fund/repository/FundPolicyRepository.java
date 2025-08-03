package com.example.fund.fund.repository;

import java.util.List;
import java.util.Optional;

import com.example.fund.fund.dto.FundPolicyResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fund.fund.entity.FundPolicy;

@Repository
public interface FundPolicyRepository extends JpaRepository<FundPolicy, Long> {

    @Query(value = "SELECT fp FROM FundPolicy fp LEFT JOIN FETCH fp.fund", countQuery = "SELECT count(fp) FROM FundPolicy fp")
    Page<FundPolicy> findAllWithFund(Pageable pageable);

    Optional<FundPolicy> findByFund_FundId(Long fundId);


    /**
     * FundPolicy 기반 필터링 조회 (isActive = true만)
     * - 투자성향에 따른 위험등급 범위 + 추가 필터들을 모두 적용
     *
     * @param startRiskLevel 최소 위험등급 (투자성향 기반)
     * @param endRiskLevel   최대 위험등급 (투자성향 기반)
     * @param riskLevels     사용자가 선택한 위험등급 리스트 (null 가능)
     * @param fundTypes      사용자가 선택한 펀드유형 리스트 (null 가능)
     * @param regions        사용자가 선택한 투자지역 리스트 (null 가능)
     * @param pageable       페이지네이션 정보
     * @return 조건에 맞는 FundPolicy 페이지 (Fund 정보 포함)
     */
    @Query("SELECT fp FROM FundPolicy fp " +
            "LEFT JOIN FETCH fp.fund f " +
            "WHERE fp.fundActive = true " +
            "AND f.riskLevel BETWEEN :startRiskLevel AND :endRiskLevel " +
            "AND (:riskLevels IS NULL OR f.riskLevel IN :riskLevels) " +
            "AND (:fundTypes IS NULL OR f.fundType IN :fundTypes) " +
            "AND (:regions IS NULL OR f.investmentRegion IN :regions)" +
            "ORDER BY f.fundId DESC")
    Page<FundPolicy> findActiveFundPoliciesWithFilters(
            @Param("startRiskLevel") int startRiskLevel,
            @Param("endRiskLevel") int endRiskLevel,
            @Param("riskLevels") List<Integer> riskLevels,
            @Param("fundTypes") List<String> fundTypes,
            @Param("regions") List<String> regions,
            Pageable pageable
    );

    /**
     * 투자성향(위험등급 범위) + (선택적) 위험/유형/지역 필터 적용 → FundPolicyResponseDTO 로 바로 생성
     */
    @Query("SELECT new com.example.fund.fund.dto.FundPolicyResponseDTO(" +
            "  f.fundId, f.fundName, f.fundType, f.investmentRegion," +
            "  f.establishDate, p.fundRelease, f.launchDate, f.nav, f.aum," +
            "  f.totalExpenseRatio, f.riskLevel, f.managementCompany," +
            "  p.fundTheme," +
            "  r.return1m, r.return3m, r.return6m, r.return12m, r.returnSince" +
            ") " +
            "FROM FundPolicy p " +
            "  JOIN p.fund f " +
            "  LEFT JOIN FundReturn r ON r.fund = f " +
            "WHERE p.fundActive = true " +
            "  AND f.riskLevel BETWEEN :startRisk AND :endRisk " +
            "  AND (:riskLevels IS NULL OR f.riskLevel   IN :riskLevels) " +
            "  AND (:fundTypes  IS NULL OR f.fundType      IN :fundTypes) " +
            "  AND (:regions    IS NULL OR f.investmentRegion IN :regions) " +
            "ORDER BY f.fundId DESC")
    Page<FundPolicyResponseDTO> findFilteredWithReturns(
            @Param("startRisk")   int startRiskLevel,
            @Param("endRisk")     int endRiskLevel,
            @Param("riskLevels")  List<Integer> riskLevels,
            @Param("fundTypes")   List<String>  fundTypes,
            @Param("regions")     List<String>  regions,
            Pageable pageable
    );


}

/*

//@Query("SELECT fp FROM FundPolicy fp " +
//        "LEFT JOIN FETCH fp.fund f " +
//        "WHERE fp.fundActive = true " +
//        "AND f.riskLevel BETWEEN :startRiskLevel AND :endRiskLevel " +
//        "AND (:riskLevels IS NULL OR f.riskLevel IN :riskLevels) " +
//        "AND (:fundTypes IS NULL OR f.fundType IN :fundTypes) " +
//        "AND (:regions IS NULL OR f.investmentRegion IN :regions))
//Page<FundPolicy> findActiveFundPoliciesWithFilters(
//        @Param("startRiskLevel") int startRiskLevel,
//        @Param("endRiskLevel") int endRiskLevel,
//        @Param("riskLevels") List<Integer> riskLevels,
//        @Param("fundTypes") List<String> fundTypes,
//        @Param("regions") List<String> regions,
//        Pageable pageable
//);
//
//
//SELECT fp FROM FundPolicy
//fp LEFT JOIN FETCH fp.fund f
//WHERE fp.fundActive = true AND f.riskLevel BETWEEN :startRiskLevel AND :endRiskLevel
//AND (:riskLevels IS NULL OR f.riskLevel IN :riskLevels_1)
//AND (:fundTypes IS NULL OR f.fundType IN :fundTypes_1)
//AND (:regions IS NULL OR f.investmentRegion IN :regions_1)
//ORDER BY f.fundId DESC, fp.fundId desc
//
//
//=====================================================================================
//
//@Query("SELECT fp FROM FundPolicy fp " +
//            "LEFT JOIN FETCH fp.fund f " +
//            "WHERE fp.fundActive = true " +
//            "AND f.riskLevel BETWEEN :startRiskLevel AND :endRiskLevel " +
//            "AND (:riskLevels IS NULL OR f.riskLevel IN :riskLevels) " +
//            "AND (:fundTypes IS NULL OR f.fundType IN :fundTypes) " +
//            "AND (:regions IS NULL OR f.investmentRegion IN :regions)" +
//            "ORDER BY f.fundId DESC")
//    Page<FundPolicy> findActiveFundPoliciesWithFilters(
//            @Param("startRiskLevel") int startRiskLevel,
//            @Param("endRiskLevel") int endRiskLevel,
//            @Param("riskLevels") List<Integer> riskLevels,
//            @Param("fundTypes") List<String> fundTypes,
//            @Param("regions") List<String> regions,
//            Pageable pageable
//    );


*/