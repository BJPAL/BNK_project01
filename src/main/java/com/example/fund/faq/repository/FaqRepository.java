package com.example.fund.faq.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fund.faq.entity.Faq;

public interface FaqRepository extends JpaRepository<Faq, Integer> {

}
