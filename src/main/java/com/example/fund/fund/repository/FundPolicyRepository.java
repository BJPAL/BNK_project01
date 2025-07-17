package com.example.fund.fund.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fund.fund.entity.FundPolicy;

@Repository
public interface FundPolicyRepository extends JpaRepository<FundPolicy, Long> {
}
