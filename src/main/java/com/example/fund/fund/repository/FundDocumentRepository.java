package com.example.fund.fund.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fund.fund.entity.FundDocument;

@Repository
public interface FundDocumentRepository extends JpaRepository<FundDocument, Long>{

    List<FundDocument> findByFund_FundId(Long fundId);

    void deleteByFund_FundIdAndDocType(Long fundId, String docType);
}
