package com.mmva.newsapp.domain.newsengagement.bookmarks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mmva.newsapp.domain.newsengagement.bookmarks.model.NewsBookmark;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NewsBookmark entity.
 * 
 * <p>
 * Table: news_bookmarks
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsBookmarkRepository extends JpaRepository<NewsBookmark, UUID> {

    List<NewsBookmark> findByNewsBookmarksUserId(UUID userId);

    List<NewsBookmark> findByNewsBookmarksNewsId(UUID newsId);

    List<NewsBookmark> findByNewsBookmarksUserIdAndNewsBookmarksFolderName(UUID userId, String folderName);

    Optional<NewsBookmark> findByNewsBookmarksUserIdAndNewsBookmarksNewsId(UUID userId, UUID newsId);

    boolean existsByNewsBookmarksUserIdAndNewsBookmarksNewsId(UUID userId, UUID newsId);

    @Query("SELECT COUNT(nb) FROM NewsBookmark nb WHERE nb.newsBookmarksUserId = :userId")
    Long countBookmarksByNewsBookmarksUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(nb) FROM NewsBookmark nb WHERE nb.newsBookmarksNewsId = :newsId")
    Long countBookmarksByNewsBookmarksNewsId(@Param("newsId") UUID newsId);

    @Query("SELECT DISTINCT nb.newsBookmarksFolderName FROM NewsBookmark nb WHERE nb.newsBookmarksUserId = :userId AND nb.newsBookmarksFolderName IS NOT NULL")
    List<String> findDistinctFolderNamesByNewsBookmarksUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(nb) FROM NewsBookmark nb WHERE nb.newsBookmarksBookmarkedAt BETWEEN :start AND :end")
    Long countBookmarksBetween(@Param("start") Instant start, @Param("end") Instant end);

    void deleteByNewsBookmarksUserIdAndNewsBookmarksNewsId(UUID userId, UUID newsId);
}
