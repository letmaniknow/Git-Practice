package com.mmva.newsapp.domain.news.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for image processing response with multiple size variants.
 * Returned after processing generates thumbnail, card, and hero versions.
 *
 * <h3>Size Variants (Inshorts-style):</h3>
 * <ul>
 * <li><b>Thumbnail:</b> 200x150 (4:3) - Optimized for lists and notifications</li>
 * <li><b>Card:</b> 400x225 (16:9) - For mobile compact views</li>
 * <li><b>Hero:</b> 800x450 (16:9) - Standard web display</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 2026-02-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageProcessingResponseDto {

    /**
     * Original filename (before processing).
     */
    private String originalFilename;

    /**
     * Processing source type.
     */
    private ProcessingSource source;

    /**
     * Main/original variant (original or configured size).
     */
    private ImageVariant main;

    /**
     * Thumbnail variant (200x150 - 4:3 aspect ratio).
     */
    private ImageVariant thumbnail;

    /**
     * Card variant (400x225 - 16:9 aspect ratio).
     */
    private ImageVariant card;

    /**
     * Hero variant (800x450 - 16:9 aspect ratio).
     */
    private ImageVariant hero;

    /**
     * Enum representing how the images were processed.
     */
    public enum ProcessingSource {
        /** Processed from uploaded image */
        IMAGE_PROCESS,
        /** Frames extracted from video */
        VIDEO_FRAME
    }

    /**
     * Represents a single image size variant.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageVariant {

        /**
         * The generated filename.
         * Pattern: {sanitized-name}_{uuid}_{size}.jpg
         */
        private String filename;

        /**
         * The full HTTP URL to access the image.
         * Example: http://localhost:8080/api/v1/public/news/images/card/breaking-news_a1b2c3d4_card.jpg
         */
        private String url;

        /**
         * The file system path where image is stored.
         * Used internally, not exposed to clients.
         */
        private String filePath;

        /**
         * File size in bytes.
         */
        private Long fileSize;

        /**
         * MIME type (always image/jpeg for processed images).
         */
        private String contentType;

        /**
         * Width of the image in pixels.
         */
        private Integer width;

        /**
         * Height of the image in pixels.
         */
        private Integer height;

        /**
         * Size variant name (thumbnail, card, hero, avatar).
         */
        private String size;
    }
}