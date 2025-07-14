package com.example.fund.qna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.qna.entity.Qna;

public interface QnaRepository extends JpaRepository<Qna, Integer> {
    Integer countByStatus(String status);

    List<Qna> findByStatus(String status);
}
