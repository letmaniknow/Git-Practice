package com.mmva.newsapp.domain.newsengagement.bookmarks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

@Schema(description = "Request DTO for adding a bookmark to a newsapp newsapp.")
public class NewsBookmarkRequestDto {

    @Schema(description = "ID of the user adding the bookmark (auto-filled from JWT)", example = "d290f1ee-6c54-4b01-90e6-d701748f0851", hidden = true)
    private UUID newsBookmarksUserId;

    @Schema(description = "ID of the news article to bookmark", example = "a123f1ee-6c54-4b01-90e6-d701748f0123")
    @NotNull(message = "News ID is required")
    private UUID newsBookmarksNewsId;

    @Schema(description = "Optional folder name for organizing bookmarks", example = "Favorites")
    @Size(max = 255, message = "Folder name must not exceed 255 characters")
    private String newsBookmarksFolderName;

    @Schema(description = "Timestamp when the bookmark was added (ISO 8601)", example = "2025-12-14T12:00:00Z")
    @Size(max = 50, message = "Bookmarked at must not exceed 50 characters")
    private String newsBookmarksBookmarkedAt;
}
