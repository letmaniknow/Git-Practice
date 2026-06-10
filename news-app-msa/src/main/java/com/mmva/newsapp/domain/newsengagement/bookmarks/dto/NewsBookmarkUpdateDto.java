package com.mmva.newsapp.domain.newsengagement.bookmarks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Request DTO for updating a user's bookmark folder.")
public class NewsBookmarkUpdateDto {

    @Schema(description = "New folder name for the bookmark", example = "Read Later")
    @Size(max = 255, message = "Folder name must not exceed 255 characters")
    private String newsBookmarksFolderName;
}
