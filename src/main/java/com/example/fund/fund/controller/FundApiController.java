package com.example.fund.fund.controller;

import com.example.fund.fund.dto.*;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.InvestProfileResult;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.fund.repository.InvestProfileResultRepository;
import com.example.fund.fund.service.FundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/funds")
@CrossOrigin(origins = "*")
public class FundApiController {

    private final FundService fundService;
    private final InvestProfileResultRepository investProfileResultRepository;
    private final FundRepository fundRepository;
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam int investType,
            @RequestParam(required = false) List<String> risk,      // 위험등급 필터
            @RequestParam(required = false) List<String> type,      // 펀드유형 필터
            @RequestParam(required = false) List<String> region     // 투자지역 필터
    ) {
        try {
            // 1. 투자성향 유효성 검사
            if (investType < MIN_INVEST_TYPE || investType > MAX_INVEST_TYPE) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(
                                "투자 성향은 1~5 사이의 값이어야 합니다.",
                                "INVALID_INVESTMENT_TYPE"
                        ));
            }

            // 2. 페이지네이션 설정 (0-based indexing)
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("fundId").descending());

            // 3. 필터 조건 로깅 (디버깅용)
            log.info("펀드 목록 조회 요청 - investType: {}, risk: {}, type: {}, region: {}, page: {}",
                    investType, risk, type, region, page);

            // 4. 펀드 데이터 조회 - 필터 조건이 있으면 필터링, 없으면 기본 조회
            Page<FundResponseDTO> fundPage;

            boolean hasFilters = (risk != null && !risk.isEmpty()) ||
                    (type != null && !type.isEmpty()) ||
                    (region != null && !region.isEmpty());

            if (hasFilters) {
                // 필터 조건이 있는 경우 - 새로운 메서드 사용
                log.info("필터링 조건 적용하여 펀드 조회");
                fundPage = fundService.findWithFilters(investType, risk, type, region, pageable);
            } else {
                // 필터 조건이 없는 경우 - 기존 메서드 사용
                log.info("투자성향만으로 펀드 조회");
                fundPage = fundService.findByInvestType(investType, pageable);
            }

            // 5. 투자성향 이름 조회
            String investTypeName = getInvestTypeName(investType);

            // 6. 응답 데이터 구성
            FundListResponse fundListResponse = FundListResponse.builder()
                    .funds(fundPage.getContent())
                    .investType(investType)
                    .investTypeName(investTypeName)
                    .build();

            // 7. 페이지네이션 정보 생성
            PaginationInfo paginationInfo = PaginationInfo.from(fundPage, page);

            // 8. 성공 응답 로깅
            log.info("펀드 목록 조회 성공: investType={}, 필터적용={}, totalElements={}",
                    investType, hasFilters, fundPage.getTotalElements());

            // 9. 성공 응답 반환
            String responseMessage = hasFilters
                    ? String.format("%s 조건으로 필터링된 펀드 %d개를 조회했습니다.", investTypeName, fundPage.getNumberOfElements())
                    : String.format("%s에 맞는 펀드 %d개를 조회했습니다.", investTypeName, fundPage.getNumberOfElements());

            return ResponseEntity.ok(
                    ApiResponse.success(
                            fundListResponse,
                            responseMessage,
                            paginationInfo
                    )
            );

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 파라미터: investType={}, error={}", investType, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage(), "INVALID_PARAMETER"));

        } catch (Exception e) {
            log.error("펀드 목록 조회 중 오류 발생: investType={}, filters=[risk:{}, type:{}, region:{}]",
                    investType, risk, type, region, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(
                            "서버 오류가 발생했습니다.",
                            "INTERNAL_SERVER_ERROR"
                    ));
        }
    }

    // ===========================================================================================

    /**
     * 펀드 상세 데이터 조회 - REST API
     */
    @GetMapping("/{fundId}")
    public ResponseEntity<Fund> getFundDetail(@PathVariable Long fundId) {
        return null;
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


}
