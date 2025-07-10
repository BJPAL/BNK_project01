package com.example.fund.faq.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.faq.entity.Faq;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Integer> {
    List<Faq> findByActiveTrue();  // 활성화된 FAQ만 조회

	// 제목 또는 답변에 키워드가 포함된 FAQ 검색
	Page<Faq> findByQuestionContainingOrAnswerContaining(String keyword1, String keyword2, Pageable pageable);

}
