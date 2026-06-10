package com.mmva.newsapp.domain.newsengagement.likes.service;

// ========================================
// Client Context Imports
// ========================================
import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeRequestDto;
import com.mmva.newsapp.domain.newsengagement.likes.dto.NewsLikeResponseDto;

// ========================================
// Mapper Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.likes.mapper.NewsLikeMapper;

// ========================================
// Model Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.likes.model.NewsLike;

// ========================================
// Repository Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.likes.repository.NewsLikeRepository;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.likes.exception.NewsLikeNotFoundException;

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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for managing newsapp likes and newsengagement
 * tracking.
 * 
 * <p>
 * Provides operations for adding, removing, and tracking likes on newsapp
 * articles with client context capture for analytics.
 * </p>
 * 
 * @see NewsLikeService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsLikeServiceImpl implements NewsLikeService {

    // ========================================
    // Dependencies
    // ========================================

    private final NewsLikeRepository newsLikeRepository;
    private final NewsRepository newsRepository;
    private final NewsLikeMapper newsLikeMapper;
    private final ClientContextService clientContextService;

    // ========================================
    // Like Management Methods
    // ========================================

    @Override
    @Transactional
    public NewsLikeResponseDto addLike(NewsLikeRequestDto dto) {
        log.info("NewsLikeService: Adding like for news: {}", dto.getNewsLikesNewsId());

        // Idempotent: If user already liked, return existing like with current count
        if (dto.getNewsLikesUserId() != null) {
            var existingLike = newsLikeRepository
                    .findByNewsLikesNewsIdAndNewsLikesUserId(dto.getNewsLikesNewsId(), dto.getNewsLikesUserId());
            if (existingLike.isPresent()) {
                log.debug("NewsLikeService: User {} already liked news: {} - returning existing like",
                        dto.getNewsLikesUserId(), dto.getNewsLikesNewsId());
                NewsLikeResponseDto response = newsLikeMapper.toResponseDto(existingLike.get());
                response.setUpdatedLikeCount(newsRepository.getLikeCount(dto.getNewsLikesNewsId()));
                return response;
            }
        }

        NewsLike newsLike = newsLikeMapper.toEntity(dto);

        // Enrich with client context
        enrichWithClientContext(newsLike);

        try {
            NewsLike savedLike = newsLikeRepository.save(newsLike);

            // Atomically increment like counter on news article (STATE type - toggle)
            newsRepository.incrementLikeCount(dto.getNewsLikesNewsId());

            // Build response with updated count
            NewsLikeResponseDto response = newsLikeMapper.toResponseDto(savedLike);
            response.setUpdatedLikeCount(newsRepository.getLikeCount(dto.getNewsLikesNewsId()));

            log.info("NewsLikeService: Like added successfully with ID: {} (country: {})",
                    savedLike.getNewsLikesId(), savedLike.getNewsLikesCountryCode());
            return response;
        } catch (Exception e) {
            log.error("NewsLikeService: Failed to add like for news: {} - Error: {}",
                    dto.getNewsLikesNewsId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Enriches a NewsLike entity with client context information.
     * 
     * @param like the entity to enrich
     */
    private void enrichWithClientContext(NewsLike like) {
        try {
            ClientContextDto ctx = clientContextService.getCurrentContext();
            if (ctx == null) {
                log.debug("NewsLikeService: No client context available for like enrichment");
                return;
            }

            // Network
            if (like.getNewsLikesIpAddress() == null && ctx.ipAddress() != null) {
                like.setNewsLikesIpAddress(ctx.ipAddress());
            }

            // Device information
            if (ctx.deviceType() != null) {
                like.setNewsLikesDeviceType(ctx.deviceType().name());
            }
            like.setNewsLikesDeviceFingerprint(ctx.deviceFingerprint());

            // Location
            like.setNewsLikesCountryCode(ctx.countryCode());
            like.setNewsLikesCity(ctx.city());

            // Request context
            if (ctx.channel() != null) {
                like.setNewsLikesChannel(ctx.channel().name());
            }

            log.debug("NewsLikeService: Enriched like with context - country: {}, device: {}",
                    ctx.countryCode(), ctx.deviceType());
        } catch (Exception e) {
            log.warn("NewsLikeService: Failed to enrich like with client context: {}", e.getMessage());
        }
    }

    // ========================================
    // Like Retrieval Methods
    // ========================================

    @Override
    public NewsLikeResponseDto getLikeById(Long id) {
        log.debug("NewsLikeService: Fetching like by ID: {}", id);
        NewsLike newsLike = newsLikeRepository.findById(id)
                .orElseThrow(() -> new NewsLikeNotFoundException(id));
        log.debug("NewsLikeService: Like retrieved successfully: {}", id);
        return newsLikeMapper.toResponseDto(newsLike);
    }

    @Override
    public List<NewsLikeResponseDto> getLikesByNewsId(UUID newsId) {
        log.debug("NewsLikeService: Fetching likes for newsapp: {}", newsId);
        List<NewsLikeResponseDto> likes = newsLikeRepository.findByNewsLikesNewsId(newsId)
                .stream()
                .map(newsLikeMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsLikeService: Retrieved {} likes for newsapp: {}", likes.size(), newsId);
        return likes;
    }

    @Override
    public List<NewsLikeResponseDto> getLikesByUserId(UUID userId) {
        log.debug("NewsLikeService: Fetching likes for user: {}", userId);
        List<NewsLikeResponseDto> likes = newsLikeRepository.findByNewsLikesUserId(userId)
                .stream()
                .map(newsLikeMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsLikeService: Retrieved {} likes for user: {}", likes.size(), userId);
        return likes;
    }

    @Override
    public Page<NewsLikeResponseDto> getAllLikes(Pageable pageable) {
        log.debug("NewsLikeService: Fetching all likes - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<NewsLikeResponseDto> likes = newsLikeRepository.findAll(pageable)
                .map(newsLikeMapper::toResponseDto);
        log.debug("NewsLikeService: Retrieved {} likes on page {}",
                likes.getNumberOfElements(), pageable.getPageNumber());
        return likes;
    }

    // ========================================
    // Like Count Methods
    // ========================================

    @Override
    public Long getLikeCountByNewsId(UUID newsId) {
        log.debug("NewsLikeService: Getting like count for newsapp: {}", newsId);
        Long count = newsLikeRepository.countLikesByNewsId(newsId);
        log.debug("NewsLikeService: Like count for newsapp {}: {}", newsId, count);
        return count;
    }

    @Override
    public boolean hasUserLikedNews(UUID newsId, UUID userId) {
        log.debug("NewsLikeService: Checking if user {} has liked newsapp: {}", userId, newsId);
        boolean hasLiked = newsLikeRepository.existsByNewsLikesNewsIdAndNewsLikesUserId(newsId, userId);
        log.debug("NewsLikeService: User {} liked newsapp {}: {}", userId, newsId, hasLiked);
        return hasLiked;
    }

    // ========================================
    // Like Removal Methods
    // ========================================

    @Override
    @Transactional
    public Long removeLikeByAdmin(Long likeId) {
        log.info("NewsLikeService: Admin removing like record: {}", likeId);

        // Fetch the like to get newsId before deleting
        NewsLike like = newsLikeRepository.findById(likeId)
                .orElseThrow(() -> {
                    log.warn("NewsLikeService: Remove failed - like not found: {}", likeId);
                    return new NewsLikeNotFoundException(likeId);
                });

        UUID newsId = like.getNewsLikesNewsId();

        try {
            newsLikeRepository.deleteById(likeId);

            // Atomically decrement like counter on news article
            newsRepository.decrementLikeCount(newsId);

            // Return updated count
            Long updatedCount = newsRepository.getLikeCount(newsId);
            log.info("NewsLikeService: Like record removed by admin successfully: {} (updated count: {})", likeId,
                    updatedCount);
            return updatedCount;
        } catch (Exception e) {
            log.error("NewsLikeService: Failed to remove like: {} - Error: {}", likeId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Long removeLikeByUser(UUID newsId, UUID userId) {
        log.info("NewsLikeService: User {} removing like for news: {}", userId, newsId);

        // Idempotent: If like doesn't exist, return current count
        if (!newsLikeRepository.existsByNewsLikesNewsIdAndNewsLikesUserId(newsId, userId)) {
            log.debug("NewsLikeService: No like found for news: {} and user: {} - already removed or never existed",
                    newsId, userId);
            return newsRepository.getLikeCount(newsId);
        }

        try {
            newsLikeRepository.deleteByNewsLikesNewsIdAndNewsLikesUserId(newsId, userId);

            // Atomically decrement like counter on news article (STATE type - toggle)
            newsRepository.decrementLikeCount(newsId);

            // Return updated count
            Long updatedCount = newsRepository.getLikeCount(newsId);
            log.info("NewsLikeService: Like removed by user successfully for news: {} by user: {} (updated count: {})",
                    newsId, userId, updatedCount);
            return updatedCount;
        } catch (Exception e) {
            log.error("NewsLikeService: Failed to remove like for news: {} and user: {} - Error: {}",
                    newsId, userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalLikeCount() {
        log.debug("NewsLikeService: Getting total like count");
        try {
            Long totalCount = newsLikeRepository.count();
            log.debug("NewsLikeService: Total like count retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsLikeService: Failed to get total like count - Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalLikeCountBetween(Instant start, Instant end) {
        log.debug("NewsLikeService: Getting total like count between {} and {}", start, end);
        try {
            Long totalCount = newsLikeRepository.countLikesBetween(start, end);
            log.debug("NewsLikeService: Total like count between dates retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsLikeService: Failed to get total like count between dates - Error: {}", e.getMessage());
            throw e;
        }
    }
}
