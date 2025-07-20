package com.example.fund.fund.repository;

import com.example.fund.fund.entity.FundReturn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundReturnRepository extends JpaRepository<FundReturn, Long> {

    FundReturn findByFund_FundId(Long fundId);
}
