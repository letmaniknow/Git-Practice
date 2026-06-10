package com.mmva.newsapp.domain.newsengagement.views.dto;

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
public class NewsViewRequestDto {

    @Schema(description = "Unique identifier of the newsapp being viewed", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "News ID is required")
    private UUID newsViewsNewsId;

    @Schema(description = "Unique identifier of the user viewing the newsapp", example = "987e6543-e21b-12d3-a456-426614174999")
    private UUID newsViewsUserId;

    @Schema(description = "IP address from which the newsapp was viewed", example = "192.168.1.1")
    @Size(max = 100, message = "IP address must not exceed 100 characters")
    private String newsViewsIpAddress;
}
