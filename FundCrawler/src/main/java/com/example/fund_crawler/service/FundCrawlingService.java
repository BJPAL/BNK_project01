package com.example.fund_crawler.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import com.example.fund_crawler.entity.Fund;
import com.example.fund_crawler.entity.FundPortfolio;
import com.example.fund_crawler.entity.FundReturn;
import com.example.fund_crawler.repository.FundAssetRepository;
import com.example.fund_crawler.repository.FundPortfolioRepository;
import com.example.fund_crawler.repository.FundRepository;
import com.example.fund_crawler.repository.FundReturnRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FundCrawlingService {

    private final FundRepository fundRepository;
    private final FundPortfolioRepository fundPortfolioRepository;
    private final FundReturnRepository fundReturnRepository;
    
    @Transactional
    public void run() {
        System.out.println("✅ 크롤러 실행 시작!");
        
        // Chrome 다운로드 설정
        String downloadFilepath = "C:\\Users\\junbi\\OneDrive\\문서\\fund_document";  // 원하는 경로

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadFilepath);          // ⬅ 다운로드 경로 지정
        prefs.put("safebrowsing.enabled", true);                            // ⬅ 안전 다운로드 설정 활성화
        prefs.put("profile.default_content_settings.popups", 0);           // ⬅ 팝업 차단 허용 설정
        prefs.put("plugins.always_open_pdf_externally", true);				// ⬅ PDF를 브라우저에서 열지 않고 다운로드

        System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");

        // Chrome 옵션 설정
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080", "--no-sandbox");
        options.addArguments("--disable-gpu", "--window-size=1920,1080", "--no-sandbox");
        // 화면 줌 
        options.addArguments("--force-device-scale-factor=1.0");  // 1.0배 축소
        // 자동화 배너 제거 시도
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        // 다운로드 설정
        options.setExperimentalOption("prefs", prefs);
        
        WebDriver driver = new ChromeDriver(options);
        System.out.println("✅ Chrome 브라우저 실행 완료");
        
        try {
            System.out.println("🌐 부산은행 펀드 페이지 접속 중...");
            String baseUrl = "https://www.busanbank.co.kr/ib20/mnu/FPMRTP323002001#none";
            driver.get(baseUrl);
            System.out.println("✅ 페이지 접속 완료");
            
            // 엔티티 담을 리스트들
            // List<Fund> fundItems = new ArrayList<>();			// Fund 엔티티 담을 리스트
            // List<FundDailyPrice> priceItems = new ArrayList<>();	// FundDailyPrice 엔티티 담을 리스트
            // List<FundPortfolio> fundAssetItems = new ArrayList<>();			// FundAsset 엔티티 담을 리스트
            
            FundDataExtractor extractor = new FundDataExtractor();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            System.out.println("🚀 펀드 정보 추출 시작...");
            // int totalProcessedCount = 0;
            
            // 3. 검색 버튼 클릭
            System.out.println("🔍 검색 버튼 클릭 중...");
            WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("doSearchBtn")));
            searchBtn.click();
            System.out.println("✅ 검색 버튼 클릭 완료");
            
            System.out.println("⏳ 페이지 로딩 대기 중...");
            waitLoading(driver);
            System.out.println("✅ 페이지 로딩 완료");
            
            System.out.println("📋 전체 펀드 목록 불러오는 중...");
            openAllFundList(driver);
            System.out.println("✅ 전체 펀드 목록 로드 완료");
            
            // 펀드 목록 요소 아이템
            List<WebElement> fundElements = driver.findElements(By.cssSelector("ul.prodResultList > li"));
            System.out.println("📊 총 " + fundElements.size() + "개의 펀드를 발견했습니다.");

            System.out.println("🚀 펀드 정보 추출 시작...");
            int processedCount = 0;
            
            
            for (WebElement item : fundElements) {
                processedCount++;
                System.out.println("\n--- 펀드 " + processedCount + "/" + fundElements.size() + " 처리 중 ---");
                
                try {
                	// 펀드 기본 데이터 추출
                    System.out.println("📈 펀드 기본 정보 추출 중...");
                    Fund fund = extractor.extractFundInfo(driver, wait, item);
                    System.out.println("✅ 펀드 정보 추출 완료: " + fund.getFundName());
                    
                    // 펀드 기본 데이터가 추출됐다면 펀드에 관련된 데이터 추출
                    if (fund != null) {
                        // 펀드 기본 데이터 DB 저장
                        System.out.println("📈 펀드 기본 정보 DB 저장");
                    	fundRepository.save(fund);

                    	
                    	// 펀드 수익률 데아터 추출
                    	System.out.println("📊 펀드 수익률 데이터 추출 중...");
                        FundReturn fundReturn = extractor.extractFundReturnInfo(driver, wait, fund, item);
                    	// 펀드 수익률 데이터 DB 저장
                        if (fundReturn != null) {
                            System.out.println("📊 펀드 수익률 데이터 DB 저장");
                            fundReturnRepository.save(fundReturn);
                            System.out.println("✅ 펀드 수익률 데이터 저장 완료");
                        } else {
                            System.err.println("⚠️ 펀드 수익률 데이터 추출 실패");
                        }
                        
                        
                    	// 펀드 포트폴리오 데이터 추출
                        System.out.println("📊 펀드 포트폴리오 데이터 추출 중...");
                        FundPortfolio fundPortfolio = extractor.extractFundPortfolioInfo(driver, wait, fund, item);
                    	// 펀드 포트폴리오 데이터 DB 저장
                        if (fundPortfolio != null) {
                            System.out.println("📊 펀드 포트폴리오 데이터 DB 저장");
                            fundPortfolioRepository.save(fundPortfolio);
                            System.out.println("✅ 펀드 포트폴리오 데이터 저장 완료");
                        } else {
                            System.err.println("⚠️ 펀드 포트폴리오 데이터 추출 실패");
                        }
                    } 
                    
                    
                } catch (Exception e) {
                    System.err.println("❌ 펀드 " + processedCount + " 처리 중 오류 발생: " + e.getMessage());
                    continue;
                }

                System.out.println("✅ 펀드 " + processedCount + "개 처리 완료");
            }
            
            System.out.println("\n🎉 모든 데이터 저장 완료!");
            
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("🔧 브라우저 종료 중...");
            driver.quit();
            System.out.println("✅ 크롤러 실행 종료");
        }
    }
    
    private void waitLoading(WebDriver driver) {
        System.out.println("⏳ 로딩 완료 대기 중...");
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading-image")));
    }

    private void openAllFundList(WebDriver driver) {
        int clickCount = 0;
        while (true) {
            try {
                WebElement moreBtn = driver.findElement(By.id("doSearchMoreBtn"));
                if (moreBtn.isDisplayed()) {
                    clickCount++;
                    System.out.println("📋 더보기 버튼 클릭 (" + clickCount + "번째)");
                    moreBtn.click();
                    waitLoading(driver);
                } else {
                    break;
                }
            } catch (Exception e) {
            	System.out.println("✅ 모든 펀드 목록 로드 완료 (더보기 버튼 없음)");
                break;
            }
        }
        if(clickCount > 0) {
            System.out.println("✅ 총 " + clickCount + "번의 더보기 클릭으로 전체 목록 로드 완료");
        }
    }
}

/*

@Service
@RequiredArgsConstructor
public class FundCrawlingService {

    private final FundRepository fundRepository;
    private final FundDailyPriceRepository fundDailyPriceRepository;
    
    @Transactional
    public void run() {
        System.out.println("✅ 크롤러 실행 시작!");

        // Chrome 브라우저 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080", "--no-sandbox");
        
        WebDriver driver = new ChromeDriver(options);
        System.out.println("✅ Chrome 브라우저 실행 완료");
        
        try {
            System.out.println("🌐 부산은행 펀드 페이지 접속 중...");
            String baseUrl = "https://www.busanbank.co.kr/ib20/mnu/FPMRTP323002001#none";
            driver.get(baseUrl);

            System.out.println("🔍 검색 버튼 찾는 중...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("doSearchBtn")));
            searchBtn.click();
            System.out.println("✅ 검색 버튼 클릭 완료");

            System.out.println("⏳ 페이지 로딩 대기 중...");
            waitLoading(driver);
            System.out.println("✅ 페이지 로딩 완료");
            
            // System.out.println("📋 전체 펀드 목록 불러오는 중...");
            // openAllFundList(driver);
            // System.out.println("✅ 전체 펀드 목록 로드 완료");
            
            // 펀드 목록 요소 아이템
            List<WebElement> fundElements = driver.findElements(By.cssSelector("ul.prodResultList > li"));
            System.out.println("📊 총 " + fundElements.size() + "개의 펀드를 발견했습니다.");
            
            // 엔티티 담을 리스트들
            List<Fund> fundItems = new ArrayList<>();			// Fund 엔티티 담을 리스트
            List<FundDailyPrice> priceItems = new ArrayList<>();	// FundDailyPrice 엔티티 담을 리스트
            
            FundDataExtractor extractor = new FundDataExtractor();

            System.out.println("🚀 펀드 정보 추출 시작...");
            int processedCount = 0;
            
            for (WebElement item : fundElements) {
                processedCount++;
                System.out.println("\n--- 펀드 " + processedCount + "/" + fundElements.size() + " 처리 중 ---");
                
                try {
                    System.out.println("📈 펀드 기본 정보 추출 중...");
                    Fund fund = extractor.extractFundInfo(driver, item);
                    
                    if (fund != null) {
                        fundItems.add(fund);
                        System.out.println("✅ 펀드 정보 추출 완료: " + fund.getFundName());

                        System.out.println("💰 펀드 일일 가격 정보 추출 중...");
                        List<FundDailyPrice> dailyPrices = extractor.extractDailyPrices(driver, item, fund);
                        priceItems.addAll(dailyPrices);
                        System.out.println("✅ 일일 가격 정보 " + dailyPrices.size() + "건 추출 완료");
                    } else {
                        System.err.println("❌ 펀드 " + processedCount + " 정보 추출 실패");
                    }
                } catch (Exception e) {
                    System.err.println("❌ 펀드 " + processedCount + " 처리 중 오류 발생: " + e.getMessage());
                    continue;
                }

                System.out.println("✅ 펀드 " + processedCount + " 처리 완료");
            }

            System.out.println("\n📊 데이터 추출 완료 통계:");
            System.out.println("- 총 펀드 수: " + fundItems.size());
            System.out.println("- 총 일일 가격 데이터 수: " + priceItems.size());
            
            // 추출 데이터 테이블 저장
            System.out.println("\n💾 데이터베이스 저장 시작...");
            System.out.println("📝 펀드 정보 저장 중...");
            fundRepository.saveAll(fundItems);
            System.out.println("✅ " + fundItems.size() + "개 펀드 정보 저장 완료");
            
            System.out.println("📝 펀드 일일 가격 정보 저장 중...");
            fundDailyPriceRepository.saveAll(priceItems);
            System.out.println("✅ " + priceItems.size() + "건 일일 가격 정보 저장 완료");
            
            System.out.println("\n🎉 모든 데이터 저장 완료!");
        } catch (Exception e) {
            System.err.println("❌ 크롤링 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("🔧 브라우저 종료 중...");
            driver.quit();
            System.out.println("✅ 크롤러 실행 종료");
        }
    }
    
    private void waitLoading(WebDriver driver) {
        System.out.println("⏳ 로딩 완료 대기 중...");
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading-image")));
    }

    private void openAllFundList(WebDriver driver) {
        int clickCount = 0;
        while (true) {
            try {
                WebElement moreBtn = driver.findElement(By.id("doSearchMoreBtn"));
                if (moreBtn.isDisplayed()) {
                    clickCount++;
                    System.out.println("📋 더보기 버튼 클릭 (" + clickCount + "번째)");
                    moreBtn.click();
                    waitLoading(driver);
                } else {
                    break;
                }
            } catch (Exception e) {
            	System.out.println("✅ 모든 펀드 목록 로드 완료 (더보기 버튼 없음)");
                break;
            }
        }
        System.out.println("✅ 총 " + clickCount + "번의 더보기 클릭으로 전체 목록 로드 완료");
    }
}

*/