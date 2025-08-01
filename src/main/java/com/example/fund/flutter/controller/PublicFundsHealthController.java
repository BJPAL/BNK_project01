package com.example.fund.flutter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/funds")
public class PublicFundsHealthController {
    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "open");
    }
}