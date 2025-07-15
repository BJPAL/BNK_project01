package com.example.fund.admin.approval.repository;

import com.example.fund.admin.approval.entity.Approval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    @Query("SELECT a FROM Approval a JOIN a.writer w WHERE w.adminname = :adminname")
    Page<Approval> findByWriterAdminname(@Param("adminname") String adminname, Pageable pageable);

    Page<Approval> findByStatus(String status, Pageable pageable);

    Page<Approval> findByWriterAdminnameAndStatusNot(String adminname, String status, Pageable pageable);

    Page<Approval> findByWriterAdminnameAndStatusNotIn(String adminname, List<String> statuses, Pageable pageable);

    List<Approval> findByWriterAdminnameAndStatus(String adminname, String status);

    List<Approval> findByStatus(String status);

    Page<Approval> findByWriterAdminnameAndStatus(String adminname, String status, Pageable pageable);

}
