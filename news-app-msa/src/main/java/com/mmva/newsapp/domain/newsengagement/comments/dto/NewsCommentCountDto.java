package com.mmva.newsapp.domain.newsengagement.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for returning updated comment and reply counts after delete operations.
 * 
 * <p>
 * Used by delete operations to return the updated counts for real-time UI
 * updates.
 * </p>
 *
 * @param updatedCommentCount the updated total comment count for the news
 *                            article
 * @param updatedReplyCount   the updated total reply count for the news article
 */
@Schema(description = "Response containing updated comment and reply counts after a delete operation")
public record NewsCommentCountDto(

        @Schema(description = "Updated total comment count for the news article", example = "155") Long updatedCommentCount,

        @Schema(description = "Updated total reply count for the news article", example = "341") Long updatedReplyCount) {
}
