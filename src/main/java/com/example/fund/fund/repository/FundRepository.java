package com.example.fund.fund.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.fund.fund.entity.Fund;

public interface FundRepository extends JpaRepository<Fund, Long> {
}