package com.mmva.newsapp.domain.newsengagement.shares.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class NewsShareRequestDto {

    @Schema(description = "Unique identifier of the news article to be shared", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "News ID is required")
    private UUID newsSharesNewsId;

    @Schema(description = "User ID (auto-filled from JWT token)", example = "987e6543-e21b-12d3-a456-426614174999", hidden = true)
    private UUID newsSharesUserId;

    @Schema(description = "Share platform: TWITTER, FACEBOOK, LINKEDIN, WHATSAPP, EMAIL, COPY_LINK", example = "TWITTER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Platform is required")
    @Size(max = 30, message = "Platform must not exceed 30 characters")
    private String newsSharesPlatform;

    @Schema(description = "IP address from which the newsapp was shared", example = "192.168.1.1")
    @Size(max = 100, message = "IP address must not exceed 100 characters")
    private String newsSharesIpAddress;

    @Schema(description = "Timestamp when the newsapp was shared (ISO 8601 format)", example = "2024-06-01T12:34:56Z")
    @Size(max = 50, message = "Shared at must not exceed 50 characters")
    private String newsSharesSharedAt;
}
