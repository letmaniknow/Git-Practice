package com.mmva.newsapp.domain.newsengagement.bookmarks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(description = "Response DTO for a user's bookmark on a newsapp newsapp.")
public class NewsBookmarkResponseDto {

    @Schema(description = "Unique ID of the bookmark", example = "b456f1ee-6c54-4b01-90e6-d701748f0456")
    private UUID newsBookmarksId;

    @Schema(description = "ID of the user who owns the bookmark", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID newsBookmarksUserId;

    @Schema(description = "ID of the bookmarked newsapp newsapp", example = "a123f1ee-6c54-4b01-90e6-d701748f0123")
    private UUID newsBookmarksNewsId;

    @Schema(description = "Folder name for organizing bookmarks", example = "Favorites")
    private String newsBookmarksFolderName;

    @Schema(description = "Timestamp when the bookmark was added (ISO 8601)", example = "2025-12-14T12:00:00Z")
    private String newsBookmarksBookmarkedAt;

    // ========================================
    // Updated Counter (for real-time UI updates)
    // ========================================

    @Schema(description = "Updated total bookmark count for the news article after this action", example = "89")
    private Long updatedBookmarkCount;
}
