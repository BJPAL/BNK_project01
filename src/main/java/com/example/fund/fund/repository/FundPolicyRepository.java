package com.example.fund.fund.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.fund.fund.entity.FundPolicy;

@Repository
public interface FundPolicyRepository extends JpaRepository<FundPolicy, Long> {

    @Query(value = "SELECT fp FROM FundPolicy fp LEFT JOIN FETCH fp.fund",
        countQuery = "SELECT count(fp) FROM FundPolicy fp")
    Page<FundPolicy> findAllWithFund(org.springframework.data.domain.Pageable pageable);

    Optional<FundPolicy> findByFund_FundId(Long fundId);
}
