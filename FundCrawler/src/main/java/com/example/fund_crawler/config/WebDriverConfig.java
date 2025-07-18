package com.example.fund_crawler.config;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDriverConfig {
	public WebDriver createDriver() {
        String downloadPath = "C:\\Users\\junbi\\OneDrive\\문서\\fund_document";

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadPath);
        prefs.put("safebrowsing.enabled", true);
        prefs.put("profile.default_content_settings.popups", 0);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--force-device-scale-factor=1.5");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("prefs", prefs);

        System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
        return new ChromeDriver(options);
    }
}
