package com.example.fund.flutter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DevAuthController {

    @GetMapping("/login-dev")
    public Map<String, String> loginDev() {
        return Map.of(
                "accessToken", "dev-access-token",
                "refreshToken", "dev-refresh-token"
        );
    }
}