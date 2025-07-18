package com.example.fund_crawler.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fund_crawler.entity.Fund;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {
    Optional<Fund> findByFundName(String fundName); // 예시: 중복 확인 등
}
