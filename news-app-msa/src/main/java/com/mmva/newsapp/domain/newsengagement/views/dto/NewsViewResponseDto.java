package com.mmva.newsapp.domain.newsengagement.views.dto;

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
public class NewsViewResponseDto {

    @Schema(description = "Unique identifier of the newsapp view record", example = "1")
    private Long newsViewsId;

    @Schema(description = "Unique identifier of the newsapp that was viewed", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID newsViewsNewsId;

    @Schema(description = "Unique identifier of the user who viewed the newsapp", example = "987e6543-e21b-12d3-a456-426614174999")
    private UUID newsViewsUserId;

    @Schema(description = "IP address from which the newsapp was viewed", example = "192.168.1.1")
    private String newsViewsIpAddress;

    @Schema(description = "Timestamp when the newsapp was viewed (ISO 8601 format)", example = "2024-06-01T12:34:56Z")
    private String newsViewsViewedAt;

    // ========================================
    // Updated Counter (for real-time UI updates)
    // ========================================

    @Schema(description = "Updated total view count for the news article after this action", example = "1542")
    private Long updatedViewCount;
}
