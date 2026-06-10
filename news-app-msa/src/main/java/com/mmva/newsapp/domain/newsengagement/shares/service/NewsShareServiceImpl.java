package com.mmva.newsapp.domain.newsengagement.shares.service;

// ========================================
// Client Context Imports
// ========================================
import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareRequestDto;
import com.mmva.newsapp.domain.newsengagement.shares.dto.NewsShareResponseDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.shares.exception.NewsShareNotFoundException;

// ========================================
// Mapper Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.shares.mapper.NewsShareMapper;

// ========================================
// Model Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.shares.model.NewsShare;

// ========================================
// Repository Imports
// ========================================
import com.mmva.newsapp.domain.newsengagement.shares.repository.NewsShareRepository;
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
 * Service implementation for managing newsapp shares and social newsengagement
 * tracking.
 * 
 * <p>
 * Provides operations for recording, retrieving, and tracking shares on newsapp
 * articles with client context capture for analytics.
 * </p>
 * 
 * @see NewsShareService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsShareServiceImpl implements NewsShareService {

    // ========================================
    // Dependencies
    // ========================================

    private final NewsShareRepository newsShareRepository;
    private final NewsRepository newsRepository;
    private final NewsShareMapper newsShareMapper;
    private final ClientContextService clientContextService;

    // ========================================
    // Share Recording Methods
    // ========================================

    @Override
    @Transactional
    public NewsShareResponseDto addShare(NewsShareRequestDto dto) {
        log.info("NewsShareService: Adding share for newsapp: {}", dto.getNewsSharesNewsId());

        // Shares are EVENTS, not STATES - every call creates a new share record
        // A user can share the same article multiple times (different platforms,
        // different times)
        NewsShare newsShare = newsShareMapper.toEntity(dto);

        // Enrich with client context
        enrichWithClientContext(newsShare);

        try {
            NewsShare savedShare = newsShareRepository.save(newsShare);

            // Atomically increment share counter on news article (EVENT type - no undo)
            newsRepository.incrementShareCount(dto.getNewsSharesNewsId());

            // Build response with updated count
            NewsShareResponseDto response = newsShareMapper.toResponseDto(savedShare);
            response.setUpdatedShareCount(newsRepository.getShareCount(dto.getNewsSharesNewsId()));

            log.info("NewsShareService: Share added successfully with ID: {} (country: {}, platform: {})",
                    savedShare.getNewsSharesId(), savedShare.getNewsSharesCountryCode(),
                    savedShare.getNewsSharesPlatform());
            return response;
        } catch (Exception e) {
            log.error("NewsShareService: Failed to add share for newsapp: {} - Error: {}",
                    dto.getNewsSharesNewsId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Enriches a NewsShare entity with client context information.
     * 
     * @param share the entity to enrich
     */
    private void enrichWithClientContext(NewsShare share) {
        try {
            ClientContextDto ctx = clientContextService.getCurrentContext();
            if (ctx == null) {
                log.debug("NewsShareService: No client context available for share enrichment");
                return;
            }

            // Network
            if (share.getNewsSharesIpAddress() == null && ctx.ipAddress() != null) {
                share.setNewsSharesIpAddress(ctx.ipAddress());
            }

            // Device information
            if (ctx.deviceType() != null) {
                share.setNewsSharesDeviceType(ctx.deviceType().name());
            }
            share.setNewsSharesDeviceFingerprint(ctx.deviceFingerprint());

            // Location
            share.setNewsSharesCountryCode(ctx.countryCode());
            share.setNewsSharesCity(ctx.city());

            // Request context
            if (ctx.channel() != null) {
                share.setNewsSharesChannel(ctx.channel().name());
            }

            log.debug("NewsShareService: Enriched share with context - country: {}, device: {}",
                    ctx.countryCode(), ctx.deviceType());
        } catch (Exception e) {
            log.warn("NewsShareService: Failed to enrich share with client context: {}", e.getMessage());
        }
    }

    // ========================================
    // Share Retrieval Methods
    // ========================================

    @Override
    public NewsShareResponseDto getShareById(Long id) {
        log.debug("NewsShareService: Fetching share by ID: {}", id);
        NewsShare newsShare = newsShareRepository.findById(id)
                .orElseThrow(() -> new NewsShareNotFoundException(id));
        log.debug("NewsShareService: Share retrieved successfully: {}", id);
        return newsShareMapper.toResponseDto(newsShare);
    }

    @Override
    public List<NewsShareResponseDto> getSharesByNewsId(UUID newsId) {
        log.debug("NewsShareService: Fetching shares for newsapp: {}", newsId);
        List<NewsShareResponseDto> shares = newsShareRepository.findByNewsSharesNewsId(newsId)
                .stream()
                .map(newsShareMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsShareService: Retrieved {} shares for newsapp: {}", shares.size(), newsId);
        return shares;
    }

    @Override
    public List<NewsShareResponseDto> getSharesByUserId(UUID userId) {
        log.debug("NewsShareService: Fetching shares for user: {}", userId);
        List<NewsShareResponseDto> shares = newsShareRepository.findByNewsSharesUserId(userId)
                .stream()
                .map(newsShareMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsShareService: Retrieved {} shares for user: {}", shares.size(), userId);
        return shares;
    }

    @Override
    public Page<NewsShareResponseDto> getAllShares(Pageable pageable) {
        log.debug("NewsShareService: Fetching all shares - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<NewsShareResponseDto> shares = newsShareRepository.findAll(pageable)
                .map(newsShareMapper::toResponseDto);
        log.debug("NewsShareService: Retrieved {} shares on page {}",
                shares.getNumberOfElements(), pageable.getPageNumber());
        return shares;
    }

    // ========================================
    // Share Count Methods
    // ========================================

    @Override
    public Long getShareCountByNewsId(UUID newsId) {
        log.debug("NewsShareService: Getting share count for newsapp: {}", newsId);
        Long count = newsShareRepository.countSharesByNewsSharesNewsId(newsId);
        log.debug("NewsShareService: Share count for newsapp {}: {}", newsId, count);
        return count;
    }

    @Override
    public Long getShareCountByUserId(UUID userId) {
        log.debug("NewsShareService: Getting share count for user: {}", userId);
        Long count = newsShareRepository.countSharesByNewsSharesUserId(userId);
        log.debug("NewsShareService: Share count for user {}: {}", userId, count);
        return count;
    }

    @Override
    public boolean hasUserShared(UUID newsId, UUID userId) {
        log.debug("NewsShareService: Checking if user {} has shared newsapp: {}", userId, newsId);
        boolean hasShared = newsShareRepository.existsByNewsSharesNewsIdAndNewsSharesUserId(newsId, userId);
        log.debug("NewsShareService: User {} shared newsapp {}: {}", userId, newsId, hasShared);
        return hasShared;
    }

    @Override
    public List<Object[]> getSharesByPlatform(UUID newsId) {
        log.debug("NewsShareService: Getting share statistics by platform for newsapp: {}", newsId);
        List<Object[]> stats = newsShareRepository.countSharesByPlatform(newsId);
        log.debug("NewsShareService: Retrieved {} platform statistics for newsapp: {}", stats.size(), newsId);
        return stats;
    }

    // ========================================
    // Share Removal Methods (Admin)
    // ========================================

    @Override
    @Transactional
    public Long removeShareByAdmin(Long shareId) {
        log.info("NewsShareService: Admin removing share record: {}", shareId);

        // Fetch the share to get newsId before deleting
        NewsShare share = newsShareRepository.findById(shareId)
                .orElseThrow(() -> {
                    log.warn("NewsShareService: Remove failed - share not found: {}", shareId);
                    return new NewsShareNotFoundException(shareId);
                });

        UUID newsId = share.getNewsSharesNewsId();

        try {
            newsShareRepository.deleteById(shareId);

            // Atomically decrement share counter to maintain consistency
            newsRepository.decrementShareCount(newsId);

            // Return updated count
            Long updatedCount = newsRepository.getShareCount(newsId);
            log.info("NewsShareService: Share record removed by admin successfully: {} (updated count: {})", shareId,
                    updatedCount);
            return updatedCount;
        } catch (Exception e) {
            log.error("NewsShareService: Failed to remove share: {} - Error: {}", shareId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalShareCount() {
        log.debug("NewsShareService: Getting total share count");
        try {
            Long totalCount = newsShareRepository.count();
            log.debug("NewsShareService: Total share count retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsShareService: Failed to get total share count - Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalShareCountBetween(Instant start, Instant end) {
        log.debug("NewsShareService: Getting total share count between {} and {}", start, end);
        try {
            Long totalCount = newsShareRepository.countSharesBetween(start, end);
            log.debug("NewsShareService: Total share count between dates retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsShareService: Failed to get total share count between dates - Error: {}", e.getMessage());
            throw e;
        }
    }
}
