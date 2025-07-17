package com.example.fund.fund.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fund.fund.dto.FundDetailResponse;
import com.example.fund.fund.dto.FundRegisterRequest;
import com.example.fund.fund.dto.FundResponseDTO;
import com.example.fund.fund.dto.FundReturnDTO;
import com.example.fund.fund.entity.Fund;
import com.example.fund.fund.entity.FundAsset;
import com.example.fund.fund.entity.FundDailyPrice;
import com.example.fund.fund.entity.FundDocument;
import com.example.fund.fund.entity.FundPolicy;
import com.example.fund.fund.repository.FundAssetRepository;
import com.example.fund.fund.repository.FundDailyPriceRepository;
import com.example.fund.fund.repository.FundDocumentRepository;
import com.example.fund.fund.repository.FundPolicyRepository;
import com.example.fund.fund.repository.FundRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundService {

    private final FundRepository fundRepository;
    private final FundAssetRepository fundAssetRepository;
    private final FundDailyPriceRepository fundDailyPriceRepository;
    private final FundDailyPriceService fundDailyPriceService;

    /** 투자 성향에 따른 펀드 목록 조회 - pagination */
    public Page<FundResponseDTO> findByInvestType(Integer investType, Pageable pageable) {
        // 투자 성향 → 위험 등급 범위 계산
        int startRiskLevel;
        String investTypeName = "";
        int endRiskLevel = 6;

        switch (investType) {
            case 1 -> startRiskLevel = 6; // 안정형
            case 2 -> startRiskLevel = 5; // 안정 추구형
            case 3 -> startRiskLevel = 4; // 위험 중립형
            case 4 -> startRiskLevel = 3; // 적극 투자형
            case 5 -> startRiskLevel = 1; // 공격 투자형
            default -> throw new IllegalArgumentException("올바르지 않은 투자 성향입니다.");
        }

        Page<Fund> fundPage = fundRepository.findByRiskLevelBetween(startRiskLevel, endRiskLevel, pageable);

        // ✅ fundPage → fundResponsePage 변환
        Page<FundResponseDTO> fundResponsePage = fundPage.map(fund -> {
            FundReturnDTO fundReturn = fundDailyPriceService.getFundReturns(fund.getFundId());
            Optional<FundDailyPrice> latestDailyPrice = fundDailyPriceService.getLatestDailyPrice(fund.getFundId());

            return FundResponseDTO.builder()
                    .fundId(fund.getFundId())
                    .fundName(fund.getFundName())
                    .fundType(fund.getFundType())
                    .investmentRegion(fund.getInvestmentRegion())
                    .managementCompany(fund.getManagementCompany())
                    .riskLevel(fund.getRiskLevel())
                    .aum(latestDailyPrice.map(FundDailyPrice::getAum).orElse(null))
                    .basePrice(latestDailyPrice.map(FundDailyPrice::getBasePrice).orElse(null))
                    .fundScale(latestDailyPrice.map(FundDailyPrice::getFundScale).orElse(null))
                    .establishDate(fund.getEstablishDate())
                    .totalExpenseRatio(fund.getTotalExpenseRatio())
                    .month1Return(fundReturn != null ? fundReturn.getMonth1Return() : null)
                    .month3Return(fundReturn != null ? fundReturn.getMonth3Return() : null)
                    .month6Return(fundReturn != null ? fundReturn.getMonth6Return() : null)
                    .month12Return(fundReturn != null ? fundReturn.getMonth12Return() : null)
                    .build();
        });

        return fundResponsePage;
    }

    /**
     * 펀드 상세 정보 조회 (간단 버전)
     */
    public FundDetailResponse getFundDetail(Long fundId) {
        // 1. 펀드 기본 정보 조회
        Fund fund = fundRepository.findByFundId(fundId)
                .orElseThrow(() -> new RuntimeException("펀드를 찾을 수 없습니다."));

        // 2. 펀드 자산 구성 조회 (1:1 관계)
        FundAsset asset = fundAssetRepository.findByFund_FundId(fundId)
                .orElse(null);

        // 3. 응답 DTO 생성
        return FundDetailResponse.builder()
                // Fund 정보
                .fundId(fund.getFundId())
                .fundName(fund.getFundName())
                .fundType(fund.getFundType())
                .classType(fund.getClassType())
                .investmentRegion(fund.getInvestmentRegion())
                .establishDate(fund.getEstablishDate())
                .managementCompany(fund.getManagementCompany())
                .baseCurrency(fund.getBaseCurrency())
                .riskLevel(fund.getRiskLevel())
                .fundStatus(fund.getFundStatus())
                .totalExpenseRatio(fund.getTotalExpenseRatio())
                .domesticStock(asset != null ? asset.getDomesticStock() : BigDecimal.ZERO)
                .overseasStock(asset != null ? asset.getOverseasStock() : BigDecimal.ZERO)
                .domesticBond(asset != null ? asset.getDomesticBond() : BigDecimal.ZERO)
                .overseasBond(asset != null ? asset.getOverseasBond() : BigDecimal.ZERO)
                .fundInvestment(asset != null ? asset.getFundInvestment() : BigDecimal.ZERO)
                .liquidity(asset != null ? asset.getLiquidity() : BigDecimal.ZERO)
                .build();
    }

    // ========

    /** 모든 펀드 목록 조회 */
    public List<Fund> findAll() {
        return fundRepository.findAll();
    }

    /** 특정 펀드 ID 조회 */
    public Optional<Fund> findById(Long id) {
        return fundRepository.findById(id);
    }

    /** 펀드 등록 */
    public Fund save(Fund fund) {
        return fundRepository.save(fund);
    }

    /** 펀드 정보 수정 */
    public Fund update(Long id, Fund updatedFund) {
        return fundRepository.findById(id)
            .map(fund -> {
                fund.setFundName(updatedFund.getFundName());
                fund.setFundType(updatedFund.getFundType());
                fund.setClassType(updatedFund.getClassType());
                fund.setInvestmentRegion(updatedFund.getInvestmentRegion());
                fund.setEstablishDate(updatedFund.getEstablishDate());
                fund.setRiskLevel(updatedFund.getRiskLevel());
                fund.setFundStatus(updatedFund.getFundStatus());
                fund.setManagementCompany(updatedFund.getManagementCompany());
                fund.setBaseCurrency(updatedFund.getBaseCurrency());
                return fundRepository.save(fund);
            }).orElseThrow(() -> new RuntimeException("Fund not found"));
    }

    /** 특정 펀드 삭제  */
    public void delete(Long id) {
        fundRepository.deleteById(id);
    }

    /*PDF 저장 & JPG 변환*/
    private final FundPolicyRepository fundPolicyRepository;
    private final FundDocumentRepository fundDocumentRepository;

    private final String UPLOAD_DIR = "C:\\bnk_project\\data\\uploads\\fund_document\\";

    @Transactional
    public void registerFundWithAllDocuments(FundRegisterRequest request,
                                            MultipartFile fileTerms,
                                            MultipartFile fileManual,
                                            MultipartFile fileProspectus) throws IOException {
        Fund fund = fundRepository.findByFundId(request.getFundId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 펀드 ID"));

        // 정책 저장 (한 번만)
        FundPolicy policy = FundPolicy.builder()
                .fund(fund)
                .fundPayout(request.getFundPayout())
                .fundTheme(request.getFundTheme())
                .fundActive(request.getFundActive())
                .fundRelease(request.getFundRelease())
                .build();
        fundPolicyRepository.save(policy);

        // 문서 저장
        saveFundDocument(fund, fileTerms, "약관");
        saveFundDocument(fund, fileManual, "상품설명서");
        saveFundDocument(fund, fileProspectus, "투자설명서");
    }

    private void saveFundDocument(Fund fund, MultipartFile file, String docType) throws IOException {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String generatedDocTitle = fund.getFundName() + "_" + docType + "_" + today;
        String filePath = savePdfAndConvertToJpg(file, "fund_doc", generatedDocTitle);

        FundDocument doc = FundDocument.builder()
                .fund(fund)
                .docType(docType)
                .docTitle(generatedDocTitle)
                .filePath(filePath)
                .fileFormat("PDF")
                .uploadedAt(LocalDate.now())
                .build();

        fundDocumentRepository.save(doc);
    }


    private String savePdfAndConvertToJpg(MultipartFile file, String fileType, String filename) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Only PDF files are allowed.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IOException("Invalid file type.");
        }

        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String storedFilename = filename;

        Path dirPath = Paths.get(UPLOAD_DIR, fileType, dateFolder);
        Files.createDirectories(dirPath);

        Path pdfPath = dirPath.resolve(storedFilename + ".pdf");
        Files.copy(file.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);

        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 125);
                String jpgName = String.format("%s_%d.jpg", storedFilename, i + 1);
                Path jpgPath = dirPath.resolve(jpgName);
                ImageIO.write(image, "jpg", jpgPath.toFile());
            }
        }

        return pdfPath.toString(); // DB에는 PDF 경로 저장
    }

    public List<Fund> getAllFunds() {
        return fundRepository.findAll(); 
    }



}
