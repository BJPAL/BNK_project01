package com.example.fund.admin.notice.service;

import com.example.fund.admin.notice.dto.AdminNoticeDTO;
import com.example.fund.admin.notice.entity.AdminNotice;
import com.example.fund.admin.notice.repository.AdminNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNoticeServiceImpl implements AdminNoticeService {
    private final AdminNoticeRepository repo;

    @Override
    public List<AdminNoticeDTO> findRecentAdminNotices(int limit) {
        return repo
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(n -> new AdminNoticeDTO(n.getId(), n.getTitle(), n.getContent(), n.getCreatedAt()))
                .toList();
    }

    @Override
    public void createAdminNotice(String title, String content, String author) {
        AdminNotice n = new AdminNotice();
        n.setTitle(title);
        n.setContent(content);
        n.setCreatedAt(LocalDateTime.now());
        repo.save(n);
    }

    @Override
    public AdminNoticeDTO findById(Long id) {
        AdminNotice n = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 없습니다. id=" + id));
        return new AdminNoticeDTO(n.getId(), n.getTitle(),n.getContent(), n.getCreatedAt());
    }

    @Override
    public void updateAdminNotice(Long id, String title, String content) {
        AdminNotice n = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지가 없습니다. id=" + id));
        n.setTitle(title);
        n.setContent(content);
        repo.save(n);
    }

    @Override
    public void deleteAdminNotice(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("해당 공지가 없습니다. id=" + id);
        }
        repo.deleteById(id);
    }
}