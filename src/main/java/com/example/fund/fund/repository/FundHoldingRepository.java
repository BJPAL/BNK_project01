package com.example.fund.fund.repository;

import com.example.fund.fund.entity.FundHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundHoldingRepository extends JpaRepository<FundHolding, Long> {
    List<FundHolding> findByUser_UserIdOrderByJoinedAtDesc(int userId);
}
