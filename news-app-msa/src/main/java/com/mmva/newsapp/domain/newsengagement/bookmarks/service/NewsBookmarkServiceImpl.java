package com.mmva.newsapp.domain.newsengagement.bookmarks.service;

// ========================================
// Client Context Imports
// ========================================
import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;

import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkRequestDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkResponseDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkUpdateDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.exception.NewsBookmarkFolderAlreadyExistsException;
import com.mmva.newsapp.domain.newsengagement.bookmarks.exception.NewsBookmarkFolderNotFoundException;
import com.mmva.newsapp.domain.newsengagement.bookmarks.exception.NewsBookmarkInvalidFolderNameException;
import com.mmva.newsapp.domain.newsengagement.bookmarks.exception.NewsBookmarkNotFoundException;
import com.mmva.newsapp.domain.newsengagement.bookmarks.mapper.NewsBookmarkMapper;
import com.mmva.newsapp.domain.newsengagement.bookmarks.model.NewsBookmark;
import com.mmva.newsapp.domain.newsengagement.bookmarks.repository.NewsBookmarkRepository;
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
 * Service implementation for managing user bookmarks.
 * 
 * <p>
 * Provides operations for adding, removing, and organizing bookmarks
 * with folder management capabilities and client context capture.
 * </p>
 * 
 * @see NewsBookmarkService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsBookmarkServiceImpl implements NewsBookmarkService {

    // ========================================
    // Constants
    // ========================================

    private static final String FOLDER_NAME_REGEX = "^[A-Za-z0-9 _-]{1,255}$";

    // ========================================
    // Dependencies
    // ========================================

    private final NewsBookmarkRepository userBookmarkRepository;
    private final NewsRepository newsRepository;
    private final NewsBookmarkMapper userBookmarkMapper;
    private final ClientContextService clientContextService;

    // ========================================
    // Folder Validation Helper
    // ========================================

    /**
     * Validates folder name format.
     *
     * @param folderName the folder name to validate
     * @throws NewsBookmarkInvalidFolderNameException if folder name is invalid
     */
    public static void validateFolderName(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new NewsBookmarkInvalidFolderNameException("Folder name must not be empty");
        }
        if (!folderName.matches(FOLDER_NAME_REGEX)) {
            throw new NewsBookmarkInvalidFolderNameException(
                    "Folder name contains invalid characters. Allowed: letters, numbers, space, dash, underscore.");
        }
    }

    // ========================================
    // Folder Management Methods
    // ========================================

    @Override
    @Transactional
    public void deleteFolderForUser(UUID userId, String folderName) {
        log.info("NewsBookmarkService: Deleting folder '{}' for user: {}", folderName, userId);
        validateFolderName(folderName);
        List<NewsBookmark> bookmarksToDelete = userBookmarkRepository
                .findByNewsBookmarksUserIdAndNewsBookmarksFolderName(userId, folderName);
        if (bookmarksToDelete.isEmpty()) {
            log.warn("NewsBookmarkService: Folder '{}' does not exist for user: {}", folderName, userId);
            throw new NewsBookmarkFolderNotFoundException("Folder does not exist or is already empty: " + folderName);
        }
        userBookmarkRepository.deleteAll(bookmarksToDelete);
        log.info("NewsBookmarkService: Folder '{}' and bookmarks deleted for user: {}", folderName, userId);
    }

    @Override
    @Transactional
    public String renameFolderForUser(UUID userId, String oldFolderName, String newFolderName) {
        log.info("NewsBookmarkService: Renaming folder '{}' to '{}' for user: {}",
                oldFolderName, newFolderName, userId);
        validateFolderName(oldFolderName);
        validateFolderName(newFolderName);

        List<String> existingFolders = userBookmarkRepository.findDistinctFolderNamesByNewsBookmarksUserId(userId);
        if (!existingFolders.contains(oldFolderName)) {
            log.warn("NewsBookmarkService: Folder '{}' does not exist for user: {}", oldFolderName, userId);
            throw new NewsBookmarkFolderNotFoundException("Folder does not exist: " + oldFolderName);
        }
        if (existingFolders.contains(newFolderName)) {
            log.warn("NewsBookmarkService: Folder '{}' already exists for user: {}", newFolderName, userId);
            throw new NewsBookmarkFolderAlreadyExistsException("New folder name already exists: " + newFolderName);
        }

        List<NewsBookmark> bookmarksToUpdate = userBookmarkRepository
                .findByNewsBookmarksUserIdAndNewsBookmarksFolderName(userId, oldFolderName);
        for (NewsBookmark bookmark : bookmarksToUpdate) {
            bookmark.setNewsBookmarksFolderName(newFolderName);
        }
        userBookmarkRepository.saveAll(bookmarksToUpdate);
        log.info("NewsBookmarkService: Folder renamed from '{}' to '{}' for user: {}",
                oldFolderName, newFolderName, userId);
        return newFolderName;
    }

    @Override
    @Transactional
    public String createFolderForUser(UUID userId, String folderName) {
        log.info("NewsBookmarkService: Creating folder '{}' for user: {}", folderName, userId);
        validateFolderName(folderName);

        List<String> existingFolders = userBookmarkRepository.findDistinctFolderNamesByNewsBookmarksUserId(userId);
        if (existingFolders.contains(folderName)) {
            log.warn("NewsBookmarkService: Folder '{}' already exists for user: {}", folderName, userId);
            throw new NewsBookmarkFolderAlreadyExistsException("Folder already exists: " + folderName);
        }

        NewsBookmark folderBookmark = NewsBookmark.builder()
                .newsBookmarksUserId(userId)
                .newsBookmarksNewsId(null)
                .newsBookmarksFolderName(folderName)
                .newsBookmarksBookmarkedAt(Instant.now())
                .build();
        userBookmarkRepository.save(folderBookmark);
        log.info("NewsBookmarkService: Folder '{}' created for user: {}", folderName, userId);
        return folderName;
    }

    // ========================================
    // Bookmark Management Methods
    // ========================================

    @Override
    @Transactional
    public NewsBookmarkResponseDto addBookmark(NewsBookmarkRequestDto dto) {
        log.info("NewsBookmarkService: Adding bookmark for user: {} and newsapp: {}",
                dto.getNewsBookmarksUserId(), dto.getNewsBookmarksNewsId());

        // Idempotent: If bookmark already exists, return existing bookmark with current
        // count
        var existingBookmark = userBookmarkRepository.findByNewsBookmarksUserIdAndNewsBookmarksNewsId(
                dto.getNewsBookmarksUserId(), dto.getNewsBookmarksNewsId());
        if (existingBookmark.isPresent()) {
            log.debug("NewsBookmarkService: Bookmark already exists for user: {} and newsapp: {} - returning existing",
                    dto.getNewsBookmarksUserId(), dto.getNewsBookmarksNewsId());
            NewsBookmarkResponseDto response = userBookmarkMapper.toResponseDto(existingBookmark.get());
            response.setUpdatedBookmarkCount(newsRepository.getBookmarkCount(dto.getNewsBookmarksNewsId()));
            return response;
        }

        try {
            NewsBookmark userBookmark = userBookmarkMapper.toEntity(dto);

            // Enrich with client context
            enrichWithClientContext(userBookmark);

            NewsBookmark savedBookmark = userBookmarkRepository.save(userBookmark);

            // Atomically increment bookmark counter on news article (STATE type - toggle)
            newsRepository.incrementBookmarkCount(dto.getNewsBookmarksNewsId());

            // Build response with updated count
            NewsBookmarkResponseDto response = userBookmarkMapper.toResponseDto(savedBookmark);
            response.setUpdatedBookmarkCount(newsRepository.getBookmarkCount(dto.getNewsBookmarksNewsId()));

            log.info("NewsBookmarkService: Bookmark added with ID: {} (country: {})",
                    savedBookmark.getNewsBookmarksId(), savedBookmark.getNewsBookmarksCountryCode());
            return response;
        } catch (Exception e) {
            log.error("NewsBookmarkService: Failed to add bookmark for user: {} and newsapp: {} - Error: {}",
                    dto.getNewsBookmarksUserId(), dto.getNewsBookmarksNewsId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Enriches a NewsBookmark entity with client context information.
     * 
     * @param bookmark the entity to enrich
     */
    private void enrichWithClientContext(NewsBookmark bookmark) {
        try {
            ClientContextDto ctx = clientContextService.getCurrentContext();
            if (ctx == null) {
                log.debug("NewsBookmarkService: No client context available for bookmark enrichment");
                return;
            }

            // Device information
            if (ctx.deviceType() != null) {
                bookmark.setNewsBookmarksDeviceType(ctx.deviceType().name());
            }
            bookmark.setNewsBookmarksDeviceFingerprint(ctx.deviceFingerprint());

            // Location
            bookmark.setNewsBookmarksCountryCode(ctx.countryCode());
            bookmark.setNewsBookmarksCity(ctx.city());

            // Request context
            if (ctx.channel() != null) {
                bookmark.setNewsBookmarksChannel(ctx.channel().name());
            }

            log.debug("NewsBookmarkService: Enriched bookmark with context - country: {}, device: {}",
                    ctx.countryCode(), ctx.deviceType());
        } catch (Exception e) {
            log.warn("NewsBookmarkService: Failed to enrich bookmark with client context: {}", e.getMessage());
        }
    }

    // ========================================
    // Bookmark Retrieval Methods
    // ========================================

    @Override
    public NewsBookmarkResponseDto getBookmarkById(UUID id) {
        log.debug("NewsBookmarkService: Fetching bookmark by ID: {}", id);
        NewsBookmark userBookmark = userBookmarkRepository.findById(id)
                .orElseThrow(() -> new NewsBookmarkNotFoundException(id));
        log.debug("NewsBookmarkService: Bookmark retrieved: {}", id);
        return userBookmarkMapper.toResponseDto(userBookmark);
    }

    @Override
    public List<NewsBookmarkResponseDto> getBookmarksByUserId(UUID userId) {
        log.debug("NewsBookmarkService: Fetching bookmarks for user: {}", userId);
        List<NewsBookmarkResponseDto> bookmarks = userBookmarkRepository.findByNewsBookmarksUserId(userId)
                .stream()
                .map(userBookmarkMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsBookmarkService: Retrieved {} bookmarks for user: {}", bookmarks.size(), userId);
        return bookmarks;
    }

    @Override
    public List<NewsBookmarkResponseDto> getBookmarksByNewsId(UUID newsId) {
        log.debug("NewsBookmarkService: Fetching bookmarks for newsapp: {}", newsId);
        List<NewsBookmarkResponseDto> bookmarks = userBookmarkRepository.findByNewsBookmarksNewsId(newsId)
                .stream()
                .map(userBookmarkMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsBookmarkService: Retrieved {} bookmarks for newsapp: {}", bookmarks.size(), newsId);
        return bookmarks;
    }

    @Override
    public List<NewsBookmarkResponseDto> getBookmarksByUserIdAndFolder(UUID userId, String folderName) {
        log.debug("NewsBookmarkService: Fetching bookmarks for user: {} in folder: {}", userId, folderName);
        List<NewsBookmarkResponseDto> bookmarks = userBookmarkRepository
                .findByNewsBookmarksUserIdAndNewsBookmarksFolderName(userId, folderName)
                .stream()
                .map(userBookmarkMapper::toResponseDto)
                .collect(Collectors.toList());
        log.debug("NewsBookmarkService: Retrieved {} bookmarks for user: {} in folder: {}",
                bookmarks.size(), userId, folderName);
        return bookmarks;
    }

    @Override
    public Page<NewsBookmarkResponseDto> getAllBookmarks(Pageable pageable) {
        log.debug("NewsBookmarkService: Fetching all bookmarks - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Page<NewsBookmarkResponseDto> bookmarks = userBookmarkRepository.findAll(pageable)
                .map(userBookmarkMapper::toResponseDto);
        log.debug("NewsBookmarkService: Retrieved {} bookmarks on page {}",
                bookmarks.getNumberOfElements(), pageable.getPageNumber());
        return bookmarks;
    }

    // ========================================
    // Bookmark Count Methods
    // ========================================

    @Override
    public Long getBookmarkCountByUserId(UUID userId) {
        log.debug("NewsBookmarkService: Getting bookmark count for user: {}", userId);
        Long count = userBookmarkRepository.countBookmarksByNewsBookmarksUserId(userId);
        log.debug("NewsBookmarkService: Bookmark count for user {}: {}", userId, count);
        return count;
    }

    @Override
    public Long getBookmarkCountByNewsId(UUID newsId) {
        log.debug("NewsBookmarkService: Getting bookmark count for newsapp: {}", newsId);
        Long count = userBookmarkRepository.countBookmarksByNewsBookmarksNewsId(newsId);
        log.debug("NewsBookmarkService: Bookmark count for newsapp {}: {}", newsId, count);
        return count;
    }

    @Override
    public List<String> getFoldersByUserId(UUID userId) {
        log.debug("NewsBookmarkService: Fetching folders for user: {}", userId);
        List<String> folders = userBookmarkRepository.findDistinctFolderNamesByNewsBookmarksUserId(userId);
        log.debug("NewsBookmarkService: Retrieved {} folders for user: {}", folders.size(), userId);
        return folders;
    }

    @Override
    public boolean isBookmarked(UUID userId, UUID newsId) {
        log.debug("NewsBookmarkService: Checking if newsapp {} is bookmarked by user: {}", newsId, userId);
        boolean isBookmarked = userBookmarkRepository.existsByNewsBookmarksUserIdAndNewsBookmarksNewsId(userId, newsId);
        log.debug("NewsBookmarkService: News {} bookmarked by user {}: {}", newsId, userId, isBookmarked);
        return isBookmarked;
    }

    // ========================================
    // Bookmark Update Methods
    // ========================================

    @Override
    @Transactional
    public NewsBookmarkResponseDto updateBookmark(UUID id, NewsBookmarkUpdateDto dto) {
        log.info("NewsBookmarkService: Updating bookmark: {}", id);
        NewsBookmark userBookmark = userBookmarkRepository.findById(id)
                .orElseThrow(() -> new NewsBookmarkNotFoundException(id));
        try {
            userBookmarkMapper.updateEntityFromDto(dto, userBookmark);
            NewsBookmark updatedBookmark = userBookmarkRepository.save(userBookmark);
            log.info("NewsBookmarkService: Bookmark updated successfully: {}", id);
            return userBookmarkMapper.toResponseDto(updatedBookmark);
        } catch (Exception e) {
            log.error("NewsBookmarkService: Failed to update bookmark: {} - Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Bookmark Removal Methods
    // ========================================

    @Override
    @Transactional
    public Long removeBookmarkByAdmin(UUID bookmarkId) {
        log.info("NewsBookmarkService: Admin removing bookmark: {}", bookmarkId);

        // Fetch the bookmark to get newsId before deleting
        NewsBookmark bookmark = userBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> {
                    log.warn("NewsBookmarkService: Remove failed - bookmark not found: {}", bookmarkId);
                    return new NewsBookmarkNotFoundException(bookmarkId);
                });

        UUID newsId = bookmark.getNewsBookmarksNewsId();

        try {
            userBookmarkRepository.deleteById(bookmarkId);

            // Atomically decrement bookmark counter on news article
            newsRepository.decrementBookmarkCount(newsId);

            // Return updated count
            Long updatedCount = newsRepository.getBookmarkCount(newsId);
            log.info("NewsBookmarkService: Bookmark removed by admin successfully: {} (updated count: {})", bookmarkId,
                    updatedCount);
            return updatedCount;
        } catch (Exception e) {
            log.error("NewsBookmarkService: Failed to remove bookmark: {} - Error: {}", bookmarkId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Long removeBookmarkByUser(UUID newsId, UUID userId) {
        log.info("NewsBookmarkService: User {} removing bookmark for newsapp: {}", userId, newsId);
        if (!userBookmarkRepository.existsByNewsBookmarksUserIdAndNewsBookmarksNewsId(userId, newsId)) {
            log.warn("NewsBookmarkService: Remove failed - bookmark not found for user: {} and newsapp: {}",
                    userId, newsId);
            throw new NewsBookmarkNotFoundException(userId, newsId);
        }
        try {
            userBookmarkRepository.deleteByNewsBookmarksUserIdAndNewsBookmarksNewsId(userId, newsId);

            // Atomically decrement bookmark counter on news article (STATE type - toggle)
            newsRepository.decrementBookmarkCount(newsId);

            // Return updated count
            Long updatedCount = newsRepository.getBookmarkCount(newsId);
            log.info(
                    "NewsBookmarkService: Bookmark removed by user successfully for newsapp: {} by user: {} (updated count: {})",
                    newsId, userId, updatedCount);
            return updatedCount;
        } catch (Exception e) {
            log.error("NewsBookmarkService: Failed to remove bookmark for newsapp: {} by user: {} - Error: {}",
                    newsId, userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalBookmarkCount() {
        log.debug("NewsBookmarkService: Getting total bookmark count");
        try {
            Long totalCount = userBookmarkRepository.count();
            log.debug("NewsBookmarkService: Total bookmark count retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsBookmarkService: Failed to get total bookmark count - Error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getTotalBookmarkCountBetween(Instant start, Instant end) {
        log.debug("NewsBookmarkService: Getting total bookmark count between {} and {}", start, end);
        try {
            Long totalCount = userBookmarkRepository.countBookmarksBetween(start, end);
            log.debug("NewsBookmarkService: Total bookmark count between dates retrieved: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("NewsBookmarkService: Failed to get total bookmark count between dates - Error: {}",
                    e.getMessage());
            throw e;
        }
    }
}
