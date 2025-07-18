package com.example.fund.ai.service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.fund.fund.dto.FundResponseDTO;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.FundReturn;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.fund.repository.FundReturnRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompareAiService {

    private final ChatClient chatClient;

    private final FundRepository fundRepository;
    private final FundReturnRepository fundReturnRepository;

    public String talk(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    public String fundsCompare(List<Long> fundIds, Integer investType) {

        List<FundResponseDTO> compareList = new ArrayList<>();

        // 펀드 ID리스트를 FundResponseDTO 리스트로 변환하는 for문
        for (Long id : fundIds) {
            Fund fund = fundRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fund not found: " + id)); // 예외처리

            FundReturn fundReturn = fundReturnRepository.findByFund_FundId(id);
            if (fundReturn == null) {
                throw new RuntimeException("Fund return not found: " + id); // 예외처리
            }

            FundResponseDTO dto = FundResponseDTO.builder()
                    .fundId(fund.getFundId())
                    .fundName(fund.getFundName())
                    .fundType(fund.getFundType())
                    .investmentRegion(fund.getInvestmentRegion())
                    .establishDate(fund.getEstablishDate())
                    .launchDate(fund.getLaunchDate())
                    .nav(fund.getNav())
                    .aum(fund.getAum())
                    .totalExpenseRatio(fund.getTotalExpenseRatio())
                    .riskLevel(fund.getRiskLevel())
                    .managementCompany(fund.getManagementCompany())
                    .return1m(fundReturn.getReturn1m())
                    .return3m(fundReturn.getReturn3m())
                    .return6m(fundReturn.getReturn6m())
                    .return12m(fundReturn.getReturn12m())
                    .returnSince(fundReturn.getReturnSince())
                    .build();

            compareList.add(dto);
        }
        String user_Invert = invertConvert(investType); // invert를 String으로 변환
        String message = buildFundComparisonPrompt(compareList, user_Invert); // 프롬프트 작성 함수 호출
        String result = talk(message);

        return result;
    }

    // 프롬프트 생성 함수
    public String buildFundComparisonPrompt(List<FundResponseDTO> fundResponseList, String investType) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("다음은 비교하고 싶은 펀드들의 정보입니다.\n\n");
        promptBuilder.append("각 펀드의 이름, 총보수, 수익률(1개월, 3개월, 6개월, 12개월, 누적), 투자국가, 위험도가 포함되어 있습니다.\n\n");
        promptBuilder.append("또한 아래는 한 사용자의 투자 성향 분석 결과입니다.\n");
        promptBuilder.append("이 성향에 가장 적합한 펀드를 분석해 주세요.\n\n");

        promptBuilder.append("⚠️ 결과는 반드시 <strong>HTML 형식</strong>으로 출력해 주세요.\n");
        promptBuilder.append("예: <h2>제목</h2>, <table>, <tr>, <td>, <ul>, <li>, <p> 등을 사용해 시각적으로 보기 좋게 구성해 주세요.\n");
        promptBuilder.append("이 결과는 innerHTML에 삽입될 것이므로 <pre> 또는 markdown 코드는 사용하지 마세요.\n\n");

        promptBuilder.append("<h3>사용자 투자 성향 분석 결과</h3>\n");
        promptBuilder.append("<p>").append(investType).append("</p>\n\n");

        promptBuilder.append("<h3>펀드 비교 요청 사항</h3>\n");
        promptBuilder.append("<ul>\n");
        promptBuilder.append("<li>각 펀드의 수익성과 안정성 요약</li>\n");
        promptBuilder.append("<li>펀드별 장단점 비교</li>\n");
        promptBuilder.append("<li>위 투자 성향에 가장 적합한 펀드 추천 및 이유</li>\n");
        promptBuilder.append("</ul>\n\n");

        promptBuilder.append("<h3>펀드 정보</h3>\n");
        promptBuilder.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">\n");
        promptBuilder.append(
                "<tr><th>펀드명</th><th>총보수(%)</th><th>1M</th><th>3M</th><th>6M</th><th>12M</th><th>누적</th><th>투자국가</th><th>위험도</th></tr>\n");

        for (FundResponseDTO fund : fundResponseList) {
            promptBuilder.append("<tr>");
            promptBuilder.append("<td>").append(fund.getFundName()).append("</td>");
            promptBuilder.append("<td>").append(fund.getTotalExpenseRatio()).append("</td>");
            promptBuilder.append("<td>").append(fund.getReturn1m()).append("%</td>");
            promptBuilder.append("<td>").append(fund.getReturn3m()).append("%</td>");
            promptBuilder.append("<td>").append(fund.getReturn6m()).append("%</td>");
            promptBuilder.append("<td>").append(fund.getReturn12m()).append("%</td>");
            promptBuilder.append("<td>").append(fund.getReturnSince()).append("%</td>");
            promptBuilder.append("<td>").append(fund.getInvestmentRegion()).append("</td>");
            promptBuilder.append("<td>").append(fund.getRiskLevel()).append("</td>");
            promptBuilder.append("</tr>\n");
        }
        promptBuilder.append("</table>\n");

        return promptBuilder.toString();
    }

    // 받아온 투자성향결과 Inteager -> String 변환 함수
    private String invertConvert(Integer invert) {
        String result = "";
        switch (invert) {
            case 1:
                result = "안정형";
                break;

            case 2:
                result = "안정 추구형";
                break;

            case 3:
                result = "위험 중립형";
                break;
            case 4:
                result = "적극 투자형";
                break;
            case 5:
                result = "공격 투자형";
                break;

            default:
                break;
        }

        return result;
    }
}