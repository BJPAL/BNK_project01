package com.example.fund.fund.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.fund.entity.FundGuide;

public interface FundGuideRepository extends JpaRepository<FundGuide, Integer> {
	
	List<FundGuide> findByCategory(String category);
}
