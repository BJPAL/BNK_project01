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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fund.fund.dto.FundRegisterRequest;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.FundDocument;
import com.example.fund.fund.entity.FundPolicy;
import com.example.fund.fund.repository.FundDocumentRepository;
import com.example.fund.fund.repository.FundPolicyRepository;
import com.example.fund.fund.repository.FundRepository;
import com.example.fund.fund.service.FundService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/fund")
public class FundServiceController {

    // 파일 업로드

    private final String UPLOAD_DIR = "C:bnk_project/data/uploads/fund_document/";

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
            return ResponseEntity.ok("Uploaded and converted successfully.");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed: " + e.getMessage());
        }
    }

    @Autowired
    private FundRepository fundRepository;
    @Autowired
    private FundPolicyRepository fundPolicyRepository;
    @Autowired
    private FundDocumentRepository fundDocumentRepository;

    @PostMapping("/register")
        public ResponseEntity<String> registerFund(
                @RequestPart("data") FundRegisterRequest request,
                @RequestPart("file") MultipartFile file) {

            try {
                // 1. 기존 펀드 정보 가져오기
                Fund fund = fundRepository.findByFundId(request.getFundId())
                        .orElse(null);
                if (fund == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("존재하지 않는 펀드 ID");
                }

                // 2. 정책 저장
                FundPolicy policy = FundPolicy.builder()
                        .fund(fund)
                        .fundPayout(request.getFundPayout())
                        .fundTheme(request.getFundTheme())
                        .fundActive(request.getFundActive())
                        .fundRelease(request.getFundRelease())
                        .build();
                fundPolicyRepository.save(policy);

                // 3. 파일 저장 및 변환
                String filePath = savePdfAndConvert(file, "fund_doc", fund.getFundId());

                // 4. 문서 저장
                FundDocument doc = FundDocument.builder()
                        .fund(fund)
                        .docType(request.getDocType())
                        .docTitle(request.getDocTitle())
                        .filePath(filePath)
                        .fileFormat(request.getFileFormat())
                        .uploadedAt(LocalDate.now())
                        .build();
                fundDocumentRepository.save(doc);

                return ResponseEntity.ok("펀드 + 정책 + 문서 등록 완료");

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러: " + e.getMessage());
            }
        }
        
        private String savePdfAndConvert(MultipartFile file, String fileType, String filename) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Only PDF files are allowed.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IOException("Invalid file type.");
        }

        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        String storedFilename = filename + "_" + timestamp;
        Path dirPath = Paths.get(UPLOAD_DIR, fileType, dateFolder);
        Files.createDirectories(dirPath);

        Path pdfPath = dirPath.resolve(storedFilename + ".pdf");
        Files.copy(file.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);

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

        return pdfPath.toString();
    }


}
