package com.example.fund.fund.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.fund.fund.entity.FundPolicy;

@Repository
public interface FundPolicyRepository extends JpaRepository<FundPolicy, Long> {

    @Query("SELECT fp FROM FundPolicy fp LEFT JOIN FETCH fp.fund")
    List<FundPolicy> findAllWithFund();
}
