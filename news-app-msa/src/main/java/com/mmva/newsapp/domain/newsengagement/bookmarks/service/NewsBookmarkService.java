package com.mmva.newsapp.domain.newsengagement.bookmarks.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkRequestDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkResponseDto;
import com.mmva.newsapp.domain.newsengagement.bookmarks.dto.NewsBookmarkUpdateDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NewsBookmarkService {
    /**
     * Deletes a folder and all bookmarks in it for the user's bookmarks.
     * 
     * @param userId     UUID of the user
     * @param folderName Name of the folder to delete
     */
    void deleteFolderForUser(UUID userId, String folderName);

    /**
     * Renames an existing folder for the user's bookmarks.
     * 
     * @param userId        UUID of the user
     * @param oldFolderName Current folder name
     * @param newFolderName New folder name
     * @return The new folder name
     */
    String renameFolderForUser(UUID userId, String oldFolderName, String newFolderName);

    NewsBookmarkResponseDto addBookmark(NewsBookmarkRequestDto dto);

    NewsBookmarkResponseDto getBookmarkById(UUID id);

    List<NewsBookmarkResponseDto> getBookmarksByUserId(UUID userId);

    List<NewsBookmarkResponseDto> getBookmarksByNewsId(UUID newsId);

    List<NewsBookmarkResponseDto> getBookmarksByUserIdAndFolder(UUID userId, String folderName);

    Page<NewsBookmarkResponseDto> getAllBookmarks(Pageable pageable);

    Long getBookmarkCountByUserId(UUID userId);

    Long getBookmarkCountByNewsId(UUID newsId);

    List<String> getFoldersByUserId(UUID userId);

    boolean isBookmarked(UUID userId, UUID newsId);

    NewsBookmarkResponseDto updateBookmark(UUID id, NewsBookmarkUpdateDto dto);

    /**
     * Removes a bookmark by admin.
     *
     * @param bookmarkId the bookmark ID to remove
     * @return the updated bookmark count for the news article
     * @throws NewsBookmarkNotFoundException if bookmark not found
     */
    Long removeBookmarkByAdmin(UUID bookmarkId);

    /**
     * Removes a bookmark by user.
     *
     * @param newsId the news article ID
     * @param userId the user ID
     * @return the updated bookmark count for the news article
     * @throws NewsBookmarkNotFoundException if bookmark not found
     */
    Long removeBookmarkByUser(UUID newsId, UUID userId);

    /**
     * Creates a new folder for the user's bookmarks. Throws exception if folder
     * exists.
     * 
     * @param userId     UUID of the user
     * @param folderName Name of the folder to create
     * @return The created folder name
     */
    String createFolderForUser(UUID userId, String folderName);

    /**
     * Gets the total bookmark count across all news articles.
     *
     * @return the total bookmark count
     */
    Long getTotalBookmarkCount();

    /**
     * Gets the total bookmark count within a date range.
     *
     * @param start the start date
     * @param end   the end date
     * @return the total bookmark count
     */
    Long getTotalBookmarkCountBetween(Instant start, Instant end);
}
