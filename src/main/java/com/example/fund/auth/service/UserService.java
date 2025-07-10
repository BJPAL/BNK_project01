package com.example.fund.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fund.auth.dto.JoinRequest;
import com.example.fund.user.entity.User;
import com.example.fund.user.repository.UserRepository;

import org.mindrot.jbcrypt.BCrypt;   // BCrypt만 수입

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    /* ---------- 회원가입 ---------- */
    @Transactional
    public void register(JoinRequest dto) {

        if (repo.existsByUsername(dto.getUsername())) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
        if (repo.existsByPhone(dto.getPhone())) {
            throw new IllegalStateException("이미 등록된 전화번호입니다.");
        }

        // 전화번호 포맷 정리
        String rawPhone       = dto.getPhone().replaceAll("[^0-9]", "");
        String formattedPhone = rawPhone.replaceAll("(\\d{3})(\\d{3,4})(\\d{4})", "$1-$2-$3");

        // 비밀번호 해싱 (cost=10이 기본, 필요하면 gensalt(12) 등으로 조정)
        String hashedPw = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());

        User user = User.builder()
                .username(dto.getUsername())
                .password(hashedPw)
                .name(dto.getName())
                .phone(formattedPhone)
                .build();

        repo.save(user);
    }

    /* ---------- 로그인 ---------- */
    public User login(String id, String pw) {
        return repo.findByUsername(id)
                .filter(u -> BCrypt.checkpw(pw, u.getPassword()))
                .orElse(null);
    }
}
