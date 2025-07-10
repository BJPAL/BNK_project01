package com.example.fund.auth.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {

    @Pattern(regexp = "^[a-z][a-zA-Z0-9]{5,14}$")
    private String username;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])\\S{8,15}$")
    private String password;

    private String confirmPassword;

    @Pattern(regexp = "^[가-힣]{2,4}$")
    private String name;

    // ✅ 하이픈 포함된 전화번호 허용 (예: 010-1234-5678)
    @Pattern(
            regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
            message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678"
    )
    private String phone;

    public boolean samePassword() {
        return password != null && password.equals(confirmPassword);
    }
}
