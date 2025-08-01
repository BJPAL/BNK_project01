package com.example.fund.flutter.controller;

import com.example.fund.flutter.dto.FundSimpleDTO;
import com.example.fund.flutter.service.PublicFundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/funds")
@RequiredArgsConstructor
public class PublicFundController {

    private final PublicFundService publicFundService;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "1") int page,   // 클라이언트는 1-based
            @RequestParam(defaultValue = "10") int size) {

        int safePage = Math.max(1, page);                // 1 이상 보장
        int pageIndex = safePage - 1;                    // 0-based로 변환

        // (선택) 로그로 확인
        System.out.println("public funds list called: page=" + page + " -> pageIndex=" + pageIndex + ", size=" + size);

        Page<FundSimpleDTO> fundPage = publicFundService.getPublicFunds(pageIndex, size);
        return ResponseEntity.ok(fundPage);
    }
}