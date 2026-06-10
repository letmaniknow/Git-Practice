package com.mmva.newsapp.domain.news.dto.core;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * News Create Request DTO - Admin input for creating news articles.
 * 
 * <p>
 * Organized by <b>Domain Sections</b> with Required fields first in each
 * section.
 * This hybrid approach follows industry best practices for maintainability
 * and aligns with the Entity structure for easy mapping.
 * </p>
 * 
 * <h3>Section Order (Hybrid Domain-First Organization):</h3>
 * <ol>
 * <li>CONTENT - Titles, body text (bilingual) [Required first, then
 * Optional]</li>
 * <li>MEDIA - File uploads [Required first, then Optional]</li>
 * <li>CATEGORIZATION & SOURCE - Category, source URL [Required first]</li>
 * <li>WORKFLOW & PUBLISHING - Status, featured, breaking [Required first]</li>
 * <li>LOCATION - Geographic targeting [All Optional]</li>
 * <li>MONETIZATION - Sponsored, premium content [All Optional]</li>
 * <li>SEO - Meta tags, keywords [All Optional, auto-generated]</li>
 * <li>SERIES - Multi-part story support [All Optional]</li>
 * <li>INTERNAL - Editor notes [Optional]</li>
 * <li>SYSTEM - Hidden fields set by server</li>
 * </ol>
 * 
 * <h3>Field Legend:</h3>
 * <ul>
 * <li>🔴 @NotNull/@NotBlank = REQUIRED (9 fields)</li>
 * <li>🟡 No annotation = OPTIONAL (30+ fields)</li>
 * <li>🟢 hidden = true = SYSTEM-SET (never in form)</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Builder
@Data
public class NewsCreateRequestDto {

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 1: CONTENT - Core article content (Bilingual)
    // Required: titleEn, titleEs, contentEn, contentEs
    // Optional: excerpts, HTML versions, format, tags
    // ═══════════════════════════════════════════════════════════════════════════

    // --- 🔴 Required Content Fields ---

    @Schema(description = "News title in English", example = "Global Market Update", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "newsTitleEn is required")
    private String newsTitleEn;

    @Schema(description = "News title in Spanish", example = "Actualización del Mercado Global", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "newsTitleEs is required")
    private String newsTitleEs;

    @Schema(description = "News content in English", example = "The global markets saw a significant...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "newsContentEn is required")
    private String newsContentEn;

    @Schema(description = "News content in Spanish", example = "Los mercados globales experimentaron...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "newsContentEs is required")
    private String newsContentEs;

    // --- 🟡 Optional Content Fields ---

    @Schema(description = "Manual excerpt/summary in English (optional, auto-generated if not provided)", example = "Global markets experienced significant volatility...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "newsExcerptEn must not exceed 500 characters")
    private String newsExcerptEn;

    @Schema(description = "Manual excerpt/summary in Spanish (optional, auto-generated if not provided)", example = "Los mercados globales experimentaron una volatilidad significativa...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "newsExcerptEs must not exceed 500 characters")
    private String newsExcerptEs;

    @Schema(description = "HTML-formatted content for English (optional, auto-generated from plain text if not provided)", example = "<p>The global markets saw a significant...</p>", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsContentHtmlEn;

    @Schema(description = "HTML-formatted content for Spanish (optional, auto-generated from plain text if not provided)", example = "<p>Los mercados globales experimentaron...</p>", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsContentHtmlEs;

    @Schema(description = "Content format: PLAIN_TEXT, HTML_BASIC, HTML_RICH, MARKDOWN (defaults to PLAIN_TEXT)", example = "PLAIN_TEXT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsContentFormat;

    @Schema(description = "Comma-separated tags/labels for the news", example = "Politics,Economy,Breaking News", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsTags;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 2: MEDIA - File uploads
    // Required: imageVideoFile
    // Optional: thumbnailFile (auto-generated if not provided)
    // ═══════════════════════════════════════════════════════════════════════════

    // --- 🔴 Required Media Field ---

    @Schema(description = "Image or video file for the news (required for CREATE, optional for UPDATE - keep existing if not provided)", type = "string", format = "binary", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MultipartFile imageVideoFile;

    // --- 🟡 Optional Media Field ---

    @Schema(description = "Custom thumbnail image (optional). If not provided, thumbnail will be auto-generated from imageVideoFile at 1024×512 px", type = "string", format = "binary", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MultipartFile thumbnailFile;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 3: CATEGORIZATION & SOURCE
    // Required: categoryId, sourceUrl
    // Optional: authorName, agencyId, contentOrigin
    // ═══════════════════════════════════════════════════════════════════════════

    // --- 🔴 Required Fields ---

    @Schema(description = "News Category ID (UUID)", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "newsNewsCategoryId is required")
    private UUID newsNewsCategoryId;

    @Schema(description = "Source URL of the news article", example = "https://news.example.com/article/123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "newsSourceUrl is required")
    private String newsSourceUrl;

    // --- 🟡 Optional Attribution Fields ---

    @Schema(description = "Author byline for display", example = "John Smith", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 255, message = "newsSourceAuthorName must not exceed 255 characters")
    private String newsSourceAuthorName;

    @Schema(description = "ID of the source news agency (for syndicated content)", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID newsSourceAgencyId;

    @Schema(description = "Source of the content: ORIGINAL, SYNDICATED, USER_SUBMITTED, PARTNER", example = "ORIGINAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsContentOrigin;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 4: WORKFLOW & PUBLISHING
    // Required: workflowStatus, isFeatured
    // Optional: breaking, embargo, expiration, urgency, audience
    // ═══════════════════════════════════════════════════════════════════════════

    // --- 🔴 Required Publishing Fields ---

    @Schema(description = "Workflow status determines when push notification is sent. " +
            "DRAFT = save for later (no notification), " +
            "PUBLISHED = publish immediately (sends push notification), " +
            "SCHEDULED = publish at scheduled time (notification on publish). " +
            "Allowed values: DRAFT, SUBMITTED, REVIEWED, APPROVED, SCHEDULED, PUBLISHED", example = "DRAFT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "newsWorkflowStatus is required. Choose: DRAFT (no notification) or PUBLISHED (sends notification)")
    private String newsWorkflowStatus;

    @Schema(description = "Whether the news is featured on homepage", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "newsIsFeatured is required")
    private Boolean newsIsFeatured;

    // --- 🟡 Optional: Scheduling ---

    @Schema(description = "Scheduled publish date/time (ISO 8601). Required if newsWorkflowStatus is SCHEDULED. Must be in the future (max 1 year)", example = "2026-02-10T09:00:00Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Instant newsScheduledPublishAt;

    // --- 🟡 Optional: Breaking News ---

    @Schema(description = "Whether this is breaking news", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean newsIsBreaking;

    @Schema(description = "When the breaking news status expires (ISO 8601). Required if newsIsBreaking is true", example = "2024-06-01T18:00:00Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsBreakingExpiresAt;

    // --- 🟡 Optional: Advanced Publishing Controls ---

    @Schema(description = "Embargo until timestamp (ISO 8601) - article not visible until this time", example = "2024-06-01T18:00:00Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsEmbargoUntil;

    @Schema(description = "When this news should expire/auto-archive (ISO 8601). Null means never expires", example = "2024-12-31T23:59:59Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsExpiresAt;

    @Schema(description = "Content urgency level: LOW, NORMAL, HIGH, BREAKING", example = "NORMAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsUrgencyLevel;

    @Schema(description = "Target audience for content segmentation", example = "general", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50, message = "newsTargetAudience must not exceed 50 characters")
    private String newsTargetAudience;

    @Schema(description = "Estimated reading time in minutes. Auto-calculated if not provided", example = "5", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer newsReadTimeMinutes;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 5: LOCATION - Geographic targeting
    // All Optional
    // ═══════════════════════════════════════════════════════════════════════════

    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "US", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 2, message = "newsCountryCode must be 2 characters")
    private String newsCountryCode;

    @Schema(description = "Geographic region/state", example = "California", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "newsRegion must not exceed 100 characters")
    private String newsRegion;

    @Schema(description = "City name", example = "Los Angeles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 100, message = "newsCity must not exceed 100 characters")
    private String newsCity;

    @Schema(description = "Latitude coordinate (-90 to 90)", example = "34.052235", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Double newsLatitude;

    @Schema(description = "Longitude coordinate (-180 to 180)", example = "-118.243683", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Double newsLongitude;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 6: MONETIZATION - Sponsored & Premium content
    // All Optional
    // ═══════════════════════════════════════════════════════════════════════════

    // --- 🟡 Sponsored Content ---

    @Schema(description = "Whether this is sponsored/paid content", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean newsIsSponsored;

    @Schema(description = "Sponsor/advertiser name (required if newsIsSponsored is true)", example = "Acme Corporation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 255, message = "newsSponsorName must not exceed 255 characters")
    private String newsSponsorName;

    @Schema(description = "URL to sponsor's logo", example = "https://cdn.example.com/sponsors/acme.png", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsSponsorLogoUrl;

    @Schema(description = "CTA link to sponsor's website", example = "https://www.acme.com", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsSponsorWebsiteUrl;

    // --- 🟡 Premium/Paywall Content ---

    @Schema(description = "Whether this content is behind a paywall", example = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean newsIsPremium;

    @Schema(description = "Minimum subscription tier required: FREE, BASIC, PRO, ENTERPRISE", example = "FREE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsPremiumTier;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 7: SEO - Search engine optimization
    // All Optional (auto-generated if not provided)
    // ═══════════════════════════════════════════════════════════════════════════

    @Schema(description = "Meta title for SEO (auto-generated if not provided)", example = "Global Market Update - Latest Breaking News", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsMetaTitle;

    @Schema(description = "Meta description for SEO (auto-generated if not provided)", example = "Stay updated with the latest global market news and financial trends.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsMetaDescription;

    @Schema(description = "SEO keywords (comma-separated, auto-generated if not provided)", example = "market,stocks,finance,economy,breaking news", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsKeywords;

    @Schema(description = "Canonical URL for SEO (prevents duplicate content penalties)", example = "https://news.example.com/article/global-market-update", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String newsCanonicalUrl;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 8: SERIES - Multi-part story support
    // All Optional
    // ═══════════════════════════════════════════════════════════════════════════

    @Schema(description = "Series ID for linking related articles (multi-part stories)", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UUID newsSeriesId;

    @Schema(description = "Order of article within a series (1, 2, 3...)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer newsSeriesOrder;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 9: INTERNAL - Editor notes
    // Optional
    // ═══════════════════════════════════════════════════════════════════════════

    @Schema(description = "Internal editor notes (not shown publicly)", example = "Needs review before publishing", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 2000, message = "newsEditorNotes must not exceed 2000 characters")
    private String newsEditorNotes;

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION 10: SYSTEM - Hidden fields set by server
    // Never shown in form - set from JWT token
    // ═══════════════════════════════════════════════════════════════════════════

    @Schema(description = "User ID of the creator (set by server from JWT)", hidden = true)
    private UUID createdBy;
}
