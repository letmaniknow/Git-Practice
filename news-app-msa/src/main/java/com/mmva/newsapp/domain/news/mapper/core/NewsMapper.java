package com.mmva.newsapp.domain.news.mapper.core;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.service.media.MediaUrlService;

/**
 * PROFESSIONAL: MapStruct-based single source of truth for News entity to DTO
 * conversions.
 *
 * <p>
 * This mapper uses MapStruct to automatically generate compile-time optimized
 * conversion code.
 * Benefits over manual builders:
 * - ✅ Compile-time type safety (catches errors before runtime)
 * - ✅ Zero reflection overhead (generates direct code)
 * - ✅ Auto-generated when fields change
 * - ✅ IDE support with validation
 * - ✅ Enterprise standard pattern
 * </p>
 *
 * <h3>Used By:</h3>
 * <ul>
 * <li>AdminSearchService: All search results</li>
 * <li>NewsService: All get/list operations</li>
 * <li>Controllers: All API responses</li>
 * </ul>
 *
 * <p>
 * MapStruct generates an implementation of this abstract class at compile time
 * with:
 * - Null-safe mappings
 * - Custom field conversions (dates, URLs, etc.)
 * - Nested object handling
 * - Spring-managed MediaUrlService injection
 * </p>
 *
 * @author MMVA Team
 * @version 2.0 (MapStruct-based with Spring injection)
 * @since 2026-03-05
 */
@Mapper(componentModel = "spring")
public abstract class NewsMapper {

    @Autowired
    protected MediaUrlService mediaUrlService;

    /**
     * PROFESSIONAL: Convert NewsMasterEntity to NewsCreateResponseDto.
     *
     * <p>
     * This is the SINGLE SOURCE OF TRUTH for all entity-to-DTO conversions.
     * MapStruct generates an optimized implementation at compile time.
     * </p>
     *
     * <h3>Fields Mapped:</h3>
     * <ul>
     * <li><strong>Core Content:</strong> Title (EN/ES), Content (EN/ES), Excerpt,
     * Slug</li>
     * <li><strong>Media Files:</strong> Filename, Type, Size, URL, Media Type</li>
     * <li><strong>Images:</strong> Card URL, Hero URL, Thumbnail URL</li>
     * <li><strong>Workflow:</strong> Status, Active flag, Featured flag, Breaking
     * flag</li>
     * <li><strong>Metadata:</strong> Category, Keywords, Tags, Meta
     * title/description</li>
     * <li><strong>Publishing:</strong> Published date, Scheduled publish date</li>
     * <li><strong>Engagement:</strong> View, share, like, comment, bookmark, reply
     * counts</li>
     * <li><strong>Audit:</strong> Created by/at, Updated by/at</li>
     * <li><strong>Versioning:</strong> Priority, Version</li>
     * </ul>
     *
     * @param entity the NewsMasterEntity to convert
     * @return complete NewsCreateResponseDto with all fields populated
     */
    @Mapping(source = "newsNewsId", target = "newsNewsId")
    @Mapping(source = "newsTitleEn", target = "newsTitleEn")
    @Mapping(source = "newsTitleEs", target = "newsTitleEs")
    @Mapping(source = "newsExcerptEn", target = "newsExcerptEn")
    @Mapping(source = "newsExcerptEs", target = "newsExcerptEs")
    @Mapping(source = "newsContentEn", target = "newsContentEn")
    @Mapping(source = "newsContentEs", target = "newsContentEs")
    @Mapping(source = "newsContentFormat", target = "newsContentFormat", qualifiedByName = "contentFormatToString")
    @Mapping(source = "newsSlug", target = "newsSlug")

    // ==================== MEDIA FILES ====================
    @Mapping(source = "newsMediaFileName", target = "newsMediaFileName")
    @Mapping(source = "newsMediaFileType", target = "newsMediaFileType")
    @Mapping(source = "newsMediaFileSize", target = "newsMediaFileSize")
    @Mapping(source = "newsMediaFileName", target = "newsMediaFileUrl", qualifiedByName = "buildMediaUrl")
    @Mapping(source = "newsMediaType", target = "newsMediaType")

    // ==================== IMAGES ====================
    @Mapping(source = "newsThumbnailUrl", target = "newsThumbnailUrl", qualifiedByName = "buildThumbnailUrl")
    @Mapping(source = "newsImageCardUrl", target = "newsImageCardUrl", qualifiedByName = "buildCardImageUrl")
    @Mapping(source = "newsImageHeroUrl", target = "newsImageHeroUrl", qualifiedByName = "buildHeroImageUrl")

    // ==================== WORKFLOW & STATUS ====================
    @Mapping(source = "newsWorkflowStatus", target = "newsWorkflowStatus", qualifiedByName = "statusToString")
    @Mapping(source = "newsIsActive", target = "newsIsActive")
    @Mapping(source = "newsIsFeatured", target = "newsIsFeatured")
    @Mapping(source = "newsIsBreaking", target = "newsIsBreaking")
    @Mapping(source = "newsBreakingExpiresAt", target = "newsBreakingExpiresAt", qualifiedByName = "instantToString")

    // ==================== CATEGORY ====================
    @Mapping(source = "newsNewsCategoryId", target = "newsNewsCategoryId", qualifiedByName = "uuidToString")

    // ==================== SOURCE & LINKS ====================
    @Mapping(source = "newsSourceUrl", target = "newsSourceUrl")

    // ==================== PUBLISHING DATES ====================
    @Mapping(source = "newsPublishedAt", target = "newsPublishedAt", qualifiedByName = "instantToString")

    @Mapping(source = "newsScheduledPublishAt", target = "newsScheduledPublishAt", qualifiedByName = "instantToString")
    @Mapping(source = "newsScheduledBy", target = "newsScheduledBy", qualifiedByName = "uuidToString")

    // ==================== AUDIT: Published By ====================
    @Mapping(source = "newsPublishedBy", target = "newsPublishedBy", qualifiedByName = "uuidToString")

    // ==================== METADATA & SEO ====================
    @Mapping(source = "newsMetaTitle", target = "newsMetaTitle")
    @Mapping(source = "newsMetaDescription", target = "newsMetaDescription")
    @Mapping(source = "newsKeywords", target = "newsKeywords")
    @Mapping(source = "newsTags", target = "newsTags")

    // ==================== ENGAGEMENT COUNTERS ====================
    @Mapping(source = "newsViewCount", target = "newsViewCount")
    @Mapping(source = "newsShareCount", target = "newsShareCount")
    @Mapping(source = "newsLikeCount", target = "newsLikeCount")
    @Mapping(source = "newsCommentCount", target = "newsCommentCount")
    @Mapping(source = "newsBookmarkCount", target = "newsBookmarkCount")
    @Mapping(source = "newsReplyCount", target = "newsReplyCount")

    // ==================== CONTENT PROPERTIES ====================
    @Mapping(source = "newsExpiresAt", target = "newsExpiresAt", qualifiedByName = "instantToString")
    @Mapping(source = "newsReadTimeMinutes", target = "newsReadTimeMinutes")
    @Mapping(source = "newsLastEditedAt", target = "newsLastEditedAt", qualifiedByName = "instantToString")
    @Mapping(source = "newsContentOrigin", target = "newsContentOrigin", qualifiedByName = "contentOriginToString")
    @Mapping(source = "newsSourceAuthorName", target = "newsSourceAuthorName")
    @Mapping(source = "newsSourceAgencyId", target = "newsSourceAgencyId", qualifiedByName = "uuidToString")
    @Mapping(source = "newsIsSponsored", target = "newsIsSponsored")
    @Mapping(source = "newsSponsorName", target = "newsSponsorName")
    @Mapping(source = "newsSponsorLogoUrl", target = "newsSponsorLogoUrl")
    @Mapping(source = "newsSponsorWebsiteUrl", target = "newsSponsorWebsiteUrl")
    @Mapping(source = "newsIsPremium", target = "newsIsPremium")
    @Mapping(source = "newsPremiumTier", target = "newsPremiumTier")
    @Mapping(source = "newsCanonicalUrl", target = "newsCanonicalUrl")
    @Mapping(source = "newsEditorNotes", target = "newsEditorNotes")

    // ==================== AUDIT TRAIL ====================
    @Mapping(source = "createdAt", target = "newsCreatedAt", qualifiedByName = "instantToString")
    @Mapping(source = "createdBy", target = "newsCreatedBy")
    @Mapping(source = "updatedAt", target = "newsUpdatedAt", qualifiedByName = "instantToString")
    @Mapping(source = "updatedBy", target = "newsUpdatedBy")
    @Mapping(source = "deletedAt", target = "newsDeletedAt", qualifiedByName = "instantToString")
    @Mapping(source = "deletedBy", target = "newsDeletedBy")

    // ==================== VERSIONING & PRIORITY ====================
    @Mapping(source = "newsPriority", target = "newsPriority")
    @Mapping(source = "newsVersion", target = "newsVersion")
    @Mapping(source = "newsPreviousVersionId", target = "newsPreviousVersionId")

    // ==================== FIELDS NOT MAPPED (LEFT FOR SERVICE LAYER)
    // ====================
    // Source agency fields - Agency details can be fetched separately using
    // newsSourceAgencyId
    @Mapping(target = "newsSourceAgencyName", ignore = true)
    @Mapping(target = "newsSourceAgencyLogoUrl", ignore = true)

    // Processed content fields - Set by service layer, not stored in DB
    @Mapping(target = "mobileContentEn", ignore = true)
    @Mapping(target = "mobileContentEs", ignore = true)
    @Mapping(target = "webContentHtmlEn", ignore = true)
    @Mapping(target = "webContentHtmlEs", ignore = true)
    @Mapping(target = "cardExcerptEn", ignore = true)
    @Mapping(target = "cardExcerptEs", ignore = true)

    public abstract NewsCreateResponseDto toNewsCreateResponseDto(NewsMasterEntity entity);

    // ==================== Custom Conversion Methods ====================

    /**
     * Convert ContentOrigin enum to string safely
     */
    @Named("contentOriginToString")
    public String formatContentOrigin(Object contentOrigin) {
        return contentOrigin != null ? contentOrigin.toString() : null;
    }

    /**
     * Convert ContentFormat enum to string safely
     */
    @Named("contentFormatToString")
    public String formatContentFormat(Object format) {
        return format != null ? format.toString() : null;
    }

    /**
     * Convert enum to string safely
     */
    @Named("statusToString")
    public String formatStatus(Object status) {
        return status != null ? status.toString() : null;
    }

    /**
     * Convert Instant to ISO string
     */
    @Named("instantToString")
    public String formatInstant(java.time.Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    /**
     * Convert UUID to string
     */
    @Named("uuidToString")
    public String formatUuid(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    /**
     * Build media file URL using MediaUrlService
     */
    @Named("buildMediaUrl")
    public String buildMediaUrl(String fileName) {
        return fileName != null && mediaUrlService != null ? mediaUrlService.buildMediaUrl(fileName) : null;
    }

    /**
     * Build thumbnail URL using MediaUrlService
     */
    @Named("buildThumbnailUrl")
    public String buildThumbnailUrl(String fileName) {
        return fileName != null && mediaUrlService != null ? mediaUrlService.buildThumbnailUrl(fileName) : null;
    }

    /**
     * Build card image URL using MediaUrlService
     */
    @Named("buildCardImageUrl")
    public String buildCardImageUrl(String fileName) {
        return fileName != null && mediaUrlService != null ? mediaUrlService.buildImageUrl("card", fileName) : null;
    }

    /**
     * Build hero image URL using MediaUrlService
     */
    @Named("buildHeroImageUrl")
    public String buildHeroImageUrl(String fileName) {
        return fileName != null && mediaUrlService != null ? mediaUrlService.buildImageUrl("hero", fileName) : null;
    }

    /**
     * PROFESSIONAL: Copies NewsCreateResponseDto to NewsCreateResponseDto,
     * preserving all existing data
     * while allowing service layer to populate platform-specific processed content
     * fields.
     *
     * <p>
     * Used for creating platform-specific versions (mobile/web) where:
     * - mobileContentEn/Es: Mobile-optimized text content
     * - webContentHtmlEn/Es: Web-optimized HTML content
     * - cardExcerptEn/Es: Card-specific limited excerpts
     * </p>
     *
     * <h3>Design Pattern:</h3>
     * <ul>
     * <li>Source DTO → Target DTO full copy</li>
     * <li>Processed fields remain null/unset (service fills them)</li>
     * <li>All other fields preserved as-is</li>
     * </ul>
     *
     * @param source the source NewsCreateResponseDto
     * @return a copy of the DTO ready for platform-specific content injection
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            // Processed content fields - left for service layer to populate
            @Mapping(target = "mobileContentEn", ignore = true),
            @Mapping(target = "mobileContentEs", ignore = true),
            @Mapping(target = "webContentHtmlEn", ignore = true),
            @Mapping(target = "webContentHtmlEs", ignore = true),
            @Mapping(target = "cardExcerptEn", ignore = true),
            @Mapping(target = "cardExcerptEs", ignore = true)
    })
    public abstract NewsCreateResponseDto copyResponseDto(NewsCreateResponseDto source);
}
