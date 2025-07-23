package com.example.fund.admin.notice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminNoticeDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public AdminNoticeDTO(Long id, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}