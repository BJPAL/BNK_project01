package com.example.fund.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.fund.auth.dto.JoinRequest;
import com.example.fund.user.entity.User;
import com.example.fund.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    /* ---------- 회원가입 ---------- */
    @Transactional
    public void register(JoinRequest dto) {
        if (repo.existsByUsername(dto.getUsername()))
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        if (repo.existsByPhone(dto.getPhone()))
            throw new IllegalStateException("이미 등록된 전화번호입니다.");

        // 하이픈 추가: 01012345678 → 010-1234-5678
        String formattedPhone = dto.getPhone()
                .replaceAll("(\\d{3})(\\d{3,4})(\\d{4})", "$1-$2-$3");

        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())   // 평문 저장
                .name(dto.getName())
                .phone(formattedPhone)         // 가공된 전화번호 저장
                .build();

        repo.save(user);
    }

    /* ---------- 로그인 ---------- */
    public User login(String id, String pw) {
        return repo.findByUsername(id)
                .filter(u -> u.getPassword().equals(pw))
                .orElse(null);
    }
}
