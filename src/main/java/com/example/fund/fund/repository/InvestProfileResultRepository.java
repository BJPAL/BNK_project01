package com.example.fund.fund.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.fund.entity.InvestProfileResult;
import com.example.fund.user.entity.User;

public interface InvestProfileResultRepository extends JpaRepository<InvestProfileResult, Integer> {
	Optional<InvestProfileResult> findTopByUserOrderByAnalysisDateDesc(User user);
	Optional<InvestProfileResult> findByUser_UserId(Integer userId);
}
