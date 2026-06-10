package com.mmva.newsapp.domain.newsengagement.likes.repository;

import com.mmva.newsapp.domain.newsengagement.likes.model.NewsLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for NewsLike entity.
 * 
 * <p>
 * Table: news_likes
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {

    List<NewsLike> findByNewsLikesNewsId(UUID newsId);

    List<NewsLike> findByNewsLikesUserId(UUID userId);

    Optional<NewsLike> findByNewsLikesNewsIdAndNewsLikesUserId(UUID newsId, UUID userId);

    boolean existsByNewsLikesNewsIdAndNewsLikesUserId(UUID newsId, UUID userId);

    @Query("SELECT COUNT(nl) FROM NewsLike nl WHERE nl.newsLikesNewsId = :newsId")
    Long countLikesByNewsId(@Param("newsId") UUID newsId);

    @Query("SELECT COUNT(nl) FROM NewsLike nl WHERE nl.newsLikesLikedAt BETWEEN :start AND :end")
    Long countLikesBetween(@Param("start") Instant start, @Param("end") Instant end);

    void deleteByNewsLikesNewsIdAndNewsLikesUserId(UUID newsId, UUID userId);
}
