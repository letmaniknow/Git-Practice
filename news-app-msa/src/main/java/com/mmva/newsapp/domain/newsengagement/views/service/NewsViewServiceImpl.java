package com.mmva.newsapp.domain.newsengagement.views.service;

// ========================================
// Client Context Imports
// ========================================
import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewRequestDto;
import com.mmva.newsapp.domain.newsengagement.views.dto.NewsViewResponseDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.views.exception.NewsViewNotFoundException;

// ========================================
// Mapper Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.views.mapper.NewsViewMapper;

// ========================================
// Model Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.views.model.NewsView;

// ========================================
// Repository Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.views.repository.NewsViewRepository;
import com.mmva.newsapp.domain.news.repository.core.NewsRepository;

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
 * Service implementation for managing newsapp view tracking and analytics.
 * 
 * <p>
 * Provides operations for recording and retrieving view data for newsapp
 * articles.
 * Captures comprehensive client context for analytics.
 * </p>
 * 
 * @see NewsViewService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsViewServiceImpl implements NewsViewService {

    // ========================================
    // Dependencies
    // ========================================

    private final NewsViewRepository newsViewRepository;
    private final NewsRepository newsRepository;
    private final NewsViewMapper newsViewMapper;
    private final ClientContextService clientContextService;

    // ========================================
    // View Recording Methods
    // ========================================

    @Override
    @Transactional
    public NewsViewResponseDto addView(NewsViewRequestDto dto) {
        log.info("NewsViewService: Adding view for newsapp: {}", dto.getNewsViewsNewsId());
        try {
            NewsView newsView = newsViewMapper.toEntity(dto);

            // Enrich with client context
            enrichWithClientContext(newsView);

            // Save view record
            NewsView savedView = newsViewRepository.save(newsView);

            // Increment view counter on news article
            newsRepository.incrementViewCount(dto.getNewsViewsNewsId());

            // Build response with updated count
            NewsViewResponseDto response = newsViewMapper.toResponseDto(savedView);
            response.setUpdatedViewCount(newsRepository.getViewCount(dto.getNewsViewsNewsId()));

            log.info("NewsViewService: View added successfully with ID: {} (country: {}, device: {})",
                    savedView.getNewsViewsId(), savedView.getNewsViewsCountryCode(),
                    savedView.getNewsViewsDeviceType());
            return response;
        } catch (Exception e) {
            log.error("NewsViewService: Failed to add view for newsapp: {} - Error: {}",
                    dto.getNewsViewsNewsId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Enriches a NewsView entity with client context information.
     * Uses the current request context from RequestContextHolder.
     * 
     * @param newsView the entity to enrich
     */
    private void enrichWithClientContext(NewsView newsView) {
        try {
            ClientContextDto ctx = clientContextService.getCurrentContext();
            if (ctx == null) {
                log.debug("NewsViewService: No client context available for view enrichment");
                return;
            }

            // Network
            if (newsView.getNewsViewsIpAddress() == null && ctx.ipAddress() != null) {
                newsView.setNewsViewsIpAddress(ctx.ipAddress());
            }

            // Device information
            if (ctx.deviceType() != null) {
                newsView.setNewsViewsDeviceType(ctx.deviceType().name());
            }
            newsView.setNewsViewsDeviceFingerprint(ctx.deviceFingerprint());
            newsView.setNewsViewsBrowserName(ctx.browserName());
            newsView.setNewsViewsBrowserVersion(ctx.browserVersion());
            newsView.setNewsViewsOsName(ctx.osName());
            newsView.setNewsViewsOsVersion(ctx.osVersion());

            // Location
            newsView.setNewsViewsCountryCode(ctx.countryCode());
            newsView.setNewsViewsCity(ctx.city());
            newsView.setNewsViewsTimezone(ctx.timezone());

            // Security
            newsView.setNewsViewsIsBot(ctx.isBot() != null ? ctx.isBot() : false);
            newsView.setNewsViewsIsAnonymized(ctx.isAnonymized() != null ? ctx.isAnonymized() : false);

            // Request context
            if (ctx.channel() != null) {
                newsView.setNewsViewsChannel(ctx.channel().name());
            }
            newsView.setNewsViewsLanguage(ctx.primaryLanguage());
            newsView.setNewsViewsRefererDomain(ctx.refererDomain());

            log.debug("NewsViewService: Enriched view with context - IP: {}, country: {}, device: {}",
                    ctx.ipAddress(), ctx.countryCode(), ctx.deviceType());
        } catch (Exception e) {
            log.warn("NewsViewService: Failed to enrich view with client context: {}", e.getMessage());
            // Don't fail the view recording if context extraction fails
        }
    }

    // ========================================
    // View Retrieval Methods
    // ========================================

    @Override
    public NewsViewResponseDto getViewById(Long id) {
        log.debug("NewsViewService: Fetching view by ID: {}", id);
        NewsView newsView = newsViewRepository.findById(id)
                .orElseThrow(() -> new NewsViewNotFoundException(id));
        log.debug("NewsViewService: View retrieved successfully: {}", id);
        return newsViewMapper.toResponseDto(newsView);
    }

    @Override
    public List<NewsViewResponseDto> getViewsByNewsId(UUID newsId) {
        log.debug("NewsViewService: Fetching views for newsapp: {}", newsId);
        List<NewsViewResponseDto> views = newsViewRepository.findByNewsViewsNewsId(newsId)
                .stream()
                .map(newsViewMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsViewService: Retrieved {} views for newsapp: {}", views.size(), newsId);
        return views;
    }

    @Override
    public List<NewsViewResponseDto> getViewsByUserId(UUID userId) {
        log.debug("NewsViewService: Fetching views for user: {}", userId);
        List<NewsViewResponseDto> views = newsViewRepository.findByNewsViewsUserId(userId)
                .stream()
                .map(newsViewMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsViewService: Retrieved {} views for user: {}", views.size(), userId);
        return views;
    }

    @Override
    public Page<NewsViewResponseDto> getAllViews(Pageable pageable) {
        log.debug("NewsViewService: Fetching all views - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<NewsViewResponseDto> views = newsViewRepository.findAll(pageable)
                .map(newsViewMapper::toResponseDto);
        log.debug("NewsViewService: Retrieved {} views on page {}",
                views.getNumberOfElements(), pageable.getPageNumber());
        return views;
    }

    // ========================================
    // View Count Methods
    // ========================================

    @Override
    public Long getViewCountByNewsId(UUID newsId) {
        log.debug("NewsViewService: Getting view count for newsapp: {}", newsId);
        Long count = newsViewRepository.countViewsByNewsId(newsId);
        log.debug("NewsViewService: View count for newsapp {}: {}", newsId, count);
        return count;
    }

    @Override
    public Long getViewCountByNewsIdAndUserId(UUID newsId, UUID userId) {
        log.debug("NewsViewService: Getting view count for newsapp: {} and user: {}", newsId, userId);
        Long count = newsViewRepository.countViewsByNewsIdAndUserId(newsId, userId);
        log.debug("NewsViewService: View count for newsapp {} by user {}: {}", newsId, userId, count);
        return count;
    }

    // ========================================
    // View Removal Methods (Admin)
    // ========================================

    @Override
    @Transactional
    public Long removeViewByAdmin(Long viewId) {
        log.info("NewsViewService: Admin removing view record: {}", viewId);

        // Fetch the view to get newsId before deleting
        NewsView view = newsViewRepository.findById(viewId)
                .orElseThrow(() -> {
                    log.warn("NewsViewService: Remove failed - view not found: {}", viewId);
                    return new NewsViewNotFoundException(viewId);
                });

        UUID newsId = view.getNewsViewsNewsId();

        try {
            newsViewRepository.deleteById(viewId);

            // Atomically decrement view counter to maintain consistency
            newsRepository.decrementViewCount(newsId);

            // Return updated count
            Long updatedCount = newsRepository.getViewCount(newsId);
            log.info("NewsViewService: View record removed by admin successfully: {} (updated count: {})", viewId,
                    updatedCount);
            return updatedCount;
        } catch (Exception e) {
            log.error("NewsViewService: Failed to remove view: {} - Error: {}", viewId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalViewCount() {
        log.debug("NewsViewService: Getting total view count");
        try {
            Long totalCount = newsViewRepository.count();
            log.debug("NewsViewService: Total view count retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsViewService: Failed to get total view count - Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalViewCountBetween(Instant start, Instant end) {
        log.debug("NewsViewService: Getting total view count between {} and {}", start, end);
        try {
            Long totalCount = newsViewRepository.countViewsBetween(start, end);
            log.debug("NewsViewService: Total view count between dates retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsViewService: Failed to get total view count between dates - Error: {}", e.getMessage());
            throw e;
        }
    }
}
