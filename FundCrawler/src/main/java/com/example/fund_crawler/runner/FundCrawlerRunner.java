package com.example.fund_crawler.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.fund_crawler.service.FundCrawlingService;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class FundCrawlerRunner implements ApplicationRunner {

    private final FundCrawlingService fundCrawlingService;

    @Override
    public void run(ApplicationArguments args) {
    	// 크롤링 시작
        fundCrawlingService.run(); 
    }
}