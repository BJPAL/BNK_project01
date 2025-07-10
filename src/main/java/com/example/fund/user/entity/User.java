package com.example.fund.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name="tbl_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer user_id;

    @Column(length = 15)
    @Pattern(regexp = "^[a-z][a-zA-Z0-9]{5,14}$", message = "ID는 영문자로 시작하고 영문자와 숫자 조합의 4~15자여야 합니다.")
    private String username;

    @Column(length = 15)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])\\S{8,15}$",
            message = "비밀번호는 영문으로 시작하고, 영문/숫자/특수문자를 모두 포함한 8~15자여야 합니다.")
    private String password;

    @Pattern(regexp = "^[가-힣]{2,5}$", message = "한글이름만 입력 가능 합니다.")
    private String name;

    @Column(length = 13)
    @Size(max = 13, message = "전화번호는 최대 13자리까지 입력됩니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.(예: 010-1234-5678)")
    private String phone;
}
