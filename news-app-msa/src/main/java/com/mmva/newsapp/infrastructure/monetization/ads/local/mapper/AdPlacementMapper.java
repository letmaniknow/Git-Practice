package com.mmva.newsapp.infrastructure.monetization.ads.local.mapper;

import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementRequestDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.dto.AdPlacementResponseDto;
import com.mmva.newsapp.infrastructure.monetization.ads.local.model.AdPlacement;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.AdType;
import com.mmva.newsapp.infrastructure.monetization.ads.local.enums.PlacementPosition;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * MapStruct mapper for Ad Placement entities and DTOs.
 * 
 * <p>
 * Field names are consistent between Entity and DTOs using the
 * {@code adPlacement} prefix pattern for clarity and maintainability.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface AdPlacementMapper {

    // ========================================
    // Request DTO to Entity
    // ========================================

    /**
     * Maps request DTO to entity for creating a new placement.
     * Most fields map automatically due to consistent naming.
     */
    @Mapping(target = "adPlacementId", ignore = true)
    @Mapping(target = "adPlacementTotalImpressions", expression = "java(0L)")
    @Mapping(target = "adPlacementTotalClicks", expression = "java(0L)")
    @Mapping(target = "adPlacementTotalRevenue", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "adPlacementLastImpressionAt", ignore = true)
    @Mapping(target = "adPlacementTenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AdPlacement toEntity(AdPlacementRequestDto dto);

    /**
     * Updates existing entity from request DTO.
     */
    @Mapping(target = "adPlacementId", ignore = true)
    @Mapping(target = "adPlacementTotalImpressions", ignore = true)
    @Mapping(target = "adPlacementTotalClicks", ignore = true)
    @Mapping(target = "adPlacementTotalRevenue", ignore = true)
    @Mapping(target = "adPlacementLastImpressionAt", ignore = true)
    @Mapping(target = "adPlacementTenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(AdPlacementRequestDto dto, @MappingTarget AdPlacement entity);

    // ========================================
    // Entity to Response DTO
    // ========================================

    /**
     * Maps entity to response DTO with computed fields.
     * Most fields map automatically due to consistent naming.
     */
    @Mapping(source = "createdAt", target = "adPlacementCreatedAt")
    @Mapping(source = "createdBy", target = "adPlacementCreatedBy")
    @Mapping(source = "updatedAt", target = "adPlacementUpdatedAt")
    @Mapping(source = "updatedBy", target = "adPlacementUpdatedBy")
    @Mapping(target = "adPlacementAdTypeDisplayName", expression = "java(getAdTypeDisplayName(entity.getAdPlacementAdType()))")
    @Mapping(target = "adPlacementPositionDisplayName", expression = "java(getPositionDisplayName(entity.getAdPlacementPosition()))")
    @Mapping(target = "adPlacementDimensionsDisplay", expression = "java(entity.getDimensionsDisplay())")
    @Mapping(target = "adPlacementEffectiveCpmRate", expression = "java(entity.getEffectiveCpmRate())")
    @Mapping(target = "adPlacementFormattedCpmRate", expression = "java(formatCurrency(entity.getAdPlacementBaseCpmRate()))")
    @Mapping(target = "adPlacementFormattedCpcRate", expression = "java(formatCurrency(entity.getAdPlacementBaseCpcRate()))")
    @Mapping(target = "adPlacementIsServing", expression = "java(entity.isServing())")
    @Mapping(target = "adPlacementClickThroughRate", expression = "java(entity.getClickThroughRate())")
    @Mapping(target = "adPlacementRevenuePerMille", expression = "java(entity.getRevenuePerMille())")
    @Mapping(target = "adPlacementFormattedRevenue", expression = "java(formatCurrency(entity.getAdPlacementTotalRevenue()))")
    AdPlacementResponseDto toResponseDto(AdPlacement entity);

    /**
     * Maps list of entities to response DTOs.
     */
    List<AdPlacementResponseDto> toResponseDtoList(List<AdPlacement> entities);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Gets display name for ad type.
     * Uses the enum's built-in getDisplayName() method.
     */
    default String getAdTypeDisplayName(AdType adType) {
        if (adType == null) {
            return "Unknown";
        }
        return adType.getDisplayName() + " (" + adType.getDimensions() + ")";
    }

    /**
     * Gets display name for placement position.
     * Uses the enum's built-in getDisplayName() method.
     */
    default String getPositionDisplayName(PlacementPosition position) {
        if (position == null) {
            return "Unknown";
        }
        return position.getDisplayName();
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
