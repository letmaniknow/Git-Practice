package com.mmva.newsapp.domain.news.dto.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for media file upload operations.
 * Contains the uploaded file details and accessible URL.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing uploaded media file details")
public class NewsMediaUploadResponseDto {

    @Schema(description = "Unique filename of the uploaded media", example = "a1b2c3d4-photo.jpg")
    private String filename;

    @Schema(description = "Original filename from upload", example = "photo.jpg")
    private String originalFilename;

    @Schema(description = "Full accessible URL to the media file", example = "http://localhost:8080/api/v1/public/news/media/a1b2c3d4-photo.jpg")
    private String url;

    @Schema(description = "Relative path to the media endpoint", example = "/api/v1/public/news/media/a1b2c3d4-photo.jpg")
    private String relativePath;

    @Schema(description = "MIME type of the uploaded file", example = "image/jpeg")
    private String contentType;

    @Schema(description = "File size in bytes", example = "102400")
    private Long fileSize;
}
