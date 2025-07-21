package com.example.fund.fund.repository;

import com.example.fund.fund.entity.FundReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundReturnRepository extends JpaRepository<FundReturn, Long> {
    /** 펀드 ID로 수익률 조회 */
    FundReturn findByFund_FundId(Long fundId);

    /** 펀드 ID로 수익률 조회 (Optional) */
    Optional<FundReturn> findOptionalByFund_FundId(Long fundId);
}
