package com.example.fund.fund.repository;

import com.example.fund.fund.entity.FileHistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileHistoryRepository extends JpaRepository<FileHistory, Long> {

    List<FileHistory> findAllByOrderByUploadedAtDesc();

    List<FileHistory> findByFileTypeOrderByUploadedAtDesc(String type);
    
    List<FileHistory> findByFileType(String type);

}