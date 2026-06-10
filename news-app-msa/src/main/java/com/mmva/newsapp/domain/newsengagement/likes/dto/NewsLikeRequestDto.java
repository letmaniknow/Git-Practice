package com.mmva.newsapp.domain.newsengagement.likes.dto;

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
public class NewsLikeRequestDto {

    @Schema(description = "Unique identifier of the news article being liked", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "News ID is required")
    private UUID newsLikesNewsId;

    @Schema(description = "User ID (auto-filled from JWT token)", example = "987e6543-e21b-12d3-a456-426614174999", hidden = true)
    private UUID newsLikesUserId;

    @Schema(description = "IP address from which the newsapp was liked", example = "192.168.1.1")
    @Size(max = 100, message = "IP address must not exceed 100 characters")
    private String newsLikesIpAddress;
}
