package com.mmva.newsapp.domain.newsengagement.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Response DTO for a comment on a newsapp newsapp.")
public class NewsCommentResponseDto {

    @Schema(description = "Unique ID of the comment", example = "c789f1ee-6c54-4b01-90e6-d701748f0789")
    private UUID newsCommentsId;

    @Schema(description = "ID of the newsapp newsapp being commented on", example = "a123f1ee-6c54-4b01-90e6-d701748f0123")
    private UUID newsCommentsNewsId;

    @Schema(description = "ID of the user who made the comment", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID newsCommentsUserId;

    @Schema(description = "ID of the parent comment (for threaded replies)", example = "p123f1ee-6c54-4b01-90e6-d701748f0123")
    private UUID newsCommentsParentId;

    @Schema(description = "The comment text", example = "Great newsapp!")
    private String newsCommentsComment;

    @Schema(description = "IP address of the commenter (if available)", example = "192.168.1.1")
    private String newsCommentsIpAddress;

    @Schema(description = "Timestamp when the comment was made (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String newsCommentsCommentedAt;

    @Schema(description = "Status of the comment", example = "APPROVED")
    private String newsCommentsStatus;

    @Schema(description = "Nested replies to this comment")
    private java.util.List<NewsCommentResponseDto> replies;

    @Schema(description = "Timestamp when the comment was created (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String createdAt;

    @Schema(description = "ID of the user who created this comment record")
    private UUID createdBy;

    @Schema(description = "Timestamp when the comment was last updated (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String updatedAt;

    @Schema(description = "ID of the user who last updated this comment record")
    private UUID updatedBy;

    // ========================================
    // Updated Counters (for real-time UI updates)
    // ========================================

    @Schema(description = "Updated total comment count for the news article after this action", example = "156")
    private Long updatedCommentCount;

    @Schema(description = "Updated total reply count for the news article after this action", example = "342")
    private Long updatedReplyCount;
}
