package com.mmva.newsapp.domain.newsengagement.comments.service;

// ========================================
// Client Context Imports
// ========================================
import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentCountDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentRequestDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentResponseDto;
import com.mmva.newsapp.domain.newsengagement.comments.dto.NewsCommentUpdateDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.comments.exception.NewsCommentNotFoundException;
import com.mmva.newsapp.infrastructure.requestanalytics.exception.RateLimitExceededException;
import com.mmva.newsapp.infrastructure.common.exception.UnauthorizedAccessException;
import com.mmva.newsapp.domain.appuser.exception.core.AppUserNotFoundException;

// ========================================
// Mapper Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.comments.mapper.NewsCommentMapper;

// ========================================
// Model Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.comments.enums.NewsCommentStatus;
import com.mmva.newsapp.domain.newsengagement.comments.model.NewsComment;
import com.mmva.newsapp.domain.newsengagement.comments.model.NewsCommentLike;
import com.mmva.newsapp.domain.newsengagement.comments.model.NewsCommentReport;
import com.mmva.newsapp.domain.appuser.repository.core.AppUserRepository;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;
// ========================================
// Repository Imports
// ========================================
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import com.mmva.newsapp.infrastructure.common.ratelimit.service.RateLimiterService;
import com.mmva.newsapp.domain.newsengagement.comments.repository.NewsCommentLikeRepository;
import com.mmva.newsapp.domain.newsengagement.comments.repository.NewsCommentReportRepository;
import com.mmva.newsapp.domain.newsengagement.comments.repository.NewsCommentRepository;

// ========================================
// Lombok Imports
// ========================================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ========================================
// Spring Framework Imports
// ========================================
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ========================================
// Java Core Imports
// ========================================
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link NewsCommentService} for managing newsapp comments.
 * 
 * <p>
 * Provides CRUD operations, newsengagement features (likes, reports), and
 * moderation capabilities with comprehensive audit logging and client
 * context capture for security and analytics.
 * </p>
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2024-06-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsCommentServiceImpl implements NewsCommentService {

    // ========================================
    // Constants
    // ========================================

    private static final String ENTITY_NAME = "Comment";

    // ========================================
    // Dependencies
    // ========================================

    private final NewsCommentRepository newsCommentRepository;
    private final NewsRepository newsRepository;
    private final NewsCommentMapper newsCommentMapper;
    private final RateLimiterService rateLimiterService;
    private final AppUserRepository userRepository;
    private final NewsCommentAuditLogService newsCommentAuditLogService;
    private final NewsCommentLikeRepository newsCommentLikeRepository;
    private final NewsCommentReportRepository newsCommentReportRepository;
    private final ClientContextService clientContextService;

    // =========================
    // Public API - Approved Comments Only
    // =========================

    @Override
    public List<NewsCommentResponseDto> getApprovedReplies(UUID parentCommentId) {
        log.debug("NewsCommentService: Fetching approved replies for comment: {}", parentCommentId);
        return newsCommentRepository.findApprovedReplies(parentCommentId)
                .stream()
                .map(newsCommentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NewsCommentResponseDto> getApprovedCommentsByNewsId(UUID newsId, Pageable pageable) {
        log.debug("NewsCommentService: Fetching approved comments for newsapp: {} - page: {}, size: {}",
                newsId, pageable.getPageNumber(), pageable.getPageSize());
        return newsCommentRepository.findApprovedByNewsCommentsNewsId(newsId, pageable)
                .map(newsCommentMapper::toResponseDto);
    }

    @Override
    public List<NewsCommentResponseDto> getApprovedCommentsByNewsId(UUID newsId) {
        log.debug("NewsCommentService: Fetching all approved comments for newsapp: {}", newsId);
        return newsCommentRepository.findApprovedByNewsCommentsNewsId(newsId)
                .stream()
                .map(newsCommentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getApprovedCommentCountByNewsId(UUID newsId) {
        log.debug("NewsCommentService: Getting approved comment count for newsapp: {}", newsId);
        return newsCommentRepository.countApprovedByNewsCommentsNewsId(newsId);
    }

    @Override
    public List<NewsCommentResponseDto> getApprovedCommentThreadForNews(UUID newsId) {
        log.debug("NewsCommentService: Fetching approved comment thread for newsapp: {}", newsId);
        List<NewsComment> topLevel = newsCommentRepository.findApprovedByNewsCommentsNewsId(newsId)
                .stream()
                .filter(c -> c.getNewsCommentsParentId() == null)
                .collect(Collectors.toList());
        return topLevel.stream()
                .map(this::mapWithApprovedReplies)
                .collect(Collectors.toList());
    }

    // =========================
    // User API - User's Own Comments
    // =========================

    @Override
    public List<NewsCommentResponseDto> getMyComments(UUID userId) {
        log.debug("NewsCommentService: Fetching all comments for user: {}", userId);
        return newsCommentRepository.findByNewsCommentsUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(newsCommentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NewsCommentResponseDto> getMyComments(UUID userId, Pageable pageable) {
        log.debug("NewsCommentService: Fetching comments for user: {} - page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        return newsCommentRepository.findByNewsCommentsUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(newsCommentMapper::toResponseDto);
    }

    // =========================
    // Admin API - Full Access
    // =========================

    @Override
    public Page<NewsCommentResponseDto> getCommentsWithFilters(String status, UUID newsId, UUID userId,
            Pageable pageable) {
        log.debug("NewsCommentService: Fetching comments with filters - status: {}, newsId: {}, userId: {}", status,
                newsId, userId);
        NewsCommentStatus statusEnum = parseStatus(status);
        return newsCommentRepository.findWithFilters(statusEnum, newsId, userId, pageable)
                .map(newsCommentMapper::toResponseDto);
    }

    @Override
    public Page<NewsCommentResponseDto> getAllComments(Pageable pageable) {
        log.debug("NewsCommentService: Fetching all comments - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        // Admin API: Use SoftDeleteSpec.includeDeleted() to see all records including
        // soft-deleted
        return newsCommentRepository.findAll(SoftDeleteSpec.includeDeleted(), pageable)
                .map(newsCommentMapper::toResponseDto);
    }

    // =========================
    // Legacy/Shared Read Methods
    // =========================

    @Override
    public List<NewsCommentResponseDto> getReplies(UUID parentCommentId) {
        log.debug("NewsCommentService: Fetching replies for comment: {}", parentCommentId);
        return newsCommentRepository.findByNewsCommentsParentIdOrderByNewsCommentsCommentedAtAsc(parentCommentId)
                .stream()
                .map(newsCommentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NewsCommentResponseDto> getCommentThreadForNews(UUID newsId) {
        log.debug("NewsCommentService: Fetching full comment thread for newsapp: {}", newsId);
        List<NewsComment> topLevel = newsCommentRepository
                .findByNewsCommentsNewsIdOrderByNewsCommentsCommentedAtDesc(newsId)
                .stream()
                .filter(c -> c.getNewsCommentsParentId() == null)
                .collect(Collectors.toList());
        return topLevel.stream()
                .map(this::mapWithReplies)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getUsersWhoLikedComment(UUID commentId) {
        log.debug("NewsCommentService: Fetching users who liked comment: {}", commentId);
        return newsCommentLikeRepository.findAllByNewsCommentLikesCommentIdAndNewsCommentLikesLikedTrue(commentId)
                .stream()
                .map(NewsCommentLike::getNewsCommentLikesUserId)
                .collect(Collectors.toList());
    }

    // =========================
    // Engagement Operations
    // =========================

    @Override
    @Transactional
    public void likeComment(UUID commentId, UUID userId) {
        log.info("NewsCommentService: User {} liking comment: {}", userId, commentId);
        NewsCommentLike like = newsCommentLikeRepository
                .findByNewsCommentLikesCommentIdAndNewsCommentLikesUserId(commentId, userId)
                .orElseGet(() -> NewsCommentLike.builder()
                        .newsCommentLikesCommentId(commentId)
                        .newsCommentLikesUserId(userId)
                        .build());
        like.setNewsCommentLikesLiked(true);
        like.setNewsCommentLikesLikedAt(Instant.now());
        enrichCommentLikeWithContext(like);
        newsCommentLikeRepository.save(like);
        logAudit(userId, commentId, "LIKE", "User liked the comment");
    }

    @Override
    @Transactional
    public void dislikeComment(UUID commentId, UUID userId) {
        log.info("NewsCommentService: User {} disliking comment: {}", userId, commentId);
        NewsCommentLike like = newsCommentLikeRepository
                .findByNewsCommentLikesCommentIdAndNewsCommentLikesUserId(commentId, userId)
                .orElseGet(() -> NewsCommentLike.builder()
                        .newsCommentLikesCommentId(commentId)
                        .newsCommentLikesUserId(userId)
                        .build());
        like.setNewsCommentLikesLiked(false);
        like.setNewsCommentLikesLikedAt(Instant.now());
        enrichCommentLikeWithContext(like);
        newsCommentLikeRepository.save(like);
        logAudit(userId, commentId, "DISLIKE", "User disliked the comment");
    }

    @Override
    public long getLikeCount(UUID commentId) {
        log.debug("NewsCommentService: Getting like count for comment: {}", commentId);
        return newsCommentLikeRepository.countByNewsCommentLikesCommentIdAndNewsCommentLikesLikedTrue(commentId);
    }

    @Override
    @Transactional
    public void reportComment(UUID commentId, UUID userId, String reason) {
        log.info("NewsCommentService: User {} reporting comment: {} - reason: {}", userId, commentId, reason);
        NewsCommentReport report = NewsCommentReport.builder()
                .newsCommentReportsCommentId(commentId)
                .newsCommentReportsUserId(userId)
                .newsCommentReportsReason(reason)
                .newsCommentReportsReportedAt(Instant.now())
                .build();
        enrichCommentReportWithContext(report);
        newsCommentReportRepository.save(report);
        logAudit(userId, commentId, "REPORT", reason);
    }

    @Override
    public long getReportCount(UUID commentId) {
        log.debug("NewsCommentService: Getting report count for comment: {}", commentId);
        return newsCommentReportRepository.countByNewsCommentReportsCommentId(commentId);
    }

    /**
     * Enriches a NewsCommentLike with client context.
     */
    private void enrichCommentLikeWithContext(NewsCommentLike like) {
        try {
            ClientContextDto ctx = clientContextService.getCurrentContext();
            if (ctx == null)
                return;

            if (ctx.deviceType() != null) {
                like.setNewsCommentLikesDeviceType(ctx.deviceType().name());
            }
            like.setNewsCommentLikesCountryCode(ctx.countryCode());
            if (ctx.channel() != null) {
                like.setNewsCommentLikesChannel(ctx.channel().name());
            }
        } catch (Exception e) {
            log.debug("NewsCommentService: Failed to enrich comment like with context: {}", e.getMessage());
        }
    }

    /**
     * Enriches a NewsCommentReport with client context for abuse tracking.
     */
    private void enrichCommentReportWithContext(NewsCommentReport report) {
        try {
            ClientContextDto ctx = clientContextService.getCurrentContext();
            if (ctx == null)
                return;

            report.setNewsCommentReportsIpAddress(ctx.ipAddress());
            if (ctx.deviceType() != null) {
                report.setNewsCommentReportsDeviceType(ctx.deviceType().name());
            }
            report.setNewsCommentReportsCountryCode(ctx.countryCode());
            if (ctx.channel() != null) {
                report.setNewsCommentReportsChannel(ctx.channel().name());
            }
        } catch (Exception e) {
            log.debug("NewsCommentService: Failed to enrich comment report with context: {}", e.getMessage());
        }
    }

    // =========================
    // Create Operations
    // =========================

    @Override
    @Transactional
    public NewsCommentResponseDto addComment(NewsCommentRequestDto dto) {
        UUID userId = dto.getNewsCommentsUserId();
        log.info("NewsCommentService: Adding comment for newsapp: {} by user: {} (parent: {})",
                dto.getNewsCommentsNewsId(), userId, dto.getNewsCommentsParentId());

        if (userId != null) {
            validatePublicUser(userId);
            checkRateLimit(dto);
        }

        try {
            NewsComment newsComment = newsCommentMapper.toEntity(dto);
            newsComment.setCreatedAt(Instant.now());
            newsComment.setCreatedBy(userId);

            // Enrich with client context for security and analytics
            enrichWithClientContext(newsComment);

            NewsComment savedComment = newsCommentRepository.save(newsComment);

            // Atomically increment counter on news article (OBJECT type - CRUD operations)
            // Distinguish between top-level comments and replies
            boolean isReply = dto.getNewsCommentsParentId() != null;
            if (!isReply) {
                newsRepository.incrementCommentCount(dto.getNewsCommentsNewsId());
            } else {
                newsRepository.incrementReplyCount(dto.getNewsCommentsNewsId());
            }

            // Build response with updated counts
            NewsCommentResponseDto response = newsCommentMapper.toResponseDto(savedComment);
            response.setUpdatedCommentCount(newsRepository.getCommentCount(dto.getNewsCommentsNewsId()));
            response.setUpdatedReplyCount(newsRepository.getReplyCount(dto.getNewsCommentsNewsId()));

            log.info("NewsCommentService: Comment created with ID: {} (country: {}, risk: {})",
                    savedComment.getNewsCommentsId(), savedComment.getNewsCommentsCountryCode(),
                    savedComment.getNewsCommentsRiskScore());
            logAudit(userId, savedComment.getNewsCommentsId(), "CREATE", "User added a comment");
            return response;
        } catch (Exception e) {
            log.error("NewsCommentService: Failed to add comment for newsapp: {} - Error: {}",
                    dto.getNewsCommentsNewsId(),
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Enriches a NewsComment entity with client context information.
     * Captures device, location, and security context for analytics and spam
     * detection.
     * 
     * @param comment the entity to enrich
     */
    private void enrichWithClientContext(NewsComment comment) {
        try {
            ClientContextDto ctx = clientContextService.getCurrentContext();
            if (ctx == null) {
                log.debug("NewsCommentService: No client context available for comment enrichment");
                return;
            }

            // Network
            if (comment.getNewsCommentsIpAddress() == null && ctx.ipAddress() != null) {
                comment.setNewsCommentsIpAddress(ctx.ipAddress());
            }

            // Device information
            if (ctx.deviceType() != null) {
                comment.setNewsCommentsDeviceType(ctx.deviceType().name());
            }
            comment.setNewsCommentsDeviceFingerprint(ctx.deviceFingerprint());
            comment.setNewsCommentsBrowserName(ctx.browserName());
            comment.setNewsCommentsOsName(ctx.osName());

            // Location
            comment.setNewsCommentsCountryCode(ctx.countryCode());
            comment.setNewsCommentsCity(ctx.city());

            // Security context (important for spam/abuse detection)
            comment.setNewsCommentsIsBot(ctx.isBot() != null ? ctx.isBot() : false);
            comment.setNewsCommentsIsAnonymized(ctx.isAnonymized() != null ? ctx.isAnonymized() : false);
            comment.setNewsCommentsRiskScore(ctx.riskScore());

            // Request context
            if (ctx.channel() != null) {
                comment.setNewsCommentsChannel(ctx.channel().name());
            }
            comment.setNewsCommentsLanguage(ctx.primaryLanguage());

            log.debug("NewsCommentService: Enriched comment with context - IP: {}, country: {}, risk: {}",
                    ctx.ipAddress(), ctx.countryCode(), ctx.riskScore());

            // Log warning for high-risk comments
            if (ctx.riskScore() != null && ctx.riskScore() > 70) {
                log.warn("NewsCommentService: High-risk comment detected - userId: {}, risk: {}, factors: {}",
                        comment.getNewsCommentsUserId(), ctx.riskScore(), ctx.riskFactors());
            }
        } catch (Exception e) {
            log.warn("NewsCommentService: Failed to enrich comment with client context: {}", e.getMessage());
            // Don't fail the comment creation if context extraction fails
        }
    }

    // =========================
    // Read Operations
    // =========================

    @Override
    public NewsCommentResponseDto getCommentById(UUID id) {
        log.debug("NewsCommentService: Fetching comment by ID: {}", id);
        NewsComment newsComment = findByIdOrThrow(id);
        return newsCommentMapper.toResponseDto(newsComment);
    }

    @Override
    public List<NewsCommentResponseDto> getCommentsByNewsId(UUID newsId) {
        log.debug("NewsCommentService: Fetching comments for newsapp: {}", newsId);
        return newsCommentRepository.findByNewsCommentsNewsIdOrderByNewsCommentsCommentedAtDesc(newsId)
                .stream()
                .map(newsCommentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NewsCommentResponseDto> getCommentsByNewsId(UUID newsId, Pageable pageable) {
        log.debug("NewsCommentService: Fetching comments for newsapp: {} - page: {}, size: {}",
                newsId, pageable.getPageNumber(), pageable.getPageSize());
        return newsCommentRepository.findByNewsCommentsNewsIdOrderByNewsCommentsCommentedAtDesc(newsId, pageable)
                .map(newsCommentMapper::toResponseDto);
    }

    @Override
    public List<NewsCommentResponseDto> getCommentsByUserId(UUID userId) {
        log.debug("NewsCommentService: Fetching comments for user: {}", userId);
        return newsCommentRepository.findByNewsCommentsUserId(userId)
                .stream()
                .map(newsCommentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long getCommentCountByNewsId(UUID newsId) {
        log.debug("NewsCommentService: Getting comment count for newsapp: {}", newsId);
        Long count = newsCommentRepository.countCommentsByNewsCommentsNewsId(newsId);
        log.debug("NewsCommentService: Comment count for newsapp {}: {}", newsId, count);
        return count;
    }

    // =========================
    // Update Operations
    // =========================

    @Override
    @Transactional
    public NewsCommentResponseDto updateComment(UUID id, UUID userId, NewsCommentUpdateDto dto) {
        log.info("NewsCommentService: Updating comment: {} by user: {}", id, userId);
        validatePublicUser(userId);
        validateCommentOwnership(id, userId);

        NewsComment newsComment = findByIdOrThrow(id);
        try {
            newsCommentMapper.updateEntityFromDto(dto, newsComment);
            newsComment.setUpdatedAt(Instant.now());
            newsComment.setUpdatedBy(userId);
            NewsComment updatedComment = newsCommentRepository.save(newsComment);

            log.info("NewsCommentService: Comment updated: {} by user: {}", id, userId);
            logAudit(userId, id, "UPDATE", "User updated their own comment");
            return newsCommentMapper.toResponseDto(updatedComment);
        } catch (Exception e) {
            log.error("Failed to update comment: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public NewsCommentResponseDto updateComment(UUID id, NewsCommentUpdateDto dto) {
        log.info("NewsCommentService: Updating comment: {} (legacy - no ownership validation)", id);
        NewsComment newsComment = findByIdOrThrow(id);
        try {
            newsCommentMapper.updateEntityFromDto(dto, newsComment);
            newsComment.setUpdatedAt(Instant.now());
            newsComment.setUpdatedBy(newsComment.getNewsCommentsUserId());
            NewsComment updatedComment = newsCommentRepository.save(newsComment);

            log.info("NewsCommentService: Comment updated: {}", id);
            logAudit(newsComment.getNewsCommentsUserId(), id, "UPDATE", "User updated the comment");
            return newsCommentMapper.toResponseDto(updatedComment);
        } catch (Exception e) {
            log.error("NewsCommentService: Failed to update comment: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // =========================
    // Delete Operations
    // =========================

    @Override
    @Transactional
    public NewsCommentCountDto removeCommentByUser(UUID commentId, UUID userId) {
        log.info("NewsCommentService: User {} removing comment: {}", userId, commentId);
        validatePublicUser(userId);
        validateCommentOwnership(commentId, userId);
        return softDeleteComment(commentId, userId);
    }

    @Override
    @Transactional
    public NewsCommentCountDto removeCommentByAdmin(UUID commentId, UUID adminId) {
        log.info("NewsCommentService: Admin {} removing comment: {}", adminId, commentId);
        return softDeleteComment(commentId, adminId);
    }

    @Override
    @Transactional
    public NewsCommentCountDto softDeleteComment(UUID commentId, UUID deletedBy) {
        log.info("NewsCommentService: Soft deleting comment: {} by: {}", commentId, deletedBy);
        NewsComment comment = findByIdOrThrow(commentId);
        UUID newsId = comment.getNewsCommentsNewsId();

        try {
            comment.setNewsCommentsStatus(NewsCommentStatus.DELETED);
            comment.setDeletedAt(Instant.now());
            comment.setDeletedBy(deletedBy);
            newsCommentRepository.save(comment);

            // Atomically decrement counter on news article (OBJECT type - CRUD operations)
            // Distinguish between top-level comments and replies
            if (comment.getNewsCommentsParentId() == null) {
                newsRepository.decrementCommentCount(newsId);
            } else {
                newsRepository.decrementReplyCount(newsId);
            }

            // Fetch updated counts
            Long updatedCommentCount = newsRepository.getCommentCount(newsId);
            Long updatedReplyCount = newsRepository.getReplyCount(newsId);

            String description = deletedBy.equals(comment.getNewsCommentsUserId())
                    ? "User deleted their own comment"
                    : "Admin deleted the comment";
            log.info("NewsCommentService: Comment soft deleted: {} by: {} (comments: {}, replies: {})",
                    commentId, deletedBy, updatedCommentCount, updatedReplyCount);
            logAudit(deletedBy, commentId, "DELETE", description);

            return new NewsCommentCountDto(updatedCommentCount, updatedReplyCount);
        } catch (Exception e) {
            log.error("NewsCommentService: Failed to soft delete comment: {} - Error: {}", commentId, e.getMessage());
            throw e;
        }
    }

    // =========================
    // Admin Moderation
    // =========================

    @Override
    @Transactional
    public NewsCommentResponseDto approveComment(UUID id, UUID adminId) {
        log.info("NewsCommentService: Admin {} approving comment: {}", adminId, id);
        NewsComment comment = findByIdOrThrow(id);

        comment.setNewsCommentsStatus(NewsCommentStatus.APPROVED);
        comment.setUpdatedAt(Instant.now());
        comment.setUpdatedBy(adminId);
        NewsComment updated = newsCommentRepository.save(comment);

        log.info("NewsCommentService: Comment approved: {} by admindashboard: {}", id, adminId);
        logAudit(adminId, id, "APPROVE", "Admin approved the comment");
        return newsCommentMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public NewsCommentResponseDto rejectComment(UUID id, UUID adminId) {
        log.info("NewsCommentService: Admin {} rejecting comment: {}", adminId, id);
        NewsComment comment = findByIdOrThrow(id);

        comment.setNewsCommentsStatus(NewsCommentStatus.REJECTED);
        comment.setUpdatedAt(Instant.now());
        comment.setUpdatedBy(adminId);
        NewsComment updated = newsCommentRepository.save(comment);

        log.info("NewsCommentService: Comment rejected: {} by admindashboard: {}", id, adminId);
        logAudit(adminId, id, "REJECT", "Admin rejected the comment");
        return newsCommentMapper.toResponseDto(updated);
    }

    // =========================
    // Private Helper Methods
    // =========================

    /**
     * Finds a comment by ID or throws an exception if not found.
     *
     * @param id the comment UUID
     * @return the found NewsComment entity
     * @throws NewsCommentNotFoundException if comment not found
     */
    private NewsComment findByIdOrThrow(UUID id) {
        return newsCommentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("NewsCommentService: {} not found with ID: {}", ENTITY_NAME, id);
                    return new NewsCommentNotFoundException(id);
                });
    }

    /**
     * Validates that a public user exists.
     *
     * @param userId the user UUID
     * @throws AppUserNotFoundException if user not found
     */
    private void validatePublicUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new AppUserNotFoundException("User not found with ID: " + userId);
        }
    }

    /**
     * Validates that the comment belongs to the specified user.
     *
     * @param commentId the comment UUID
     * @param userId    the user UUID
     * @throws UnauthorizedAccessException if user doesn't own the comment
     */
    private void validateCommentOwnership(UUID commentId, UUID userId) {
        NewsComment comment = findByIdOrThrow(commentId);
        if (!comment.getNewsCommentsUserId().equals(userId)) {
            throw new UnauthorizedAccessException(userId, "modify comment " + commentId);
        }
    }

    /**
     * Checks rate limit for comment creation.
     *
     * @param dto the comment request
     * @throws RateLimitExceededException if rate limit exceeded
     */
    private void checkRateLimit(NewsCommentRequestDto dto) {
        UUID userId = dto.getNewsCommentsUserId();
        String rateLimitKey = dto.getNewsCommentsParentId() != null
                ? "comment-reply:" + userId + ":" + dto.getNewsCommentsParentId()
                : "comment-newsapp:" + userId + ":" + dto.getNewsCommentsNewsId();

        if (!rateLimiterService.isAllowed(rateLimitKey)) {
            long retryAfter = rateLimiterService.getRemainingCooldownSeconds(rateLimitKey);
            log.warn("NewsCommentService: Rate limit exceeded for key: {} - retry after {} seconds", rateLimitKey,
                    retryAfter);
            throw new RateLimitExceededException("Too many comments/replies. Please try again later.", retryAfter);
        }
        rateLimiterService.recordRequest(rateLimitKey);
    }

    /**
     * Parses status string to enum.
     *
     * @param status the status string
     * @return the NewsCommentStatus enum or null if invalid
     */
    private NewsCommentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return NewsCommentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("NewsCommentService: Invalid status filter: {}", status);
            return null;
        }
    }

    /**
     * Logs an audit action for the comment.
     *
     * @param userId      the actor UUID
     * @param commentId   the comment UUID
     * @param action      the action type
     * @param description the action description
     */
    private void logAudit(UUID userId, UUID commentId, String action, String description) {
        newsCommentAuditLogService.logAction(userId, commentId, action, description);
    }

    /**
     * Maps a comment with all its replies recursively.
     *
     * @param comment the parent comment
     * @return DTO with replies attached
     */
    private NewsCommentResponseDto mapWithReplies(NewsComment comment) {
        NewsCommentResponseDto dto = newsCommentMapper.toResponseDto(comment);
        List<NewsCommentResponseDto> replies = getReplies(comment.getNewsCommentsId());
        setRepliesOnDto(dto, replies);
        return dto;
    }

    /**
     * Maps a comment with approved replies only.
     *
     * @param comment the parent comment
     * @return DTO with approved replies attached
     */
    private NewsCommentResponseDto mapWithApprovedReplies(NewsComment comment) {
        NewsCommentResponseDto dto = newsCommentMapper.toResponseDto(comment);
        List<NewsCommentResponseDto> replies = getApprovedReplies(comment.getNewsCommentsId());
        setRepliesOnDto(dto, replies);
        return dto;
    }

    /**
     * Sets replies on a DTO using reflection (if method exists).
     *
     * @param dto     the comment DTO
     * @param replies the replies to set
     */
    private void setRepliesOnDto(NewsCommentResponseDto dto, List<NewsCommentResponseDto> replies) {
        try {
            Method setReplies = dto.getClass().getMethod("setReplies", List.class);
            setReplies.invoke(dto, replies);
        } catch (Exception ignored) {
            // Method doesn't exist, skip setting replies
        }
    }

    @Override
    public Long getTotalCommentCount() {
        log.debug("NewsCommentService: Getting total comment count");
        try {
            Long totalCount = newsCommentRepository.count();
            log.debug("NewsCommentService: Total comment count retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsCommentService: Failed to get total comment count - Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalCommentCountBetween(Instant start, Instant end) {
        log.debug("NewsCommentService: Getting total comment count between {} and {}", start, end);
        try {
            Long totalCount = newsCommentRepository.countCommentsBetween(start, end);
            log.debug("NewsCommentService: Total comment count between dates retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsCommentService: Failed to get total comment count between dates - Error: {}",
                    e.getMessage());
            throw e;
        }
    }
}
