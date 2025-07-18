package com.example.fund.fund.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fund.fund.dto.ApiResponse;
import com.example.fund.fund.dto.FundDetailResponse;
import com.example.fund.fund.dto.FundListResponse;
import com.example.fund.fund.dto.FundResponseDTO;
import com.example.fund.fund.dto.InvestTypeResponse;
import com.example.fund.fund.dto.PaginationInfo;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.FundDocument;
import com.example.fund.fund.entity.InvestProfileResult;
import com.example.fund.fund.repository.FundDocumentRepository;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.fund.repository.InvestProfileResultRepository;
import com.example.fund.fund.service.FundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/funds")
@CrossOrigin(origins = "*")
public class FundApiController {

    private final FundService fundService;
    private final InvestProfileResultRepository investProfileResultRepository;
    private final FundRepository fundRepository;
    private final FundDocumentRepository fundDocumentRepository;
    private static final int MIN_INVEST_TYPE = 1;
    private static final int MAX_INVEST_TYPE = 5;
    /**
     * 사용자 투자성향 조회 API
     */
    /*
        // 투자성향이 있는 경우
        {
          "success": true,
          "hasProfile": true,
          "investType": 3,
          "investTypeName": "위험 중립형",
          "message": "투자 성향을 성공적으로 조회했습니다."
        }

        // 투자성향이 없는 경우
        {
          "success": true,
          "hasProfile": false,
          "message": "투자 성향 검사가 필요합니다.",
          "investType": null
        }
    */
    @GetMapping("/invest-type")
    public ResponseEntity<ApiResponse<InvestTypeResponse>> getUserInvestType(@RequestParam Integer userId) {
        try {
            // 사용자 투자성향 조회
            Optional<InvestProfileResult> investResult = investProfileResultRepository.findByUser_UserId(userId);

            if (investResult.isEmpty()) {
                InvestTypeResponse response = InvestTypeResponse.builder()
                        .hasProfile(false)
                        .investType(null)
                        .investTypeName(null)
                        .build();

                return ResponseEntity.ok(
                        ApiResponse.success(response, "투자 성향 검사가 필요합니다.")
                );
            }

            InvestProfileResult result = investResult.get();
            Integer investType = result.getType().getTypeId().intValue();
            String investTypeName = getInvestTypeName(investType);

            InvestTypeResponse response = InvestTypeResponse.builder()
                    .hasProfile(true)
                    .investType(investType)
                    .investTypeName(investTypeName)
                    .build();

            log.info("투자 성향 조회 성공: userId={}, investType={}", userId, investType);
            return ResponseEntity.ok(
                    ApiResponse.success(response, "투자 성향을 성공적으로 조회했습니다.")
            );

        } catch (Exception e) {
            log.error("투자성향 조회 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(
                            "투자 성향 조회 중 오류가 발생했습니다.",
                            "INTERNAL_SERVER_ERROR"
                    ));
        }
    }

    /**
     * 투자 성향에 따른 펀드 목록 - REST API
     */
    /*
        {
          "success": true,
          "data": [...],
          "investType": 3,
          "investTypeName": "위험 중립형",
          "pagination": {
            "page": 1,
            "limit": 10,
            "total": 50,
            "totalPages": 5,
            "hasNext": true,
            "hasPrev": false,
            "currentItems": 10
          }
        }
    */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<FundListResponse>> getFundList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam int investType
    ) {
        try {
            // 투자성향 유효성 검사
            if (investType < MIN_INVEST_TYPE || investType > MAX_INVEST_TYPE) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(
                                "투자 성향은 1~5 사이의 값이어야 합니다.",
                                "INVALID_INVESTMENT_TYPE"
                        ));
            }

            // 페이지네이션 설정 (0-based indexing)
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("fundId").descending());

            // 펀드 데이터 조회
            Page<FundResponseDTO> fundPage = fundService.findByInvestType(investType, pageable);

            // 투자성향 이름
            String investTypeName = getInvestTypeName(investType);

            // 응답 데이터 구성
            FundListResponse fundListResponse = FundListResponse.builder()
                    .funds(fundPage.getContent())
                    .investType(investType)
                    .investTypeName(investTypeName)
                    .build();

            // 페이지네이션 정보
            PaginationInfo paginationInfo = PaginationInfo.from(fundPage, page);

            log.info("펀드 목록 조회 성공: investType={}, totalElements={}", investType, fundPage.getTotalElements());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            fundListResponse,
                            String.format("%s에 맞는 펀드 %d개를 조회했습니다.", investTypeName, fundPage.getNumberOfElements()),
                            paginationInfo
                    )
            );

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 파라미터: investType={}", investType, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "INVALID_INVESTMENT_TYPE"));
        } catch (Exception e) {
            log.error("펀드 목록 조회 중 오류 발생: investType={}", investType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(
                            "서버 오류가 발생했습니다.",
                            "INTERNAL_SERVER_ERROR"
                    ));
        }
    }

    // ===================================

    /** 펀드 상세 데이터 조회 - REST API */
    @GetMapping("/{fundId}")
    public ResponseEntity<?> getFundDetail(@PathVariable("fundId") Long fundId,
                                        @RequestParam(name = "includePolicy", defaultValue = "false") boolean includePolicy) {
        try {
            if (includePolicy) {
                FundDetailResponse response = fundService.getFundDetailWithPolicy(fundId);
                return ResponseEntity.ok(response);
            } else {
                FundDetailResponse response = fundService.getFundDetailBasic(fundId);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            e.printStackTrace(); // 간단한 로그
            return ResponseEntity.notFound().build();
        }
    }


    // =======================

    /**
     * 투자성향 이름 반환
     */
    private String getInvestTypeName(int investType) {
        return switch (investType) {
            case 1 -> "안정형";
            case 2 -> "안정 추구형";
            case 3 -> "위험 중립형";
            case 4 -> "적극 투자형";
            case 5 -> "공격 투자형";
            default -> "알 수 없음";
        };
    }

    /* 펀드 이름으로 검색  API */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchFund(@RequestParam("name") String name) {
        List<Fund> funds = fundRepository.findByFundNameContainingIgnoreCase(name);

        List<Map<String, Object>> result = funds.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("fundId", f.getFundId());
            map.put("fundName", f.getFundName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /* 공시파일 다운로드 */ 

    @GetMapping("/files/document/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFundDocument(@PathVariable("id") Long id) {
        FundDocument document = fundDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다."));

        Path path = Paths.get(document.getFilePath());
        org.springframework.core.io.Resource resource;

        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 잘못되었습니다.", e);
        }

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 읽을 수 없습니다.");
        }

        // 파일명 인코딩 (한글 파일 대응)
        String encodedFileName;
        try {
            encodedFileName = java.net.URLEncoder.encode(document.getDocTitle() + ".pdf", "UTF-8")
                    .replaceAll("\\+", "%20");
        } catch (Exception e) {
            encodedFileName = "document.pdf";
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }



}
