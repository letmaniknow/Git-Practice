package com.mmva.newsapp.domain.newsengagement.likes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsLikeResponseDto {

    @Schema(description = "Unique identifier of the newsapp like record", example = "1")
    private Long newsLikesId;

    @Schema(description = "Unique identifier of the newsapp that was liked", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID newsLikesNewsId;

    @Schema(description = "Unique identifier of the user who liked the newsapp", example = "987e6543-e21b-12d3-a456-426614174999")
    private UUID newsLikesUserId;

    @Schema(description = "IP address from which the newsapp was liked", example = "192.168.1.1")
    private String newsLikesIpAddress;

    @Schema(description = "Timestamp when the newsapp was liked (ISO 8601 format)", example = "2024-06-01T12:34:56Z")
    private String newsLikesLikedAt;

    // ========================================
    // Updated Counter (for real-time UI updates)
    // ========================================

    @Schema(description = "Updated total like count for the news article after this action", example = "1542")
    private Long updatedLikeCount;
}
