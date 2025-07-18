package com.example.fund_crawler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fund_crawler.entity.FundReturn;

@Repository
public interface FundReturnRepository extends JpaRepository<FundReturn, Long> {

}
