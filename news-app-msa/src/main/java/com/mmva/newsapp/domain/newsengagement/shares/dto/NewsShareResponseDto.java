package com.mmva.newsapp.domain.newsengagement.shares.dto;

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
public class NewsShareResponseDto {

    @Schema(description = "Unique identifier of the newsapp share record", example = "1")
    private Long newsSharesId;

    @Schema(description = "Unique identifier of the newsapp that was shared", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID newsSharesNewsId;

    @Schema(description = "Unique identifier of the user who shared the newsapp", example = "987e6543-e21b-12d3-a456-426614174999")
    private UUID newsSharesUserId;

    @Schema(description = "Share infrastructure: TWITTER, FACEBOOK, LINKEDIN, WHATSAPP, EMAIL, COPY_LINK", example = "TWITTER")
    private String newsSharesPlatform;

    @Schema(description = "IP address from which the newsapp was shared", example = "192.168.1.1")
    private String newsSharesIpAddress;

    @Schema(description = "Timestamp when the newsapp was shared (ISO 8601 format)", example = "2024-06-01T12:34:56Z")
    private String newsSharesSharedAt;

    // ========================================
    // Updated Counter (for real-time UI updates)
    // ========================================

    @Schema(description = "Updated total share count for the news article after this action", example = "328")
    private Long updatedShareCount;
}
