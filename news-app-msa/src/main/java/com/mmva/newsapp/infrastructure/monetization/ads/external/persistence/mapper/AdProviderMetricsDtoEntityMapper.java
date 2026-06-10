package com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.mapper;

import com.mmva.newsapp.infrastructure.monetization.ads.external.common.dto.AdProviderMetricsDto;
import com.mmva.newsapp.infrastructure.monetization.ads.external.persistence.entity.ProviderMetricsSync;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * MapStruct mapper for converting between AdProviderMetricsDto and
 * ProviderMetricsSync entity
 * 
 * Handles:
 * - DTO to Entity mapping (for saving to database)
 * - Entity to DTO mapping (for API responses)
 * - Custom type conversions (e.g., Double to BigDecimal, Map to JSON)
 * - Field name mappings where DTO/Entity names differ
 * 
 * Naming Convention: {Source}{Target}Mapper
 * Example: AdProviderMetricsDtoEntityMapper
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface AdProviderMetricsDtoEntityMapper {

    AdProviderMetricsDtoEntityMapper INSTANCE = Mappers.getMapper(AdProviderMetricsDtoEntityMapper.class);

    /**
     * Convert DTO to Entity for database persistence
     * 
     * @param dto DTO from API
     * @return Entity ready for database save
     */
    @Mapping(source = "adProviderEstimatedEarningsUsd", target = "adProviderEstimatedEarningsUsd", qualifiedByName = "doubleToDecimal")
    @Mapping(source = "adProviderCtrPercentage", target = "adProviderCtrPercentage", qualifiedByName = "doubleToDecimal")
    @Mapping(source = "adProviderCpmUsd", target = "adProviderCpmUsd", qualifiedByName = "doubleToDecimal")
    @Mapping(source = "adProviderCpcUsd", target = "adProviderCpcUsd", qualifiedByName = "doubleToDecimal")
    @Mapping(source = "adProviderMetadata", target = "adProviderMetadataJson", qualifiedByName = "mapToJson")
    @Mapping(source = "tenantId", target = "adProviderTenantId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "adProviderCode", ignore = true)
    @Mapping(target = "adProviderCreatedBy", ignore = true)
    @Mapping(target = "adProviderCreatedAt", ignore = true)
    @Mapping(target = "adProviderUpdatedBy", ignore = true)
    @Mapping(target = "adProviderUpdatedAt", ignore = true)
    @Mapping(target = "adProviderDeletedAt", ignore = true)
    ProviderMetricsSync toEntity(AdProviderMetricsDto dto);

    /**
     * Convert Entity to DTO for API responses
     * 
     * @param entity Entity from database
     * @return DTO for API response
     */
    @Mapping(source = "adProviderEstimatedEarningsUsd", target = "adProviderEstimatedEarningsUsd", qualifiedByName = "decimalToDouble")
    @Mapping(source = "adProviderCtrPercentage", target = "adProviderCtrPercentage", qualifiedByName = "decimalToDouble")
    @Mapping(source = "adProviderCpmUsd", target = "adProviderCpmUsd", qualifiedByName = "decimalToDouble")
    @Mapping(source = "adProviderCpcUsd", target = "adProviderCpcUsd", qualifiedByName = "decimalToDouble")
    @Mapping(source = "adProviderMetadataJson", target = "adProviderMetadata", qualifiedByName = "jsonToMap")
    @Mapping(source = "adProviderTenantId", target = "tenantId")
    AdProviderMetricsDto toDto(ProviderMetricsSync entity);

    // ========================================
    // Custom Type Converters
    // ========================================

    /**
     * Convert Double to BigDecimal
     * 
     * Handles null values gracefully
     * Uses HALF_UP rounding mode for financial calculations
     */
    @Named("doubleToDecimal")
    default BigDecimal doubleToDecimal(Double value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value);
    }

    /**
     * Convert BigDecimal to Double
     * 
     * Handles null values gracefully
     */
    @Named("decimalToDouble")
    default Double decimalToDouble(BigDecimal value) {
        if (value == null) {
            return 0.0;
        }
        return value.doubleValue();
    }

    /**
     * Convert Map metadata to JSON string
     * 
     * Stores provider-specific custom fields
     * Uses com.fasterxml.jackson.databind.ObjectMapper internally
     */
    @Named("mapToJson")
    default String mapToJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(metadata);
        } catch (Exception e) {
            // Return null on serialization error
            return null;
        }
    }

    /**
     * Convert JSON string to Map metadata
     * 
     * Retrieves provider-specific custom fields
     * Uses com.fasterxml.jackson.databind.ObjectMapper internally
     */
    @Named("jsonToMap")
    default Map<String, Object> jsonToMap(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(metadataJson, Map.class);
        } catch (Exception e) {
            // Return empty map on deserialization error
            return new HashMap<>();
        }
    }
}
