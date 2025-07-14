package com.example.fund.qna.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.qna.entity.Qna;

public interface QnaRepository extends JpaRepository<Qna, Integer> {
    Integer countByStatus(String status);

    List<Qna> findByStatus(String status);

    Page<Qna> findByStatus(String status, Pageable pageable);

    List<Qna> findByUser_UserIdOrderByRegDateDesc(int userId);
}
