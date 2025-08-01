package com.example.fund.fund.repository;

import com.example.fund.fund.entity.FundPublic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundPublicRepository extends JpaRepository<FundPublic, Long> {
    /** 최신 공개본만 꺼내오기 */
    Optional<FundPublic> findTopByOriginFundIdAndActiveTrueOrderByVersionNoDesc(Long originFundId);

    /** 전체 공개 펀드 리스트 */
    List<FundPublic> findByActiveTrueOrderByPubIdDesc();

    // ▶ NEW: pageable 지원 오버로드 메서드
    Page<FundPublic> findByActiveTrueOrderByPubIdDesc(Pageable pageable);

    Page<FundPublic> findByActiveTrue(Pageable pageable);
}