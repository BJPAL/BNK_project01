package com.example.fund.fund.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.fund.fund.entity.InvestProfileQuestion;
import com.example.fund.fund.repository.InvestProfileQuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvestProfileService {

	@Autowired
    private InvestProfileQuestionRepository questionRepository;

    public List<InvestProfileQuestion> findAllWithOptions() {
        return questionRepository.findAllWithOptions();  // 아래에 나오는 커스텀 쿼리 사용
    }

    // 점수 총합을 기반으로 투자성향 유형명 리턴
    public String analyzeTypeByScore(int totalScore) {
        if (totalScore <= 10) {
            return "안정형";
        } else if (totalScore <= 17) {
            return "안정추구형";
        } else if (totalScore <= 24) {
            return "위험중립형";
        } else if (totalScore <= 31) {
            return "적극투자형";
        } else {
            return "공격투자형";
        }
    }
}

