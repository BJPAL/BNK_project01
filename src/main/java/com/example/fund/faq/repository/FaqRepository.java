package com.example.fund.faq.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.faq.entity.Faq;

public interface FaqRepository extends JpaRepository<Faq, Integer> {
	
	// 제목 또는 답변에 키워드가 포함된 FAQ 검색
	Page<Faq> findByQuestionContainingOrAnswerContaining(String keyword1, String keyword2, Pageable pageable);

}
