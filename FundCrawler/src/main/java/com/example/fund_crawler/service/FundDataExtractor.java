package com.example.fund_crawler.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import com.example.fund_crawler.entity.Fund;
import com.example.fund_crawler.entity.FundPortfolio;
import com.example.fund_crawler.entity.FundReturn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FundDataExtractor {
    
	public Fund extractFundInfo(WebDriver driver, WebDriverWait wait, WebElement item) {
		try {        	
	        // 현재 윈도우 핸들 저장
	        String originalWindow = driver.getWindowHandle();
	        
	        // 페이지에서 기본 정보 추출
	        // 펀드명 추출
        	System.out.println("  📋 펀드 제목 추출 중...");
        	WebElement titleElement = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(item, By.cssSelector("p.item-thumb-tit > a")));
            String fundName = titleElement.getText();
            System.out.println("  ✅ 펀드명: " + fundName);

            // 펀드 유형, 위험도, 운용사 정보 추출 
            System.out.println("  📋 펀드 유형, 위험도, 운용사 정보 추출 중...");
            WebElement descElement = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(item, By.className("desc")));
            String desc = descElement.getText();
            String[] parts = desc.split("\\|");
            String riskText = parts.length > 0 ? parts[0].trim() : "";
            String fundType = parts.length > 1 ? parts[1].trim() : "";
            String managementCompany = parts.length > 2 ? parts[2].trim() : "";
            int riskLevel = mapRiskLevel(riskText);
            System.out.println("  ✅ 펀드 유형: " + fundType + ", 위험도: " + riskLevel + ", 운용사: " + managementCompany);

            // 펀드 설정일, 보수률 추출
            System.out.println("  📅 펀드 설정일, 보수률 추출 중...");
            WebElement priceDateElement = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(item, By.className("price-date")));
            List<WebElement> spans = priceDateElement.findElements(By.tagName("span"));
			
            String establishDateStr = "";
			String totalExpenseRatioStr = "";
			String navStr = "";
			
            for (WebElement span : spans) {
                String text = span.getText();
                if (text.contains("기준가")) {
                    navStr = text.split(":")[1].trim();
                } else if (text.contains("설정일")) {
                    establishDateStr = text.split(":")[1].trim();
                } else if (text.contains("총 보수")) {
                    totalExpenseRatioStr = text.split(":")[1].replace("%", "").trim();
                }
            }
            LocalDate establishDate = LocalDate.parse(establishDateStr.replace(".", "-"));
            BigDecimal totalExpenseRatio = new BigDecimal(totalExpenseRatioStr);
            BigDecimal nav = new BigDecimal(navStr.replace(",", "")); // 쉼표 제거 추가
            System.out.println("  ✅ 설정일: " + establishDate + ", 보수율: " + totalExpenseRatio);

            // 펀드 코드 추출 - 펀드 정보 페이지 열기 위함
            String fundCode = extractFundCodeWithJson(titleElement);
            if (fundCode.isEmpty()) {
                System.err.println("  ❌ 펀드 코드를 추출할 수 없어 상세 정보 조회를 건너뜁니다.");
                return null;
            }
            
            // iframe URL 구성
            String iframeUrl = "https://busanbank.funddoctor.co.kr/app/fund/fprofile.jsp?top_gb=P&panme_fund_cd=" + fundCode + "&furl=https://www.busanbank.co.kr/ib20/mnu/BHPCMN000000001?height=";
            
            // 새 탭 열기
            System.out.println("새 탭 열기");
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", iframeUrl);
            
            // 새 탭
            Set<String> allWindows = driver.getWindowHandles();
            String newWindow = null;
            for (String windowHandle : allWindows) {
                if (!windowHandle.equals(originalWindow)) {
                    newWindow = windowHandle;
                    break;
                }
            }
            
            if (newWindow != null) {
                System.out.println("  새 탭으로 전환");
                driver.switchTo().window(newWindow);
                System.out.println("  새 탭으로 전환 완료");
                
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                String bodyText = driver.findElement(By.tagName("body")).getText().trim();

                // ✅ 서버 오류 여부 확인 후 즉시 리턴
                if (bodyText.contains("판매펀드코드가 등록되어 있지 않습니다")
                        || bodyText.contains("데이타 받는중 통신장애")
                        || bodyText.contains("접속자수가 많아서")) {
                    System.out.println("  ❗ 서버 오류 감지 - 스킵: " + bodyText.substring(0, Math.min(100, bodyText.length())) + "...");

                    // 새 탭 닫기
                    System.out.println("  새 탭 닫기");
                    driver.close();
                    
                    // 새 탭으로 전환
                    System.out.println("  원래 탭으로 전환");
                    driver.switchTo().window(originalWindow);
                    return null;
                }
             
                // 상세 페이지 로딩 대기 및 정보 추출
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("tbl-type1")));

				LocalDate launchDate = null;
				Integer aum = null;
				String investmentRegion = "";
                
                List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table.tbl-type1 > tbody > tr")));
                for (WebElement row : rows) {
                    List<WebElement> ths = row.findElements(By.tagName("th"));
                    for (WebElement th : ths) {
                        String thText = th.getText().trim();
                        
                        // 펀드출범일 추출 및 변환
                        if (thText.equals("펀드출범일")) {
                            WebElement td = th.findElement(By.xpath("following-sibling::td[1]"));
                            String launchDateStr = td.getText().trim();
                            String dateOnly = launchDateStr.split("\\(")[0].trim();
                            launchDate = LocalDate.parse(dateOnly.replace(".", "-"));
                        }
                        // 펀드순자산액 추출 및 변환
                        else if (thText.equals("펀드순자산액")) {
                            WebElement td = th.findElement(By.xpath("following-sibling::td[1]"));
                            String aumStr = td.getText().trim();
                            String numberPart = aumStr.replace("억원", "").replace(",", "").trim();
                            aum = Integer.parseInt(numberPart);
                        }
                        // 투자지역 추출 및 변환
                        else if (thText.equals("투자지역")) {
                            WebElement td = th.findElement(By.xpath("following-sibling::td[1]"));
                            String regionStr = td.getText().trim();
                            investmentRegion = regionStr;
                        }
                    }
                }

                System.out.println("  펀드출범일: " + launchDate + ", 순자산액: " + aum + ", 투자지역: " + investmentRegion);
                
                // 새 탭 닫기
                System.out.println("  새 탭 닫기");
                driver.close();
                
                // 새 탭으로 전환
                System.out.println("  원래 탭으로 전환");
                driver.switchTo().window(originalWindow);
                
                return Fund.builder()
                        .fundName(fundName)
                        .fundType(fundType)
                        .investmentRegion(investmentRegion)	
                        .establishDate(establishDate)		
                        .launchDate(launchDate)				
                        .nav(nav)
                        .aum(aum)							
                        .totalExpenseRatio(totalExpenseRatio)
                        .riskLevel(riskLevel)
                        .managementCompany(managementCompany)
                        .build();
                
            } else {
                System.err.println("  ❌ 새 탭을 찾을 수 없습니다.");
                return null;
            }
            
        } catch (Exception e) {
        	System.err.println("  ❌ 펀드 정보 추출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
	

	public FundReturn extractFundReturnInfo(WebDriver driver, WebDriverWait wait, Fund fund, WebElement item) {
		try {       
	        System.out.println("  📊 펀드 수익률 정보 추출 중...");
	        
	        // 수익률 데이터 초기화
	        BigDecimal return1m = null;
	        BigDecimal return3m = null;
	        BigDecimal return6m = null;
	        BigDecimal return12m = null;
	        BigDecimal returnSince = null; 	

	        // tag-month 요소 찾기
	        WebElement tagMonthElement = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(item, By.className("tag-month")));
	        
	        // tag-box 요소들 찾기
	        List<WebElement> tagBoxes = tagMonthElement.findElements(By.className("tag-box"));
	        
	        for (WebElement tagBox : tagBoxes) {
	            try {
	                // 수익률 값 추출 (strong 태그)
	                WebElement strongElement = tagBox.findElement(By.tagName("strong"));
	                String returnValue = strongElement.getText().replace("%", "").trim();
	                
	                // 기간 정보 추출 (month 클래스)
	                WebElement monthElement = tagBox.findElement(By.className("month"));
	                String monthText = monthElement.getText().trim();
	                
	                // 기간에 따라 해당 수익률 설정
	                if (monthText.contains("1개월")) {
	                    return1m = new BigDecimal(returnValue.replace(",", ""));
	                } else if (monthText.contains("3개월")) {
	                    return3m = new BigDecimal(returnValue.replace(",", ""));
	                } else if (monthText.contains("6개월")) {
	                    return6m = new BigDecimal(returnValue.replace(",", ""));
	                } else if (monthText.contains("12개월")) {
	                    return12m = new BigDecimal(returnValue.replace(",", ""));
	                } else if (monthText.contains("누적")) {
	                    returnSince = new BigDecimal(returnValue.replace(",", ""));
	                }
	                
	            } catch (Exception e) {
	                System.err.println("  ⚠️ 수익률 데이터 파싱 중 오류: " + e.getMessage());
	                continue;
	            }
	        }

	        System.out.println("  ✅ 펀드 수익률 정보 추출 완료");
	        System.out.println("    - 1개월: " + return1m + "%");
	        System.out.println("    - 3개월: " + return3m + "%");
	        System.out.println("    - 6개월: " + return6m + "%");
	        System.out.println("    - 12개월: " + return12m + "%");
	        System.out.println("    - 누적: " + returnSince + "%");
	        
	        // FundReturn 객체 생성 및 반환
	        return FundReturn.builder()
	            .fund(fund)
	            .return1m(return1m)
	            .return3m(return3m)
	            .return6m(return6m)
	            .return12m(return12m)
	            .returnSince(returnSince)
	            .build();
		} catch (Exception e) {
        	System.err.println("  ❌ 펀드 정보 추출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
	}
	
	public FundPortfolio extractFundPortfolioInfo(WebDriver driver, WebDriverWait wait, Fund fund, WebElement item) {
		try {
	        // 현재 윈도우 핸들 저장
	        String originalWindow = driver.getWindowHandle();

            // 펀드 코드 추출 - 펀드 정보 페이지 열기 위함
	        WebElement titleElement = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(item, By.cssSelector("p.item-thumb-tit > a")));
	        String fundCode = extractFundCodeWithJson(titleElement);
	        
	        // iframe URL 구성
	        String iframeUrl = "https://busanbank.funddoctor.co.kr/app/fund/fportfolio.jsp?top_gb=P&panme_fund_cd=" + fundCode + "&furl=https://www.busanbank.co.kr/ib20/mnu/BHPCMN000000001?height=";
	        
            // 새 탭 열기
            System.out.println("새 탭 열기");
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", iframeUrl);
            
            // 새 탭
            Set<String> allWindows = driver.getWindowHandles();
            String newWindow = null;
            for (String windowHandle : allWindows) {
                if (!windowHandle.equals(originalWindow)) {
                    newWindow = windowHandle;
                    break;
                }
            }
            
            if (newWindow != null) {
                System.out.println("  새 탭으로 전환");
                driver.switchTo().window(newWindow);
                System.out.println("  새 탭으로 전환 완료");
                
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                String bodyText = driver.findElement(By.tagName("body")).getText().trim();

                // ✅ 서버 오류 여부 확인 후 즉시 리턴
                if (bodyText.contains("판매펀드코드가 등록되어 있지 않습니다")
                        || bodyText.contains("데이타 받는중 통신장애")
                        || bodyText.contains("접속자수가 많아서")) {
                    System.out.println("  ❗ 서버 오류 감지 - 스킵: " + bodyText.substring(0, Math.min(100, bodyText.length())) + "...");

                    // 새 탭 닫기
                    System.out.println("  새 탭 닫기");
                    driver.close();
                    
                    // 새 탭으로 전환
                    System.out.println("  원래 탭으로 전환");
                    driver.switchTo().window(originalWindow);
                    return null;
                }
                
                // 페이지 로딩 대기
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.tbl-type2")));
                
                // 4. 자산구성 테이블 찾기 (첫 번째 테이블만 사용)
                // List<WebElement> tables = driver.findElements(By.cssSelector("table.tbl-type2"));
                List<WebElement> tables = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table.tbl-type2")));
                
                // 펀드 포트폴리오 데이터 추출 중...
                WebElement assetTable = tables.get(0);
                List<WebElement> rows = assetTable.findElements(By.cssSelector("tbody > tr"));
                
                BigDecimal domesticStock = BigDecimal.ZERO;
                BigDecimal overseasStock = BigDecimal.ZERO;
                BigDecimal domesticBond = BigDecimal.ZERO;
                BigDecimal overseasBond = BigDecimal.ZERO;
                BigDecimal fundInvestment = BigDecimal.ZERO;
                BigDecimal liquidity = BigDecimal.ZERO;

                System.out.println("    🔄 자산 정보 추출 중...");
                for (WebElement row : rows) {
                    try {
                        List<WebElement> thElements = row.findElements(By.tagName("th"));
                        List<WebElement> tdElements = row.findElements(By.tagName("td"));
                        
                        // th가 없거나 td가 3개 미만인 경우 스킵 (빈 행 등)
                        if (thElements.isEmpty() || tdElements.size() < 3) {
                            continue;
                        }
                        
                        String assetType = thElements.get(0).getText().trim();
                        String ratioStr = tdElements.get(1).getText().trim(); // 펀드내 비중 (3번째 열)
                        
                        if (ratioStr.isEmpty() || ratioStr.equals("0") || ratioStr.equals("0.00")) {
                            continue;
                        }
                        
                        BigDecimal ratio = new BigDecimal(ratioStr.replace(",", ""));
                        
                        switch (assetType) {
                            case "국내주식" -> {
                                domesticStock = ratio;
                                System.out.println("      ▶ 국내주식: " + domesticStock + "%");
                            }
                            case "해외주식" -> {
                                overseasStock = ratio;
                                System.out.println("      ▶ 해외주식: " + overseasStock + "%");
                            }
                            case "국내채권" -> {
                                domesticBond = ratio;
                                System.out.println("      ▶ 국내채권: " + domesticBond + "%");
                            }
                            case "해외채권" -> {
                                overseasBond = ratio;
                                System.out.println("      ▶ 해외채권: " + overseasBond + "%");
                            }
                            case "펀드" -> {
                                fundInvestment = ratio;
                                System.out.println("      ▶ 펀드: " + fundInvestment + "%");
                            }
                            case "유동성 등" -> {
                                liquidity = ratio;
                                System.out.println("      ▶ 유동성 등: " + liquidity + "%");
                            }
                        }
                        
                    } catch (Exception e) {
                        System.err.println("    ⚠️ 자산 정보 행 파싱 중 오류: " + e.getMessage());
                        continue;
                    }
                }
                
                System.out.println("    ✅ 자산 정보 추출 완료");
                
                // 새 탭 닫기
                System.out.println("  새 탭 닫기");
                driver.close();
                
                // 새 탭으로 전환
                System.out.println("  원래 탭으로 전환");
                driver.switchTo().window(originalWindow);
                
                return FundPortfolio.builder()
                        .fund(fund)
                        .domesticStock(domesticStock)
                        .overseasStock(overseasStock)
                        .domesticBond(domesticBond)
                        .overseasBond(overseasBond)
                        .fundInvestment(fundInvestment)
                        .liquidity(liquidity)
                        .build();
                
            } else {
                System.err.println("  ❌ 새 탭을 찾을 수 없습니다.");
                return null;
            }
		} catch (Exception e) {
        	System.err.println("  ❌ 펀드 정보 추출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
	}

    
	/*
    public FundPortfolio extractFundAssetInfo(WebDriver driver, WebDriverWait wait, WebElement item, Fund fund) {
    	try {
        	System.out.println("  🖱️ 펀드 상세 페이지 접속 중...");
            WebElement titleElement = wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(item, By.cssSelector("p.item-thumb-tit > a")));
            titleElement.click();
            waitLoading(driver, wait);
        	System.out.println("  🖱️ 펀드 상세 페이지 접속 완료");
        	
            System.out.println("  💰 포트폴리오 분석 탭 클릭 중...");
            WebElement dailyTab = driver.findElement(By.id("tab07"));
            dailyTab.click();
            System.out.println("  💰 포트폴리오 분석 탭 클릭 완료");
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("wbframe")));

            System.out.println("  🔄 자산 정보 iframe 전환 중...");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("wbframe")));
            WebElement iframe = driver.findElement(By.id("wbframe"));
            driver.switchTo().frame(iframe);
            System.out.println("  🔄 자산 정보 iframe 전환 완료");
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            String bodyText = driver.findElement(By.tagName("body")).getText().trim();

            // ✅ 서버 오류 여부 확인 후 즉시 리턴
            if (bodyText.contains("판매펀드코드가 등록되어 있지 않습니다")
                    || bodyText.contains("데이타 받는중 통신장애")
                    || bodyText.contains("접속자수가 많아서")) {
                System.out.println("  ❗ 서버 오류 감지 - 스킵: " + bodyText.substring(0, Math.min(100, bodyText.length())) + "...");
                driver.switchTo().defaultContent();
                driver.findElement(By.className("layer-close")).click();
                return null;
            }
            
            // 자산구성 테이블 찾기
            List<WebElement> tables = driver.findElements(By.cssSelector("table.tbl-type2"));

            // 첫 번째 테이블의 데이터 파싱
            WebElement assetTable = tables.get(0);
            List<WebElement> rows = assetTable.findElements(By.cssSelector("tbody > tr"));

            BigDecimal domesticStock = BigDecimal.ZERO;
            BigDecimal overseasStock = BigDecimal.ZERO;
            BigDecimal domesticBond = BigDecimal.ZERO;
            BigDecimal overseasBond = BigDecimal.ZERO;
            BigDecimal fundInvestment = BigDecimal.ZERO;
            BigDecimal liquidity = BigDecimal.ZERO;
            
            System.out.println("  🔄 자산 정보 추출 중...");
            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < 2) continue;  // 빈행 스킵

                String assetType = row.findElement(By.tagName("th")).getText().trim();
                String ratioStr = cells.get(1).getText().trim(); // 펀드내 비중

                if (ratioStr.isEmpty()) continue;

                BigDecimal ratio = new BigDecimal(ratioStr);

                switch (assetType) {
                    case "국내주식" -> {
                        domesticStock = ratio;
                        System.out.println("    ▶ 국내주식: " + domesticStock + "%");
                    }
                    case "해외주식" -> {
                        overseasStock = ratio;
                        System.out.println("    ▶ 해외주식: " + overseasStock + "%");
                    }
                    case "국내채권" -> {
                        domesticBond = ratio;
                        System.out.println("    ▶ 국내채권: " + domesticBond + "%");
                    }
                    case "해외채권" -> {
                        overseasBond = ratio;
                        System.out.println("    ▶ 해외채권: " + overseasBond + "%");
                    }
                    case "펀드" -> {
                        fundInvestment = ratio;
                        System.out.println("    ▶ 펀드: " + fundInvestment + "%");
                    }
                    case "유동성 등" -> {
                        liquidity = ratio;
                        System.out.println("    ▶ 유동성 등: " + liquidity + "%");
                    }
                }
                

            }
            System.out.println("  🔄 자산 정보 추출 완료...");
            
            System.out.println("  🔄 메인 페이지로 복귀 중...");
            driver.switchTo().defaultContent();
            driver.findElement(By.className("layer-close")).click();
            System.out.println("  🔄 메인 페이지로 복귀 완료");
            
            return FundPortfolio.builder()
            		.fund(fund)
                    .domesticStock(domesticStock)
                    .overseasStock(overseasStock)
                    .domesticBond(domesticBond)
                    .overseasBond(overseasBond)
                    .fundInvestment(fundInvestment)
                    .liquidity(liquidity)
                    .build();
            
    	} catch (Exception e) {
        	System.err.println("  ❌ 펀드 정보 추출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    */
    
	
	private String extractFundCodeWithJson(WebElement element) {
	    try {
	        String dataVar = element.getAttribute("data-var");
	        if (dataVar == null || dataVar.isEmpty()) {
	            return "";
	        }
	        
	        // HTML 엔티티 디코딩
	        String decodedDataVar = dataVar.replace("&quot;", "\"");
	        
	        // JSON 파싱
	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode jsonNode = mapper.readTree(decodedDataVar);
	        
	        JsonNode fpNoNode = jsonNode.get("FP_NO");
	        if (fpNoNode != null) {
	            return fpNoNode.asText();
	        }
	        
	        return "";
	        
	    } catch (Exception e) {
	        System.err.println("  ❌ JSON 파싱 중 오류 발생: " + e.getMessage());
	        return "";
	    }
	}
	
	private String extractFundCodeWithRegex(WebElement element) {
	    try {
	        // data-var 속성 값 가져오기
	        String dataVar = element.getAttribute("data-var");
	        
	        if (dataVar == null || dataVar.isEmpty()) {
	            System.err.println("  ⚠️ data-var 속성이 없습니다.");
	            return "";
	        }
	        
	        // HTML 엔티티 디코딩 (&quot; → ")
	        String decodedDataVar = dataVar.replace("&quot;", "\"");
	        
	        // FP_NO 값 추출 (정규식 사용)
	        Pattern pattern = Pattern.compile("\"FP_NO\"\\s*:\\s*\"([^\"]+)\"");
	        Matcher matcher = pattern.matcher(decodedDataVar);
	        
	        if (matcher.find()) {
	            String fpNo = matcher.group(1);
	            System.out.println("  ✅ FP_NO 추출 성공: " + fpNo);
	            return fpNo;
	        } else {
	            System.err.println("  ⚠️ FP_NO를 찾을 수 없습니다.");
	            return "";
	        }
	        
	    } catch (Exception e) {
	        System.err.println("  ❌ FP_NO 추출 중 오류 발생: " + e.getMessage());
	        return "";
	    }
	}

    public void waitLoading(WebDriver driver, WebDriverWait wait) {
        System.out.println("  ⏳ 로딩 완료 대기 중...");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading-image")));
		System.out.println("  ✅ 로딩 완료");
    }
    
    private int mapRiskLevel(String text) {
        if (text.equals("초고위험(매우높은위험)")) return 1;
        if (text.equals("초고위험(높은위험)")) return 2;
        if (text.equals("고위험(다소높은위험)")) return 3;
        if (text.equals("중위험(보통위험)")) return 4;
        if (text.equals("저위험(낮은위험)")) return 5;
        if (text.equals("초저위험(매우낮은위험)")) return 6;
        return 0;
    }
}

/*

// 6. 다운로드 확인 버튼 클릭 (팝업 닫기)
try {
    WebElement confirmBtn = wait.until(
        ExpectedConditions.elementToBeClickable(By.id("confirmBtn"))
    );
    confirmBtn.click();
    System.out.println("  ✅ 다운로드 창 닫기 완료");
    
} catch (Exception confirmException) {
    System.err.println("  ❌ 다운로드 창 닫기 실패: " + confirmException.getMessage());
    
    // 대체 방법: ESC 키나 다른 닫기 버튼 시도
    try {
        WebElement closeBtn = driver.findElement(By.className("layer-close"));
        closeBtn.click();
        System.out.println("  ✅ 대체 방법으로 창 닫기 완료");
    } catch (Exception altCloseException) {
        System.err.println("  ❌ 대체 닫기 방법도 실패: " + altCloseException.getMessage());
    }
}



// iframe 기다리기
try {
    WebDriverWait iframeWait = new WebDriverWait(driver, Duration.ofSeconds(15));
    WebElement iframe = iframeWait.until(ExpectedConditions.presenceOfElementLocated(By.id("wbframe")));
    
    // iframe이 로드될 때까지 추가 대기
    Thread.sleep(2000);
    
    // iframe 전환 시도
    driver.switchTo().frame(iframe);
    System.out.println("  ✅ iframe 전환 완료");
    
} catch (Exception iframeException) {
    System.err.println("  ❌ iframe 전환 실패: " + iframeException.getMessage());
    System.err.println("  🔄 투자지역 정보 없이 계속 진행...");
    
    // 메인 콘텐츠로 돌아가기
    driver.switchTo().defaultContent();
    
    // 팝업 닫기 시도
    try {
        driver.findElement(By.className("layer-close")).click();
    } catch (Exception closeException) {
        // 닫기 버튼이 없어도 계속 진행
    }
}








/*
switch (assetType) {
    case "국내주식" -> domesticStock = ratio;
    case "해외주식" -> overseasStock = ratio;
    case "국내채권" -> domesticBond = ratio;
    case "해외채권" -> overseasBond = ratio;
    case "펀드" -> fundInvestment = ratio;
    case "유동성 등" -> liquidity = ratio;
}
*/