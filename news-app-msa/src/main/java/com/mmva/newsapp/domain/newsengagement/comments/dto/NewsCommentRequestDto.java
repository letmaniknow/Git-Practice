package com.mmva.newsapp.domain.newsengagement.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Request DTO for adding a comment to a newsapp newsapp.")
public class NewsCommentRequestDto {
    private UUID newsCommentsParentId;

    @Schema(description = "ID of the news article being commented on", example = "a123f1ee-6c54-4b01-90e6-d701748f0123")
    @NotNull(message = "News ID is required")
    private UUID newsCommentsNewsId;

    @Schema(description = "User ID (auto-filled from JWT token)", example = "d290f1ee-6c54-4b01-90e6-d701748f0851", hidden = true)
    private UUID newsCommentsUserId;

    @Schema(description = "The comment text", example = "Great newsapp!")
    @NotBlank(message = "Comment is required")
    private String newsCommentsComment;

    @Schema(description = "IP address of the commenter (optional)", example = "192.168.1.1")
    @Size(max = 100, message = "IP address must not exceed 100 characters")
    private String newsCommentsIpAddress;
}
