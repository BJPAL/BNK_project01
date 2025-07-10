package com.example.fund.fund.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/fund")
public class FundServiceController {

    private final String UPLOAD_DIR = "C:/uploads/"; // 예시 경로, 실제로는 application.yml

    @PostMapping("/upload/manual") // 상품설명서
    public ResponseEntity<String> uploadManual(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {
        return saveFileAndConvertToJpg(file, "manual/", filename);
    }

    @PostMapping("/upload/prospectus") // 투자설명서
    public ResponseEntity<String> uploadProspectus(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {
        return saveFileAndConvertToJpg(file, "prospectus/", filename);
    }

    @PostMapping("/upload/terms") // 약관
    public ResponseEntity<String> uploadTerms(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {
        return saveFileAndConvertToJpg(file, "terms/", filename);
    }

    private ResponseEntity<String> saveFileAndConvertToJpg(MultipartFile file, String subDir, String filename) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Only PDF files are allowed.");
            }

            // MIME 타입 검사
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid file type. Only application/pdf allowed.");
            }

            // 1. 저장 경로 생성
            Path dirPath = Paths.get(UPLOAD_DIR + subDir);
            Files.createDirectories(dirPath);

            // 2. PDF 저장
            Path pdfPath = dirPath.resolve(filename + ".pdf");
            Files.copy(file.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);

            // 3. PDF → JPG (모든 페이지)
            try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();

                for (int i = 0; i < pageCount; i++) {
                    BufferedImage bim = pdfRenderer.renderImage(i); // 기본 DPI (72)
                    String jpgFileName = String.format("%s_%d.jpg", filename, i + 1);
                    Path jpgPath = dirPath.resolve(jpgFileName);
                    ImageIO.write(bim, "jpg", jpgPath.toFile());
                }
            }

            return ResponseEntity.ok("PDF uploaded and converted to JPG.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload/convert failed: " + e.getMessage());
        }
    }


}
