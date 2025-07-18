package com.example.fund_crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fund_crawler.entity.FundPortfolio;

@Repository
public interface FundAssetRepository extends JpaRepository<FundPortfolio, Long> {

}
