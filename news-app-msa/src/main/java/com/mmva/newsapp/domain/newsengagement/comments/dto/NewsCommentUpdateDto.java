package com.mmva.newsapp.domain.newsengagement.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Request DTO for updating a comment on a newsapp newsapp.")
public class NewsCommentUpdateDto {

    @Schema(description = "The updated comment text", example = "Updated comment text.")
    @NotBlank(message = "Comment is required")
    private String newsCommentsComment;
}
