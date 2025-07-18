package com.example.fund_crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FundCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundCrawlerApplication.class, args);
	}
}
