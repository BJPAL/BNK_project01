package com.example.fund_crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund_crawler.entity.FundPortfolio;

public interface FundPortfolioRepository extends JpaRepository<FundPortfolio, Long> {

}
