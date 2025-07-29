package com.example.fund.fund.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.fund.fund.entity.FundStatus;

public interface FundStatusRepository extends JpaRepository<FundStatus, Integer> {
	
	// 이전글
    FundStatus findTopByStatusIdLessThanOrderByStatusIdDesc(Integer id);

    // 다음글
    FundStatus findTopByStatusIdGreaterThanOrderByStatusIdAsc(Integer id);
    
    Page<FundStatus> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrCategoryContainingIgnoreCase(String title, String content, String category, Pageable pageable);
}
