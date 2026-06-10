package com.mmva.newsapp.infrastructure.monetization.ads.local.mapper;

import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdCreativeResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdCreativeType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdCreative;
import com.mmva.newsapp.infrastructure.monetization.ads.local.service.AdsUrlService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * MapStruct mapper for Ad Creative entities and DTOs.
 *
 * <p>
 * Field names are consistent between Entity and DTOs using the
 * {@code adCreative} prefix pattern for clarity and maintainability.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface AdCreativeMapper {

    // ========================================
    // Request DTO to Entity
    // ========================================

    /**
     * Maps request DTO to entity for creating a new creative.
     * Most fields map automatically due to consistent naming.
     */
    @Mapping(target = "adCreativeId", ignore = true)
    @Mapping(target = "adCreativeTotalImpressions", expression = "java(0L)")
    @Mapping(target = "adCreativeTotalClicks", expression = "java(0L)")
    @Mapping(target = "adCreativeTotalRevenue", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "adCreativeLastServedAt", ignore = true)
    @Mapping(target = "adCreativeTenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    // Fields set by service layer during file processing
    @Mapping(target = "adCreativeFileName", ignore = true)
    @Mapping(target = "adCreativeThumbnailFilename", ignore = true)
    @Mapping(target = "adCreativeFileSizeBytes", ignore = true)
    @Mapping(target = "adCreativeMimeType", ignore = true)
    @Mapping(target = "adCreativeDurationSeconds", ignore = true)
    @Mapping(target = "adCreativeIsActive", ignore = true)
    @Mapping(target = "adCreativeApprovalStatus", ignore = true)
    @Mapping(target = "adCreativeRejectionReason", ignore = true)
    AdCreative toEntity(AdCreativeRequestDto dto);

    /**
     * Updates existing entity from request DTO.
     */
    @Mapping(target = "adCreativeId", ignore = true)
    @Mapping(target = "adCreativeTotalImpressions", ignore = true)
    @Mapping(target = "adCreativeTotalClicks", ignore = true)
    @Mapping(target = "adCreativeTotalRevenue", ignore = true)
    @Mapping(target = "adCreativeLastServedAt", ignore = true)
    @Mapping(target = "adCreativeTenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    // Fields managed by service layer
    @Mapping(target = "adCreativeFileName", ignore = true)
    @Mapping(target = "adCreativeThumbnailFilename", ignore = true)
    @Mapping(target = "adCreativeFileSizeBytes", ignore = true)
    @Mapping(target = "adCreativeMimeType", ignore = true)
    @Mapping(target = "adCreativeDurationSeconds", ignore = true)
    @Mapping(target = "adCreativeIsActive", ignore = true)
    @Mapping(target = "adCreativeApprovalStatus", ignore = true)
    @Mapping(target = "adCreativeRejectionReason", ignore = true)
    void updateEntityFromDto(AdCreativeRequestDto dto, @MappingTarget AdCreative entity);

    // ========================================
    // Entity to Response DTO
    // ========================================

    /**
     * Maps entity to response DTO with computed fields.
     * Most fields map automatically due to consistent naming.
     */
    @Mapping(source = "createdAt", target = "adCreativeCreatedAt")
    @Mapping(source = "createdBy", target = "adCreativeCreatedBy")
    @Mapping(source = "updatedAt", target = "adCreativeUpdatedAt")
    @Mapping(source = "updatedBy", target = "adCreativeUpdatedBy")
    @Mapping(target = "adCreativeTypeDisplayName", expression = "java(getCreativeTypeDisplayName(entity.getAdCreativeType()))")
    @Mapping(target = "adCreativeDimensionsDisplay", expression = "java(entity.getDimensionsDisplay())")
    @Mapping(target = "adCreativeFileSizeDisplay", expression = "java(formatFileSize(entity.getAdCreativeFileSizeBytes()))")
    @Mapping(target = "adCreativeDurationDisplay", expression = "java(formatDuration(entity.getAdCreativeDurationSeconds()))")
    @Mapping(target = "adCreativeIsAvailable", expression = "java(entity.isAvailable())")
    @Mapping(target = "adCreativeClickThroughRate", expression = "java(entity.getClickThroughRate())")
    @Mapping(target = "adCreativeRevenuePerMille", expression = "java(entity.getRevenuePerMille())")
    @Mapping(target = "adCreativeTotalRevenueDisplay", expression = "java(formatCurrency(entity.getAdCreativeTotalRevenue()))")
    @Mapping(target = "adCreativeFileUrl", expression = "java(entity.getAdCreativeFileName() != null && !entity.getAdCreativeFileName().trim().isEmpty() ? \"/api/v1/ads/creatives/files/\" + entity.getAdCreativeFileName() : null)")
    @Mapping(target = "adCreativeThumbnailUrl", expression = "java(entity.getAdCreativeThumbnailFilename() != null && !entity.getAdCreativeThumbnailFilename().trim().isEmpty() ? \"/api/v1/ads/creatives/thumbnails/\" + entity.getAdCreativeThumbnailFilename() : null)")
    // Computed display fields (not directly mapped)
    @Mapping(target = "adCreativeClickThroughRateDisplay", ignore = true)
    @Mapping(target = "adCreativeRevenuePerMilleDisplay", ignore = true)
    AdCreativeResponseDto toResponseDto(AdCreative entity);

    /**
     * Maps list of entities to response DTOs.
     */
    List<AdCreativeResponseDto> toResponseDtoList(List<AdCreative> entities);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Gets display name for creative type.
     * Uses the enum's built-in getDisplayName() method.
     */
    default String getCreativeTypeDisplayName(AdCreativeType creativeType) {
        if (creativeType == null) {
            return "Unknown";
        }
        return creativeType.getDisplayName() + " (" + String.join(", ", creativeType.getSupportedMimeTypes()) + ")";
    }

    /**
     * Formats file size for display.
     */
    default String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }

        double size = bytes;
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * Formats duration for display.
     */
    default String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) {
            return "N/A";
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }

    /**
     * Formats currency value for display.
     */
    default String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}