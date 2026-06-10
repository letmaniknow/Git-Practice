package com.mmva.newsapp.domain.news.dto.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for news content in card format for list views and previews.
 *
 * <p>
 * Contains minimal content optimized for card-based displays
 * in lists, feeds, and preview components.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "News content in card format for list views and previews")
public class NewsCardDto {

    @Schema(description = "Unique identifier for the news item", example = "1")
    private Long newsId;

    @Schema(description = "News title in English", example = "Breaking News: Major Event Occurs")
    private String newsTitleEn;

    @Schema(description = "News title in Spanish", example = "Noticia de Última Hora: Evento Mayor Ocurre")
    private String newsTitleEs;

    @Schema(description = "Auto-generated excerpt in English", example = "Breaking news summary...")
    private String newsExcerptEn;

    @Schema(description = "Auto-generated excerpt in Spanish", example = "Resumen de noticia de última hora...")
    private String newsExcerptEs;

    @Schema(description = "Content format used", example = "PLAIN_TEXT")
    private ContentFormat contentFormat;

    @Schema(description = "Estimated reading time in minutes", example = "3")
    private Integer readingTimeMinutes;

    @Schema(description = "News category", example = "Politics")
    private String newsCategory;

    @Schema(description = "News priority level", example = "HIGH")
    private String newsPriority;

    @Schema(description = "Publication timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    @Schema(description = "Author name", example = "John Doe")
    private String authorName;

    @Schema(description = "Source name", example = "Reuters")
    private String sourceName;

    @Schema(description = "Whether this is a card format", example = "true")
    @Builder.Default
    private Boolean isCard = true;

    @Schema(description = "Platform identifier", example = "card")
    @Builder.Default
    private String platform = "card";

    @Schema(description = "Version of the content format", example = "1.0")
    @Builder.Default
    private String contentVersion = "1.0";

    @Schema(description = "Thumbnail image URL", example = "https://newsapp.com/images/thumbnail.jpg")
    private String thumbnailUrl;

    @Schema(description = "Full media file URL (image/video)", example = "https://newsapp.com/media/article-image.jpg")
    private String mediaFileUrl;

    @Schema(description = "Card display priority", example = "1")
    private Integer displayPriority;

    // ========================================
    // Engagement Metrics
    // ========================================

    @Schema(description = "Number of views for the news item", example = "1234")
    private Long viewCount;

    @Schema(description = "Number of likes for the news item", example = "789")
    private Long likeCount;

    @Schema(description = "Number of shares for the news item", example = "56")
    private Long shareCount;

    @Schema(description = "Number of comments on the news item", example = "42")
    private Long commentCount;

    @Schema(description = "Number of bookmarks for the news item", example = "15")
    private Long bookmarkCount;

    @Schema(description = "Number of replies on comments", example = "28")
    private Long replyCount;
}