package com.mmva.newsapp.infrastructure.monetization.ads.local.dto;

import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdCreativeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for uploading an Ad Creative file.
 * Used specifically for the file upload endpoint.
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdCreativeUploadRequestDto {

    /**
     * The creative file to upload (optional for updates).
     */
    private MultipartFile file;

    /**
     * Unique code for this creative.
     */
    @NotBlank(message = "Creative code is required")
    @Size(min = 3, max = 100, message = "Creative code must be between 3 and 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Creative code must contain only letters, numbers, and underscores")
    private String adCreativeCode;

    /**
     * Human-readable name for the creative.
     */
    @NotBlank(message = "Creative name is required")
    @Size(min = 1, max = 200, message = "Creative name must be between 1 and 200 characters")
    private String adCreativeName;

    /**
     * Detailed description of the creative content.
     */
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String adCreativeDescription;

    /**
     * Type of creative (will be auto-detected from file if not provided).
     */
    private AdCreativeType adCreativeType;

    /**
     * Alternative text for accessibility.
     */
    @Size(max = 300, message = "Alt text cannot exceed 300 characters")
    private String adCreativeAltText;

    /**
     * Click destination URL when ad is clicked.
     */
    @Pattern(regexp = "^https?://.*", message = "Click URL must be a valid HTTP/HTTPS URL")
    @Size(max = 1000, message = "Click URL cannot exceed 1000 characters")
    private String adCreativeClickUrl;

    /**
     * Whether this creative requires approval before use.
     */
    @Builder.Default
    private Boolean adCreativeRequiresApproval = false;

    /**
     * Additional metadata as JSON string.
     */
    private String adCreativeMetadataJson;

    /**
     * External creative ID from ad server.
     */
    @Size(max = 200, message = "External creative ID cannot exceed 200 characters")
    private String adCreativeExternalCreativeId;

    /**
     * External ad server name.
     */
    @Size(max = 100, message = "External ad server name cannot exceed 100 characters")
    private String adCreativeExternalAdServer;

    /**
     * Native ad title for structured content ads.
     */
    @Size(max = 200, message = "Native ad title cannot exceed 200 characters")
    private String adCreativeAdTitle;

    /**
     * Native ad media URL for structured content ads.
     */
    @Size(max = 500, message = "Native ad media URL cannot exceed 500 characters")
    private String adCreativeAdMediaUrl;

    /**
     * Native ad summary for structured content ads.
     */
    @Size(max = 500, message = "Native ad summary cannot exceed 500 characters")
    private String adCreativeAdSummary;
}