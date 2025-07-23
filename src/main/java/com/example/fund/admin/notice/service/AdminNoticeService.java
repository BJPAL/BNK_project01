package com.example.fund.admin.notice.service;

import com.example.fund.admin.notice.dto.AdminNoticeDTO;

import java.util.List;

public interface AdminNoticeService {
    List<AdminNoticeDTO> findRecentAdminNotices(int limit);
    void createAdminNotice(String title, String content, String author);

    AdminNoticeDTO findById(Long id);
    void updateAdminNotice(Long id, String title, String content);
    void deleteAdminNotice(Long id);
}