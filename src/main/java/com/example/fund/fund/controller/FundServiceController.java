package com.example.fund.fund.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/fund")
public class FundServiceController {










    // file upload logic

    @PostMapping("/upload/manual")      // 상품설명서
    public ResponseEntity<String> manual(@RequestParam("file") MultipartFile file) {
        return saveFile(file, "manual/");
    }

    @PostMapping("/upload/prospectus")  // 투자설명서   
    public ResponseEntity<String> prospectus(@RequestParam("file") MultipartFile file) {
        return saveFile(file, "prospectus/");
    }

    @PostMapping("/upload/terms")       // 약관   
    public ResponseEntity<String> terms(@RequestParam("file") MultipartFile file) {
        return saveFile(file, "terms/");
    }

    // 파일 업로드를 DB에 저장하는 로직은 생략합니다.
    // 실제로는 파일 경로를 DB에 저장하거나, 파일 메타데이터 를 DB에 저장하는 방식으로 구현할 수 있습니다.
    // 파일 업로드 경로는 실제 환경에 맞게 설정해야 합니다.
    // 예를 들어, 로컬 파일 시스템에 저장하거나, 클라우드 스토 rage(예: AWS S3)에 저장할 수 있습니다.
    private final String UPLOAD_DIR = ""; // 여기에 파일 업로드 경로를 지정하세요. 예: "c:/uploads/"

    private ResponseEntity<String> saveFile(MultipartFile file, String subDir) {
        try {
            Path dirPath = Paths.get(UPLOAD_DIR + subDir);
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("Uploaded to: " + subDir + file.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

  
}
