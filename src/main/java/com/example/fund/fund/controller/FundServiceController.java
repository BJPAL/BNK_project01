package com.example.fund.fund.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fund.fund.entity.FileHistory;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.repository.FileHistoryRepository;
import com.example.fund.fund.service.FundService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/fund")
public class FundServiceController {

    @Autowired
    private FundService fundService;

    @Autowired
    private FileHistoryRepository fileHistoryRepository;
    
    // 펀드 CRUD

    @GetMapping
    public List<Fund> getAllFunds() {
        return fundService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fund> getFund(@PathVariable Long id) {
        return fundService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Fund> createFund(@RequestBody Fund fund) {
        return ResponseEntity.ok(fundService.save(fund));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fund> updateFund(@PathVariable Long id, @RequestBody Fund fund) {
        return ResponseEntity.ok(fundService.update(id, fund));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFund(@PathVariable Long id) {
        fundService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 파일 업로드

    private final String UPLOAD_DIR = "C:/uploads/";

    @PostMapping("/upload/manual")
    public ResponseEntity<String> uploadManual(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {
        return saveFileAndConvertToJpg(file, "manual", filename);
    }

    @PostMapping("/upload/prospectus")
    public ResponseEntity<String> uploadProspectus(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {
        return saveFileAndConvertToJpg(file, "prospectus", filename);
    }

    @PostMapping("/upload/terms")
    public ResponseEntity<String> uploadTerms(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {
        return saveFileAndConvertToJpg(file, "terms", filename);
    }

    private ResponseEntity<String> saveFileAndConvertToJpg(MultipartFile file, String fileType, String filename) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only PDF files are allowed.");
            }

            // MIME 타입 검사
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type.");
            }

            // 날짜 기반 경로 및 타임스탬프
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
            String storedFilename = filename + "_" + timestamp;
            Path dirPath = Paths.get(UPLOAD_DIR, fileType, dateFolder);
            Files.createDirectories(dirPath);

            // PDF 저장
            Path pdfPath = dirPath.resolve(storedFilename + ".pdf");
            Files.copy(file.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);

            // PDF → JPG
            try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();

                for (int i = 0; i < pageCount; i++) {
                    BufferedImage bim = pdfRenderer.renderImage(i);
                    String jpgName = String.format("%s_%d.jpg", storedFilename, i + 1);
                    Path jpgPath = dirPath.resolve(jpgName);
                    ImageIO.write(bim, "jpg", jpgPath.toFile());
                }
            }

            // DB 기록
            FileHistory history = FileHistory.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .fileType(fileType)
                    .filePath(dirPath.toString())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            fileHistoryRepository.save(history);

            return ResponseEntity.ok("Uploaded and converted successfully.");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<FileHistory>> getFileHistories(@RequestParam(required = false) String type) {
        List<FileHistory> histories;

        if (type != null && !type.isBlank()) {
            histories = fileHistoryRepository.findByFileType(type);
        } else {
            histories = fileHistoryRepository.findAll();
        }

        return ResponseEntity.ok(histories);
    }

}
