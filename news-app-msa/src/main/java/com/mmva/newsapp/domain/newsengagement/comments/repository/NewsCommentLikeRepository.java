package com.mmva.newsapp.domain.newsengagement.comments.repository;

import com.mmva.newsapp.domain.newsengagement.comments.model.NewsCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link NewsCommentLike} entity operations.
 *
 * <p>
 * Follows PROJECT_PRINCIPLES.md field naming convention using
 * entity field names with domain prefix.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface NewsCommentLikeRepository extends JpaRepository<NewsCommentLike, UUID> {

    Optional<NewsCommentLike> findByNewsCommentLikesCommentIdAndNewsCommentLikesUserId(UUID commentId, UUID userId);

    long countByNewsCommentLikesCommentIdAndNewsCommentLikesLikedTrue(UUID commentId);

    java.util.List<NewsCommentLike> findAllByNewsCommentLikesCommentIdAndNewsCommentLikesLikedTrue(UUID commentId);
}
