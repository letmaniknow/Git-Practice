
package com.mmva.newsapp.domain.news.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.mmva.newsapp.domain.adminuser.model.core.AdminUser;
import com.mmva.newsapp.domain.adminuser.repository.core.AdminUserRepository;
import com.mmva.newsapp.domain.news.config.core.NewsCacheConstants;
import com.mmva.newsapp.domain.news.dto.core.ImageProcessingResponseDto;
import com.mmva.newsapp.domain.news.dto.core.NewsCreateRequestDto;
import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;
import com.mmva.newsapp.domain.news.dto.core.NewsMediaFileRequestDto;
import com.mmva.newsapp.domain.news.dto.core.NewsMediaUploadResponseDto;
import com.mmva.newsapp.domain.news.dto.core.ThumbnailResponseDto;
import com.mmva.newsapp.infrastructure.common.content.dto.ContentQualityMetrics;
import com.mmva.newsapp.domain.news.exception.core.NewsNotFoundException;
import com.mmva.newsapp.domain.news.mapper.core.NewsMapper;
import com.mmva.newsapp.domain.news.audit.model.NewsAuditLog;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity.WorkflowStatus;
import com.mmva.newsapp.domain.news.enums.core.ContentOrigin;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;
import com.mmva.newsapp.domain.news.repository.audit.NewsAuditLogRepository;
import com.mmva.newsapp.domain.news.audit.constants.NewsAuditActions;
import com.mmva.newsapp.domain.news.service.media.MediaUrlService;
import com.mmva.newsapp.domain.news.service.media.NewsImageProcessingService;
import com.mmva.newsapp.domain.news.service.media.NewsMediaStorageService;
import com.mmva.newsapp.domain.news.service.validation.NewsValidationService;
import com.mmva.newsapp.domain.news.service.audit.NewsAuditLogService;
import com.mmva.newsapp.domain.news.service.recommendation.NewsRecommendationService;
import com.mmva.newsapp.domain.news.service.search.NewsSearchSpecifications;
import com.mmva.newsapp.infrastructure.common.audit.service.AuditingUtility;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.model.NewsBookmark;
import com.mmva.newsapp.domain.newsengagement.bookmarks.repository.NewsBookmarkRepository;
import com.mmva.newsapp.domain.newsengagement.comments.model.NewsComment;
import com.mmva.newsapp.domain.newsengagement.comments.repository.NewsCommentRepository;
import com.mmva.newsapp.domain.newsengagement.comments.service.NewsCommentService;
import com.mmva.newsapp.domain.newsengagement.likes.model.NewsLike;
import com.mmva.newsapp.domain.newsengagement.likes.repository.NewsLikeRepository;
import com.mmva.newsapp.domain.newsengagement.shares.model.NewsShare;
import com.mmva.newsapp.domain.newsengagement.shares.repository.NewsShareRepository;
import com.mmva.newsapp.domain.newsengagement.views.model.NewsView;
import com.mmva.newsapp.domain.newsengagement.views.repository.NewsViewRepository;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.common.exception.UnauthorizedAccessException;
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import com.mmva.newsapp.domain.news.service.content.NewsContentProcessingService;
import com.mmva.newsapp.infrastructure.common.content.service.ContentQualityService;
import com.mmva.newsapp.infrastructure.common.geographic.service.GeographicValidationService;
import com.mmva.newsapp.infrastructure.push.service.PushNotificationService;
import com.mmva.newsapp.infrastructure.storage.model.MediaUpload;
import com.mmva.newsapp.infrastructure.common.content.service.SeoOptimizationService;
import com.mmva.newsapp.infrastructure.common.audit.service.SoftDeleteContext;
import com.mmva.newsapp.infrastructure.common.util.InputSanitizer;
import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import com.mmva.newsapp.domain.news.enums.core.UrgencyLevel;
import com.mmva.newsapp.infrastructure.common.util.SortUtils;
import com.mmva.newsapp.domain.news.service.social.SocialMediaShareService;
import com.mmva.newsapp.domain.news.service.scheduler.NewsSchedulingService;
import com.mmva.newsapp.domain.news.service.elasticsearch.NewsElasticSearchService;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import java.util.stream.Collectors;

/**
 * Implementation of the NewsService interface.
 * <p>
 * Provides comprehensive news management including CRUD operations,
 * workflow management, search functionality, and platform-specific content
 * processing.
 * </p>
 * 
 * @version 1.0
 * @since 2024-06-01
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final WorkflowStatus PUBLISHED_STATUS = WorkflowStatus.PUBLISHED;
    private static final long SCHEDULE_CHECK_INTERVAL_MS = 60_000L; // 1 minute

    private final NewsAuditLogService newsAuditLogService;
    private final AuditingUtility auditingUtility;
    private final NewsAuditLogRepository newsAuditLogRepository;
    private final NewsRepository newsRepository;
    private final AdminUserRepository adminUserRepository;
    private final NewsCommentService newsCommentService;
    private final SoftDeleteContext softDeleteContext;
    private final NewsContentProcessingService newsContentProcessingService;
    private final ContentQualityService contentQualityService;
    private final SeoOptimizationService seoOptimizationService;
    private final GeographicValidationService geographicValidationService;
    private final NewsLikeRepository newsLikeRepository;
    private final NewsViewRepository newsViewRepository;
    private final NewsShareRepository newsShareRepository;
    private final NewsCommentRepository newsCommentRepository;
    private final NewsBookmarkRepository userBookmarkRepository;
    private final NewsRecommendationService newsRecommendationService;
    private final MediaUrlService mediaUrlService;
    private final NewsMediaStorageService newsMediaStorageService;
    private final NewsImageProcessingService newsImageProcessingService;
    private final NewsMapper newsMapper;
    private final InputSanitizer inputSanitizer;
    private final NewsValidationService newsValidationService;
    private final PushNotificationService pushNotificationService;
    private final SocialMediaShareService socialMediaShareService;
    private final NewsSchedulingService newsSchedulingService;
    private final Optional<NewsElasticSearchService> newsElasticSearchService; // Auto-index to Elasticsearch (optional)

    // =========================
    // Scheduled Tasks
    // =========================

    /**
     * Scheduled task to automatically publish newsapp articles at their scheduled
     * time.
     * Runs every minute to check for articles ready for publication.
     *
     * Enhanced with Phase 1 job tracking:
     * - Creates job record with correlation ID for distributed tracing
     * - Records per-article attempts with error categorization
     * - Supports retry logic for transient failures
     * - Full observability via structured logging and metrics
     */
    @Transactional
    @Scheduled(fixedRate = SCHEDULE_CHECK_INTERVAL_MS)
    public void publishScheduledNews() {
        Instant now = Instant.now();
        List<NewsMasterEntity> scheduledNews = newsRepository
                .findAllByNewsWorkflowStatusAndNewsScheduledPublishAtBefore(
                        WorkflowStatus.SCHEDULED, now);

        if (scheduledNews.isEmpty()) {
            return;
        }

        // Create job record for tracking and observability
        var job = newsSchedulingService.startJob();
        var attempts = new ArrayList<com.mmva.newsapp.domain.news.model.scheduler.NewsSchedulingAttempt>();

        // Process each article with per-article error handling
        for (NewsMasterEntity news : scheduledNews) {
            try {
                // Publish article using existing business logic
                var attempt = newsSchedulingService.publishArticleWithErrorHandling(
                        news,
                        job,
                        article -> {
                            article.setNewsWorkflowStatus(WorkflowStatus.PUBLISHED);
                            article.setNewsPublishedAt(now);
                            // Clear scheduledBy since it's now published
                            article.setNewsScheduledBy(null);

                            // Send push notification on publish (Option B - industry best practice)
                            sendPublishNotification(article);

                            // Create social sharing record for published news
                            socialMediaShareService.ensureSharingRecordExists(article.getNewsNewsId());

                            logAuditAction(article.getNewsNewsId(), "SCHEDULED_PUBLISH",
                                    "News published by schedule", article.getUpdatedBy());
                        });

                attempts.add(attempt);

                // Persist article changes if successful
                if ("SUCCESS".equals(attempt.getStatus())) {
                    newsRepository.save(news);
                }

            } catch (Exception e) {
                // Prevent single article failure from stopping other articles
                log.error("[SCHEDULED-PUBLISH] Unexpected error processing article={}", news.getNewsNewsId(), e);
            }
        }

        // Complete job with aggregated metrics
        newsSchedulingService.completeJob(job, attempts);
        log.info("Scheduled newsapp published - count={}, jobId={}", scheduledNews.size(), job.getJobId());
    }

    // =========================
    // CRUD Operations
    // =========================

    @Override
    @Transactional(readOnly = false)
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.NEWS_BY_SLUG_CACHE,
            NewsCacheConstants.PUBLISHED_NEWS_CACHE, NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public NewsCreateResponseDto createNews(NewsCreateRequestDto request) {
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        String titleEn = request.getNewsTitleEn();

        // Milestone 1/7: START
        log.info("[NEWS-CREATE] [{}] [1/7] START - title='{}', hasCustomThumbnail={}, mediaSize={}KB",
                correlationId, titleEn,
                request.getThumbnailFile() != null && !request.getThumbnailFile().isEmpty(),
                request.getImageVideoFile() != null ? request.getImageVideoFile().getSize() / 1024 : 0);

        // ========================================
        // VALIDATE-ALL APPROACH: Collect Custom + Service Validations
        // ========================================
        // Step 1: Collect custom field-level validations (title uniqueness, content
        // length)
        Map<String, String> customFieldErrors = collectAllValidationErrors(request, null);

        // Step 2: Run service-level validations (format, required fields)
        var validationResult = newsValidationService.validateNewsCreation(request);
        Map<String, String> serviceFieldErrors = !validationResult.isValid()
                ? parseValidationErrors(validationResult.getErrors())
                : new HashMap<>();

        // Step 3: Merge all errors (custom + service)
        Map<String, String> allFieldErrors = new LinkedHashMap<>();
        allFieldErrors.putAll(customFieldErrors);
        allFieldErrors.putAll(serviceFieldErrors);

        // Step 4: If ANY errors found, throw them ALL at once (Validate-All Pattern)
        if (!allFieldErrors.isEmpty()) {
            String errorMessage = "Validation failed for " + allFieldErrors.size() + " field(s)";
            log.warn("[NEWS-CREATE] [{}] Validation FAILED - errors={}", correlationId, allFieldErrors);
            throw new InvalidRequestException(errorMessage, allFieldErrors);
        }

        // Milestone 2/7: Validation PASSED
        log.info("[NEWS-CREATE] [{}] [2/7] Validation PASSED - allValidationsPassed=true", correlationId);

        // ========================================
        // STEP 1: PRESERVE ORIGINAL FIRST (Data-First Approach)
        // ========================================
        // Save original file IMMEDIATELY before any processing
        // This ensures we never lose uploaded data, even if processing fails
        MediaUpload mediaUpload = null;
        byte[] mediaBytes = null;
        if (request.getImageVideoFile() != null && !request.getImageVideoFile().isEmpty()) {
            // Read bytes once for reuse
            try {
                mediaBytes = request.getImageVideoFile().getInputStream().readAllBytes();
            } catch (IOException e) {
                log.error("[NEWS-CREATE] [{}] Failed to read media bytes - error={}", correlationId, e.getMessage(), e);
                throw new InvalidRequestException("imageVideoFile", "Failed to read media file: " + e.getMessage());
            }

            String uploadedBy = request.getCreatedBy() != null ? request.getCreatedBy().toString() : "system";
            mediaUpload = newsMediaStorageService.preserveOriginal(
                    mediaBytes,
                    request.getImageVideoFile().getOriginalFilename(),
                    request.getImageVideoFile().getContentType(),
                    request.getImageVideoFile().getSize(),
                    correlationId,
                    uploadedBy);
            log.info("[NEWS-CREATE] [{}] [2.5/7] Original PRESERVED - uploadId={}, path='{}'",
                    correlationId, mediaUpload.getUploadId(), mediaUpload.getOriginalPath());
        }

        // ========================================
        // STEP 2: Process images and generate variants
        // ========================================
        String processedImageBaseFilename = null; // Track base filename for cleanup of all processed images (thumbnail,
                                                  // hero, card)

        try {
            // Build entity without media filename initially (will be set after processing)
            NewsMasterEntity entity = buildNewsEntity(request);

            ImageProcessingResponseDto imageResponse = null;
            if (mediaBytes != null) {
                // Process images directly - MANDATORY (throws exception if fails)
                // Image processing service handles all file creation in proper locations
                imageResponse = processImages(request, mediaBytes);
                // Milestone 3/7: Images generated
                log.info("[NEWS-CREATE] [{}] [3/7] Images generated - strategy={}, processedImageBaseFilename='{}'",
                        correlationId, imageResponse.getSource(), imageResponse.getThumbnail().getFilename());

                // Set processedImageBaseFilename for cleanup on failure
                processedImageBaseFilename = imageResponse.getThumbnail().getFilename();

                // Validate that all mandatory image variants were generated
                if (imageResponse.getThumbnail() == null) {
                    throw new InvalidRequestException("thumbnail",
                            "Failed to generate thumbnail image. Please check the media file and try again.");
                }
                if (imageResponse.getCard() == null) {
                    throw new InvalidRequestException("imageVideoFile",
                            "Failed to generate card image. Please check the media file and try again.");
                }
                if (imageResponse.getHero() == null) {
                    throw new InvalidRequestException("imageVideoFile",
                            "Failed to generate hero image. Please check the media file and try again.");
                }

                // Set image URLs (mandatory sizes: thumbnail, card, hero)
                entity.setNewsThumbnailUrl(imageResponse.getThumbnail().getFilename());
                entity.setNewsImageCardUrl(imageResponse.getCard().getFilename());
                entity.setNewsImageHeroUrl(imageResponse.getHero().getFilename());
                // CRITICAL: Set main image filename from processed response (not the original
                // filename)
                entity.setNewsMediaFileName(imageResponse.getMain().getFilename());
                entity.setNewsMediaFileUrl(imageResponse.getMain().getFilename());
                log.debug("[NEWS-CREATE] [{}] Images assigned - main={}, thumbnail={}, card={}, hero={}",
                        correlationId,
                        imageResponse.getMain().getFilename(),
                        imageResponse.getThumbnail().getFilename(),
                        imageResponse.getCard().getFilename(),
                        imageResponse.getHero().getFilename());
            }

            // STEP 3: VALIDATE WORKFLOW STATUS AND TIMING
            // ========================================
            WorkflowStatus workflowStatus = entity.getNewsWorkflowStatus();
            Instant now = Instant.now();

            // Validate SCHEDULED status requirements
            if (WorkflowStatus.SCHEDULED == workflowStatus) {
                Instant scheduledTime = request.getNewsScheduledPublishAt();
                if (scheduledTime == null) {
                    log.warn("[NEWS-CREATE] [{}] Validation FAILED - SCHEDULED status requires newsScheduledPublishAt",
                            correlationId);
                    throw new InvalidRequestException("newsScheduledPublishAt",
                            "Scheduled publish date/time is required when workflow status is SCHEDULED");
                }
                // Validate not in the past
                if (scheduledTime.isBefore(now)) {
                    log.warn(
                            "[NEWS-CREATE] [{}] Validation FAILED - past date not allowed, scheduledTime={}, now={}",
                            correlationId, scheduledTime, now);
                    throw new InvalidRequestException("newsScheduledPublishAt",
                            "Cannot schedule publication in the past. Scheduled time must be in the future");
                }
                // Validate reasonable future limit (1 year max)
                Instant oneYearFromNow = now.plus(365, ChronoUnit.DAYS);
                if (scheduledTime.isAfter(oneYearFromNow)) {
                    log.warn("[NEWS-CREATE] [{}] Validation FAILED - too far future, scheduledTime={}, limit={}",
                            correlationId, scheduledTime, oneYearFromNow);
                    throw new InvalidRequestException("newsScheduledPublishAt",
                            "Cannot schedule publication more than 1 year in the future");
                }
                entity.setNewsScheduledPublishAt(scheduledTime);
                entity.setNewsScheduledBy(request.getCreatedBy());
                log.info("[NEWS-CREATE] [{}] [4.5/7] Scheduled publication validated - scheduledTime={}",
                        correlationId, scheduledTime);
            }

            // Set published timestamp and publishedBy if status is PUBLISHED
            if (WorkflowStatus.PUBLISHED == workflowStatus) {
                entity.setNewsPublishedAt(now);
                // Set publishedBy to creator/admin user
                entity.setNewsPublishedBy(request.getCreatedBy());
            }

            NewsMasterEntity saved = newsRepository.saveAndFlush(Objects.requireNonNull(entity));
            // Milestone 4/6: Entity saved
            log.info("[NEWS-CREATE] [{}] [4/6] Entity saved - newsId={}, status={}",
                    correlationId, saved.getNewsNewsId(), saved.getNewsWorkflowStatus());

            // Link media upload to the saved news entity
            // NOTE: saveAndFlush() guarantees the entity is persisted to DB before
            // continuing.
            // The following operations (linkToEntity, markPublished) are within the same
            // @Transactional context,
            // ensuring consistency: if these operations fail, the entire transaction rolls
            // back (all-or-nothing).
            // No explicit flush needed here as the @Transactional boundary handles it.
            if (mediaUpload != null && imageResponse != null && imageResponse.getMain() != null) {
                newsMediaStorageService.linkToEntity(mediaUpload.getUploadId(), saved.getNewsNewsId().toString());
                // Use processed main image filename from image processing
                String processedMainFilename = imageResponse.getMain().getFilename();
                String processedMainPath = imageResponse.getMain().getFilePath();
                newsMediaStorageService.markPublished(
                        mediaUpload.getUploadId(),
                        processedMainFilename,
                        processedMainPath,
                        mediaUrlService.buildMediaUrl(processedMainFilename));
            }

            // Send push notification if created with PUBLISHED status
            String pushNotificationType = "NONE";
            if (PUBLISHED_STATUS.equals(saved.getNewsWorkflowStatus())) {
                sendPublishNotification(saved);
                newsRepository.save(saved); // Save notification tracking fields

                // Create social sharing record for published news
                socialMediaShareService.ensureSharingRecordExists(saved.getNewsNewsId());

                // Milestone 5/6: Push notification sent and sharing record created
                pushNotificationType = Boolean.TRUE.equals(saved.getNewsIsBreaking()) ? "BREAKING"
                        : Boolean.TRUE.equals(saved.getNewsIsFeatured()) ? "FEATURED" : "NEWS_UPDATE";
                log.info(
                        "[NEWS-CREATE] [{}] [5/6] Push notification sent and sharing record created - type={}, newsId={}",
                        correlationId, pushNotificationType, saved.getNewsNewsId());
            }

            logAuditAction(saved.getNewsNewsId(), "CREATE",
                    "News created with status: " + saved.getNewsWorkflowStatus(), saved.getCreatedBy());

            // Audit category assignment if category is set
            if (saved.getNewsNewsCategoryId() != null) {
                logAuditAction(saved.getNewsNewsId(), "CATEGORY_ASSIGNED",
                        "News assigned to category: " + saved.getNewsNewsCategoryId(), saved.getCreatedBy());
            }

            // Auto-index to Elasticsearch (non-critical - failure doesn't block news
            // creation)
            try {
                newsElasticSearchService.ifPresent(service -> service.indexNewsArticle(saved.getNewsNewsId()));
                log.debug("[NEWS-CREATE] [{}] Indexed to Elasticsearch - newsId={}",
                        correlationId, saved.getNewsNewsId());
            } catch (Exception esError) {
                log.warn("[NEWS-CREATE] [{}] Failed to index to Elasticsearch (non-critical) - newsId={}, error={}",
                        correlationId, saved.getNewsNewsId(), esError.getMessage());
                // Continue anyway - ES indexing is not critical for news creation
            }

            NewsCreateResponseDto response = mapToResponse(saved);

            // Milestone 6/6: COMPLETE
            long duration = System.currentTimeMillis() - startTime;
            log.info("[NEWS-CREATE] [{}] [6/6] COMPLETE - newsId={}, status={}, pushType={}, duration={}ms",
                    correlationId, saved.getNewsNewsId(), saved.getNewsWorkflowStatus(), pushNotificationType,
                    duration);

            return response;
        } catch (

        DataIntegrityViolationException e) {
            // Let database constraint violations bubble up to GlobalExceptionHandler
            throw e;
        } catch (InvalidRequestException e) {
            // Let validation exceptions bubble up directly (they're already properly
            // formatted)
            throw e;
        } catch (Exception e) {
            // Cleanup processed image variants if they were created
            if (processedImageBaseFilename != null) {
                try {
                    newsImageProcessingService.deleteImages(processedImageBaseFilename);
                    log.debug("[NEWS-CREATE] [{}] Cleaned up orphaned images - filename='{}'",
                            correlationId, processedImageBaseFilename);
                } catch (Exception thumbnailError) {
                    log.warn("[NEWS-CREATE] [{}] Failed to cleanup images - filename='{}', error={}",
                            correlationId, processedImageBaseFilename, thumbnailError.getMessage());
                }
            }
            long duration = System.currentTimeMillis() - startTime;
            log.error("[NEWS-CREATE] [{}] FAILED - title='{}', duration={}ms, error={}",
                    correlationId, titleEn, duration, e.getMessage(), e);
            throw new InvalidRequestException("news", "Failed to create news: " + e.getMessage());
        }
    }

    /**
     * Processes thumbnail for news creation - MANDATORY.
     * 
     * <h3>Thumbnail Generation Strategy (Priority Order):</h3>
     * <ol>
     * <li><b>Custom Thumbnail:</b> If client provides a thumbnail file, use it
     * directly (resized to 1024x512)</li>
     * <li><b>Image Media:</b> If media is an image, auto-generate thumbnail by
     * resizing</li>
     * <li><b>Video Media:</b> If media is a video, extract frame at 2 seconds and
     * resize</li>
     * </ol>
     * 
     * <h3>MANDATORY Behavior:</h3>
     * <p>
     * Thumbnail is REQUIRED for all news articles because:
     * </p>
     * <ul>
     * <li>Push notifications require thumbnails for rich display</li>
     * <li>News cards/listings need consistent thumbnail display</li>
     * <li>Social sharing requires Open Graph images</li>
     * </ul>
     * 
     * <p>
     * <b>This method will THROW an exception if thumbnail cannot be generated.</b>
     * </p>
     *
     * @param request    the news creation request
     * @param mediaBytes the media file content as bytes
     * @return thumbnail response with URL and metadata (NEVER null)
     * @throws InvalidRequestException if thumbnail generation fails
     */
    private ImageProcessingResponseDto processImages(NewsCreateRequestDto request, byte[] mediaBytes) {
        MultipartFile thumbnailFile = request.getThumbnailFile();
        MultipartFile mediaFile = request.getImageVideoFile();
        String originalFilename = mediaFile != null ? mediaFile.getOriginalFilename() : "unknown";

        // ========================================
        // Strategy 1: Custom Thumbnail (Highest Priority)
        // ========================================
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try {
                log.info("Image Processing Strategy: Using CUSTOM thumbnail - originalMedia={}, thumbnailSize={}KB",
                        originalFilename, thumbnailFile.getSize() / 1024);
                ImageProcessingResponseDto response = newsImageProcessingService.processImage(thumbnailFile,
                        originalFilename);
                log.info("Images processed successfully - strategy=CUSTOM, thumbnailFilename={}",
                        response.getThumbnail().getFilename());
                return response;
            } catch (Exception e) {
                log.error("Custom thumbnail processing failed - error={}", e.getMessage(), e);
                throw new InvalidRequestException("thumbnailFile",
                        "Failed to process custom thumbnail: " + e.getMessage());
            }
        }

        // ========================================
        // Strategy 2 & 3: Auto-generate from Media
        // ========================================
        if (mediaFile == null || mediaFile.isEmpty()) {
            throw new InvalidRequestException("imageVideoFile",
                    "Media file is required to auto-generate images. Please provide either a custom thumbnail or a media file.");
        }

        String contentType = mediaFile.getContentType();

        try {
            // Strategy 2: Image Media → Generate all sizes
            if (newsImageProcessingService.isImage(contentType)) {
                log.info("Image Processing Strategy: Auto-generating from IMAGE - mediaFile={}, contentType={}",
                        originalFilename, contentType);
                ImageProcessingResponseDto response = newsImageProcessingService.processImage(mediaBytes,
                        originalFilename, contentType);
                log.info("Images processed successfully - strategy=IMAGE_PROCESS, thumbnailFilename={}",
                        response.getThumbnail().getFilename());
                return response;
            }

            // Strategy 3: Video Media → Extract frame and generate all sizes
            if (newsImageProcessingService.isVideo(contentType)) {
                log.info("Image Processing Strategy: Extracting frame from VIDEO - mediaFile={}, contentType={}",
                        originalFilename, contentType);
                ImageProcessingResponseDto response = newsImageProcessingService.processVideo(mediaFile,
                        originalFilename);

                // Check if video processing succeeded (has at least a thumbnail)
                if (response.getThumbnail() != null) {
                    log.info("Images processed successfully - strategy=VIDEO_FRAME, thumbnailFilename={}",
                            response.getThumbnail().getFilename());
                    return response;
                } else {
                    log.warn(
                            "Video processing returned no thumbnails - mediaFile={}, falling back to custom thumbnail requirement",
                            originalFilename);
                    throw new InvalidRequestException("imageVideoFile",
                            String.format("Failed to generate thumbnail from video '%s'. " +
                                    "The video file may be corrupted or have an unsupported format. " +
                                    "Please provide a custom thumbnail image.", originalFilename));
                }
            }

            // Unsupported media type
            throw new InvalidRequestException("imageVideoFile",
                    String.format("Cannot generate images from media type '%s'. " +
                            "Supported formats: Images (JPEG, PNG, GIF, WebP, BMP) | Videos (MP4, MOV, WebM). " +
                            "Note: AVIF requires additional libraries. Please convert to a supported format or provide a custom thumbnail.",
                            contentType));

        } catch (InvalidRequestException e) {
            throw e; // Re-throw validation errors
        } catch (Exception e) {
            log.error("Auto-image generation failed - mediaFile={}, error={}", originalFilename, e.getMessage(), e);
            throw new InvalidRequestException("imageVideoFile",
                    String.format("Failed to generate thumbnail from uploaded media '%s'. " +
                            "The image file may be corrupted or in an unsupported format. " +
                            "Please try with a different image or provide a custom thumbnail image. Error: %s",
                            originalFilename, e.getMessage()));
        }
    }

    @Override
    @Cacheable(value = NewsCacheConstants.NEWS_CACHE, key = "#id")
    public NewsCreateResponseDto getNewsById(String id) {
        log.debug("Fetching newsapp - newsId={}", id);
        NewsMasterEntity entity = findNewsByIdOrThrow(id);
        return mapToResponse(entity);
    }

    @Override
    public Page<NewsCreateResponseDto> getAllNews(Pageable pageable) {
        log.debug("Fetching all newsapp (including deleted) - page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin API: Use SoftDeleteSpec.includeDeleted() to see all records
        return newsRepository.findAll(SoftDeleteSpec.includeDeleted(), pageable).map(this::mapToResponse);
    }

    /**
     * Get only active (non-deleted) newsapp articles.
     * Use for public-facing APIs.
     *
     * @param pageable pagination info
     * @return page of active newsapp
     */
    @Override
    public Page<NewsCreateResponseDto> getActiveNews(Pageable pageable) {
        log.debug("Fetching active newsapp - page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Public API: Use SoftDeleteSpec.notDeleted() to exclude soft-deleted
        return newsRepository.findAll(SoftDeleteSpec.notDeleted(), pageable).map(this::mapToResponse);
    }

    /**
     * Get only soft-deleted newsapp articles for admindashboard restore listing.
     *
     * @param pageable pagination info
     * @return page of deleted newsapp
     */
    @Override
    public Page<NewsCreateResponseDto> getDeletedNews(Pageable pageable) {
        log.debug("Fetching deleted newsapp - page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin restore listing: Use SoftDeleteSpec.onlyDeleted()
        return newsRepository.findAll(SoftDeleteSpec.onlyDeleted(), pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.NEWS_BY_SLUG_CACHE,
            NewsCacheConstants.PUBLISHED_NEWS_CACHE, NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public NewsCreateResponseDto updateNews(String id, NewsCreateRequestDto request) {
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // Milestone 1/6: START
        log.info("[NEWS-UPDATE] [{}] [1/6] START - newsId={}, newTitle='{}', hasNewMedia={}",
                correlationId, id, request.getNewsTitleEn(),
                request.getImageVideoFile() != null && !request.getImageVideoFile().isEmpty());

        // Find existing news first (for validation context)
        NewsMasterEntity existingNews = findNewsByIdOrThrow(id);
        UUID existingNewsId = existingNews.getNewsNewsId();

        // ========================================
        // VALIDATE-ALL APPROACH: Collect Custom + Service Validations
        // ========================================
        // Step 1: Collect custom field-level validations (title uniqueness, content
        // length)
        Map<String, String> customFieldErrors = collectAllValidationErrors(request, existingNewsId);

        // Step 2: Run service-level validations for UPDATE (imageVideoFile is optional)
        var validationResult = newsValidationService.validateNewsUpdate(request);
        Map<String, String> serviceFieldErrors = !validationResult.isValid()
                ? parseValidationErrors(validationResult.getErrors())
                : new HashMap<>();

        // Step 3: Merge all errors (custom + service)
        Map<String, String> allFieldErrors = new LinkedHashMap<>();
        allFieldErrors.putAll(customFieldErrors);
        allFieldErrors.putAll(serviceFieldErrors);

        // Step 4: If ANY errors found, throw them ALL at once (Validate-All Pattern)
        if (!allFieldErrors.isEmpty()) {
            String errorMessage = "Validation failed for " + allFieldErrors.size() + " field(s)";
            log.warn("[NEWS-UPDATE] [{}] Validation FAILED - errors={}", correlationId, allFieldErrors);
            throw new InvalidRequestException(errorMessage, allFieldErrors);
        }
        // Milestone 2/6: Validation PASSED
        log.info("[NEWS-UPDATE] [{}] [2/6] Validation PASSED - allValidationsPassed=true", correlationId);
        String oldMediaFileName = existingNews.getNewsMediaFileName();
        String oldThumbnailUrl = existingNews.getNewsThumbnailUrl();
        String oldCardUrl = existingNews.getNewsImageCardUrl();
        String oldHeroUrl = existingNews.getNewsImageHeroUrl();
        log.debug("[NEWS-UPDATE] [{}] Old media to backup - oldMedia={}, oldThumbnail={}",
                correlationId, oldMediaFileName, oldThumbnailUrl);

        String newFileName;
        ImageProcessingResponseDto imageResponse = null;

        try {
            // Only process new media if provided by user
            if (request.getImageVideoFile() != null) {
                // Read bytes for new media
                byte[] mediaBytes;
                try {
                    mediaBytes = request.getImageVideoFile().getInputStream().readAllBytes();
                } catch (IOException e) {
                    log.error("[NEWS-UPDATE] [{}] Failed to read media bytes - error={}", correlationId, e.getMessage(),
                            e);
                    throw new InvalidRequestException("imageVideoFile", "Failed to read media file: " + e.getMessage());
                }

                // ========================================
                // STEP 1: PRESERVE ORIGINAL FIRST (Data-First Approach)
                // ========================================
                // Save original file IMMEDIATELY before any processing
                // This ensures we never lose uploaded data, even if processing fails
                String uploadedBy = request.getCreatedBy() != null ? request.getCreatedBy().toString() : "system";
                MediaUpload mediaUpload = newsMediaStorageService.preserveOriginal(
                        mediaBytes,
                        request.getImageVideoFile().getOriginalFilename(),
                        request.getImageVideoFile().getContentType(),
                        request.getImageVideoFile().getSize(),
                        correlationId,
                        uploadedBy);
                log.info("[NEWS-UPDATE] [{}] [2.5/6] Original PRESERVED - uploadId={}, path='{}'",
                        correlationId, mediaUpload.getUploadId(), mediaUpload.getOriginalPath());

                // ========================================
                // STEP 2: Process images and generate variants
                // ========================================
                // Process images directly - no raw file writing
                imageResponse = processImages(request, mediaBytes);
                // Milestone 3/5: Images generated
                log.info("[NEWS-UPDATE] [{}] [3/5] Images generated - strategy={}, thumbnailFilename='{}'",
                        correlationId, imageResponse.getSource(), imageResponse.getThumbnail().getFilename());

                // Set new filename from processed response
                newFileName = imageResponse.getMain().getFilename();
            } else {
                // User didn't upload new media - keep existing
                newFileName = oldMediaFileName;
                log.debug("[NEWS-UPDATE] [{}] No new media provided - keeping existing media={}", correlationId,
                        oldMediaFileName);
            }

            // Capture old status BEFORE updating entity
            WorkflowStatus oldStatus = existingNews.getNewsWorkflowStatus();
            UUID oldCategoryId = existingNews.getNewsNewsCategoryId();

            updateNewsEntity(existingNews, request, newFileName, null);
            // Set newsScheduledBy if status is SCHEDULED
            if (existingNews.getNewsWorkflowStatus() == WorkflowStatus.SCHEDULED) {
                existingNews.setNewsScheduledBy(request.getCreatedBy());
            }

            // Set new image filenames (not full URLs)
            if (imageResponse != null) {
                existingNews.setNewsThumbnailUrl(imageResponse.getThumbnail().getFilename());
                existingNews.setNewsImageCardUrl(imageResponse.getCard().getFilename());
                existingNews.setNewsImageHeroUrl(imageResponse.getHero().getFilename());
                // Ensure main image filename in DB is the processed/generated filename
                existingNews.setNewsMediaFileName(imageResponse.getMain().getFilename());
                existingNews.setNewsMediaFileUrl(imageResponse.getMain().getFilename());
            }

            NewsMasterEntity updated = newsRepository.save(existingNews);
            // Milestone 4/5: Entity saved
            log.info("[NEWS-UPDATE] [{}] [4/5] Entity saved - newsId={}, status={}",
                    correlationId, updated.getNewsNewsId(), updated.getNewsWorkflowStatus());

            // Handle status change to PUBLISHED - set publishedBy, send notifications, and
            // create sharing records
            WorkflowStatus newStatus = updated.getNewsWorkflowStatus();
            if (newStatus == WorkflowStatus.PUBLISHED && !oldStatus.equals(WorkflowStatus.PUBLISHED)) {
                // Status changed TO PUBLISHED
                updated.setNewsPublishedBy(request.getCreatedBy());
                sendPublishNotification(updated);
                newsRepository.save(updated); // Save notification tracking fields

                // Create social sharing record for newly published news
                socialMediaShareService.ensureSharingRecordExists(updated.getNewsNewsId());

                log.info("[NEWS-UPDATE] [{}] [4.5/5] Push notification sent for status change to PUBLISHED - newsId={}",
                        correlationId, updated.getNewsNewsId());
            }

            // Archive main media
            archiveOldMediaIfUnused(
                    oldMediaFileName,
                    imageResponse != null ? imageResponse.getMain().getFilename() : null,
                    "main",
                    updated.getNewsNewsId(),
                    this::extractFilenameFromUrl,
                    (filename, excludeId) -> newsRepository.countByMediaFileNameExcludingNewsId(filename, excludeId),
                    filename -> newsMediaStorageService.moveMediaVariantToBackup(filename, "main"),
                    correlationId);

            // Archive thumbnail
            archiveOldMediaIfUnused(
                    oldThumbnailUrl,
                    imageResponse != null ? imageResponse.getThumbnail().getFilename() : null,
                    "thumbnail",
                    updated.getNewsNewsId(),
                    this::extractFilenameFromUrl,
                    (filename, excludeId) -> newsRepository.countByThumbnailUrlExcludingNewsId(filename, excludeId),
                    filename -> newsMediaStorageService.moveMediaVariantToBackup(filename, "thumbnail"),
                    correlationId);

            // Archive card image
            archiveOldMediaIfUnused(
                    oldCardUrl,
                    imageResponse != null ? imageResponse.getCard().getFilename() : null,
                    "card",
                    updated.getNewsNewsId(),
                    this::extractFilenameFromUrl,
                    (filename, excludeId) -> newsRepository.countByCardUrlExcludingNewsId(filename, excludeId),
                    filename -> newsMediaStorageService.moveMediaVariantToBackup(filename, "card"),
                    correlationId);

            // Archive hero image
            archiveOldMediaIfUnused(
                    oldHeroUrl,
                    imageResponse != null ? imageResponse.getHero().getFilename() : null,
                    "hero",
                    updated.getNewsNewsId(),
                    this::extractFilenameFromUrl,
                    (filename, excludeId) -> newsRepository.countByHeroUrlExcludingNewsId(filename, excludeId),
                    filename -> newsMediaStorageService.moveMediaVariantToBackup(filename, "hero"),
                    correlationId);

            logAuditAction(updated.getNewsNewsId(), "UPDATE", "News updated", updated.getUpdatedBy());

            // Audit category changes
            UUID newCategoryId = updated.getNewsNewsCategoryId();
            if (!Objects.equals(oldCategoryId, newCategoryId)) {
                if (oldCategoryId != null) {
                    logAuditAction(updated.getNewsNewsId(), "CATEGORY_REMOVED",
                            "News removed from category: " + oldCategoryId, updated.getUpdatedBy());
                }
                if (newCategoryId != null) {
                    logAuditAction(updated.getNewsNewsId(), "CATEGORY_ASSIGNED",
                            "News assigned to new category: " + newCategoryId, updated.getUpdatedBy());
                }
            }

            // Auto-reindex to Elasticsearch (non-critical - failure doesn't block news
            // update)
            try {
                newsElasticSearchService.ifPresent(service -> service.indexNewsArticle(updated.getNewsNewsId()));
                log.debug("[NEWS-UPDATE] [{}] Reindexed in Elasticsearch - newsId={}", correlationId,
                        updated.getNewsNewsId());
            } catch (Exception esError) {
                log.warn("[NEWS-UPDATE] [{}] Failed to reindex in Elasticsearch (non-critical) - newsId={}, error={}",
                        correlationId, updated.getNewsNewsId(), esError.getMessage());
                // Continue anyway - ES indexing is not critical for news update
            }

            NewsCreateResponseDto response = mapToResponse(updated);

            // Milestone 5/5: COMPLETE
            long duration = System.currentTimeMillis() - startTime;
            log.info("[NEWS-UPDATE] [{}] [5/5] COMPLETE - newsId={}, oldMediaBackedUp={}, duration={}ms",
                    correlationId, updated.getNewsNewsId(), oldMediaFileName != null, duration);

            return response;
        } catch (DataIntegrityViolationException e) {
            // Let database constraint violations bubble up to GlobalExceptionHandler
            throw e;
        } catch (InvalidRequestException e) {
            // Let validation exceptions bubble up directly (they're already properly
            // formatted)
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[NEWS-UPDATE] [{}] FAILED - newsId={}, duration={}ms, error={}",
                    correlationId, id, duration, e.getMessage(), e);
            throw new InvalidRequestException("news", "Failed to update news: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.NEWS_BY_SLUG_CACHE,
            NewsCacheConstants.PUBLISHED_NEWS_CACHE, NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public void softDeleteNews(String id, UUID deletedBy) {
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // Milestone 1/4: START
        log.info("[NEWS-DELETE] [{}] [1/4] START - newsId={}, deletedBy={}", correlationId, id, deletedBy);

        NewsMasterEntity existing = findNewsByIdOrThrow(id);
        String newsTitle = existing.getNewsTitleEn();
        // Milestone 2/3: Entity found
        log.info("[NEWS-DELETE] [{}] [2/3] Entity found - title='{}', status={}",
                correlationId, newsTitle, existing.getNewsWorkflowStatus());

        try {
            // STEP 1: Soft delete - mark as deleted, preserve files for potential restore
            // Files are NOT moved during soft delete - only during updateNews (media
            // replaced) or permanentDelete
            existing.softDelete(deletedBy);
            NewsMasterEntity softDeleted = newsRepository.save(existing);
            // Milestone 3/4: Soft delete persisted
            log.info("[NEWS-DELETE] [{}] [3/4] Soft deleted - deletedAt={}, deletedBy={}",
                    correlationId, softDeleted.getDeletedAt(), deletedBy);

            // STEP 2: Audit logging
            logAuditAction(softDeleted.getNewsNewsId(), "DELETE",
                    "News soft deleted (can be restored)", deletedBy);

            // Milestone 4/4: COMPLETE
            long duration = System.currentTimeMillis() - startTime;
            log.info("[NEWS-DELETE] [{}] [4/4] COMPLETE - newsId={}, title='{}', filesPreserved=true, duration={}ms",
                    correlationId, id, newsTitle, duration);
        } catch (DataIntegrityViolationException e) {
            // Let database constraint violations bubble up to GlobalExceptionHandler
            throw e;
        } catch (InvalidRequestException e) {
            // Let validation exceptions bubble up directly (they're already properly
            // formatted)
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[NEWS-DELETE] [{}] FAILED - newsId={}, duration={}ms, error={}",
                    correlationId, id, duration, e.getMessage(), e);
            throw new InvalidRequestException("news", "Failed to delete news: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.NEWS_BY_SLUG_CACHE,
            NewsCacheConstants.PUBLISHED_NEWS_CACHE, NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public void permanentDelete(String id) {
        log.info("Permanently deleting newsapp - newsId={}", id);
        NewsMasterEntity existing = findNewsByIdOrThrow(id);
        String newsTitle = existing.getNewsTitleEn();
        UUID newsId = existing.getNewsNewsId();
        try {
            newsRepository.hardDeleteById(newsId);
            newsRepository.flush();
            log.debug("DB record deleted - newsId={}", id);
            logAuditAction(newsId, "PERMANENT_DELETE", "News permanently deleted (hard delete): " + newsTitle,
                    existing.getCreatedBy());
        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (InvalidRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete news from database - newsId={}, error={}", id, e.getMessage(), e);
            throw new InvalidRequestException("news", "Failed to permanently delete news: " + e.getMessage());
        }
        backupUnusedMediaFiles(existing, newsId);
        log.info("News permanently deleted - newsId={}, title='{}'", id, newsTitle);
    }

    /**
     * Extracts filename from a URL path.
     * Delegates to MediaUrlService for consistent URL parsing.
     *
     * @param url the URL to extract filename from
     * @return the extracted filename or null if URL is invalid
     */
    private String extractFilenameFromUrl(String url) {
        return mediaUrlService.extractFilename(url);
    }

    @Override
    @Transactional
    public NewsCreateResponseDto saveDraftNews(NewsCreateRequestDto request) {
        String titleEn = request.getNewsTitleEn();
        boolean hasCustomThumbnail = request.getThumbnailFile() != null && !request.getThumbnailFile().isEmpty();
        log.info("Saving draft newsapp - title='{}', hasCustomThumbnail={}", titleEn, hasCustomThumbnail);

        // Read bytes once for reuse
        byte[] mediaBytes = null;
        if (request.getImageVideoFile() != null) {
            try {
                mediaBytes = request.getImageVideoFile().getInputStream().readAllBytes();
            } catch (IOException e) {
                log.error("Failed to read media bytes for draft - error={}", e.getMessage(), e);
                throw new InvalidRequestException("imageVideoFile", "Failed to read media file: " + e.getMessage());
            }
        }

        try {
            NewsMasterEntity entity;
            if (mediaBytes != null) {
                // Process images directly without writing raw file
                ImageProcessingResponseDto imageResponse = processImages(request, mediaBytes);

                entity = buildNewsEntity(request);
                entity.setNewsWorkflowStatus(WorkflowStatus.DRAFT);
                entity.setNewsIsActive(true);

                // Set image filenames (not full URLs) from the generated/uploaded images
                if (imageResponse != null) {
                    entity.setNewsThumbnailUrl(imageResponse.getThumbnail().getFilename());
                    entity.setNewsImageCardUrl(imageResponse.getCard().getFilename());
                    entity.setNewsImageHeroUrl(imageResponse.getHero().getFilename());
                    entity.setNewsMediaFileName(imageResponse.getMain().getFilename());
                    entity.setNewsMediaFileUrl(imageResponse.getMain().getFilename());
                }
            } else {
                entity = buildNewsEntity(request);
                entity.setNewsWorkflowStatus(WorkflowStatus.DRAFT);
                entity.setNewsIsActive(true);
            }

            NewsMasterEntity saved = newsRepository.save(entity);
            logAuditAction(saved.getNewsNewsId(), "DRAFT_CREATE", "Draft newsapp created", saved.getCreatedBy());
            log.info("Draft newsapp saved - newsId={}, title='{}', mediaFile={}",
                    saved.getNewsNewsId(), saved.getNewsTitleEn(), saved.getNewsMediaFileName());

            return mapToResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // Let database constraint violations bubble up to GlobalExceptionHandler
            throw e;
        } catch (InvalidRequestException e) {
            // Let validation exceptions bubble up directly (they're already properly
            // formatted)
            throw e;
        } catch (Exception e) {
            log.error("Failed to save draft news - title='{}', error={}", titleEn, e.getMessage(), e);
            throw new InvalidRequestException("news", "Failed to save draft news: " + e.getMessage());
        }
    }

    // =========================
    // Media Operations
    // =========================

    @Override
    public NewsMediaFileRequestDto getMediaFile(String filename) {
        log.debug("Fetching media file - filename={}", filename);
        File file = new File(newsMediaStorageService.resolveFilePath(filename));

        if (!file.exists()) {
            log.warn("Media file not found - filename={}", filename);
            throw new ResourceNotFoundException("Media file", "filename", filename);
        }

        try {
            Resource resource = new UrlResource(file.toURI());
            String contentType = Files.probeContentType(file.toPath());

            NewsMediaFileRequestDto mediaRequest = new NewsMediaFileRequestDto();
            mediaRequest.setResource(resource);
            mediaRequest.setContentType(contentType);
            log.debug("Media file fetched - filename={}, contentType={}", filename, contentType);

            return mediaRequest;
        } catch (IOException e) {
            log.error("Cannot read file - filename={}, error={}", filename, e.getMessage());
            throw new InvalidRequestException("filename", "Cannot read file: " + filename);
        }
    }

    @Transactional
    public NewsMediaUploadResponseDto uploadMediaFile(MultipartFile file) {
        log.info("Uploading media file - originalFilename={}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new InvalidRequestException("file", "File is empty");
        }

        String filename = newsMediaStorageService.generateMediaFilename(file);
        String filePath = newsMediaStorageService.resolveFilePath(filename);
        File targetFile = new File(filePath);

        try {
            newsMediaStorageService.ensureMediaFolderExists();
            file.transferTo(targetFile);

            String relativePath = "/api/v1/public/news/media/" + filename;
            String fullUrl = mediaUrlService.buildMediaUrl(filename);

            log.info("Media file uploaded - filename={}, size={}", filename, file.getSize());

            return NewsMediaUploadResponseDto.builder()
                    .filename(filename)
                    .originalFilename(file.getOriginalFilename())
                    .url(fullUrl)
                    .relativePath(relativePath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {
            newsMediaStorageService.cleanupFile(targetFile);
            log.error("Failed to upload media file - originalFilename={}, error={}",
                    file.getOriginalFilename(), e.getMessage());
            throw new InvalidRequestException("file", "Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteMediaFileByName(String filename) {
        log.info("Deleting media file - filename={}", filename);

        if (filename == null || filename.isEmpty()) {
            throw new InvalidRequestException("filename", "Filename cannot be empty");
        }

        File file = new File(newsMediaStorageService.resolveFilePath(filename));

        if (!file.exists()) {
            log.warn("Media file does not exist - filename={}", filename);
            return false;
        }

        if (file.delete()) {
            log.info("Media file deleted - filename={}", filename);
            return true;
        } else {
            log.error("Failed to delete media file - filename={}", filename);
            throw new InvalidRequestException("filename", "Failed to delete file: " + filename);
        }
    }

    // =========================
    // Thumbnail Operations
    // =========================

    @Override
    public NewsMediaFileRequestDto getThumbnailFile(String filename) {
        log.debug("Fetching thumbnail file - filename={}", filename);

        String thumbnailPath = newsImageProcessingService.getImagePath(filename, "thumbnail");
        File file = new File(thumbnailPath);

        if (!file.exists()) {
            log.warn("Thumbnail file not found - filename={}", filename);
            throw new ResourceNotFoundException("Thumbnail file", "filename", filename);
        }

        try {
            Resource resource = new UrlResource(file.toURI());
            String contentType = Files.probeContentType(file.toPath());

            NewsMediaFileRequestDto mediaRequest = new NewsMediaFileRequestDto();
            mediaRequest.setResource(resource);
            mediaRequest.setContentType(contentType != null ? contentType : "image/jpeg");

            log.debug("Thumbnail file fetched - filename={}", filename);
            return mediaRequest;
        } catch (IOException e) {
            log.error("Cannot read thumbnail file - filename={}, error={}", filename, e.getMessage());
            throw new InvalidRequestException("filename", "Cannot read thumbnail file: " + filename);
        }
    }

    @Override
    public NewsMediaFileRequestDto getCardImageFile(String filename) {
        log.debug("Fetching card image file - filename={}", filename);

        String imagePath = newsImageProcessingService.getImagePath(filename, "card");
        File file = new File(imagePath);

        if (!file.exists()) {
            log.warn("Card image file not found - filename={}", filename);
            throw new ResourceNotFoundException("Card image file", "filename", filename);
        }

        try {
            Resource resource = new UrlResource(file.toURI());
            String contentType = Files.probeContentType(file.toPath());

            NewsMediaFileRequestDto mediaRequest = new NewsMediaFileRequestDto();
            mediaRequest.setResource(resource);
            mediaRequest.setContentType(contentType != null ? contentType : "image/jpeg");

            log.debug("Card image file fetched - filename={}", filename);
            return mediaRequest;
        } catch (IOException e) {
            log.error("Cannot read card image file - filename={}, error={}", filename, e.getMessage());
            throw new InvalidRequestException("filename", "Cannot read card image file: " + filename);
        }
    }

    @Override
    public NewsMediaFileRequestDto getHeroImageFile(String filename) {
        log.debug("Fetching hero image file - filename={}", filename);

        String imagePath = newsImageProcessingService.getImagePath(filename, "hero");
        File file = new File(imagePath);

        if (!file.exists()) {
            log.warn("Hero image file not found - filename={}", filename);
            throw new ResourceNotFoundException("Hero image file", "filename", filename);
        }

        try {
            Resource resource = new UrlResource(file.toURI());
            String contentType = Files.probeContentType(file.toPath());

            NewsMediaFileRequestDto mediaRequest = new NewsMediaFileRequestDto();
            mediaRequest.setResource(resource);
            mediaRequest.setContentType(contentType != null ? contentType : "image/jpeg");

            log.debug("Hero image file fetched - filename={}", filename);
            return mediaRequest;
        } catch (IOException e) {
            log.error("Cannot read hero image file - filename={}, error={}", filename, e.getMessage());
            throw new InvalidRequestException("filename", "Cannot read hero image file: " + filename);
        }
    }

    @Override
    @Transactional
    public ThumbnailResponseDto uploadThumbnail(MultipartFile file, String originalMediaName) throws IOException {
        log.info("Uploading custom thumbnail - originalMediaName={}", originalMediaName);

        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("file", "Thumbnail file is required");
        }

        if (!newsImageProcessingService.isImage(file.getContentType())) {
            throw new InvalidRequestException("file", "Thumbnail must be an image file");
        }

        // Generate all sizes from custom thumbnail and return thumbnail variant
        ImageProcessingResponseDto response = newsImageProcessingService.processImage(file, originalMediaName);
        return convertImageVariantToThumbnailResponse(response.getThumbnail(), originalMediaName,
                ThumbnailResponseDto.ThumbnailSource.CUSTOM_UPLOAD);
    }

    @Override
    @Transactional
    public ThumbnailResponseDto generateThumbnail(MultipartFile mediaFile) throws IOException {
        log.info("Generating thumbnail from media - filename={}", mediaFile.getOriginalFilename());

        if (mediaFile == null || mediaFile.isEmpty()) {
            throw new InvalidRequestException("file", "Media file is required");
        }

        String contentType = mediaFile.getContentType();

        if (newsImageProcessingService.isImage(contentType)) {
            ImageProcessingResponseDto response = newsImageProcessingService.processImage(mediaFile, "generated");
            return convertImageVariantToThumbnailResponse(response.getThumbnail(), "generated",
                    ThumbnailResponseDto.ThumbnailSource.IMAGE_RESIZE);
        } else if (newsImageProcessingService.isVideo(contentType)) {
            ImageProcessingResponseDto response = newsImageProcessingService.processVideo(mediaFile, "generated");
            return convertImageVariantToThumbnailResponse(response.getThumbnail(), "generated",
                    ThumbnailResponseDto.ThumbnailSource.VIDEO_FRAME);
        } else {
            throw new InvalidRequestException("file",
                    "Unsupported media type for thumbnail generation: " + contentType);
        }
    }

    @Override
    @Transactional
    public ThumbnailResponseDto generateThumbnailFromPath(String mediaFilePath, String originalFilename)
            throws IOException {
        log.info("Generating thumbnail from saved media - path={}", mediaFilePath);

        if (mediaFilePath == null || mediaFilePath.isEmpty()) {
            throw new InvalidRequestException("mediaFilePath", "Media file path is required");
        }

        Path mediaPath = Paths.get(mediaFilePath);
        if (!Files.exists(mediaPath)) {
            throw new ResourceNotFoundException("Media file", "path", mediaFilePath);
        }

        String contentType = Files.probeContentType(mediaPath);
        MultipartFile tempFile = createMultipartFileFromPath(mediaPath, originalFilename, contentType);

        ImageProcessingResponseDto response;
        if (newsImageProcessingService.isImage(contentType)) {
            response = newsImageProcessingService.processImage(tempFile, originalFilename);
        } else if (newsImageProcessingService.isVideo(contentType)) {
            response = newsImageProcessingService.processVideo(tempFile, originalFilename);
        } else {
            throw new InvalidRequestException("mediaFile",
                    "Unsupported media type for thumbnail generation: " + contentType);
        }

        return convertImageVariantToThumbnailResponse(response.getThumbnail(), originalFilename,
                newsImageProcessingService.isImage(contentType) ? ThumbnailResponseDto.ThumbnailSource.IMAGE_RESIZE
                        : ThumbnailResponseDto.ThumbnailSource.VIDEO_FRAME);
    }

    @Override
    @Transactional
    public boolean deleteThumbnail(String filename) {
        log.info("Deleting thumbnail - filename={}", filename);

        if (filename == null || filename.isEmpty()) {
            throw new InvalidRequestException("filename", "Thumbnail filename cannot be empty");
        }

        boolean deleted = newsImageProcessingService.deleteImage(filename, "thumb");

        // Audit log - thumbnail deletion
        if (deleted) {
            log.debug("Thumbnail deleted and audit logged - filename={}", filename);
        }

        return deleted;
    }

    // =========================
    // Search & Filter Operations
    // =========================

    @Override
    public Page<NewsCreateResponseDto> searchNews(String query, String categoryId, String fromDate,
            String toDate, String sort, Pageable pageable) {
        log.debug("Searching newsapp - query={}, categoryId={}, fromDate={}, toDate={}",
                query, categoryId, fromDate, toDate);

        // Sanitize search query to prevent XSS in search results
        String sanitizedQuery = query != null ? inputSanitizer.sanitizeHtml(query.trim()) : null;

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(SortUtils.parseSort(sort)));

        Page<NewsMasterEntity> results = executeSearch(sanitizedQuery, categoryId, fromDate, toDate, sortedPageable);
        log.debug("Search completed - resultCount={}", results.getTotalElements());
        return results.map(this::mapToResponse);
    }

    @Override
    public List<NewsCreateResponseDto> getRelatedNews(String newsId, int limit) {
        log.debug("Fetching related newsapp - newsId={}, limit={}, sort=default", newsId, limit);
        return getRelatedNews(newsId, limit, "createdAt,desc");
    }

    @Override
    public List<NewsCreateResponseDto> getRelatedNews(String newsId, int limit, String sort) {
        log.debug("Fetching related newsapp - newsId={}, limit={}", newsId, limit);

        NewsMasterEntity currentNews = findNewsByIdOrThrow(newsId);

        if (currentNews.getNewsNewsCategoryId() == null) {
            log.debug("News has no newscategory, returning empty - newsId={}", newsId);
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by(SortUtils.parseSort(sort)));
        List<NewsMasterEntity> relatedNews = newsRepository.findRelatedNewsByCategory(
                currentNews.getNewsNewsCategoryId(),
                currentNews.getNewsNewsId(),
                pageable);
        log.debug("Found related newsapp - newsId={}, count={}", newsId, relatedNews.size());

        return relatedNews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<NewsCreateResponseDto> getNewsByCategory(String categoryId, Pageable pageable) {
        log.debug("Fetching newsapp by newscategory - categoryId={}", categoryId);
        UUID catId = parseUuid(categoryId);
        return newsRepository.findByNewsNewsCategoryId(catId, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<NewsCreateResponseDto> getNewsByAuthor(String authorId, Pageable pageable) {
        log.debug("Fetching newsapp by author - authorId={}", authorId);
        UUID authorUuid = parseUuid(authorId);
        return newsRepository.findByCreatedBy(authorUuid, pageable).map(this::mapToResponse);
    }

    @Override
    public NewsCreateResponseDto getNewsBySlug(String slug) {
        log.debug("Fetching newsapp by slug - slug={}", slug);
        // Use findActiveBySlug to exclude soft-deleted records for public API
        NewsMasterEntity entity = newsRepository.findActiveBySlug(slug)
                .orElseThrow(() -> new NewsNotFoundException("slug", slug));
        return mapToResponse(entity);
    }

    @Override
    public List<NewsCreateResponseDto> getTrendingNews(int limit) {
        log.debug("Fetching trending newsapp - limit={}", limit);
        return getTrendingNews(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "viewCount"))).getContent();
    }

    @Override
    @Cacheable(value = NewsCacheConstants.TRENDING_NEWS_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<NewsCreateResponseDto> getTrendingNews(Pageable pageable) {
        log.debug("Fetching trending newsapp - page={}, size={}", pageable.getPageNumber(),
                pageable.getPageSize());

        Sort sort = pageable.getSort().isUnsorted()
                ? Sort.by(Sort.Direction.DESC, "viewCount", "likeCount", "shareCount")
                : pageable.getSort();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Context-aware: Admin sees all (including deleted), Public sees only
        // non-deleted
        return newsRepository.findAll(softDeleteContext.getSpec(), sortedPageable).map(this::mapToResponse);
    }

    // =========================
    // Workflow Management
    // =========================

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.NEWS_BY_SLUG_CACHE,
            NewsCacheConstants.PUBLISHED_NEWS_CACHE, NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public void updateWorkflowStatus(String newsId, String newStatus, String userId) {
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // Milestone 1/5: START
        log.info("[WORKFLOW-UPDATE] [{}] [1/5] START - newsId={}, newStatus={}, userId={}",
                correlationId, newsId, newStatus, userId);

        AdminUser user = adminUserRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        NewsMasterEntity news = findNewsByIdOrThrow(newsId);
        log.debug("[WORKFLOW-UPDATE] [{}] [DB LOAD] newsId={}, version={}, status={}", correlationId,
                news.getNewsNewsId(), news.getNewsLockVersion(), news.getNewsWorkflowStatus());
        WorkflowStatus oldStatus = news.getNewsWorkflowStatus();
        WorkflowStatus targetStatus = parseWorkflowStatus(newStatus);
        // Milestone 2/5: Validation
        log.info("[WORKFLOW-UPDATE] [{}] [2/5] Validated - oldStatus={}, targetStatus={}, userRole={}",
                correlationId, oldStatus, targetStatus, user.getRole() != null ? user.getRole().getRoleName() : "NONE");

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : null;
        validateWorkflowTransition(roleName, targetStatus, userId);

        news.setNewsWorkflowStatus(targetStatus);
        news.setUpdatedBy(parseUuid(userId));
        news.setUpdatedAt(Instant.now());

        String pushNotificationType = "NONE";

        // --- Industry Standard Field Clearing Logic ---
        if (targetStatus == WorkflowStatus.DRAFT || targetStatus == WorkflowStatus.PENDING_APPROVAL) {
            // Clear all scheduling and publishing fields
            news.setNewsScheduledBy(null);
            news.setNewsScheduledPublishAt(null);
            news.setNewsPublishedBy(null);
            news.setNewsPublishedAt(null);
            logAuditAction(news.getNewsNewsId(), "WORKFLOW_UPDATE",
                    "Workflow status changed to " + targetStatus + ", all scheduling/publishing fields cleared",
                    parseUuid(userId));
        } else if (targetStatus == WorkflowStatus.SCHEDULED) {
            // Set scheduling fields, clear publishing fields
            news.setNewsPublishedBy(null);
            news.setNewsPublishedAt(null);
            // scheduledBy and scheduledPublishAt should be set by the scheduling API, not
            // here
            logAuditAction(news.getNewsNewsId(), "WORKFLOW_UPDATE",
                    "Workflow status changed to SCHEDULED, publishing fields cleared", parseUuid(userId));
        } else if (targetStatus == WorkflowStatus.PUBLISHED) {
            // Set publishing fields, DO NOT clear scheduling fields (retain for
            // audit/history)
            news.setNewsPublishedAt(Instant.now());
            news.setNewsPublishedBy(parseUuid(userId));
            sendPublishNotification(news);
            pushNotificationType = Boolean.TRUE.equals(news.getNewsIsBreaking()) ? "BREAKING"
                    : Boolean.TRUE.equals(news.getNewsIsFeatured()) ? "FEATURED" : "NEWS_UPDATE";
            log.info("[WORKFLOW-UPDATE] [{}] [3/5] Push notification sent - type={}", correlationId,
                    pushNotificationType);
            logAuditAction(news.getNewsNewsId(), "PUBLISH",
                    "News published (status changed to PUBLISHED, scheduling fields retained)",
                    parseUuid(userId));
        } else {
            // For all other workflow changes, log generic workflow update
            logAuditAction(news.getNewsNewsId(), "WORKFLOW_UPDATE",
                    "Workflow status changed to " + newStatus, parseUuid(userId));
        }

        NewsMasterEntity saved = newsRepository.save(news);
        log.info("[WORKFLOW-UPDATE] [{}] [4/5] Entity saved - newsId={}, status={}, version={}",
                correlationId, saved.getNewsNewsId(), targetStatus, saved.getNewsLockVersion());

        // Milestone 5/5: COMPLETE
        long duration = System.currentTimeMillis() - startTime;
        log.info(
                "[WORKFLOW-UPDATE] [{}] [5/5] COMPLETE - newsId={}, oldStatus={}, newStatus={}, pushType={}, duration={}ms",
                correlationId, newsId, oldStatus, targetStatus, pushNotificationType, duration);
    }

    @Override
    public Page<NewsCreateResponseDto> getNewsByWorkflowStatus(String workflowStatus, Pageable pageable) {
        log.debug("Fetching newsapp by workflow status - status={}", workflowStatus);
        WorkflowStatus status = parseWorkflowStatusFromRequest(workflowStatus);
        return newsRepository.findByNewsWorkflowStatus(status, pageable).map(this::mapToResponse);
    }

    @Override
    public List<String> getAvailableWorkflowStatuses() {
        log.debug("Fetching available workflow status options");
        List<String> statuses = java.util.Arrays.stream(NewsMasterEntity.WorkflowStatus.values())
                .map(Enum::name)
                .toList();
        log.debug("Available workflow statuses: {}", statuses);
        return statuses;
    }

    @Override
    @Cacheable(value = NewsCacheConstants.PUBLISHED_NEWS_CACHE, key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<NewsCreateResponseDto> getPublishedNews(Pageable pageable) {
        log.debug("Fetching published newsapp - page={}, size={}", pageable.getPageNumber(),
                pageable.getPageSize());
        return newsRepository.findByNewsWorkflowStatus(PUBLISHED_STATUS, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.NEWS_BY_SLUG_CACHE,
            NewsCacheConstants.PUBLISHED_NEWS_CACHE, NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public NewsCreateResponseDto schedulePublish(String id, String publishDateTime, UUID scheduledBy) {
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // Milestone 1/5: START
        log.info("[SCHEDULE-PUBLISH] [{}] [1/5] START - newsId={}, scheduledAt={}",
                correlationId, id, publishDateTime);

        // ========================================
        // STEP 1: Input Validation
        // ========================================
        if (publishDateTime == null || publishDateTime.trim().isEmpty()) {
            log.warn("[SCHEDULE-PUBLISH] [{}] Validation FAILED - publishDateTime is null/empty", correlationId);
            throw new InvalidRequestException("publishDateTime", "Scheduled publish date/time is required");
        }

        // Parse and validate date/time format
        Instant scheduledTime;
        try {
            scheduledTime = Instant.parse(publishDateTime.trim());
            // Milestone 2/5: Date parsing successful
            log.info("[SCHEDULE-PUBLISH] [{}] [2/5] Date parsed - scheduledTime={}",
                    correlationId, scheduledTime);
        } catch (Exception e) {
            log.warn("[SCHEDULE-PUBLISH] [{}] Date parsing FAILED - invalidFormat='{}', error={}",
                    correlationId, publishDateTime, e.getMessage());
            throw new InvalidRequestException("publishDateTime",
                    "Invalid date/time format. Expected ISO 8601 format (e.g., 2026-02-10T09:00:00Z)");
        }

        // ========================================
        // STEP 2: Business Logic Validation
        // ========================================
        Instant now = Instant.now();

        // Validate not in the past
        if (scheduledTime.isBefore(now)) {
            log.warn("[SCHEDULE-PUBLISH] [{}] Validation FAILED - past date not allowed, scheduledTime={}, now={}",
                    correlationId, scheduledTime, now);
            throw new InvalidRequestException("publishDateTime",
                    "Cannot schedule publication in the past. Scheduled time must be in the future");
        }

        // Validate reasonable future limit (1 year max)
        Instant oneYearFromNow = now.plus(365, ChronoUnit.DAYS);
        if (scheduledTime.isAfter(oneYearFromNow)) {
            log.warn("[SCHEDULE-PUBLISH] [{}] Validation FAILED - too far future, scheduledTime={}, limit={}",
                    correlationId, scheduledTime, oneYearFromNow);
            throw new InvalidRequestException("publishDateTime",
                    "Cannot schedule publication more than 1 year in the future");
        }

        // Find and validate news entity
        NewsMasterEntity news = findNewsByIdOrThrow(id);
        WorkflowStatus currentStatus = news.getNewsWorkflowStatus();

        // Validate workflow status - cannot schedule already published articles
        if (WorkflowStatus.PUBLISHED == currentStatus) {
            log.warn("[SCHEDULE-PUBLISH] [{}] Validation FAILED - already published, newsId={}, status={}",
                    correlationId, id, currentStatus);
            throw new InvalidRequestException("news",
                    "Cannot schedule publication for already published articles");
        }

        // Check if already scheduled (log warning but allow rescheduling)
        boolean isReschedule = WorkflowStatus.SCHEDULED == currentStatus;
        Instant oldScheduledTime = news.getNewsScheduledPublishAt(); // Always capture before update
        if (isReschedule) {
            log.warn("[SCHEDULE-PUBLISH] [{}] Rescheduling - oldTime={}, newTime={}",
                    correlationId, oldScheduledTime, scheduledTime);
        }

        // Milestone 3/5: All validations passed
        log.info("[SCHEDULE-PUBLISH] [{}] [3/5] Validation PASSED - currentStatus={}, isReschedule={}",
                correlationId, currentStatus, isReschedule);

        // ========================================
        // STEP 3: Update Entity
        // ========================================
        // IMPORTANT: Always reload the entity from the DB before each update to avoid
        // optimistic locking errors.
        news.setNewsScheduledPublishAt(scheduledTime);
        news.setNewsWorkflowStatus(WorkflowStatus.SCHEDULED);
        news.setUpdatedAt(Instant.now());
        news.setNewsScheduledBy(scheduledBy);
        news.setUpdatedBy(scheduledBy);

        NewsMasterEntity saved = newsRepository.save(news);
        // Milestone 4/5: Entity updated
        log.info("[SCHEDULE-PUBLISH] [{}] [4/5] Entity updated - newsId={}, newStatus={}, scheduledAt={}",
                correlationId, saved.getNewsNewsId(), saved.getNewsWorkflowStatus(), saved.getNewsScheduledPublishAt());

        // ========================================
        // STEP 4: Audit Logging (Consistent)
        // ========================================
        String auditAction = isReschedule ? "RESCHEDULE_PUBLISH" : "SCHEDULE_PUBLISH";
        String auditDetails = isReschedule
                ? String.format("Rescheduled publish from %s to %s", oldScheduledTime, scheduledTime)
                : "Scheduled publish at " + scheduledTime;

        logAuditAction(saved.getNewsNewsId(), auditAction, auditDetails, scheduledBy);

        // ========================================
        // STEP 5: Return Response
        // ========================================
        NewsCreateResponseDto response = mapToResponse(saved);

        // Milestone 5/5: COMPLETE
        long duration = System.currentTimeMillis() - startTime;
        log.info("[SCHEDULE-PUBLISH] [{}] [5/5] COMPLETE - newsId={}, scheduledAt={}, duration={}ms",
                correlationId, saved.getNewsNewsId(), saved.getNewsScheduledPublishAt(), duration);

        return response;
    }

    @Override
    @Transactional
    public void archiveNews(String id, UUID archivedBy) {
        log.info("Archiving newsapp - newsId={}, archivedBy={}", id, archivedBy);

        NewsMasterEntity news = findNewsByIdOrThrow(id);
        String articleTitle = news.getNewsTitleEn(); // Capture title BEFORE archiving

        news.setDeletedAt(Instant.now()); // Mark as deleted/archived
        news.setDeletedBy(archivedBy); // Track who archived it
        news.setUpdatedAt(Instant.now());

        newsRepository.save(news);

        // Lookup admin email for audit trail
        String actorDisplayName = archivedBy.toString(); // Default to UUID
        AdminUser admin = adminUserRepository.findById(archivedBy).orElse(null);
        if (admin != null && admin.getAdminUsersEmail() != null) {
            actorDisplayName = admin.getAdminUsersEmail();
        }

        // Log with enriched context (title + actor display name)
        logAuditActionEnriched(news.getNewsNewsId(), "ARCHIVE",
                "News archived (hidden from public view, can be restored)",
                archivedBy,
                actorDisplayName,
                articleTitle);
        log.info("News archived successfully - newsId={}", id);
    }

    @Override
    @Transactional
    public void unarchiveNews(String id, UUID unarchivedBy) {
        log.info("Unarchiving newsapp - newsId={}, unarchivedBy={}", id, unarchivedBy);

        NewsMasterEntity news = findNewsByIdOrThrow(id);
        String articleTitle = news.getNewsTitleEn(); // Capture title BEFORE unarchiving

        news.setDeletedAt(null); // Clear deletion mark to make visible again
        news.setDeletedBy(null);
        news.setUpdatedAt(Instant.now());

        newsRepository.save(news);

        // Lookup admin email for audit trail
        String actorDisplayName = unarchivedBy.toString(); // Default to UUID
        AdminUser admin = adminUserRepository.findById(unarchivedBy).orElse(null);
        if (admin != null && admin.getAdminUsersEmail() != null) {
            actorDisplayName = admin.getAdminUsersEmail();
        }

        // Log with enriched context (title + actor display name)
        logAuditActionEnriched(news.getNewsNewsId(), "UNARCHIVE",
                "News unarchived and made visible again",
                unarchivedBy,
                actorDisplayName,
                articleTitle);
        log.info("News unarchived successfully - newsId={}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.NEWS_BY_SLUG_CACHE,
            NewsCacheConstants.PUBLISHED_NEWS_CACHE, NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public void restoreNews(String id) {
        log.info("Restoring newsapp - newsId={}", id);

        // Filter is disabled for admindashboard endpoints, so regular findById works
        // for deleted records
        NewsMasterEntity news = findNewsByIdOrThrow(id);

        news.restore(); // Clears deletedAt and deletedBy
        news.setNewsIsActive(true);

        newsRepository.save(news);
        logAuditAction(news.getNewsNewsId(), "RESTORE", "News restored from soft delete", news.getUpdatedBy());
        log.info("News restored - newsId={}", id);
    }

    @Override
    @Transactional
    public void pinNews(String id, boolean featured) {
        log.info("{} newsapp - newsId={}", featured ? "Pinning" : "Unpinning", id);

        NewsMasterEntity news = findNewsByIdOrThrow(id);
        news.setNewsIsFeatured(featured);

        newsRepository.save(news);
        logAuditAction(news.getNewsNewsId(), featured ? "PIN" : "UNPIN",
                featured ? "News pinned" : "News unpinned", news.getUpdatedBy());
        log.info("News {} - newsId={}", featured ? "pinned" : "unpinned", id);
    }

    // =========================
    // Bulk Operations
    // =========================

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.PUBLISHED_NEWS_CACHE,
            NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public void bulkPublish(List<String> ids) {
        log.info("Bulk publishing newsapp - count={}", ids.size());

        Instant now = Instant.now();

        List<NewsMasterEntity> newsItems = ids.stream()
                .map(this::findNewsByIdOrThrow)
                .toList();

        for (NewsMasterEntity news : newsItems) {
            news.setNewsWorkflowStatus(WorkflowStatus.PUBLISHED);
            news.setNewsPublishedAt(now);
            news.setUpdatedAt(now);

            // Send push notification on publish (Option B - industry best practice)
            sendPublishNotification(news);

            // Audit log - bulk publish operation
            logAuditAction(news.getNewsNewsId(), "BULK_PUBLISH", "News published in bulk operation",
                    news.getUpdatedBy());
        }

        newsRepository.saveAll(newsItems);
        log.info("Bulk publish completed - count={}", ids.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = { NewsCacheConstants.NEWS_CACHE, NewsCacheConstants.PUBLISHED_NEWS_CACHE,
            NewsCacheConstants.TRENDING_NEWS_CACHE }, allEntries = true)
    public void bulkSoftDeleteNews(List<String> ids, UUID deletedBy) {
        log.info("Bulk soft deleting newsapp - count={}, deletedBy={}", ids.size(), deletedBy);

        List<UUID> uuids = ids.stream().map(this::parseUuid).toList();
        List<NewsMasterEntity> newsItems = newsRepository.findAllById(uuids);

        // Soft delete - files are preserved for potential restore
        // Files will only be moved to backup during permanentDelete
        for (NewsMasterEntity news : newsItems) {
            logAuditAction(news.getNewsNewsId(), "BULK_DELETE", "Bulk soft delete", deletedBy);
            log.debug("Soft deleting newsapp in bulk - newsId={}, title='{}'", news.getNewsNewsId(),
                    news.getNewsTitleEn());
        }

        newsRepository.deleteAllById(uuids);
        log.info("Bulk soft delete completed - count={}, deletedBy={}", ids.size(), deletedBy);
    }

    // =========================
    // Analytics & Statistics
    // =========================

    @Override
    public List<NewsCreateResponseDto> getVersionHistory(String id) {
        log.debug("Getting version history - newsId={}", id);
        // Version history is not yet implemented - return empty list
        findNewsByIdOrThrow(id); // Validate the newsapp exists
        return List.of();
    }

    @Override
    public List<String> getAuditLog(String id) {
        log.debug("Getting audit log - newsId={}", id);
        UUID newsId = parseUuid(id);
        List<NewsAuditLog> logs = newsAuditLogService.findByNewsId(newsId);
        log.debug("Found audit log entries - newsId={}, count={}", id, logs.size());
        return logs.stream()
                .map(l -> String.format("[%s] %s: %s (by %s)",
                        l.getCreatedAt(), l.getAction(), l.getDetails(), l.getActorId()))
                .toList();
    }

    @Override
    public Object getAnalytics(String id) {
        log.debug("Getting analytics - newsId={}", id);
        NewsMasterEntity news = findNewsByIdOrThrow(id);
        Long commentCount = newsCommentService.getCommentCountByNewsId(news.getNewsNewsId());

        return Map.of(
                "newsId", news.getNewsNewsId(),
                "title", news.getNewsTitleEn(),
                "viewCount", news.getNewsViewCount(),
                "likeCount", news.getNewsLikeCount(),
                "shareCount", news.getNewsShareCount(),
                "commentCount", commentCount,
                "isFeatured", news.getNewsIsFeatured(),
                "workflowStatus", news.getNewsWorkflowStatus(),
                "createdAt", news.getCreatedAt(),
                "publishedAt", news.getNewsPublishedAt() != null ? news.getNewsPublishedAt() : "Not published");
    }

    @Override
    public Object getNewsStatistics(String id) {
        log.debug("Getting statistics - newsId={}", id);

        NewsMasterEntity news = findNewsByIdOrThrow(id);
        Long commentCount = newsCommentService.getCommentCountByNewsId(news.getNewsNewsId());

        return Map.of(
                "viewCount", news.getNewsViewCount(),
                "likeCount", news.getNewsLikeCount(),
                "shareCount", news.getNewsShareCount(),
                "commentCount", commentCount,
                "isFeatured", news.getNewsIsFeatured(),
                "workflowStatus", news.getNewsWorkflowStatus());
    }

    @Override
    public Resource exportNews(String format, Pageable pageable) {
        log.info("Exporting newsapp - format={}", format);

        Page<NewsMasterEntity> page = newsRepository.findAll(pageable);
        StringBuilder csv = new StringBuilder();
        csv.append("id,titleEn,contentEn,createdAt,workflowStatus\n");

        for (NewsMasterEntity news : page) {
            csv.append(escapeCsvField(String.valueOf(news.getNewsNewsId()))).append(",")
                    .append(escapeCsvField(news.getNewsTitleEn())).append(",")
                    .append(escapeCsvField(news.getNewsContentEn())).append(",")
                    .append(escapeCsvField(String.valueOf(news.getCreatedAt()))).append(",")
                    .append(escapeCsvField(news.getNewsWorkflowStatus().name())).append("\n");
        }

        try {
            Path tempFile = Files.createTempFile("news_export_", ".csv");
            Files.writeString(tempFile, csv.toString());
            log.info("News export completed - file={}, recordCount={}", tempFile.getFileName(),
                    page.getTotalElements());
            return new UrlResource(tempFile.toUri());
        } catch (Exception e) {
            log.error("Failed to export newsapp - error={}", e.getMessage());
            throw new InvalidRequestException("export", "Export failed: " + e.getMessage());
        }
    }

    // =========================
    // Audit Logging
    // =========================

    @Override
    public void logAction(UUID newsId, String action, String details, UUID actorId) {
        log.debug("Logging action - newsId={}, action={}", newsId, action);
        logAuditAction(newsId, action, details, actorId);
    }

    @Override
    public List<NewsAuditLog> findAuditLogsByNewsId(UUID newsId) {
        log.debug("Fetching audit logs - newsId={}", newsId);
        List<NewsAuditLog> logs = newsAuditLogService.findByNewsId(newsId);
        log.debug("Found audit logs - newsId={}, count={}", newsId, logs.size());
        return logs;
    }

    // =========================
    // Public Access Methods
    // =========================

    @Override
    @Cacheable(value = NewsCacheConstants.PUBLISHED_NEWS_CACHE, key = "'public-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<NewsCreateResponseDto> getPublishedActiveNews(Pageable pageable) {
        log.debug("Fetching published active newsapp - page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return newsRepository.findPublishedAndActive(PUBLISHED_STATUS, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public NewsCreateResponseDto getPublishedNewsById(String id) {
        log.debug("Fetching published newsapp - newsId={}", id);
        NewsMasterEntity entity = newsRepository.findPublishedById(parseUuid(id), PUBLISHED_STATUS)
                .orElseThrow(() -> new NewsNotFoundException("Published newsapp not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    public NewsCreateResponseDto getPublishedNewsBySlug(String slug) {
        log.debug("Fetching published newsapp - slug={}", slug);
        NewsMasterEntity entity = newsRepository.findPublishedBySlug(slug, PUBLISHED_STATUS)
                .orElseThrow(() -> new NewsNotFoundException("Published newsapp not found with slug: " + slug));
        return mapToResponse(entity);
    }

    @Override
    public NewsMasterEntity getPublishedNewsEntityById(String id) {
        log.debug("Fetching published newsapp entity - newsId={}", id);
        return newsRepository.findPublishedById(parseUuid(id), PUBLISHED_STATUS)
                .orElseThrow(() -> new NewsNotFoundException("Published newsapp not found with id: " + id));
    }

    @Override
    public NewsMasterEntity getPublishedNewsEntityBySlug(String slug) {
        log.debug("Fetching published newsapp entity - slug={}", slug);
        return newsRepository.findPublishedBySlug(slug, PUBLISHED_STATUS)
                .orElseThrow(() -> new NewsNotFoundException("Published newsapp not found with slug: " + slug));
    }

    @Override
    public Page<NewsCreateResponseDto> getPublishedNewsByCategory(String categoryId, Pageable pageable) {
        log.debug("Fetching published newsapp by newscategory - categoryId={}", categoryId);
        UUID catId = parseUuid(categoryId);
        return newsRepository.findPublishedByCategory(catId, PUBLISHED_STATUS, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<NewsCreateResponseDto> searchPublishedNews(String query, String categoryId, Pageable pageable) {
        log.debug("Searching published newsapp - query={}, categoryId={}", query, categoryId);

        // Sanitize search query to prevent XSS in search results
        String sanitizedQuery = query != null ? inputSanitizer.sanitizeHtml(query.trim()) : null;

        if (hasValue(sanitizedQuery) && hasValue(categoryId)) {
            UUID catId = parseUuid(categoryId);
            return newsRepository
                    .findByDeletedAtIsNullAndNewsNewsCategoryIdAndNewsWorkflowStatusAndNewsIsActive(catId,
                            PUBLISHED_STATUS, true, pageable)
                    .map(this::mapToResponse);
        } else if (hasValue(sanitizedQuery)) {
            return newsRepository
                    .findByDeletedAtIsNullAndNewsWorkflowStatusAndNewsIsActive(PUBLISHED_STATUS, true, pageable)
                    .map(this::mapToResponse);
        } else if (hasValue(categoryId)) {
            UUID catId = parseUuid(categoryId);
            return newsRepository
                    .findPublishedByCategory(catId, PUBLISHED_STATUS, pageable)
                    .map(this::mapToResponse);
        } else {
            return getPublishedActiveNews(pageable);
        }
    }

    @Override
    public Page<NewsCreateResponseDto> getTrendingPublishedNews(Pageable pageable) {
        log.debug("Fetching trending published newsapp - page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return newsRepository.findTrendingPublishedNews(PUBLISHED_STATUS, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<NewsCreateResponseDto> getFeaturedPublishedNews(Pageable pageable) {
        log.debug("Fetching featured published newsapp - page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return newsRepository.findFeaturedPublished(PUBLISHED_STATUS, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<NewsCreateResponseDto> getRelatedPublishedNews(String newsId, int limit) {
        log.debug("Fetching related published newsapp - newsId={}, limit={}", newsId, limit);

        NewsMasterEntity currentNews = newsRepository.findPublishedById(parseUuid(newsId), PUBLISHED_STATUS)
                .orElseThrow(() -> new NewsNotFoundException("Published newsapp not found with id: " + newsId));

        if (currentNews.getNewsNewsCategoryId() == null) {
            log.debug("News has no newscategory, returning empty - newsId={}", newsId);
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<NewsMasterEntity> relatedNews = newsRepository.findRelatedPublishedNewsByCategory(
                currentNews.getNewsNewsCategoryId(), currentNews.getNewsNewsId(), PUBLISHED_STATUS, pageable);
        log.debug("Found related published newsapp - newsId={}, count={}", newsId, relatedNews.size());

        return relatedNews.stream().map(this::mapToResponse).toList();
    }

    @Override
    public Page<NewsCreateResponseDto> getPublishedNewsByAuthor(String authorId, Pageable pageable) {
        log.debug("Fetching published newsapp by author - authorId={}", authorId);
        UUID authId = parseUuid(authorId);
        return newsRepository
                .findByCreatedByAndNewsWorkflowStatusAndNewsIsActiveTrue(authId, PUBLISHED_STATUS, pageable)
                .map(this::mapToResponse);
    }

    // =========================
    // Recommendations
    // =========================

    @Override
    public List<NewsCreateResponseDto> getUserRecommendations(UUID userId, Pageable pageable) {
        log.debug("Generating recommendations - userId={}", userId);

        List<NewsLike> likes = newsLikeRepository.findByNewsLikesUserId(userId);
        List<NewsView> views = newsViewRepository.findByNewsViewsUserId(userId);
        List<NewsShare> shares = newsShareRepository.findByNewsSharesUserId(userId);
        List<NewsComment> comments = newsCommentRepository.findByNewsCommentsUserId(userId);
        List<NewsBookmark> bookmarks = userBookmarkRepository.findByNewsBookmarksUserId(userId);

        List<NewsMasterEntity> userActivity = aggregateUserActivity(likes, views, shares, comments, bookmarks);
        log.debug("User activity aggregated - userId={}, activityCount={}", userId, userActivity.size());
        List<NewsMasterEntity> recommendedNews = newsRecommendationService.generateRecommendations(userActivity);
        log.debug("Recommendations generated - userId={}, count={}", userId, recommendedNews.size());

        Page<NewsMasterEntity> paginatedRecommendations = new PageImpl<>(recommendedNews, pageable,
                recommendedNews.size());
        return paginatedRecommendations.map(this::mapToResponse).getContent();
    }

    @Override
    public List<NewsCreateResponseDto> getDefaultRecommendations(Pageable pageable) {
        log.debug("Generating default recommendations");
        List<NewsMasterEntity> trendingNews = newsRepository.findTrendingNews(pageable);
        log.debug("Default recommendations generated - count={}", trendingNews.size());
        return trendingNews.stream().map(this::mapToResponse).toList();
    }

    // =========================
    // Private Helper Methods
    // =========================

    /**
     * Finds a newsapp entity by ID or throws a NewsNotFoundException.
     */
    private NewsMasterEntity findNewsByIdOrThrow(String id) {
        return newsRepository.findById(parseUuid(id))
                .orElseThrow(() -> new NewsNotFoundException("News not found with id: " + id));
    }

    /**
     * Parses a string to UUID with proper error handling.
     */
    private UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("id", "Invalid UUID format: " + id);
        }
    }

    /**
     * Checks if a string has a non-empty value.
     */
    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Collects ALL validation errors without throwing (Validate-All Pattern).
     * This follows industry standards (Google Forms, Stripe, AWS) by showing
     * all validation failures at once instead of fail-fast approach.
     *
     * @param request        the news creation request
     * @param existingNewsId optional ID of existing news for update validation
     * @return Map of fieldName → errorMessage for all validation failures
     */
    private Map<String, String> collectAllValidationErrors(NewsCreateRequestDto request, UUID existingNewsId) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        if (request == null) {
            fieldErrors.put("news", "News request cannot be null");
            return fieldErrors;
        }

        // =====================================================
        // LAYER 1: Content Format & Length Validations
        // =====================================================

        // Validate English content length (minimum 100 characters)
        if (request.getNewsContentEn() != null && request.getNewsContentEn().trim().length() < 100) {
            fieldErrors.put("news_content_en",
                    "Content is too short (minimum 100 characters)");
        }

        // Validate Spanish content length (minimum 100 characters)
        if (request.getNewsContentEs() != null && request.getNewsContentEs().trim().length() < 100) {
            fieldErrors.put("news_content_es",
                    "Content is too short (minimum 100 characters)");
        }

        // =====================================================
        // LAYER 2: Business Logic Validations (Database Checks)
        // =====================================================

        // Check English title uniqueness - only if not already failed
        if (!fieldErrors.containsKey("news_title_en") && request.getNewsTitleEn() != null) {
            NewsMasterEntity existingEnTitle = newsRepository.findByNewsTitleEn(request.getNewsTitleEn());
            if (existingEnTitle != null) {
                // For create: any match is a duplicate
                // For update: match only if it's a different news article
                boolean isDuplicate = (existingNewsId == null)
                        || (!existingEnTitle.getNewsNewsId().equals(existingNewsId));

                if (isDuplicate) {
                    log.warn(
                            "[VALIDATION] English title already exists - title='{}', existingNewsId={}, currentNewsId={}",
                            request.getNewsTitleEn(), existingEnTitle.getNewsNewsId(), existingNewsId);
                    fieldErrors.put("news_title_en",
                            "An article with this English title already exists. Please use a different title.");
                }
            }
        }

        // Check Spanish title uniqueness - only if not already failed
        if (!fieldErrors.containsKey("news_title_es") && request.getNewsTitleEs() != null) {
            NewsMasterEntity existingEsTitle = newsRepository.findByNewsTitleEs(request.getNewsTitleEs());
            if (existingEsTitle != null) {
                // For create: any match is a duplicate
                // For update: match only if it's a different news article
                boolean isDuplicate = (existingNewsId == null)
                        || (!existingEsTitle.getNewsNewsId().equals(existingNewsId));

                if (isDuplicate) {
                    log.warn(
                            "[VALIDATION] Spanish title already exists - title='{}', existingNewsId={}, currentNewsId={}",
                            request.getNewsTitleEs(), existingEsTitle.getNewsNewsId(), existingNewsId);
                    fieldErrors.put("news_title_es",
                            "An article with this Spanish title already exists. Please use a different title.");
                }
            }
        }

        return fieldErrors;
    }

    /**
     * Generates a smart excerpt from news content.
     */
    private String generateExcerpt(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        // Use the content processing service to generate excerpt
        return newsContentProcessingService.generateExcerpt(content, ContentFormat.PLAIN_TEXT, 200, true);
    }

    private NewsMasterEntity buildNewsEntity(NewsCreateRequestDto request) {
        Instant timestamp = Instant.now();

        // Determine content format from request (default to PLAIN_TEXT if
        // missing/invalid)
        ContentFormat contentFormat;
        try {
            contentFormat = request.getNewsContentFormat() != null
                    ? ContentFormat.valueOf(request.getNewsContentFormat())
                    : ContentFormat.PLAIN_TEXT;
        } catch (Exception e) {
            contentFormat = ContentFormat.PLAIN_TEXT;
        }

        // Generate excerpt if not provided
        String excerptEn = request.getNewsExcerptEn();
        Boolean excerptAutoGenerated = null;
        if (excerptEn == null || excerptEn.trim().isEmpty()) {
            excerptEn = newsContentProcessingService.generateExcerpt(request.getNewsContentEn(), contentFormat, 200,
                    true);
            excerptAutoGenerated = true;
        }

        // Process geographic data - validation only, metadata stored via entity fields
        if (request.getNewsLatitude() != null && request.getNewsLongitude() != null) {
            geographicValidationService.getGeographicMetadata(
                    request.getNewsLatitude(), request.getNewsLongitude());
        }

        // Process content quality metrics
        var contentMetrics = contentQualityService.calculateContentMetrics(
                request.getNewsContentEn(), contentFormat);

        var qualityMetrics = contentQualityService.analyzeContentQuality(
                request.getNewsContentEn(), contentFormat);

        // Generate SEO metadata for ENGLISH
        String metaDescriptionEn = request.getNewsMetaDescription();
        if (metaDescriptionEn == null || metaDescriptionEn.trim().isEmpty()) {
            metaDescriptionEn = seoOptimizationService.generateMetaDescription(
                    request.getNewsTitleEn(), request.getNewsContentEn(), contentFormat);
        }

        String keywordsEn = request.getNewsKeywords();
        if (keywordsEn == null || keywordsEn.trim().isEmpty()) {
            String[] extractedKeywords = seoOptimizationService.extractKeywords(
                    request.getNewsTitleEn(), request.getNewsContentEn(), contentFormat, 5);
            keywordsEn = String.join(",", extractedKeywords);
        }

        // NOTE: Spanish SEO metadata generation is not yet implemented.
        // FUTURE ENHANCEMENT: When entity schema supports bilingual SEO fields
        // (metaDescriptionEs, keywordsEs), generate Spanish metadata here.

        String slugEn = seoOptimizationService.generateSlug(request.getNewsTitleEn());

        // NOTE: Title uniqueness validations are now performed in
        // collectAllValidationErrors()
        // using the Validate-All pattern (industry standard)

        return NewsMasterEntity.builder()
                // Basic content fields - sanitized for XSS protection
                .newsTitleEn(inputSanitizer.sanitizeHtml(request.getNewsTitleEn()))
                .newsContentEn(inputSanitizer.sanitizeHtml(request.getNewsContentEn()))
                .newsTitleEs(inputSanitizer.sanitizeHtml(request.getNewsTitleEs()))
                .newsContentEs(inputSanitizer.sanitizeHtml(request.getNewsContentEs()))
                .newsContentFormat(contentFormat)
                .newsExcerptEn(inputSanitizer.sanitizeHtml(excerptEn))
                .newsExcerptEs(inputSanitizer.sanitizeHtml(request.getNewsExcerptEs()))
                .newsExcerptAutoGenerated(excerptAutoGenerated)

                // Geographic data
                .newsLatitude(request.getNewsLatitude())
                .newsLongitude(request.getNewsLongitude())
                .newsCity(request.getNewsCity())
                .newsRegion(request.getNewsRegion())
                .newsCountryCode(request.getNewsCountryCode())

                // Content quality metrics
                .newsReadabilityScore(qualityMetrics.getReadabilityScore())
                .newsWordCount(contentMetrics.getWordCount())
                .newsReadTimeMinutes(contentMetrics.getEstimatedReadingTimeMinutes())

                // SEO metadata (English - Note: Spanish metadata generated but entity supports
                // single language for now)
                .newsMetaDescription(inputSanitizer.sanitizeHtml(metaDescriptionEn))
                .newsKeywords(inputSanitizer.sanitizeHtml(keywordsEn))
                // .seoScore(seoScore)
                // .titleOptimized(seoScore > 70)
                // .metaDescriptionOptimal(metaDescriptionEn.length() >= 120 &&
                // metaDescriptionEn.length() <= 160)
                // .keywordsPresent(!keywordsEn.isEmpty())
                // .slugOptimized(slugEn.matches("[a-z0-9-]+"))

                // Publishing and workflow
                .newsUrgencyLevel(request.getNewsUrgencyLevel() != null
                        ? UrgencyLevel.valueOf(request.getNewsUrgencyLevel().toUpperCase())
                        : UrgencyLevel.NORMAL)
                .newsWorkflowStatus(parseWorkflowStatusFromRequest(request.getNewsWorkflowStatus()))
                .newsEmbargoUntil(parseInstant(request.getNewsEmbargoUntil()))
                .newsTargetAudience(inputSanitizer.sanitizeHtml(request.getNewsTargetAudience()))

                // Series support
                .newsSeriesId(request.getNewsSeriesId())
                .newsSeriesOrder(request.getNewsSeriesOrder())

                // Legacy fields (keeping for backward compatibility)
                .newsSlug(slugEn)
                .newsSourceUrl(inputSanitizer.sanitizeUrl(request.getNewsSourceUrl()))
                .newsNewsCategoryId(request.getNewsNewsCategoryId())
                .createdBy(request.getCreatedBy())
                .newsIsFeatured(request.getNewsIsFeatured() != null ? request.getNewsIsFeatured() : false)
                .newsMetaTitle(inputSanitizer.sanitizeHtml(request.getNewsMetaTitle()))
                .newsMetaDescription(metaDescriptionEn)
                .newsKeywords(keywordsEn)
                .newsTags(inputSanitizer.sanitizeHtml(request.getNewsTags()))
                .newsMediaType(
                        request.getImageVideoFile() != null ? request.getImageVideoFile().getContentType() : null)
                .newsThumbnailUrl(null) // Set by processImages after entity creation
                .newsImageCardUrl(null) // Set by processImages after entity creation
                .newsImageHeroUrl(null) // Set by processImages after entity creation
                .newsMediaFileName(null) // Set by processImages after entity creation
                .newsMediaFileType(
                        request.getImageVideoFile() != null ? request.getImageVideoFile().getContentType() : null)
                .newsMediaFileSize(request.getImageVideoFile() != null ? request.getImageVideoFile().getSize() : 0L)
                .newsMediaFileUrl(null) // Set by processImages after entity creation
                .createdAt(timestamp)
                .newsIsActive(true)

                // Breaking news fields
                .newsIsBreaking(Boolean.TRUE.equals(request.getNewsIsBreaking()))
                .newsBreakingExpiresAt(parseInstant(request.getNewsBreakingExpiresAt()))

                // Content expiration & read time
                .newsExpiresAt(parseInstant(request.getNewsExpiresAt()))
                .newsReadTimeMinutes(request.getNewsReadTimeMinutes() != null ? request.getNewsReadTimeMinutes()
                        : contentMetrics.getEstimatedReadingTimeMinutes())

                // Content origin & attribution
                .newsContentOrigin(parseContentOrigin(request.getNewsContentOrigin()))
                .newsSourceAuthorName(inputSanitizer.sanitizeHtml(request.getNewsSourceAuthorName()))
                .newsSourceAgencyId(request.getNewsSourceAgencyId())

                // Monetization - sponsored content
                .newsIsSponsored(Boolean.TRUE.equals(request.getNewsIsSponsored()))
                .newsSponsorName(inputSanitizer.sanitizeHtml(request.getNewsSponsorName()))
                .newsSponsorLogoUrl(inputSanitizer.sanitizeUrl(request.getNewsSponsorLogoUrl()))
                .newsSponsorWebsiteUrl(inputSanitizer.sanitizeUrl(request.getNewsSponsorWebsiteUrl()))

                // Monetization - premium content
                .newsIsPremium(Boolean.TRUE.equals(request.getNewsIsPremium()))
                .newsPremiumTier(request.getNewsPremiumTier())

                // SEO & optional fields
                .newsCanonicalUrl(inputSanitizer.sanitizeUrl(request.getNewsCanonicalUrl()))
                .newsEditorNotes(inputSanitizer.sanitizeHtml(request.getNewsEditorNotes()))

                // Engagement counts (default to 0)
                .newsViewCount(0L)
                .newsLikeCount(0L)
                .newsShareCount(0L)
                .newsCommentCount(0L)
                .newsBookmarkCount(0L)
                .newsReplyCount(0L)

                .build();
    }

    /**
     * Updates an existing newsapp entity from a request.
     */
    private void updateNewsEntity(NewsMasterEntity entity, NewsCreateRequestDto request, String filename,
            String filePath) {
        Instant updateTimestamp = Instant.now();

        // Basic content fields - sanitized for XSS protection
        if (request.getNewsTitleEn() != null) {
            entity.setNewsTitleEn(inputSanitizer.sanitizeHtml(request.getNewsTitleEn()));
        }
        if (request.getNewsContentEn() != null) {
            entity.setNewsContentEn(inputSanitizer.sanitizeHtml(request.getNewsContentEn()));
        }
        if (request.getNewsTitleEs() != null) {
            entity.setNewsTitleEs(inputSanitizer.sanitizeHtml(request.getNewsTitleEs()));
        }
        if (request.getNewsContentEs() != null) {
            entity.setNewsContentEs(inputSanitizer.sanitizeHtml(request.getNewsContentEs()));
        }

        // Generate/update excerpt if content changed
        if (request.getNewsContentEn() != null) {
            String excerptEn = request.getNewsExcerptEn();
            Boolean excerptAutoGenerated = null;
            if (excerptEn == null || excerptEn.trim().isEmpty()) {
                excerptEn = generateExcerpt(request.getNewsContentEn());
                excerptAutoGenerated = true;
            }
            entity.setNewsExcerptEn(inputSanitizer.sanitizeHtml(excerptEn));
            entity.setNewsExcerptAutoGenerated(excerptAutoGenerated);
        }
        if (request.getNewsExcerptEs() != null) {
            entity.setNewsExcerptEs(inputSanitizer.sanitizeHtml(request.getNewsExcerptEs()));
        }

        // Geographic data updates
        if (request.getNewsLatitude() != null) {
            entity.setNewsLatitude(request.getNewsLatitude());
        }
        if (request.getNewsLongitude() != null) {
            entity.setNewsLongitude(request.getNewsLongitude());
        }
        if (request.getNewsCity() != null) {
            entity.setNewsCity(request.getNewsCity());
        }
        if (request.getNewsCountryCode() != null) {
            entity.setNewsCountryCode(request.getNewsCountryCode());
        }
        if (request.getNewsRegion() != null) {
            entity.setNewsRegion(request.getNewsRegion());
        }

        // Update geographic metadata if coordinates changed
        if (request.getNewsLatitude() != null && request.getNewsLongitude() != null) {
            // Validate coordinates - throws exception if invalid
            geographicValidationService.getGeographicMetadata(
                    request.getNewsLatitude(), request.getNewsLongitude());
        }

        // Update content quality metrics if content changed
        if (request.getNewsContentEn() != null) {
            ContentQualityMetrics metrics = contentQualityService.analyzeContentQuality(
                    request.getNewsContentEn(), ContentFormat.PLAIN_TEXT);

            entity.setNewsReadabilityScore(metrics.getReadabilityScore());
            entity.setNewsWordCount(metrics.getContentMetrics().getWordCount());
            entity.setNewsReadTimeMinutes(metrics.getContentMetrics().getEstimatedReadingTimeMinutes());
        }

        // Update content format if provided
        if (request.getNewsContentFormat() != null) {
            try {
                ContentFormat contentFormat = ContentFormat.valueOf(request.getNewsContentFormat());
                entity.setNewsContentFormat(contentFormat);
            } catch (IllegalArgumentException e) {
                entity.setNewsContentFormat(ContentFormat.PLAIN_TEXT);
            }
        }

        // Update tags if provided
        if (request.getNewsTags() != null) {
            entity.setNewsTags(inputSanitizer.sanitizeHtml(request.getNewsTags()));
        }

        // Update SEO metadata
        if (request.getNewsTitleEn() != null || request.getNewsContentEn() != null) {
            String title = request.getNewsTitleEn() != null ? request.getNewsTitleEn() : entity.getNewsTitleEn();
            String content = request.getNewsContentEn() != null ? request.getNewsContentEn()
                    : entity.getNewsContentEn();

            // Update meta description if not provided
            if (request.getNewsMetaDescription() != null) {
                entity.setNewsMetaDescription(inputSanitizer.sanitizeHtml(request.getNewsMetaDescription()));
            } else if (request.getNewsContentEn() != null) {
                String metaDesc = seoOptimizationService.generateMetaDescription(
                        title, content, ContentFormat.PLAIN_TEXT);
                entity.setNewsMetaDescription(inputSanitizer.sanitizeHtml(metaDesc));
            }

            // Update keywords if not provided
            if (request.getNewsKeywords() != null) {
                entity.setNewsKeywords(inputSanitizer.sanitizeHtml(request.getNewsKeywords()));
            } else if (request.getNewsContentEn() != null) {
                String[] keywords = seoOptimizationService.extractKeywords(
                        title, content, ContentFormat.PLAIN_TEXT, 5);
                entity.setNewsKeywords(inputSanitizer.sanitizeHtml(String.join(",", keywords)));
            }

            // Update slug if title changed
            if (request.getNewsTitleEn() != null) {
                // NOTE: Title uniqueness validations are now performed in
                // collectAllValidationErrors()
                // using the Validate-All pattern (industry standard)
                String newSlug = seoOptimizationService.generateSlug(request.getNewsTitleEn());
                entity.setNewsSlug(newSlug);
            }

            // NOTE: Spanish title uniqueness validation is now performed in
            // collectAllValidationErrors()
            // using the Validate-All pattern (industry standard)

            // Note: SEO score can be recalculated via
            // seoOptimizationService.calculateSeoScore()
            // Currently not stored in entity - available for future enhancement
        }

        // Publishing and workflow updates
        // if (request.getPublishDateTime() != null) { // commented out as not in DTO
        // entity.setNewsScheduledPublishAt(parseInstant(request.getPublishDateTime()));
        // }
        if (request.getNewsUrgencyLevel() != null) {
            entity.setNewsUrgencyLevel(UrgencyLevel.valueOf(request.getNewsUrgencyLevel().toUpperCase()));
        }

        // Handle workflow status changes with proper timestamp management
        if (request.getNewsWorkflowStatus() != null) {
            WorkflowStatus newStatus = parseWorkflowStatusFromRequest(request.getNewsWorkflowStatus());
            entity.setNewsWorkflowStatus(newStatus);

            // When transitioning to PUBLISHED, set the published timestamp
            if (newStatus == WorkflowStatus.PUBLISHED && entity.getNewsPublishedAt() == null) {
                entity.setNewsPublishedAt(updateTimestamp);
            }

            // Handle SCHEDULED status - validate and set scheduled publish time
            if (newStatus == WorkflowStatus.SCHEDULED) {
                Instant scheduledTime = request.getNewsScheduledPublishAt();
                if (scheduledTime == null) {
                    throw new InvalidRequestException("newsScheduledPublishAt",
                            "Scheduled publish date/time is required when workflow status is SCHEDULED");
                }
                Instant now = Instant.now();
                if (scheduledTime.isBefore(now)) {
                    throw new InvalidRequestException("newsScheduledPublishAt",
                            "Cannot schedule publication in the past. Scheduled time must be in the future");
                }
                Instant oneYearFromNow = now.plus(365, ChronoUnit.DAYS);
                if (scheduledTime.isAfter(oneYearFromNow)) {
                    throw new InvalidRequestException("newsScheduledPublishAt",
                            "Cannot schedule publication more than 1 year in the future");
                }
                entity.setNewsScheduledPublishAt(scheduledTime);
            }
        }

        if (request.getNewsEmbargoUntil() != null) {
            entity.setNewsEmbargoUntil(parseInstant(request.getNewsEmbargoUntil()));
        }
        if (request.getNewsTargetAudience() != null) {
            entity.setNewsTargetAudience(inputSanitizer.sanitizeHtml(request.getNewsTargetAudience()));
        }

        // Series support
        if (request.getNewsSeriesId() != null) {
            entity.setNewsSeriesId(request.getNewsSeriesId());
        }
        if (request.getNewsSeriesOrder() != null) {
            entity.setNewsSeriesOrder(request.getNewsSeriesOrder());
        }

        // Legacy fields (keeping for backward compatibility)
        if (request.getNewsTitleEn() != null) {
            // NOTE: Slug is already set above via seoOptimizationService.generateSlug()
            // Removed duplicate:
            // entity.setNewsSlug(SlugUtils.slugify(request.getNewsTitleEn()));
            entity.setNewsMetaTitle(inputSanitizer.sanitizeHtml(request.getNewsTitleEn()));
        }
        if (request.getNewsSourceUrl() != null) {
            entity.setNewsSourceUrl(inputSanitizer.sanitizeUrl(request.getNewsSourceUrl()));
        }
        if (request.getNewsNewsCategoryId() != null) {
            entity.setNewsNewsCategoryId(request.getNewsNewsCategoryId());
        }
        if (request.getNewsIsFeatured() != null) {
            entity.setNewsIsFeatured(request.getNewsIsFeatured());
        }
        if (request.getNewsMetaDescription() != null) {
            entity.setNewsMetaDescription(inputSanitizer.sanitizeHtml(request.getNewsMetaDescription()));
        }
        if (request.getNewsKeywords() != null) {
            entity.setNewsKeywords(inputSanitizer.sanitizeHtml(request.getNewsKeywords()));
        }

        // Media updates
        if (request.getImageVideoFile() != null) {
            entity.setNewsMediaType(request.getImageVideoFile().getContentType());
            entity.setNewsMediaFileName(filename);
            entity.setNewsMediaFileType(request.getImageVideoFile().getContentType());
            entity.setNewsMediaFileSize(request.getImageVideoFile().getSize());
            entity.setNewsMediaFileUrl(filename);
        }

        // Update timestamps
        entity.setUpdatedAt(updateTimestamp);
        // AUDIT SEMANTICS: In UPDATE context, request.getCreatedBy() contains the admin
        // ID of who is performing
        // the update. We set this as 'updatedBy' to maintain proper audit trail.
        // Original 'createdBy' is never changed.
        entity.setUpdatedBy(request.getCreatedBy());
        entity.setNewsLastEditedAt(updateTimestamp);

        // Breaking news fields
        if (request.getNewsIsBreaking() != null) {
            entity.setNewsIsBreaking(request.getNewsIsBreaking());
        }
        if (request.getNewsBreakingExpiresAt() != null) {
            entity.setNewsBreakingExpiresAt(parseInstant(request.getNewsBreakingExpiresAt()));
        }

        // Content expiration & read time
        if (request.getNewsExpiresAt() != null) {
            entity.setNewsExpiresAt(parseInstant(request.getNewsExpiresAt()));
        }
        if (request.getNewsReadTimeMinutes() != null) {
            entity.setNewsReadTimeMinutes(request.getNewsReadTimeMinutes());
        } else if (request.getNewsContentEn() != null) {
            // Recalculate read time on content update
            ContentQualityMetrics metrics = contentQualityService.analyzeContentQuality(
                    request.getNewsContentEn(), ContentFormat.PLAIN_TEXT);
            entity.setNewsReadTimeMinutes(metrics.getContentMetrics().getEstimatedReadingTimeMinutes());
        }

        // Content origin & attribution
        if (request.getNewsContentOrigin() != null) {
            try {
                entity.setNewsContentOrigin(ContentOrigin.valueOf(request.getNewsContentOrigin().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setNewsContentOrigin(ContentOrigin.ORIGINAL);
            }
        }
        if (request.getNewsSourceAuthorName() != null) {
            entity.setNewsSourceAuthorName(inputSanitizer.sanitizeHtml(request.getNewsSourceAuthorName()));
        }
        if (request.getNewsSourceAgencyId() != null) {
            entity.setNewsSourceAgencyId(request.getNewsSourceAgencyId());
        }

        // Monetization - sponsored content
        if (request.getNewsIsSponsored() != null) {
            entity.setNewsIsSponsored(request.getNewsIsSponsored());
        }
        if (request.getNewsSponsorName() != null) {
            entity.setNewsSponsorName(inputSanitizer.sanitizeHtml(request.getNewsSponsorName()));
        }
        if (request.getNewsSponsorLogoUrl() != null) {
            entity.setNewsSponsorLogoUrl(inputSanitizer.sanitizeUrl(request.getNewsSponsorLogoUrl()));
        }
        if (request.getNewsSponsorWebsiteUrl() != null) {
            entity.setNewsSponsorWebsiteUrl(inputSanitizer.sanitizeUrl(request.getNewsSponsorWebsiteUrl()));
        }

        // Monetization - premium content
        if (request.getNewsIsPremium() != null) {
            entity.setNewsIsPremium(request.getNewsIsPremium());
        }
        if (request.getNewsPremiumTier() != null) {
            entity.setNewsPremiumTier(request.getNewsPremiumTier());
        }

        // SEO & optional fields
        if (request.getNewsCanonicalUrl() != null) {
            entity.setNewsCanonicalUrl(inputSanitizer.sanitizeUrl(request.getNewsCanonicalUrl()));
        }
        if (request.getNewsEditorNotes() != null) {
            entity.setNewsEditorNotes(inputSanitizer.sanitizeHtml(request.getNewsEditorNotes()));
        }
    }

    /**
     * Logs an audit action for a newsapp item.
     */
    private void logAuditAction(UUID newsId, String action, String details, UUID actorId) {
        log.debug("Audit action - newsId={}, action={}, actorId={}", newsId, action, actorId);

        try {
            // Fetch article title for enriched audit logging
            NewsMasterEntity news = newsRepository.findById(newsId).orElse(null);
            String resourceName = (news != null) ? news.getNewsTitleEn() : "";

            // Fetch admin user display name (email) from repository
            String actorDisplayName = actorId.toString(); // Default to UUID
            if (actorId != null) {
                AdminUser admin = adminUserRepository.findById(actorId).orElse(null);
                if (admin != null && admin.getAdminUsersEmail() != null) {
                    actorDisplayName = admin.getAdminUsersEmail(); // Use email as display name
                }
            }

            // Use enriched logging with article title and actor display name
            logAuditActionEnriched(newsId, action, details, actorId,
                    actorDisplayName,
                    resourceName);
        } catch (Exception e) {
            log.warn("Failed to create audit log: {}", e.getMessage());
            // Soft failure - audit errors should not block operations
        }
    }

    /**
     * Enriched audit logging with article title and actor display name.
     * 
     * <p>
     * RECOMMENDED METHOD for all critical news operations (publish, archive,
     * delete).
     * Provides complete audit context for dashboard display and compliance
     * reporting.
     * </p>
     *
     * @param newsId           the news article UUID
     * @param action           the action performed
     * @param details          additional details (JSON format)
     * @param actorId          the UUID of the actor
     * @param actorDisplayName human-readable name of actor (email, full name)
     * @param resourceName     human-readable name of resource (article title)
     */
    private void logAuditActionEnriched(UUID newsId, String action, String details, UUID actorId,
            String actorDisplayName, String resourceName) {
        log.debug("Audit action - newsId={}, action={}, actor={}, article={}",
                newsId, action, actorDisplayName, resourceName);

        try {
            String mappedAction = mapActionName(action);

            // Log via service - stores complete audit context (24+ fields)
            newsAuditLogService.logAction(newsId, mappedAction, details, actorId,
                    actorDisplayName, resourceName);

            log.debug("Audit log created - title: {}, actor: {}", resourceName, actorDisplayName);
        } catch (Exception e) {
            log.warn("Failed to create audit log: {}", e.getMessage());
            // Soft failure - audit errors should not block operations
        }
    }

    /**
     * Maps legacy action names to new NewsAuditActions constants for consistency
     */
    private String mapActionName(String action) {
        return switch (action) {
            case "CREATE" -> NewsAuditActions.NEWS_CREATED;
            case "UPDATE" -> NewsAuditActions.NEWS_UPDATED;
            case "DELETE", "ARCHIVE" -> NewsAuditActions.NEWS_ARCHIVED;
            case "UNARCHIVE" -> NewsAuditActions.NEWS_RESTORED;
            case "DRAFT_CREATE" -> NewsAuditActions.NEWS_CREATED;
            case "SCHEDULED_PUBLISH" -> NewsAuditActions.NEWS_PUBLISHED;
            case "RESTORE" -> NewsAuditActions.NEWS_RESTORED;
            case "PIN", "FEATURED" -> NewsAuditActions.NEWS_FEATURED;
            case "UNPIN", "UNFEATURED" -> NewsAuditActions.NEWS_UNFEATURED;
            default -> action;
        };
    }

    /**
     * PROFESSIONAL: Maps entity to DTO using centralized MapStruct mapper.
     * 
     * This method now delegates to the single source of truth NewsMapper,
     * which is generated at compile time by MapStruct for optimal performance.
     * The only special handling is fetching the comment count from service.
     * 
     * @param entity the NewsMasterEntity to convert
     * @return complete NewsCreateResponseDto with all fields populated
     */
    private NewsCreateResponseDto mapToResponse(NewsMasterEntity entity) {
        // Use centralized mapper for all field conversions
        NewsCreateResponseDto dto = newsMapper.toNewsCreateResponseDto(entity);

        // PROFESSIONAL: Fetch and set comment count (special case - requires service
        // call)
        try {
            Long commentCount = newsCommentService.getCommentCountByNewsId(entity.getNewsNewsId());
            dto.setNewsCommentCount(commentCount);
        } catch (Exception e) {
            log.warn("[NEWS-MAPPING] [newsId={}] Failed to fetch comment count - defaulting to 0",
                    entity.getNewsNewsId(), e);
            dto.setNewsCommentCount(0L);
        }

        return dto;
    }

    /**
     * Escapes a field for CSV format (RFC 4180 compliant).
     */
    private String escapeCsvField(String field) {
        if (field == null)
            return "";

        boolean needsQuotes = field.contains(",") || field.contains("\"") ||
                field.contains("\n") || field.contains("\r");
        if (needsQuotes) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Parses an ISO-8601 timestamp string to Instant.
     * Returns null if the input is null, empty, or invalid.
     *
     * @param timestamp ISO-8601 formatted timestamp string
     * @return parsed Instant or null
     */
    private Instant parseInstant(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp - input={}, error={}", timestamp, e.getMessage());
            return null;
        }
    }

    // Note: Read time calculation is handled by
    // contentQualityService.calculateContentMetrics()
    // which provides getEstimatedReadingTimeMinutes()

    /**
     * Parses a workflow status string to enum.
     */
    private WorkflowStatus parseWorkflowStatus(String status) {
        try {
            return WorkflowStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("status", "Invalid workflow status: " + status);
        }
    }

    /**
     * Parses workflow status from request and returns UPPERCASE string for entity.
     * Validates the status is a valid WorkflowStatus enum value.
     * Industry standard: Store enum values in UPPERCASE for direct matching.
     * 
     * @param status the workflow status from request
     * @return UPPERCASE workflow status string
     * @throws InvalidRequestException if status is invalid
     */
    private WorkflowStatus parseWorkflowStatusFromRequest(String status) {
        if (status == null || status.isBlank()) {
            return WorkflowStatus.DRAFT;
        }
        try {
            return WorkflowStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("newsWorkflowStatus",
                    "Invalid workflow status: " + status
                            + ". Valid values: DRAFT, SUBMITTED, REVIEWED, APPROVED, SCHEDULED, PUBLISHED");
        }
    }

    /**
     * Parses content origin from request String to ContentOrigin enum.
     * Defaults to ORIGINAL if null or invalid.
     */
    private ContentOrigin parseContentOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return ContentOrigin.ORIGINAL;
        }
        try {
            return ContentOrigin.valueOf(origin.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid content origin: {}, defaulting to ORIGINAL", origin);
            return ContentOrigin.ORIGINAL;
        }
    }

    /**
     * Validates that a user has permission for a workflow transition.
     */
    private void validateWorkflowTransition(String roleName, WorkflowStatus targetStatus, String userId) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(roleName) || "SUPER_ADMIN".equalsIgnoreCase(roleName);
        boolean isPublishAction = targetStatus == WorkflowStatus.PUBLISHED;

        if (isPublishAction && !isAdmin) {
            throw new UnauthorizedAccessException("User " + userId + " is not authorized to " +
                    targetStatus.name().toLowerCase() + " newsapp");
        }
    }

    /**
     * Executes a search based on provided parameters.
     */
    private Page<NewsMasterEntity> executeSearch(String query, String categoryId,
            String fromDate, String toDate, Pageable pageable) {

        if (hasValue(query) && hasValue(categoryId)) {
            UUID catId = parseUuid(categoryId);
            Specification<NewsMasterEntity> spec = NewsSearchSpecifications.isNotDeleted()
                    .and(NewsSearchSpecifications.byCategoryId(String.valueOf(catId)))
                    .and(NewsSearchSpecifications.byQuery(query.trim()));
            return newsRepository.findAll(spec, pageable);
        } else if (hasValue(query)) {
            Specification<NewsMasterEntity> spec = NewsSearchSpecifications.isNotDeleted()
                    .and(NewsSearchSpecifications.byQuery(query.trim()));
            return newsRepository.findAll(spec, pageable);
        } else if (hasValue(categoryId)) {
            UUID catId = parseUuid(categoryId);
            return newsRepository.findByNewsNewsCategoryId(catId, pageable);
        } else if (hasValue(fromDate) && hasValue(toDate)) {
            Instant from = Instant.parse(fromDate);
            Instant to = Instant.parse(toDate);
            return newsRepository.findByDateRange(from, to, pageable);
        } else {
            return newsRepository.findAll(pageable);
        }
    }

    /**
     * Aggregates user activity for recommendations.
     */
    private List<NewsMasterEntity> aggregateUserActivity(List<NewsLike> likes, List<NewsView> views,
            List<NewsShare> shares, List<NewsComment> comments, List<NewsBookmark> bookmarks) {
        List<NewsMasterEntity> activity = new ArrayList<>();
        activity.addAll(likes.stream().map(NewsLike::getNews).toList());
        activity.addAll(views.stream().map(NewsView::getNews).toList());
        activity.addAll(shares.stream().map(NewsShare::getNews).toList());
        activity.addAll(comments.stream().map(NewsComment::getNews).toList());
        activity.addAll(bookmarks.stream().map(NewsBookmark::getNews).toList());
        return activity;
    }

    // =========================
    // Presentation Methods (Platform-Specific Content Processing)
    // =========================

    @Override
    public NewsCreateResponseDto getNewsForMobile(Long newsId) {
        log.debug("NewsService: Retrieving mobile-optimized news for ID: {}", newsId);

        NewsCreateResponseDto news = getPublishedNewsById(newsId.toString());

        // Transform content for mobile consumption
        String processedContentEn = newsContentProcessingService.optimizeForWebPublishing(
                news.getNewsContentEn(), ContentFormat.valueOf(news.getNewsContentFormat()));
        String processedContentEs = newsContentProcessingService.optimizeForWebPublishing(
                news.getNewsContentEs(), ContentFormat.valueOf(news.getNewsContentFormat()));

        // Create a copy and populate mobile-specific processed content
        NewsCreateResponseDto response = newsMapper.copyResponseDto(news);
        response.setMobileContentEn(processedContentEn);
        response.setMobileContentEs(processedContentEs);

        log.debug("NewsService: Successfully processed mobile news for ID: {}", newsId);
        return response;
    }

    @Override
    public NewsCreateResponseDto getNewsForWeb(Long newsId) {
        log.debug("NewsService: Retrieving web-optimized news for ID: {}", newsId);

        NewsCreateResponseDto news = getPublishedNewsById(newsId.toString());

        // Transform content for web consumption with HTML
        String htmlContentEn = newsContentProcessingService.optimizeForWebPublishing(
                news.getNewsContentEn(), ContentFormat.valueOf(news.getNewsContentFormat()));
        String htmlContentEs = newsContentProcessingService.optimizeForWebPublishing(
                news.getNewsContentEs(), ContentFormat.valueOf(news.getNewsContentFormat()));

        // Create a copy and populate web-specific processed content
        NewsCreateResponseDto response = newsMapper.copyResponseDto(news);
        response.setWebContentHtmlEn(htmlContentEn);
        response.setWebContentHtmlEs(htmlContentEs);

        log.debug("NewsService: Successfully processed web news for ID: {}", newsId);
        return response;
    }

    @Override
    public Page<NewsCreateResponseDto> getNewsCards(Pageable pageable) {
        log.debug("NewsService: Retrieving news cards - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<NewsCreateResponseDto> newsPage = getPublishedNews(pageable);

        List<NewsCreateResponseDto> cards = newsPage.getContent().stream()
                .map(this::convertToNewsCard)
                .collect(Collectors.toList());

        log.debug("NewsService: Converted {} news items to cards", cards.size());
        return new PageImpl<>(cards, pageable, newsPage.getTotalElements());
    }

    @Override
    public List<NewsCreateResponseDto> getTrendingNewsCards(int limit) {
        log.debug("NewsService: Retrieving trending news cards, limit: {}", limit);

        // For now, use published news sorted by view count
        // TODO: Implement proper trending algorithm based on engagement
        Pageable pageable = Pageable.ofSize(limit);
        Page<NewsCreateResponseDto> trendingNews = getPublishedNews(pageable);

        List<NewsCreateResponseDto> cards = trendingNews.getContent().stream()
                .map(this::convertToNewsCard)
                .collect(Collectors.toList());

        log.debug("NewsService: Retrieved {} trending cards", cards.size());
        return cards;
    }

    @Override
    public Page<NewsCreateResponseDto> getNewsCardsByCategory(String categoryId, Pageable pageable) {
        log.debug("NewsService: Retrieving news cards for category: {} - page: {}, size: {}",
                categoryId, pageable.getPageNumber(), pageable.getPageSize());

        Page<NewsCreateResponseDto> newsPage = getPublishedNewsByCategory(categoryId, pageable);

        List<NewsCreateResponseDto> cards = newsPage.getContent().stream()
                .map(this::convertToNewsCard)
                .collect(Collectors.toList());

        log.debug("NewsService: Converted {} category news items to cards", cards.size());
        return new PageImpl<>(cards, pageable, newsPage.getTotalElements());
    }

    @Override
    public Page<NewsCreateResponseDto> getNewsCardsForMobile(Pageable pageable, String category, String priority) {
        log.debug("NewsService: Retrieving mobile news cards - page: {}, size: {}, category: {}, priority: {}",
                pageable.getPageNumber(), pageable.getPageSize(), category, priority);

        // For now, delegate to existing method - TODO: implement filtering logic
        return getNewsCards(pageable);
    }

    @Override
    public Page<NewsCreateResponseDto> getBreakingNewsForMobile(Pageable pageable) {
        log.debug("NewsService: Retrieving breaking news for mobile - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // For now, get published news and filter for breaking news - TODO: implement
        // proper breaking news logic
        Page<NewsCreateResponseDto> newsPage = getPublishedNews(pageable);

        List<NewsCreateResponseDto> breakingNews = newsPage.getContent().stream()
                .filter(news -> Boolean.TRUE.equals(news.getNewsIsBreaking())) // Assuming this field exists
                .map(this::convertToMobileNews)
                .collect(Collectors.toList());

        log.debug("NewsService: Found {} breaking news items for mobile", breakingNews.size());
        return new PageImpl<>(breakingNews, pageable, breakingNews.size());
    }

    @Override
    public Page<NewsCreateResponseDto> getNewsByCategoryForMobile(String category, Pageable pageable) {
        log.debug("NewsService: Retrieving mobile news by category: {} - page: {}, size: {}",
                category, pageable.getPageNumber(), pageable.getPageSize());

        // Delegate to existing category method
        return getNewsCardsByCategory(category, pageable);
    }

    /**
     * Converts NewsCreateResponseDto to NewsCreateResponseDto with card excerpts.
     */
    private NewsCreateResponseDto convertToNewsCard(NewsCreateResponseDto news) {
        // Generate excerpts from content
        String excerptEn = newsContentProcessingService.generateExcerpt(
                news.getNewsContentEn(), ContentFormat.valueOf(news.getNewsContentFormat()), 150, true); // 150
                                                                                                         // characters
        String excerptEs = newsContentProcessingService.generateExcerpt(
                news.getNewsContentEs(), ContentFormat.valueOf(news.getNewsContentFormat()), 150, true);

        NewsCreateResponseDto response = newsMapper.copyResponseDto(news);
        response.setCardExcerptEn(excerptEn);
        response.setCardExcerptEs(excerptEs);
        return response;
    }

    /**
     * Converts NewsCreateResponseDto to NewsCreateResponseDto with mobile processed
     * content.
     */
    private NewsCreateResponseDto convertToMobileNews(NewsCreateResponseDto news) {
        // Transform content for mobile consumption
        String processedContentEn = newsContentProcessingService.optimizeForWebPublishing(
                news.getNewsContentEn(), ContentFormat.valueOf(news.getNewsContentFormat()));
        String processedContentEs = newsContentProcessingService.optimizeForWebPublishing(
                news.getNewsContentEs(), ContentFormat.valueOf(news.getNewsContentFormat()));

        NewsCreateResponseDto response = newsMapper.copyResponseDto(news);
        response.setMobileContentEn(processedContentEn);
        response.setMobileContentEs(processedContentEs);
        return response;
    }

    // ========================================
    // Push Notification Integration
    // ========================================

    /**
     * Sends push notification when news is published.
     * 
     * <h4>Industry Best Practice (Option B - Publish-Time Notification):</h4>
     * <ul>
     * <li>Notifications are sent ONLY when news status changes to PUBLISHED</li>
     * <li>Prevents duplicate notifications via {@code newsPushNotificationSent}
     * flag</li>
     * <li>Thumbnail is MANDATORY for rich notifications (1024x512)</li>
     * <li>Breaking news gets HIGH priority, others get NORMAL priority</li>
     * </ul>
     * 
     * <h4>Trigger Points:</h4>
     * <ol>
     * <li>{@code updateWorkflowStatus()} - Manual publish by admin</li>
     * <li>{@code publishScheduledNews()} - Scheduled auto-publish</li>
     * <li>{@code bulkPublish()} - Bulk publish operation</li>
     * </ol>
     * 
     * @param news the news entity being published
     */
    private void sendPublishNotification(NewsMasterEntity news) {
        // Prevent duplicate notifications
        if (Boolean.TRUE.equals(news.getNewsPushNotificationSent())) {
            log.debug("Push notification already sent for newsId={}, skipping duplicate", news.getNewsNewsId());
            return;
        }

        try {
            // Get thumbnail URL - MANDATORY for push notifications
            String thumbnailFilename = news.getNewsThumbnailUrl();
            if (thumbnailFilename == null || thumbnailFilename.isBlank()) {
                log.warn("Push notification skipped - no thumbnail for newsId={}. " +
                        "Thumbnail is required for rich notification display.", news.getNewsNewsId());
                return;
            }

            // Build full thumbnail URL
            String imageUrl = mediaUrlService.buildThumbnailUrl(thumbnailFilename);

            // Build notification content
            String notificationTitle = truncateForNotification(news.getNewsTitleEn(), 65);
            String notificationBody = truncateForNotification(
                    news.getNewsExcerptEn() != null ? news.getNewsExcerptEn() : news.getNewsTitleEn(), 200);

            UUID newsId = news.getNewsNewsId();

            // Send notification based on news type
            if (Boolean.TRUE.equals(news.getNewsIsBreaking())) {
                log.info("Sending BREAKING NEWS push notification on publish - newsId={}, title='{}'",
                        newsId, notificationTitle);
                pushNotificationService.sendBreakingNews(newsId, notificationTitle, notificationBody, imageUrl);
            } else if (Boolean.TRUE.equals(news.getNewsIsFeatured())) {
                log.info("Sending FEATURED NEWS push notification on publish - newsId={}, title='{}'",
                        newsId, notificationTitle);
                pushNotificationService.sendBreakingNews(newsId, notificationTitle, notificationBody, imageUrl);
            } else {
                // Regular news - send as news update to category subscribers
                log.info("Sending NEWS UPDATE push notification on publish - newsId={}, title='{}'",
                        newsId, notificationTitle);
                pushNotificationService.sendNewsUpdate(newsId, null, notificationTitle, notificationBody, imageUrl);
            }

            // Mark notification as sent to prevent duplicates
            news.setNewsPushNotificationSent(true);
            news.setNewsPushNotificationSentAt(Instant.now());
            // Note: The calling method will save the entity

            log.info("Push notification sent successfully on publish - newsId={}", newsId);

        } catch (Exception e) {
            // Don't fail publish operation if notification fails
            log.error("Failed to send push notification on publish for newsId={}, error={}",
                    news.getNewsNewsId(), e.getMessage(), e);
        }
    }

    /**
     * Truncates text for notification display with ellipsis.
     * 
     * @param text      the text to truncate
     * @param maxLength maximum length
     * @return truncated text with ellipsis if needed
     */
    private String truncateForNotification(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Converts an ImageVariant to ThumbnailResponseDto for backward compatibility.
     */
    private ThumbnailResponseDto convertImageVariantToThumbnailResponse(
            ImageProcessingResponseDto.ImageVariant imageVariant,
            String originalFilename,
            ThumbnailResponseDto.ThumbnailSource source) {
        return ThumbnailResponseDto.builder()
                .filename(imageVariant.getFilename())
                .url(imageVariant.getUrl())
                .filePath(imageVariant.getFilePath())
                .originalFilename(originalFilename)
                .fileSize(imageVariant.getFileSize())
                .contentType(imageVariant.getContentType())
                .width(imageVariant.getWidth())
                .height(imageVariant.getHeight())
                .source(source)
                .build();
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

    @Override
    public List<UUID> getAllNewsIds() {
        log.debug("Fetching all news article IDs for bulk operations");
        List<UUID> allIds = newsRepository.findAll()
                .stream()
                .map(NewsMasterEntity::getNewsNewsId)
                .collect(Collectors.toList());
        log.debug("Found {} news articles", allIds.size());
        return allIds;
    }

    /**
     * Clones an existing news article with a new unique slug and title.
     * Creates an independent DRAFT copy ready for editing or variations.
     *
     * <p>
     * <strong>Fields Copied:</strong>
     * <ul>
     * <li>Content: titles, content, excerpts (all languages)</li>
     * <li>Media: images, thumbnails, URLs</li>
     * <li>Metadata: category, tags, keywords, location</li>
     * <li>Configuration: urgency level, content origin, content format</li>
     * </ul>
     * </p>
     *
     * <p>
     * <strong>Fields Reset (Fresh Start):</strong>
     * <ul>
     * <li>Workflow Status: DRAFT (independent lifecycle)</li>
     * <li>Engagement Metrics: all counters reset to 0</li>
     * <li>Publishing Data: all dates cleared</li>
     * <li>Flags: featured/breaking reset to false</li>
     * <li>ID: new UUID (unique article, not linked to source)</li>
     * </ul>
     * </p>
     *
     * @param sourceNewsId the ID of the news article to clone
     * @return cloned news article as DRAFT (independent copy)
     * @throws InvalidRequestException if source news not found
     */
    @Override
    @Transactional(readOnly = false)
    public NewsCreateResponseDto cloneNews(String sourceNewsId, java.util.UUID adminUserId) {
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        log.info("[NEWS-CLONE] [{}] [1/3] START - sourceNewsId='{}'", correlationId, sourceNewsId);

        // Step 1: Fetch source news
        NewsMasterEntity sourceNews = newsRepository.findById(UUID.fromString(sourceNewsId))
                .orElseThrow(() -> {
                    log.error("[NEWS-CLONE] [{}] Source news not found - sourceNewsId='{}'",
                            correlationId, sourceNewsId);
                    return new InvalidRequestException("news", "News not found with id: " + sourceNewsId);
                });

        log.info("[NEWS-CLONE] [{}] [2/3] Source fetched - title='{}', category='{}'",
                correlationId, sourceNews.getNewsTitleEn(), sourceNews.getNewsNewsCategoryId());

        // Step 2: Create cloned entity
        // Generate timestamp suffix for uniqueness
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String timestamp = java.time.LocalDateTime.now().format(formatter);
        String newTitleEn = "News Copy - " + timestamp;
        if (sourceNews.getNewsTitleEn() != null && !sourceNews.getNewsTitleEn().isBlank()) {
            newTitleEn += ": " + sourceNews.getNewsTitleEn();
        }
        String newTitleEs = "Noticias Copia - " + timestamp;
        if (sourceNews.getNewsTitleEs() != null && !sourceNews.getNewsTitleEs().isBlank()) {
            newTitleEs += ": " + sourceNews.getNewsTitleEs();
        }

        // Build a NewsCreateRequestDto from the source entity
        NewsCreateRequestDto cloneRequest = NewsCreateRequestDto.builder()
                .newsTitleEn(newTitleEn)
                .newsTitleEs(newTitleEs)
                .newsContentEn(sourceNews.getNewsContentEn())
                .newsContentEs(sourceNews.getNewsContentEs())
                .newsExcerptEn(sourceNews.getNewsExcerptEn())
                .newsExcerptEs(sourceNews.getNewsExcerptEs())
                .newsContentHtmlEn(sourceNews.getNewsContentHtmlEn())
                .newsContentHtmlEs(sourceNews.getNewsContentHtmlEs())
                .newsContentFormat(
                        sourceNews.getNewsContentFormat() != null ? sourceNews.getNewsContentFormat().toString() : null)
                .newsTags(sourceNews.getNewsTags())
                // MEDIA FIELDS (only those present in DTO)
                .imageVideoFile(null) // File upload not cloned
                .thumbnailFile(null) // File upload not cloned
                // CATEGORIZATION & SOURCE
                .newsNewsCategoryId(sourceNews.getNewsNewsCategoryId())
                .newsSourceUrl(sourceNews.getNewsSourceUrl())
                .newsSourceAuthorName(sourceNews.getNewsSourceAuthorName())
                .newsSourceAgencyId(sourceNews.getNewsSourceAgencyId())
                .newsContentOrigin(
                        sourceNews.getNewsContentOrigin() != null ? sourceNews.getNewsContentOrigin().toString() : null)
                // WORKFLOW & PUBLISHING
                .newsWorkflowStatus("DRAFT")
                .newsIsFeatured(false)
                .newsScheduledPublishAt(null)
                .newsIsBreaking(false)
                .newsBreakingExpiresAt(null)
                .newsEmbargoUntil(null)
                .newsExpiresAt(sourceNews.getNewsExpiresAt() != null ? sourceNews.getNewsExpiresAt().toString() : null)
                .newsUrgencyLevel(
                        sourceNews.getNewsUrgencyLevel() != null ? sourceNews.getNewsUrgencyLevel().toString() : null)
                .newsTargetAudience(sourceNews.getNewsTargetAudience())
                .newsReadTimeMinutes(sourceNews.getNewsReadTimeMinutes())
                // LOCATION
                .newsCountryCode(sourceNews.getNewsCountryCode())
                .newsRegion(sourceNews.getNewsRegion())
                .newsCity(sourceNews.getNewsCity())
                .newsLatitude(sourceNews.getNewsLatitude())
                .newsLongitude(sourceNews.getNewsLongitude())
                // MONETIZATION
                .newsIsSponsored(sourceNews.getNewsIsSponsored())
                .newsSponsorName(sourceNews.getNewsSponsorName())
                .newsSponsorLogoUrl(sourceNews.getNewsSponsorLogoUrl())
                .newsSponsorWebsiteUrl(sourceNews.getNewsSponsorWebsiteUrl())
                .newsIsPremium(sourceNews.getNewsIsPremium())
                .newsPremiumTier(sourceNews.getNewsPremiumTier())
                // SEO
                .newsMetaTitle(sourceNews.getNewsMetaTitle())
                .newsMetaDescription(sourceNews.getNewsMetaDescription())
                .newsKeywords(sourceNews.getNewsKeywords())
                .newsCanonicalUrl(sourceNews.getNewsCanonicalUrl())
                // SERIES
                .newsSeriesId(sourceNews.getNewsSeriesId())
                .newsSeriesOrder(sourceNews.getNewsSeriesOrder())
                // INTERNAL
                .newsEditorNotes(sourceNews.getNewsEditorNotes())
                // SYSTEM
                .createdBy(adminUserId)
                .build();

        // Use buildNewsEntity to construct the entity (like create)
        NewsMasterEntity clonedNews = buildNewsEntity(cloneRequest);
        // Set clone-specific fields
        clonedNews.setNewsPreviousVersionId(sourceNews.getNewsNewsId());
        // If the clone is scheduled, set newsScheduledBy to the adminUserId
        if (clonedNews.getNewsWorkflowStatus() == WorkflowStatus.SCHEDULED) {
            clonedNews.setNewsScheduledBy(adminUserId);
        }

        /*
         * NOTE: The following fields are NOT present in NewsCreateRequestDto and are
         * not set by buildNewsEntity.
         * These are system-generated or post-upload fields (media URLs, filenames,
         * etc.) that must be copied
         * explicitly from the source entity to preserve media references in the clone.
         * This ensures that all
         * media-related data (such as URLs, filenames, types, and sizes) is retained,
         * while all other fields
         * are handled by the DTO builder or intentionally reset for a new entity.
         *
         * This explicit mapping prevents accidental omission or overwriting of media
         * fields and keeps the clone
         * logic robust and clear. If new non-DTO fields are added in the future that
         * need to be cloned, they
         * should be added here with a similar comment.
         */
        clonedNews.setNewsMediaFileUrl(sourceNews.getNewsMediaFileUrl());
        clonedNews.setNewsMediaFileName(sourceNews.getNewsMediaFileName());
        clonedNews.setNewsMediaFileType(sourceNews.getNewsMediaFileType());
        clonedNews.setNewsMediaFileSize(sourceNews.getNewsMediaFileSize());
        clonedNews.setNewsMediaType(sourceNews.getNewsMediaType());
        clonedNews.setNewsThumbnailUrl(sourceNews.getNewsThumbnailUrl());
        clonedNews.setNewsImageCardUrl(sourceNews.getNewsImageCardUrl());
        clonedNews.setNewsImageHeroUrl(sourceNews.getNewsImageHeroUrl());

        // Step 3: Save cloned news
        NewsMasterEntity saved = newsRepository.saveAndFlush(clonedNews);
        log.info("[NEWS-CLONE] [{}] [3/3] COMPLETE - cloneId='{}', sourceId='{}', duration={}ms",
                correlationId, saved.getNewsNewsId(), sourceNewsId, System.currentTimeMillis() - startTime);

        // Audit log the clone operation
        try {
            RequestClientInfoDto clientInfo = RequestClientInfoDto.minimal("system", "system");

            auditingUtility.audit(
                    "NEWS",
                    sourceNews.getCreatedBy(),
                    NewsAuditActions.NEWS_CREATED, // Use CREATED action with reason indicating clone
                    saved.getNewsNewsId(),
                    "News: " + saved.getNewsNewsId(),
                    "Cloned from news article: " + sourceNewsId,
                    clientInfo,
                    "INFO",
                    NewsAuditLog.class,
                    newsAuditLogRepository);
            log.debug("[NEWS-CLONE] [{}] Audit logged successfully", correlationId);
        } catch (Exception e) {
            log.error("[NEWS-CLONE] [{}] Audit logging failed (non-blocking) - error={}",
                    correlationId, e.getMessage());
            // Soft failure - don't cascade
        }

        return newsMapper.toNewsCreateResponseDto(saved);
    }

    /**
     * Maps generic validation messages to specific field names for RFC 7807
     * compliance.
     * 
     * @param errors List of validation error messages
     * @return Map of field names to error messages
     */
    private Map<String, String> parseValidationErrors(List<String> errors) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        if (errors == null || errors.isEmpty()) {
            return fieldErrors;
        }

        for (String error : errors) {
            // Parse error messages to extract field names
            // Examples: "Title is required", "Content is too short", "Content is too long"
            String fieldName = null;
            String errorMessage = error;

            if (error.toLowerCase().contains("title")) {
                // For now, map both "Title is..." to the English content field
                // In future, could distinguish between En and Es based on context
                fieldName = "newsTitleEn";
            } else if (error.toLowerCase().contains("content")) {
                fieldName = "newsContentEn";
            } else if (error.toLowerCase().contains("excerpt")) {
                fieldName = "newsExcerptEn";
            } else if (error.toLowerCase().contains("category")) {
                fieldName = "newsNewsCategoryId";
            } else if (error.toLowerCase().contains("media") || error.toLowerCase().contains("file")) {
                fieldName = "imageVideoFile";
            } else if (error.toLowerCase().contains("source")) {
                fieldName = "newsSourceUrl";
            }

            // If we matched a field, add it to the map
            if (fieldName != null) {
                fieldErrors.putIfAbsent(fieldName, errorMessage);
            } else {
                // Fallback: Add unmatched errors to a general field
                fieldErrors.putIfAbsent("news", errorMessage);
            }
        }

        return fieldErrors;
    }

    /**
     * Moves all associated media files (main, thumbnail, card, hero) to backup if
     * not referenced by other news.
     * Used by both update and permanent delete operations for robust, DRY logic.
     *
     * @param newsEntity    The news entity whose media files are to be
     *                      checked/moved
     * @param excludeNewsId The newsId to exclude from reference check (current
     *                      news)
     */
    private void backupUnusedMediaFiles(NewsMasterEntity newsEntity, UUID excludeNewsId) {
        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        // Main media file
        String mediaFileName = newsEntity.getNewsMediaFileName();
        if (mediaFileName != null && !mediaFileName.isBlank()) {
            long referenceCount = newsRepository.countByMediaFileNameExcludingNewsId(mediaFileName, excludeNewsId);
            if (referenceCount == 0) {
                try {
                    newsMediaStorageService.moveMediaVariantToBackup(mediaFileName, "main");
                    log.info("[MEDIA-BACKUP] [{}] Main media '{}' moved to backup (no other references)", correlationId,
                            mediaFileName);
                } catch (Exception e) {
                    log.warn("[MEDIA-BACKUP] [{}] Failed to move main media '{}' to backup: {}", correlationId,
                            mediaFileName, e.getMessage());
                }
            } else {
                log.info("[MEDIA-BACKUP] [{}] Main media '{}' NOT moved to backup ({} other references exist)",
                        correlationId, mediaFileName, referenceCount);
            }
        }

        // Thumbnail
        String thumbnailFilename = extractFilenameFromUrl(newsEntity.getNewsThumbnailUrl());
        if (thumbnailFilename != null && !thumbnailFilename.isBlank()) {
            long count = newsRepository.countByThumbnailUrlExcludingNewsId(thumbnailFilename, excludeNewsId);
            if (count == 0) {
                try {
                    newsMediaStorageService.moveMediaVariantToBackup(thumbnailFilename, "thumbnail");
                    log.info("[MEDIA-BACKUP] [{}] Thumbnail '{}' moved to backup (no other references)", correlationId,
                            thumbnailFilename);
                } catch (Exception e) {
                    log.warn("[MEDIA-BACKUP] [{}] Failed to move thumbnail '{}' to backup: {}", correlationId,
                            thumbnailFilename, e.getMessage());
                }
            } else {
                log.info("[MEDIA-BACKUP] [{}] Thumbnail '{}' NOT moved to backup ({} other references exist)",
                        correlationId, thumbnailFilename, count);
            }
        }

        // Card image
        String cardFilename = extractFilenameFromUrl(newsEntity.getNewsImageCardUrl());
        if (cardFilename != null && !cardFilename.isBlank()) {
            long count = newsRepository.countByCardUrlExcludingNewsId(cardFilename, excludeNewsId);
            if (count == 0) {
                try {
                    newsMediaStorageService.moveMediaVariantToBackup(cardFilename, "card");
                    log.info("[MEDIA-BACKUP] [{}] Card image '{}' moved to backup (no other references)", correlationId,
                            cardFilename);
                } catch (Exception e) {
                    log.warn("[MEDIA-BACKUP] [{}] Failed to move card image '{}' to backup: {}", correlationId,
                            cardFilename, e.getMessage());
                }
            } else {
                log.info("[MEDIA-BACKUP] [{}] Card image '{}' NOT moved to backup ({} other references exist)",
                        correlationId, cardFilename, count);
            }
        }

        // Hero image
        String heroFilename = extractFilenameFromUrl(newsEntity.getNewsImageHeroUrl());
        if (heroFilename != null && !heroFilename.isBlank()) {
            long count = newsRepository.countByHeroUrlExcludingNewsId(heroFilename, excludeNewsId);
            if (count == 0) {
                try {
                    newsMediaStorageService.moveMediaVariantToBackup(heroFilename, "hero");
                    log.info("[MEDIA-BACKUP] [{}] Hero image '{}' moved to backup (no other references)", correlationId,
                            heroFilename);
                } catch (Exception e) {
                    log.warn("[MEDIA-BACKUP] [{}] Failed to move hero image '{}' to backup: {}", correlationId,
                            heroFilename, e.getMessage());
                }
            } else {
                log.info("[MEDIA-BACKUP] [{}] Hero image '{}' NOT moved to backup ({} other references exist)",
                        correlationId, heroFilename, count);
            }
        }
    }

    /**
     * Archives an old media file if it is unused by any other news item.
     * Handles filename extraction, null checks, reference counting, and archival
     * call.
     *
     * @param oldValue          The old filename or URL (can be null)
     * @param newValue          The new filename (can be null)
     * @param label             Media type label for logging (e.g., "main",
     *                          "thumbnail", "card", "hero")
     * @param newsId            The current news ID to exclude from reference check
     * @param filenameExtractor Function to extract filename from the value (String
     *                          -> String)
     * @param referenceCounter  Function to count references (String filename, UUID
     *                          excludeId) -> long
     * @param archivalAction    Function to archive the file (String filename)
     * @param correlationId     Correlation ID for logging
     */
    private void archiveOldMediaIfUnused(
            String oldValue,
            String newValue,
            String label,
            UUID newsId,
            java.util.function.Function<String, String> filenameExtractor,
            java.util.function.BiFunction<String, UUID, Long> referenceCounter,
            java.util.function.Consumer<String> archivalAction,
            String correlationId) {
        String oldFilename = oldValue == null ? null : filenameExtractor.apply(oldValue);
        String newFilename = newValue == null ? null : filenameExtractor.apply(newValue);
        if (oldFilename != null && !oldFilename.equals(newFilename)) {
            long refCount = referenceCounter.apply(oldFilename, newsId);
            if (refCount == 0) {
                archivalAction.accept(oldFilename);
                log.info("[NEWS-UPDATE] [{}] Old {} '{}' moved to backup (no other references)", correlationId, label,
                        oldFilename);
            } else {
                log.info("[NEWS-UPDATE] [{}] Old {} '{}' NOT moved to backup ({} other references exist)",
                        correlationId, label, oldFilename, refCount);
            }
        } else {
            log.info("[NEWS-UPDATE] [{}] Old {} '{}' not moved (filename unchanged or null)", correlationId, label,
                    oldFilename);
        }
    }

}
