package com.mmva.newsapp.infrastructure.monetization.ads.local.service;

import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdClickRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeUploadRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdImpressionRecordRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.mapper.AdCreativeMapper;
import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdCreative;
import com.mmva.newsapp.infrastructure.monetization.ads.local.repository.AdCreativeRepository;
import com.mmva.newsapp.infrastructure.monetization.ads.local.mapper.AdPlacementMapper;
import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdClick;
import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdImpression;
import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdPlacement;
import com.mmva.newsapp.infrastructure.monetization.ads.local.repository.AdClickRepository;
import com.mmva.newsapp.infrastructure.monetization.ads.local.repository.AdImpressionRepository;
import com.mmva.newsapp.infrastructure.monetization.ads.local.repository.AdPlacementRepository;
import com.mmva.newsapp.domain.news.service.media.NewsImageProcessingService;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdCreativeType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.monetization.campaign.repository.SponsorshipCampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// Java imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Implementation of {@link AdsService}.
 * 
 * <p>
 * Per PROJECT_PRINCIPLES.md section 6.3, this implementation resides
 * in the same package as its interface. The impl/ subfolder pattern
 * is not used in this project for consistency.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdsServiceImpl implements AdsService {

    private final AdPlacementRepository placementRepository;
    private final AdImpressionRepository impressionRepository;
    private final AdClickRepository clickRepository;
    private final AdCreativeRepository creativeRepository;
    private final SponsorshipCampaignRepository campaignRepository;
    private final AdPlacementMapper placementMapper;
    private final AdCreativeMapper creativeMapper;
    private final AdsFileStorageService fileStorageService;
    private final NewsImageProcessingService newsImageProcessingService;
    // private final ThumbnailService thumbnailService; // Deprecated - consolidated
    // into NewsImageProcessingService
    private final AdsUrlService adsUrlService;

    /**
     * Maximum impressions from same IP within 1 hour before flagging as suspicious.
     */
    @Value("${monetization.fraud.max-impressions-per-ip:50}")
    private int maxImpressionsPerIp;

    /**
     * Maximum clicks from same IP within 1 hour before flagging as suspicious.
     */
    @Value("${monetization.fraud.max-clicks-per-ip:10}")
    private int maxClicksPerIp;

    /**
     * Minimum time between clicks from same user (in seconds).
     */
    @Value("${monetization.fraud.min-click-interval-seconds:5}")
    private int minClickIntervalSeconds;

    @Value("${media.entities.ads.backup:${media.root-path}/archive/ads/backup}")
    private String backupFolderPath;

    // ========================================
    // Constants for File Operations
    // ========================================

    private static final int MAX_BACKUP_RETRIES = 3;
    private static final long BACKUP_RETRY_DELAY_MS = 1000; // 1 second base delay
    private static final String BACKUP_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    // ========================================
    // Configuration Validation & Initialization
    // ========================================

    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating AdsService configuration...");

        // Validate required paths
        if (backupFolderPath == null || backupFolderPath.trim().isEmpty()) {
            throw new IllegalStateException("app.ads.backup-path must be configured");
        }

        // Validate fraud detection thresholds
        if (maxImpressionsPerIp <= 0) {
            throw new IllegalStateException("monetization.fraud.max-impressions-per-ip must be positive");
        }
        if (maxClicksPerIp <= 0) {
            throw new IllegalStateException("monetization.fraud.max-clicks-per-ip must be positive");
        }
        if (minClickIntervalSeconds < 0) {
            throw new IllegalStateException("monetization.fraud.min-click-interval-seconds must be non-negative");
        }

        // Validate backup directory exists or can be created
        try {
            Path backupPath = Path.of(backupFolderPath);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                log.info("Created backup directory: {}", backupFolderPath);
            } else if (!Files.isDirectory(backupPath)) {
                throw new IllegalStateException("Backup path exists but is not a directory: " + backupFolderPath);
            } else if (!Files.isWritable(backupPath)) {
                throw new IllegalStateException("Backup directory is not writable: " + backupFolderPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create/access backup directory: " + backupFolderPath, e);
        }

        log.info("AdsService configuration validation completed successfully");
    }

    // ========================================
    // Placement CRUD
    // ========================================

    @Override
    @Transactional
    public AdPlacementResponseDto createPlacement(AdPlacementRequestDto request, String tenantId) {
        log.info("Creating ad placement: {} for tenant: {}", request.getAdPlacementCode(), tenantId);

        // Check for duplicate code
        if (placementRepository.existsByAdPlacementCodeAndAdPlacementTenantId(request.getAdPlacementCode(), tenantId)) {
            throw new InvalidRequestException("Placement code already exists: " + request.getAdPlacementCode());
        }

        AdPlacement placement = placementMapper.toEntity(request);
        placement.setAdPlacementTenantId(tenantId);

        // Set defaults if not provided
        if (placement.getAdPlacementIsActive() == null) {
            placement.setAdPlacementIsActive(true);
        }
        if (placement.getAdPlacementIsResponsive() == null) {
            placement.setAdPlacementIsResponsive(true);
        }

        AdPlacement saved = placementRepository.save(placement);
        log.info("Created placement with ID: {}", saved.getAdPlacementId());

        return placementMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AdPlacementResponseDto getPlacementById(UUID placementId) {
        AdPlacement placement = findPlacementOrThrow(placementId);
        return placementMapper.toResponseDto(placement);
    }

    @Override
    @Transactional(readOnly = true)
    public AdPlacementResponseDto getPlacementByCode(String placementCode, String tenantId) {
        AdPlacement placement = placementRepository.findByAdPlacementCodeAndAdPlacementTenantId(placementCode, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Placement not found with code: " + placementCode));
        return placementMapper.toResponseDto(placement);
    }

    @Override
    @Transactional
    public AdPlacementResponseDto updatePlacement(UUID placementId, AdPlacementRequestDto request) {
        log.info("Updating placement: {}", placementId);

        AdPlacement placement = findPlacementOrThrow(placementId);

        // Check code uniqueness if changing
        if (request.getAdPlacementCode() != null &&
                !request.getAdPlacementCode().equals(placement.getAdPlacementCode()) &&
                placementRepository.existsByAdPlacementCodeAndAdPlacementTenantId(
                        request.getAdPlacementCode(), placement.getAdPlacementTenantId())) {
            throw new InvalidRequestException("Placement code already exists: " + request.getAdPlacementCode());
        }

        placementMapper.updateEntityFromDto(request, placement);
        AdPlacement saved = placementRepository.save(placement);
        log.info("Updated placement: {}", placementId);

        return placementMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deletePlacement(UUID placementId) {
        log.info("Deleting placement: {}", placementId);

        AdPlacement placement = findPlacementOrThrow(placementId);
        placement.setDeletedAt(Instant.now());
        placementRepository.save(placement);

        log.info("Soft deleted placement: {}", placementId);
    }

    // ========================================
    // Placement Queries
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public Page<AdPlacementResponseDto> getAllPlacements(String tenantId, Pageable pageable) {
        return placementRepository.findAllByTenantId(tenantId, pageable)
                .map(placementMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdPlacementResponseDto> getActivePlacements(String tenantId) {
        return placementMapper.toResponseDtoList(
                placementRepository.findActiveByTenantId(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdPlacementResponseDto> getPlacementsByPage(String pageType, String tenantId) {
        return placementMapper.toResponseDtoList(
                placementRepository.findActiveByPageType(pageType, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdPlacementResponseDto> getPlacementsByPosition(PlacementPosition position, String tenantId) {
        return placementMapper.toResponseDtoList(
                placementRepository.findActiveByPosition(position, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdPlacementResponseDto> getPlacementsByAdType(AdType adType, String tenantId) {
        return placementMapper.toResponseDtoList(
                placementRepository.findActiveByAdType(adType, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdPlacementResponseDto> getPremiumPlacements(String tenantId) {
        return placementMapper.toResponseDtoList(
                placementRepository.findPremiumPlacements(tenantId));
    }

    // ========================================
    // Placement Status
    // ========================================

    @Override
    @Transactional
    public AdPlacementResponseDto activatePlacement(UUID placementId) {
        log.info("Activating placement: {}", placementId);

        AdPlacement placement = findPlacementOrThrow(placementId);
        placement.setAdPlacementIsActive(true);
        AdPlacement saved = placementRepository.save(placement);

        log.info("Activated placement: {}", placementId);
        return placementMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public AdPlacementResponseDto deactivatePlacement(UUID placementId) {
        log.info("Deactivating placement: {}", placementId);

        AdPlacement placement = findPlacementOrThrow(placementId);
        placement.setAdPlacementIsActive(false);
        AdPlacement saved = placementRepository.save(placement);

        log.info("Deactivated placement: {}", placementId);
        return placementMapper.toResponseDto(saved);
    }

    // ========================================
    // Impression Tracking
    // ========================================

    @Override
    @Transactional
    public UUID recordImpression(AdImpressionRecordRequestDto request, String tenantId,
            String ipHash, String userAgent) {
        log.debug("Recording impression for campaign: {}, placement: {}",
                request.getAdImpressionCampaignId(), request.getAdImpressionPlacementId());

        // Check if suspicious
        boolean suspicious = isSuspiciousImpression(request.getAdImpressionCampaignId(), ipHash);

        AdImpression impression = AdImpression.builder()
                .adImpressionCampaignId(request.getAdImpressionCampaignId())
                .adImpressionPlacementId(request.getAdImpressionPlacementId())
                .adImpressionUserId(request.getAdImpressionUserId())
                .adImpressionContentId(request.getAdImpressionContentId())
                .adImpressionTenantId(tenantId)
                .adImpressionRecordedAt(Instant.now())
                .adImpressionIsViewable(
                        request.getAdImpressionIsViewable() != null ? request.getAdImpressionIsViewable() : true)
                .adImpressionViewDurationMs(request.getAdImpressionViewDurationMs())
                .adImpressionVisibilityPercent(request.getAdImpressionVisibilityPercent())
                .adImpressionPageUrl(request.getAdImpressionPageUrl())
                .adImpressionPageTitle(request.getAdImpressionPageTitle())
                .adImpressionContentCategory(request.getAdImpressionContentCategory())
                .adImpressionDeviceType(request.getAdImpressionDeviceType())
                .adImpressionBrowser(request.getAdImpressionBrowser())
                .adImpressionOs(request.getAdImpressionOs())
                .adImpressionScreenResolution(request.getAdImpressionScreenResolution())
                .adImpressionSessionId(request.getAdImpressionSessionId())
                .adImpressionIpHash(ipHash)
                .adImpressionUserAgent(userAgent)
                .adImpressionIsSuspicious(suspicious)
                .adImpressionSuspicionReason(suspicious ? "High frequency from same IP" : null)
                .adImpressionIsValidated(false)
                .build();

        AdImpression saved = impressionRepository.save(impression);

        // Update placement stats
        placementRepository.recordImpression(request.getAdImpressionPlacementId(), Instant.now());

        // Update campaign stats
        campaignRepository.incrementImpressions(request.getAdImpressionCampaignId(), Instant.now());

        log.debug("Recorded impression: {}", saved.getAdImpressionId());
        return saved.getAdImpressionId();
    }

    @Override
    @Transactional(readOnly = true)
    public long getImpressionCount(UUID campaignId, Instant startDate, Instant endDate) {
        return impressionRepository.countByCampaignIdAndDateRange(campaignId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPlacementImpressionCount(UUID placementId, Instant startDate, Instant endDate) {
        return impressionRepository.countByPlacementIdAndDateRange(placementId, startDate, endDate);
    }

    // ========================================
    // Click Tracking
    // ========================================

    @Override
    @Transactional
    public UUID recordClick(AdClickRecordRequestDto request, String tenantId,
            String ipHash, String userAgent) {
        log.debug("Recording click for campaign: {}, placement: {}",
                request.getAdClickCampaignId(), request.getAdClickPlacementId());

        // Check if suspicious
        boolean suspicious = isSuspiciousClick(request.getAdClickCampaignId(), request.getAdClickUserId(), ipHash);

        // Check for duplicates
        boolean isDuplicate = false;
        if (request.getAdClickUserId() != null) {
            Instant since = Instant.now().minus(minClickIntervalSeconds, ChronoUnit.SECONDS);
            List<AdClick> recentClicks = clickRepository.findPotentialDuplicates(
                    request.getAdClickCampaignId(), request.getAdClickUserId(), since);
            isDuplicate = !recentClicks.isEmpty();
        }

        AdClick click = AdClick.builder()
                .adClickCampaignId(request.getAdClickCampaignId())
                .adClickPlacementId(request.getAdClickPlacementId())
                .adClickImpressionId(request.getAdClickImpressionId())
                .adClickUserId(request.getAdClickUserId())
                .adClickContentId(request.getAdClickContentId())
                .adClickTenantId(tenantId)
                .adClickRecordedAt(Instant.now())
                .adClickDestinationUrl(request.getAdClickDestinationUrl())
                .adClickType(request.getAdClickType() != null ? request.getAdClickType() : "PRIMARY")
                .adClickClickedElement(request.getAdClickClickedElement())
                .adClickX(request.getAdClickX())
                .adClickY(request.getAdClickY())
                .adClickSourcePageUrl(request.getAdClickSourcePageUrl())
                .adClickReferrerUrl(request.getAdClickReferrerUrl())
                .adClickDeviceType(request.getAdClickDeviceType())
                .adClickBrowser(request.getAdClickBrowser())
                .adClickOs(request.getAdClickOs())
                .adClickSessionId(request.getAdClickSessionId())
                .adClickIpHash(ipHash)
                .adClickUserAgent(userAgent)
                .adClickUtmSource(request.getAdClickUtmSource())
                .adClickUtmMedium(request.getAdClickUtmMedium())
                .adClickUtmCampaign(request.getAdClickUtmCampaign())
                .adClickUtmContent(request.getAdClickUtmContent())
                .adClickUtmTerm(request.getAdClickUtmTerm())
                .adClickTimeSinceImpressionMs(request.getAdClickTimeSinceImpressionMs())
                .adClickIsSuspicious(suspicious)
                .adClickSuspicionReason(suspicious ? "High frequency from same IP/user" : null)
                .adClickIsDuplicate(isDuplicate)
                .adClickIsValidated(false)
                .adClickConverted(false)
                .build();

        AdClick saved = clickRepository.save(click);

        // Update placement stats
        placementRepository.recordClick(request.getAdClickPlacementId(), Instant.now());

        // Update campaign stats
        campaignRepository.incrementClicks(request.getAdClickCampaignId(), Instant.now());

        log.debug("Recorded click: {}", saved.getAdClickId());
        return saved.getAdClickId();
    }

    @Override
    @Transactional(readOnly = true)
    public long getClickCount(UUID campaignId, Instant startDate, Instant endDate) {
        return clickRepository.countByCampaignIdAndDateRange(campaignId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBillableClickCount(UUID campaignId) {
        return clickRepository.countBillableByCampaignId(campaignId);
    }

    @Override
    @Transactional
    public void markClickAsConverted(UUID clickId, UUID conversionId, Long conversionTimeSeconds) {
        clickRepository.markAsConverted(clickId, conversionId, conversionTimeSeconds);
        log.info("Marked click {} as converted", clickId);
    }

    // ========================================
    // Analytics
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public double getCampaignCtr(UUID campaignId) {
        long impressions = impressionRepository.countByAdImpressionCampaignId(campaignId);
        if (impressions == 0) {
            return 0.0;
        }
        long clicks = clickRepository.countByAdClickCampaignId(campaignId);
        return BigDecimal.valueOf(clicks)
                .divide(BigDecimal.valueOf(impressions), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    @Override
    @Transactional(readOnly = true)
    public double getPlacementCtr(UUID placementId) {
        long impressions = impressionRepository.countByAdImpressionPlacementId(placementId);
        if (impressions == 0) {
            return 0.0;
        }
        long clicks = clickRepository.countByAdClickPlacementId(placementId);
        return BigDecimal.valueOf(clicks)
                .divide(BigDecimal.valueOf(impressions), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDailyImpressionCounts(String tenantId, Instant startDate, Instant endDate) {
        return impressionRepository.getDailyImpressionCounts(tenantId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getDailyClickCounts(String tenantId, Instant startDate, Instant endDate) {
        return clickRepository.getDailyClickCounts(tenantId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getImpressionsByDeviceType(String tenantId, Instant startDate, Instant endDate) {
        return impressionRepository.countByDeviceType(tenantId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getClicksByDeviceType(String tenantId, Instant startDate, Instant endDate) {
        return clickRepository.countByDeviceType(tenantId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getImpressionsByCountry(String tenantId, Instant startDate, Instant endDate) {
        return impressionRepository.countByCountry(tenantId, startDate, endDate);
    }

    // ========================================
    // Fraud Detection
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public boolean isSuspiciousImpression(UUID campaignId, String ipHash) {
        if (ipHash == null) {
            return false;
        }
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);
        long count = impressionRepository.countByIpHashWithinWindow(campaignId, ipHash, since);
        return count >= maxImpressionsPerIp;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSuspiciousClick(UUID campaignId, UUID userId, String ipHash) {
        if (ipHash == null) {
            return false;
        }
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);
        long ipCount = clickRepository.countByIpHashWithinWindow(campaignId, ipHash, since);
        return ipCount >= maxClicksPerIp;
    }

    @Override
    @Transactional(readOnly = true)
    public long getSuspiciousImpressionCount(String tenantId, Instant startDate, Instant endDate) {
        return impressionRepository.countSuspicious(tenantId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long getSuspiciousClickCount(String tenantId, Instant startDate, Instant endDate) {
        return clickRepository.countSuspicious(tenantId, startDate, endDate);
    }

    // ========================================
    // Creative Management
    // ========================================

    @Override
    @Transactional
    public AdCreativeResponseDto createCreative(AdCreativeUploadRequestDto request, String tenantId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("createCreative");

        // Declare variables for cleanup in catch block
        String filename = null;
        String thumbnailFilename = null;
        String filePath = null;
        String fullFilePath = null;
        CreativeFileMetadata metadata = null;

        try {
            log.info("Creating creative: {} for tenant: {}", request.getAdCreativeCode(), tenantId);

            // Normalize creative code to uppercase for consistency
            request.setAdCreativeCode(request.getAdCreativeCode().toUpperCase());

            // Check for duplicate code
            if (creativeRepository.existsByAdCreativeCodeAndAdCreativeTenantId(request.getAdCreativeCode(), tenantId)) {
                throw new InvalidRequestException("Creative code already exists: " + request.getAdCreativeCode());
            }

            // Process file upload if file is provided
            if (request.getFile() != null && !request.getFile().isEmpty()) {
                stopWatch.stop(); // Stop the main createCreative task
                stopWatch.start("fileProcessing");

                // Validate file
                validateCreativeFile(request.getFile(), request.getAdCreativeType());

                // Extract file metadata BEFORE storing the file (to avoid consuming the input
                // stream)
                metadata = extractFileMetadata(request.getFile());

                // Process file upload using file storage service
                filePath = fileStorageService.storeCreativeFile(request.getFile(), request.getAdCreativeCode(),
                        tenantId);

                // Get full file path for thumbnail generation
                fullFilePath = fileStorageService.getCreativeFilePath(filePath);

                // Extract filename from the stored path
                filename = adsUrlService.extractFilename(filePath);

                // Generate thumbnail for image/video creatives
                if (newsImageProcessingService.isVideo(request.getFile().getContentType())) {
                    // For videos, generate a thumbnail frame
                    try {
                        // Create MultipartFile from saved path for processing
                        Path mediaPath = Paths.get(fullFilePath);
                        String contentType = Files.probeContentType(mediaPath);
                        MultipartFile tempFile = createMultipartFileFromPath(mediaPath, request.getAdCreativeCode(),
                                contentType);

                        var imageResponse = newsImageProcessingService.processVideo(tempFile,
                                request.getAdCreativeCode());
                        thumbnailFilename = imageResponse.getThumbnail().getFilename();
                        log.debug("Generated thumbnail for video creative: {}", thumbnailFilename);
                    } catch (Exception e) {
                        log.warn("Failed to generate thumbnail for video creative: {}, continuing without thumbnail",
                                request.getAdCreativeCode(), e);
                    }
                } else if (newsImageProcessingService.isImage(request.getFile().getContentType())) {
                    // For images, use the image itself as thumbnail (no separate thumbnail needed)
                    thumbnailFilename = filename;
                    log.debug("Using image file as thumbnail for creative: {}", thumbnailFilename);
                }

                stopWatch.stop();
                log.debug("File processing completed in {} ms", stopWatch.getLastTaskTimeMillis());
                stopWatch.start("createCreative"); // Resume the main task
            }

            stopWatch.stop(); // Stop createCreative before starting entityCreation
            stopWatch.start("entityCreation");

            // Create entity
            AdCreative creative = AdCreative.builder()
                    .adCreativeCode(request.getAdCreativeCode())
                    .adCreativeName(request.getAdCreativeName())
                    .adCreativeDescription(request.getAdCreativeDescription())
                    .adCreativeType(request.getAdCreativeType() != null ? request.getAdCreativeType()
                            : (metadata != null ? metadata.detectedType : null))
                    .adCreativeFileName(filename) // Store filename only
                    .adCreativeThumbnailFilename(thumbnailFilename) // Store filename only
                    .adCreativeAltText(request.getAdCreativeAltText())
                    .adCreativeClickUrl(request.getAdCreativeClickUrl())
                    .adCreativeWidth(metadata != null ? metadata.width : null)
                    .adCreativeHeight(metadata != null ? metadata.height : null)
                    .adCreativeFileSizeBytes(metadata != null ? metadata.fileSize : null)
                    .adCreativeMimeType(metadata != null ? metadata.mimeType : null)
                    .adCreativeDurationSeconds(metadata != null ? metadata.durationSeconds : null)
                    .adCreativeRequiresApproval(request.getAdCreativeRequiresApproval())
                    .adCreativeMetadataJson(request.getAdCreativeMetadataJson())
                    .adCreativeExternalCreativeId(request.getAdCreativeExternalCreativeId())
                    .adCreativeExternalAdServer(request.getAdCreativeExternalAdServer())
                    .adCreativeAdTitle(request.getAdCreativeAdTitle())
                    .adCreativeAdMediaUrl(request.getAdCreativeAdMediaUrl())
                    .adCreativeAdSummary(request.getAdCreativeAdSummary())
                    .adCreativeTenantId(tenantId)
                    .build();

            // Set defaults
            if (creative.getAdCreativeIsActive() == null) {
                creative.setAdCreativeIsActive(true);
            }
            if (creative.getAdCreativeRequiresApproval() == null) {
                creative.setAdCreativeRequiresApproval(false);
            }
            if (creative.getAdCreativeApprovalStatus() == null) {
                creative.setAdCreativeApprovalStatus("PENDING");
            }

            AdCreative saved = creativeRepository.save(creative);

            stopWatch.stop();
            log.debug("Entity creation and save completed in {} ms", stopWatch.getLastTaskTimeMillis());

            AdCreativeResponseDto response = creativeMapper.toResponseDto(saved);
            buildUrls(response);

            log.info("Creative created successfully: {} (total time: {} ms)",
                    saved.getAdCreativeId(), stopWatch.getTotalTimeMillis());

            return response;

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Creative creation failed after {} ms: {}", stopWatch.getTotalTimeMillis(), e.getMessage(), e);

            // Cleanup: delete uploaded file and thumbnail if creation failed
            if (filename != null) {
                try {
                    fileStorageService.deleteCreativeFile(filePath);
                    log.info("Cleaned up uploaded file after failed creative creation: {}", filePath);
                } catch (Exception cleanupException) {
                    log.warn("Failed to cleanup uploaded file after failed creative creation: {}", filePath,
                            cleanupException);
                }
            }

            if (thumbnailFilename != null) {
                try {
                    newsImageProcessingService.deleteImage(thumbnailFilename, "thumb");
                    log.info("Cleaned up generated thumbnail after failed creative creation: {}", thumbnailFilename);
                } catch (Exception cleanupException) {
                    log.warn("Failed to cleanup generated thumbnail after failed creative creation: {}",
                            thumbnailFilename, cleanupException);
                }
            }

            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdCreativeResponseDto getCreativeById(UUID creativeId) {
        log.debug("Getting creative: {}", creativeId);
        AdCreative creative = findCreativeOrThrow(creativeId);
        AdCreativeResponseDto response = creativeMapper.toResponseDto(creative);
        buildUrls(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AdCreativeResponseDto getCreativeByCode(String creativeCode, String tenantId) {
        log.debug("Getting creative by code: {} for tenant: {}", creativeCode, tenantId);
        AdCreative creative = creativeRepository.findByAdCreativeCodeAndAdCreativeTenantId(creativeCode, tenantId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Creative not found with code: " + creativeCode));
        AdCreativeResponseDto response = creativeMapper.toResponseDto(creative);
        buildUrls(response);
        return response;
    }

    @Override
    @Transactional
    public AdCreativeResponseDto updateCreative(UUID creativeId, AdCreativeUploadRequestDto request) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("updateCreative");

        // Declare variables for cleanup in catch block
        String filename = null;
        String thumbnailFilename = null;
        String filePath = null;
        String fullFilePath = null;
        AdCreative creative = null;

        try {
            log.info("Updating creative with upload: {}", creativeId);

            // Normalize creative code to uppercase for consistency
            if (request.getAdCreativeCode() != null) {
                request.setAdCreativeCode(request.getAdCreativeCode().toUpperCase());
            }

            creative = findCreativeOrThrow(creativeId);

            // Initialize with existing values
            filename = creative.getAdCreativeFileName();
            thumbnailFilename = creative.getAdCreativeThumbnailFilename();

            // Check code uniqueness if changed
            if (!creative.getAdCreativeCode().equals(request.getAdCreativeCode())) {
                if (creativeRepository.existsByAdCreativeCodeAndAdCreativeTenantId(request.getAdCreativeCode(),
                        creative.getAdCreativeTenantId())) {
                    throw new InvalidRequestException("Creative code already exists: " + request.getAdCreativeCode());
                }
            }

            // Handle file upload if provided
            boolean fileProcessed = false;
            CreativeFileMetadata metadata = null;

            if (request.getFile() != null && !request.getFile().isEmpty()) {
                fileProcessed = true;
                stopWatch.stop(); // Stop the main updateCreative task
                stopWatch.start("fileProcessing");

                // Validate new file
                validateCreativeFile(request.getFile(), request.getAdCreativeType());

                // Process file upload using file storage service
                filePath = fileStorageService.storeCreativeFile(request.getFile(), request.getAdCreativeCode(),
                        creative.getAdCreativeTenantId());

                // Get full file path for thumbnail generation
                fullFilePath = fileStorageService.getCreativeFilePath(filePath);

                // Extract filename from the stored path
                filename = adsUrlService.extractFilename(filePath);

                // Generate thumbnail for image/video creatives
                if (newsImageProcessingService.isVideo(request.getFile().getContentType())) {
                    // For videos, generate a thumbnail frame
                    try {
                        // Create MultipartFile from saved path for processing
                        Path mediaPath = Paths.get(fullFilePath);
                        String contentType = Files.probeContentType(mediaPath);
                        MultipartFile tempFile = createMultipartFileFromPath(mediaPath, request.getAdCreativeCode(),
                                contentType);

                        var imageResponse = newsImageProcessingService.processVideo(tempFile,
                                request.getAdCreativeCode());
                        thumbnailFilename = imageResponse.getThumbnail().getFilename();
                        log.debug("Generated thumbnail for updated video creative: {}", thumbnailFilename);
                    } catch (Exception e) {
                        log.warn(
                                "Failed to generate thumbnail for updated video creative: {}, continuing without thumbnail",
                                request.getAdCreativeCode(), e);
                    }
                } else if (newsImageProcessingService.isImage(request.getFile().getContentType())) {
                    // For images, use the image itself as thumbnail (no separate thumbnail needed)
                    thumbnailFilename = filename;
                    log.debug("Using image file as thumbnail for updated creative: {}", thumbnailFilename);
                }

                // Extract file metadata
                metadata = extractFileMetadata(request.getFile());

                stopWatch.stop();
                log.debug("File processing completed in {} ms", stopWatch.getLastTaskTimeMillis());
                stopWatch.start("updateCreative"); // Resume the main task
            }

            stopWatch.stop(); // Stop updateCreative before starting entityUpdate
            stopWatch.start("entityUpdate");

            // Update metadata fields
            creative.setAdCreativeCode(request.getAdCreativeCode());
            creative.setAdCreativeName(request.getAdCreativeName());
            creative.setAdCreativeDescription(request.getAdCreativeDescription());
            creative.setAdCreativeAltText(request.getAdCreativeAltText());
            creative.setAdCreativeClickUrl(request.getAdCreativeClickUrl());
            creative.setAdCreativeRequiresApproval(request.getAdCreativeRequiresApproval());
            creative.setAdCreativeMetadataJson(request.getAdCreativeMetadataJson());

            // Update file-related fields only if a file was processed
            if (fileProcessed && metadata != null) {
                creative.setAdCreativeFileName(filename);
                creative.setAdCreativeThumbnailFilename(thumbnailFilename);
                creative.setAdCreativeWidth(metadata.width);
                creative.setAdCreativeHeight(metadata.height);
                creative.setAdCreativeFileSizeBytes(metadata.fileSize);
                creative.setAdCreativeMimeType(metadata.mimeType);
                creative.setAdCreativeDurationSeconds(metadata.durationSeconds);
                creative.setAdCreativeType(
                        request.getAdCreativeType() != null ? request.getAdCreativeType() : metadata.detectedType);
            } else {
                // For metadata-only updates, only update type if explicitly provided
                if (request.getAdCreativeType() != null) {
                    creative.setAdCreativeType(request.getAdCreativeType());
                }
            }
            creative.setAdCreativeExternalCreativeId(request.getAdCreativeExternalCreativeId());
            creative.setAdCreativeExternalAdServer(request.getAdCreativeExternalAdServer());
            creative.setAdCreativeAdTitle(request.getAdCreativeAdTitle());
            creative.setAdCreativeAdMediaUrl(request.getAdCreativeAdMediaUrl());
            creative.setAdCreativeAdSummary(request.getAdCreativeAdSummary());

            AdCreative saved = creativeRepository.save(creative);

            stopWatch.stop();
            log.debug("Entity update and save completed in {} ms", stopWatch.getLastTaskTimeMillis());

            log.info("Creative updated with upload successfully: {} (total time: {} ms)",
                    saved.getAdCreativeId(), stopWatch.getTotalTimeMillis());

            AdCreativeResponseDto response = creativeMapper.toResponseDto(saved);
            buildUrls(response);
            return response;

        } catch (Exception e) {
            stopWatch.stop();
            log.error("Creative update failed after {} ms: {}", stopWatch.getTotalTimeMillis(), e.getMessage(), e);

            // Cleanup: delete newly uploaded file and thumbnail if update failed
            if (request.getFile() != null && !request.getFile().isEmpty() && filename != null &&
                    !filename.equals(creative.getAdCreativeFileName())) {
                try {
                    fileStorageService.deleteCreativeFile(filePath);
                    log.info("Cleaned up newly uploaded file after failed creative update: {}", filePath);
                } catch (Exception cleanupException) {
                    log.warn("Failed to cleanup newly uploaded file after failed creative update: {}", filePath,
                            cleanupException);
                }
            }

            if (thumbnailFilename != null && !thumbnailFilename.equals(creative.getAdCreativeThumbnailFilename())) {
                try {
                    newsImageProcessingService.deleteImage(thumbnailFilename, "thumb");
                    log.info("Cleaned up newly generated thumbnail after failed creative update: {}",
                            thumbnailFilename);
                } catch (Exception cleanupException) {
                    log.warn("Failed to cleanup newly generated thumbnail after failed creative update: {}",
                            thumbnailFilename, cleanupException);
                }
            }

            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteCreative(UUID creativeId) {
        log.info("Deleting creative: {}", creativeId);
        AdCreative creative = findCreativeOrThrow(creativeId);
        creative.setDeletedAt(Instant.now());
        creativeRepository.save(creative);
        log.info("Creative deleted successfully: {}", creativeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdCreativeResponseDto> getAllCreatives(String tenantId, Pageable pageable) {
        log.debug("Getting all creatives for tenant: {}", tenantId);
        Page<AdCreative> creatives = creativeRepository.findAllByAdCreativeTenantIdAndDeletedAtIsNull(tenantId,
                pageable);
        return creatives.map(creativeMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdCreativeResponseDto> getActiveCreatives(String tenantId) {
        log.debug("Getting active creatives for tenant: {}", tenantId);
        List<AdCreative> creatives = creativeRepository.findActiveCreativesByTenantId(tenantId);
        List<AdCreativeResponseDto> responses = creativeMapper.toResponseDtoList(creatives);
        responses.forEach(this::buildUrls);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdCreativeResponseDto> getAvailableCreatives(String tenantId) {
        log.debug("Getting available creatives for tenant: {}", tenantId);
        List<AdCreative> creatives = creativeRepository.findAvailableCreativesByTenantId(tenantId);
        List<AdCreativeResponseDto> responses = creativeMapper.toResponseDtoList(creatives);
        responses.forEach(this::buildUrls);
        return responses;
    }

    @Override
    @Transactional
    public AdCreativeResponseDto approveCreative(UUID creativeId) {
        log.info("Approving creative: {}", creativeId);
        AdCreative creative = findCreativeOrThrow(creativeId);
        creative.setAdCreativeApprovalStatus("APPROVED");
        AdCreative saved = creativeRepository.save(creative);
        log.info("Creative approved successfully: {}", creativeId);
        AdCreativeResponseDto response = creativeMapper.toResponseDto(saved);
        buildUrls(response);
        return response;
    }

    @Override
    @Transactional
    public AdCreativeResponseDto rejectCreative(UUID creativeId, String reason) {
        log.info("Rejecting creative: {} with reason: {}", creativeId, reason);
        AdCreative creative = findCreativeOrThrow(creativeId);
        creative.setAdCreativeApprovalStatus("REJECTED");
        creative.setAdCreativeRejectionReason(reason);
        AdCreative saved = creativeRepository.save(creative);
        log.info("Creative rejected successfully: {}", creativeId);
        AdCreativeResponseDto response = creativeMapper.toResponseDto(saved);
        buildUrls(response);
        return response;
    }

    @Override
    @Transactional
    public AdCreativeResponseDto activateCreative(UUID creativeId) {
        log.info("Activating creative: {}", creativeId);
        AdCreative creative = findCreativeOrThrow(creativeId);
        creative.setAdCreativeIsActive(true);
        AdCreative saved = creativeRepository.save(creative);
        log.info("Creative activated successfully: {}", creativeId);
        AdCreativeResponseDto response = creativeMapper.toResponseDto(saved);
        buildUrls(response);
        return response;
    }

    @Override
    @Transactional
    public AdCreativeResponseDto deactivateCreative(UUID creativeId) {
        log.info("Deactivating creative: {}", creativeId);
        AdCreative creative = findCreativeOrThrow(creativeId);
        creative.setAdCreativeIsActive(false);
        AdCreative saved = creativeRepository.save(creative);
        log.info("Creative deactivated successfully: {}", creativeId);
        AdCreativeResponseDto response = creativeMapper.toResponseDto(saved);
        buildUrls(response);
        return response;
    }

    // ========================================
    // Creative File Serving
    // ========================================

    @Override
    public org.springframework.core.io.Resource getCreativeFile(String filename) {
        log.debug("Serving creative file: {}", filename);
        return fileStorageService.loadCreativeFile(filename);
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    private AdPlacement findPlacementOrThrow(UUID placementId) {
        return placementRepository.findById(placementId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Placement not found with ID: " + placementId));
    }

    private AdCreative findCreativeOrThrow(UUID creativeId) {
        return creativeRepository.findById(creativeId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Creative not found with ID: " + creativeId));
    }

    private void validateCreativeFile(MultipartFile file, AdCreativeType type) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is required");
        }

        // Check file size - use a reasonable default limit (50MB) for flexibility like
        // news service
        long maxSize = 50 * 1024 * 1024; // 50MB default, similar to video limit
        if (file.getSize() > maxSize) {
            throw new InvalidRequestException(
                    String.format("File size exceeds maximum allowed size of %d MB", 50));
        }

        // Note: MIME type validation removed for flexibility - accept any file type
        // Actual file type will be detected from content and used for processing
        String mimeType = file.getContentType();
        log.debug("Accepting file with MIME type: {} for creative type: {}", mimeType, type);
    }

    private CreativeFileMetadata extractFileMetadata(MultipartFile file) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("extractFileMetadata");

        CreativeFileMetadata metadata = new CreativeFileMetadata();
        metadata.fileSize = file.getSize();
        metadata.mimeType = file.getContentType();

        // Detect creative type based on MIME type
        metadata.detectedType = detectCreativeTypeFromMimeType(metadata.mimeType);

        try {
            // Extract metadata based on file type
            if (newsImageProcessingService.isImage(metadata.mimeType)) {
                extractImageMetadata(file, metadata);
            } else if (newsImageProcessingService.isVideo(metadata.mimeType)) {
                extractVideoMetadata(file, metadata);
            } else {
                log.debug("Skipping metadata extraction for unsupported file type: {}", metadata.mimeType);
            }
        } catch (Exception e) {
            log.warn("Failed to extract metadata from file: {}, using defaults. Error: {}",
                    file.getOriginalFilename(), e.getMessage());
            // Continue with null values - don't fail the upload
            metadata.width = null;
            metadata.height = null;
            metadata.durationSeconds = null;
        }

        stopWatch.stop();
        log.debug("Metadata extraction completed in {} ms for file: {}",
                stopWatch.getTotalTimeMillis(), file.getOriginalFilename());

        return metadata;
    }

    /**
     * Extracts width and height from image files.
     */
    private void extractImageMetadata(MultipartFile file, CreativeFileMetadata metadata) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                metadata.width = image.getWidth();
                metadata.height = image.getHeight();
                log.debug("Extracted image dimensions: {}x{} for file: {}",
                        metadata.width, metadata.height, file.getOriginalFilename());
            } else {
                log.warn("Failed to read image for metadata extraction: {}", file.getOriginalFilename());
            }
        } catch (IOException e) {
            log.warn("IOException during image metadata extraction: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to extract image metadata", e);
        }
    }

    /**
     * Extracts duration from video files.
     * Note: Basic implementation - could be enhanced with FFmpeg or similar
     * libraries.
     */
    private void extractVideoMetadata(MultipartFile file, CreativeFileMetadata metadata) {
        // For now, we can't easily extract video duration without additional libraries
        // This would require FFmpeg, Xuggler, or similar video processing libraries
        // Setting to null for now, but logging for future enhancement
        log.debug("Video metadata extraction not implemented yet. File: {}", file.getOriginalFilename());
        metadata.width = null;
        metadata.height = null;
        metadata.durationSeconds = null;

        // TODO: Implement video metadata extraction with:
        // - FFmpeg probe for duration, width, height
        // - Or use existing JCodec library if available
        // - Or integrate with media processing service
    }

    private AdCreativeType detectCreativeTypeFromMimeType(String mimeType) {
        if (mimeType == null) {
            return AdCreativeType.IMAGE;
        }

        if (mimeType.startsWith("image/")) {
            return AdCreativeType.IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return AdCreativeType.VIDEO;
        } else if (mimeType.equals("application/x-shockwave-flash") || mimeType.equals("application/octet-stream")) {
            return AdCreativeType.HTML5; // Assuming SWF or other rich media
        } else {
            return AdCreativeType.IMAGE; // Default fallback
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private String detectDeviceType(String userAgent) {
        if (userAgent == null)
            return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone"))
            return "mobile";
        if (ua.contains("tablet") || ua.contains("ipad"))
            return "tablet";
        return "desktop";
    }

    private String detectBrowser(String userAgent) {
        if (userAgent == null)
            return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("chrome") && !ua.contains("edg"))
            return "chrome";
        if (ua.contains("firefox"))
            return "firefox";
        if (ua.contains("safari") && !ua.contains("chrome"))
            return "safari";
        if (ua.contains("edg"))
            return "edge";
        return "other";
    }

    private String detectOS(String userAgent) {
        if (userAgent == null)
            return "unknown";
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows"))
            return "windows";
        if (ua.contains("mac"))
            return "macos";
        if (ua.contains("linux"))
            return "linux";
        if (ua.contains("android"))
            return "android";
        if (ua.contains("ios") || ua.contains("iphone") || ua.contains("ipad"))
            return "ios";
        return "other";
    }

    private static class CreativeFileMetadata {
        long fileSize;
        String mimeType;
        Integer width;
        Integer height;
        Integer durationSeconds;
        AdCreativeType detectedType;
    }

    /**
     * Moves the creative file to backup folder.
     * Used when updating a creative with a new file.
     *
     * @param filename the original filename
     */
    private void moveCreativeFileToBackup(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return;
        }
        String filePath = fileStorageService.getCreativeFilePath(filename);
        moveFileToBackup(filename, filePath, "creative");
    }

    /**
     * Moves the creative thumbnail to backup folder.
     * Used when updating a creative with a new file.
     *
     * @param filename the original thumbnail filename
     */
    private void moveCreativeThumbnailToBackup(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return;
        }
        String filePath = fileStorageService.getCreativeThumbnailPath(filename);
        moveFileToBackup(filename, filePath, "creative-thumbnail");
    }

    /**
     * Generic method to move a file to backup folder.
     * Used for both creative files and thumbnails.
     *
     * @param filename   the original filename
     * @param sourcePath the full source path of the file
     * @param fileType   descriptive type for logging (e.g., "creative",
     *                   "creative-thumbnail")
     */
    private void moveFileToBackup(String filename, String sourcePath, String fileType) {
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            log.debug("File does not exist, skipping backup - type={}, filename={}", fileType, filename);
            return;
        }

        final int MAX_RETRIES = MAX_BACKUP_RETRIES;
        final long RETRY_DELAY_MS = BACKUP_RETRY_DELAY_MS; // 1 second

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                ensureBackupFolderExists();
                String backupFileName = generateBackupFilename(filename);
                Path backupPath = Path.of(backupFolderPath, backupFileName);

                // Use atomic move for better reliability
                Files.move(sourceFile.toPath(), backupPath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);

                log.info("File moved to backup successfully - type={}, original={}, backup={}, attempt={}",
                        fileType, filename, backupFileName, attempt);
                return; // Success, exit retry loop

            } catch (IOException e) {
                log.warn("Backup attempt {} failed - type={}, filename={}, error={}",
                        attempt, fileType, filename, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    // All retries exhausted, try fallback deletion
                    log.error("All backup attempts failed, attempting cleanup - type={}, filename={}", fileType,
                            filename);
                    performBackupFailureCleanup(sourceFile, fileType, filename);
                    return;
                }

                // Wait before retry
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Backup retry interrupted - type={}, filename={}", fileType, filename);
                    return;
                }
            }
        }
    }

    /**
     * Performs cleanup when backup completely fails.
     */
    private void performBackupFailureCleanup(File sourceFile, String fileType, String filename) {
        try {
            // Fallback: delete if backup fails to avoid orphaned files
            if (sourceFile.exists() && sourceFile.delete()) {
                log.info("Cleanup successful: deleted original file after backup failure - type={}, filename={}",
                        fileType, filename);
            } else {
                log.error("Cleanup failed: could not delete original file after backup failure - type={}, filename={}",
                        fileType, filename);
            }
        } catch (Exception cleanupException) {
            log.error("Critical error during backup cleanup - type={}, filename={}, error={}",
                    fileType, filename, cleanupException.getMessage(), cleanupException);
        }
    }

    /**
     * Generates a backup filename with timestamp.
     * Format: {original}_{yyyyMMdd_HHmmss}
     */
    private String generateBackupFilename(String originalFilename) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern(BACKUP_TIMESTAMP_FORMAT));
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            String name = originalFilename.substring(0, dotIndex);
            String ext = originalFilename.substring(dotIndex);
            return name + "_" + timestamp + ext;
        }
        return originalFilename + "_" + timestamp;
    }

    /**
     * Ensures backup folder exists.
     */
    private void ensureBackupFolderExists() throws IOException {
        Path backupPath = Path.of(backupFolderPath);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
            log.info("Created backup folder - path={}", backupFolderPath);
        }
    }

    // ========================================
    // Public Analytics Implementation
    // ========================================

    @Override
    public java.util.Map<String, Object> getPublicPerformanceMetrics(java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        log.debug("Getting public performance metrics from {} to {}", startDate, endDate);

        java.time.Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        java.time.Instant endInstant = endDate.atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC);

        // Get impression and click counts
        Long totalImpressions = impressionRepository.countByAdImpressionRecordedAtBetween(startInstant, endInstant);
        Long totalClicks = clickRepository.countByAdClickRecordedAtBetween(startInstant, endInstant);

        // Calculate CTR
        double ctr = totalImpressions > 0 ? (double) totalClicks / totalImpressions * 100 : 0.0;

        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("totalImpressions", totalImpressions);
        metrics.put("totalClicks", totalClicks);
        metrics.put("clickThroughRate", java.math.BigDecimal.valueOf(ctr).setScale(2, java.math.RoundingMode.HALF_UP));
        metrics.put("periodStart", startDate.toString());
        metrics.put("periodEnd", endDate.toString());

        log.info("Retrieved public performance metrics: {} impressions, {} clicks, CTR: {}%",
                totalImpressions, totalClicks, String.format("%.2f", ctr));

        return metrics;
    }

    @Override
    public java.util.Map<String, Object> getPublicCampaignMetrics(String campaignId) {
        log.debug("Getting public metrics for campaign: {}", campaignId);

        try {
            java.util.UUID campaignUUID = java.util.UUID.fromString(campaignId);

            // Get campaign impression and click counts
            Long impressions = impressionRepository.countByAdImpressionCampaignId(campaignUUID);
            Long clicks = clickRepository.countByAdClickCampaignId(campaignUUID);

            // Calculate CTR
            double ctr = impressions > 0 ? (double) clicks / impressions * 100 : 0.0;

            java.util.Map<String, Object> metrics = new java.util.HashMap<>();
            metrics.put("campaignId", campaignId);
            metrics.put("impressions", impressions);
            metrics.put("clicks", clicks);
            metrics.put("clickThroughRate",
                    java.math.BigDecimal.valueOf(ctr).setScale(2, java.math.RoundingMode.HALF_UP));

            log.info("Retrieved public metrics for campaign {}: {} impressions, {} clicks",
                    campaignId, impressions, clicks);

            return metrics;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid campaign ID format: {}", campaignId);
            return java.util.Collections.emptyMap();
        }
    }

    /**
     * Builds full URLs for creative file and thumbnail in the response DTO.
     * Replaces the relative paths set by the mapper with full HTTP URLs.
     *
     * @param response the response DTO to update
     */
    private void buildUrls(AdCreativeResponseDto response) {
        // Extract filename from the file URL path set by mapper
        String fileUrl = response.getAdCreativeFileUrl();
        String creativeFilename = null;
        if (fileUrl != null && !fileUrl.trim().isEmpty()) {
            // Extract filename from path like "/api/v1/ads/creatives/files/filename"
            creativeFilename = extractFilenameFromPath(fileUrl);
            if (creativeFilename != null) {
                response.setAdCreativeFileUrl(adsUrlService.buildCreativeUrl(creativeFilename));
            }
        }

        // Extract filename from the thumbnail URL path set by mapper
        String thumbnailUrl = response.getAdCreativeThumbnailUrl();
        if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) {
            // Extract filename from path like "/api/v1/ads/creatives/thumbnails/filename"
            String thumbnailFilename = extractFilenameFromPath(thumbnailUrl);
            if (thumbnailFilename != null) {
                // For images, thumbnail filename is the same as creative filename, so use
                // creative URL
                if (thumbnailFilename.equals(creativeFilename)) {
                    response.setAdCreativeThumbnailUrl(adsUrlService.buildCreativeUrl(thumbnailFilename));
                } else {
                    response.setAdCreativeThumbnailUrl(adsUrlService.buildThumbnailUrl(thumbnailFilename));
                }
            }
        }
    }

    /**
     * Extracts filename from a URL path.
     * For example: "/api/v1/ads/creatives/files/myfile.jpg" -> "myfile.jpg"
     *
     * @param path the URL path
     * @return the filename, or null if extraction fails
     */
    private String extractFilenameFromPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        // Find the last "/" and extract everything after it
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
            return path.substring(lastSlashIndex + 1);
        }

        return null;
    }

    /**
     * Creates a MultipartFile from a file path for processing.
     * Used when we need to process already-saved files.
     */
    private MultipartFile createMultipartFileFromPath(Path filePath, String originalFilename, String contentType)
            throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return originalFilename;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                try {
                    return Files.size(filePath) == 0;
                } catch (IOException e) {
                    return true; // Assume empty if we can't check
                }
            }

            @Override
            public long getSize() {
                try {
                    return Files.size(filePath);
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public byte[] getBytes() throws IOException {
                return Files.readAllBytes(filePath);
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return Files.newInputStream(filePath);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.copy(filePath, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        };
    }
}
