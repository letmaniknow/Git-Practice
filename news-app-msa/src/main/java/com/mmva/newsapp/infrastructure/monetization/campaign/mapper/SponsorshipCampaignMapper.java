package com.mmva.newsapp.infrastructure.monetization.campaign.mapper;

import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignRequestDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.dto.SponsorshipCampaignResponseDto;
import com.mmva.newsapp.infrastructure.monetization.campaign.model.SponsorshipCampaign;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignStatus;
import com.mmva.newsapp.infrastructure.monetization.campaign.enums.SponsorshipCampaignType;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * MapStruct mapper for SponsorshipCampaign entities and DTOs.
 * All field mappings use sponsorshipCampaign prefix for consistency.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface SponsorshipCampaignMapper {

    // ========================================
    // Request DTO to Entity
    // ========================================

    /**
     * Maps request DTO to entity for creating a new campaign.
     */
    @Mapping(target = "sponsorshipCampaignId", ignore = true)
    @Mapping(source = "sponsorshipCampaignCode", target = "sponsorshipCampaignCode", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignName", target = "sponsorshipCampaignName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignDescription", target = "sponsorshipCampaignDescription", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignType", target = "sponsorshipCampaignType")
    @Mapping(source = "sponsorshipCampaignAdvertiserId", target = "sponsorshipCampaignAdvertiserId")
    @Mapping(source = "sponsorshipCampaignAdvertiserName", target = "sponsorshipCampaignAdvertiserName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignAdvertiserEmail", target = "sponsorshipCampaignAdvertiserEmail", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBrandName", target = "sponsorshipCampaignBrandName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBrandLogoUrl", target = "sponsorshipCampaignBrandLogoUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignStartDate", target = "sponsorshipCampaignStartDate")
    @Mapping(source = "sponsorshipCampaignEndDate", target = "sponsorshipCampaignEndDate")
    @Mapping(source = "sponsorshipCampaignTotalBudget", target = "sponsorshipCampaignTotalBudget")
    @Mapping(source = "sponsorshipCampaignDailyBudget", target = "sponsorshipCampaignDailyBudget")
    @Mapping(source = "sponsorshipCampaignPricingModel", target = "sponsorshipCampaignPricingModel", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignRate", target = "sponsorshipCampaignRate")
    @Mapping(source = "sponsorshipCampaignTargetImpressions", target = "sponsorshipCampaignTargetImpressions")
    @Mapping(source = "sponsorshipCampaignTargetClicks", target = "sponsorshipCampaignTargetClicks")
    @Mapping(source = "sponsorshipCampaignTargetCategoriesJson", target = "sponsorshipCampaignTargetCategoriesJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetLocationsJson", target = "sponsorshipCampaignTargetLocationsJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetDevices", target = "sponsorshipCampaignTargetDevices", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetTiersJson", target = "sponsorshipCampaignTargetTiersJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignContentId", target = "sponsorshipCampaignContentId")
    @Mapping(source = "sponsorshipCampaignDestinationUrl", target = "sponsorshipCampaignDestinationUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignCtaText", target = "sponsorshipCampaignCtaText", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignHeadline", target = "sponsorshipCampaignHeadline", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBodyText", target = "sponsorshipCampaignBodyText", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignImageUrl", target = "sponsorshipCampaignImageUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignVideoUrl", target = "sponsorshipCampaignVideoUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignCreativeAssetsJson", target = "sponsorshipCampaignCreativeAssetsJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmCampaign", target = "sponsorshipCampaignUtmCampaign", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmSource", target = "sponsorshipCampaignUtmSource", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmMedium", target = "sponsorshipCampaignUtmMedium", qualifiedByName = "decodeString")
    @Mapping(target = "sponsorshipCampaignStatus", constant = "DRAFT")
    @Mapping(target = "sponsorshipCampaignAmountSpent", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "sponsorshipCampaignImpressionCount", expression = "java(0L)")
    @Mapping(target = "sponsorshipCampaignClickCount", expression = "java(0L)")
    @Mapping(target = "sponsorshipCampaignConversionCount", expression = "java(0L)")
    @Mapping(target = "sponsorshipCampaignActivatedAt", ignore = true)
    @Mapping(target = "sponsorshipCampaignCompletedAt", ignore = true)
    @Mapping(target = "sponsorshipCampaignApprovedBy", ignore = true)
    @Mapping(target = "sponsorshipCampaignApprovedAt", ignore = true)
    @Mapping(target = "sponsorshipCampaignRejectionReason", ignore = true)
    @Mapping(target = "sponsorshipCampaignTenantId", ignore = true)
    @Mapping(target = "sponsorshipCampaignCurrency", ignore = true)
    @Mapping(target = "sponsorshipCampaignCommissionPercent", ignore = true)
    @Mapping(target = "sponsorshipCampaignMetadataJson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    SponsorshipCampaign toEntity(SponsorshipCampaignRequestDto dto);

    /**
     * Updates existing entity from request DTO.
     */
    @Mapping(target = "sponsorshipCampaignId", ignore = true)
    @Mapping(source = "sponsorshipCampaignCode", target = "sponsorshipCampaignCode", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignName", target = "sponsorshipCampaignName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignDescription", target = "sponsorshipCampaignDescription", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignType", target = "sponsorshipCampaignType")
    @Mapping(source = "sponsorshipCampaignAdvertiserId", target = "sponsorshipCampaignAdvertiserId")
    @Mapping(source = "sponsorshipCampaignAdvertiserName", target = "sponsorshipCampaignAdvertiserName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignAdvertiserEmail", target = "sponsorshipCampaignAdvertiserEmail", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBrandName", target = "sponsorshipCampaignBrandName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBrandLogoUrl", target = "sponsorshipCampaignBrandLogoUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignStartDate", target = "sponsorshipCampaignStartDate")
    @Mapping(source = "sponsorshipCampaignEndDate", target = "sponsorshipCampaignEndDate")
    @Mapping(source = "sponsorshipCampaignTotalBudget", target = "sponsorshipCampaignTotalBudget")
    @Mapping(source = "sponsorshipCampaignDailyBudget", target = "sponsorshipCampaignDailyBudget")
    @Mapping(source = "sponsorshipCampaignPricingModel", target = "sponsorshipCampaignPricingModel", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignRate", target = "sponsorshipCampaignRate")
    @Mapping(source = "sponsorshipCampaignTargetImpressions", target = "sponsorshipCampaignTargetImpressions")
    @Mapping(source = "sponsorshipCampaignTargetClicks", target = "sponsorshipCampaignTargetClicks")
    @Mapping(source = "sponsorshipCampaignTargetCategoriesJson", target = "sponsorshipCampaignTargetCategoriesJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetLocationsJson", target = "sponsorshipCampaignTargetLocationsJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetDevices", target = "sponsorshipCampaignTargetDevices", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetTiersJson", target = "sponsorshipCampaignTargetTiersJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignContentId", target = "sponsorshipCampaignContentId")
    @Mapping(source = "sponsorshipCampaignDestinationUrl", target = "sponsorshipCampaignDestinationUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignCtaText", target = "sponsorshipCampaignCtaText", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignHeadline", target = "sponsorshipCampaignHeadline", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBodyText", target = "sponsorshipCampaignBodyText", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignImageUrl", target = "sponsorshipCampaignImageUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignVideoUrl", target = "sponsorshipCampaignVideoUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignCreativeAssetsJson", target = "sponsorshipCampaignCreativeAssetsJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmCampaign", target = "sponsorshipCampaignUtmCampaign", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmSource", target = "sponsorshipCampaignUtmSource", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmMedium", target = "sponsorshipCampaignUtmMedium", qualifiedByName = "decodeString")
    @Mapping(target = "sponsorshipCampaignStatus", ignore = true)
    @Mapping(target = "sponsorshipCampaignAmountSpent", ignore = true)
    @Mapping(target = "sponsorshipCampaignImpressionCount", ignore = true)
    @Mapping(target = "sponsorshipCampaignClickCount", ignore = true)
    @Mapping(target = "sponsorshipCampaignConversionCount", ignore = true)
    @Mapping(target = "sponsorshipCampaignActivatedAt", ignore = true)
    @Mapping(target = "sponsorshipCampaignCompletedAt", ignore = true)
    @Mapping(target = "sponsorshipCampaignApprovedBy", ignore = true)
    @Mapping(target = "sponsorshipCampaignApprovedAt", ignore = true)
    @Mapping(target = "sponsorshipCampaignRejectionReason", ignore = true)
    @Mapping(target = "sponsorshipCampaignTenantId", ignore = true)
    @Mapping(target = "sponsorshipCampaignCurrency", ignore = true)
    @Mapping(target = "sponsorshipCampaignCommissionPercent", ignore = true)
    @Mapping(target = "sponsorshipCampaignMetadataJson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromDto(SponsorshipCampaignRequestDto dto, @MappingTarget SponsorshipCampaign entity);

    // ========================================
    // Entity to Response DTO
    // ========================================

    /**
     * Maps entity to response DTO with computed fields.
     */
    @Mapping(source = "sponsorshipCampaignId", target = "sponsorshipCampaignId")
    @Mapping(source = "sponsorshipCampaignCode", target = "sponsorshipCampaignCode", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignName", target = "sponsorshipCampaignName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignDescription", target = "sponsorshipCampaignDescription", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignStatus", target = "sponsorshipCampaignStatus")
    @Mapping(source = "sponsorshipCampaignType", target = "sponsorshipCampaignType")
    @Mapping(source = "sponsorshipCampaignAdvertiserId", target = "sponsorshipCampaignAdvertiserId")
    @Mapping(source = "sponsorshipCampaignAdvertiserName", target = "sponsorshipCampaignAdvertiserName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignAdvertiserEmail", target = "sponsorshipCampaignAdvertiserEmail", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBrandName", target = "sponsorshipCampaignBrandName", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBrandLogoUrl", target = "sponsorshipCampaignBrandLogoUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignStartDate", target = "sponsorshipCampaignStartDate")
    @Mapping(source = "sponsorshipCampaignEndDate", target = "sponsorshipCampaignEndDate")
    @Mapping(source = "sponsorshipCampaignActivatedAt", target = "sponsorshipCampaignActivatedAt")
    @Mapping(source = "sponsorshipCampaignCompletedAt", target = "sponsorshipCampaignCompletedAt")
    @Mapping(source = "sponsorshipCampaignTotalBudget", target = "sponsorshipCampaignTotalBudget")
    @Mapping(source = "sponsorshipCampaignDailyBudget", target = "sponsorshipCampaignDailyBudget")
    @Mapping(source = "sponsorshipCampaignAmountSpent", target = "sponsorshipCampaignAmountSpent")
    @Mapping(source = "sponsorshipCampaignPricingModel", target = "sponsorshipCampaignPricingModel", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignRate", target = "sponsorshipCampaignRate")
    @Mapping(source = "sponsorshipCampaignTargetImpressions", target = "sponsorshipCampaignTargetImpressions")
    @Mapping(source = "sponsorshipCampaignTargetClicks", target = "sponsorshipCampaignTargetClicks")
    @Mapping(source = "sponsorshipCampaignImpressionCount", target = "sponsorshipCampaignImpressionCount")
    @Mapping(source = "sponsorshipCampaignClickCount", target = "sponsorshipCampaignClickCount")
    @Mapping(source = "sponsorshipCampaignConversionCount", target = "sponsorshipCampaignConversionCount")
    @Mapping(source = "sponsorshipCampaignTargetCategoriesJson", target = "sponsorshipCampaignTargetCategoriesJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetLocationsJson", target = "sponsorshipCampaignTargetLocationsJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetDevices", target = "sponsorshipCampaignTargetDevices", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTargetTiersJson", target = "sponsorshipCampaignTargetTiersJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignContentId", target = "sponsorshipCampaignContentId")
    @Mapping(source = "sponsorshipCampaignDestinationUrl", target = "sponsorshipCampaignDestinationUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignCtaText", target = "sponsorshipCampaignCtaText", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignHeadline", target = "sponsorshipCampaignHeadline", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignBodyText", target = "sponsorshipCampaignBodyText", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignImageUrl", target = "sponsorshipCampaignImageUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignVideoUrl", target = "sponsorshipCampaignVideoUrl", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignCreativeAssetsJson", target = "sponsorshipCampaignCreativeAssetsJson", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignApprovedBy", target = "sponsorshipCampaignApprovedBy")
    @Mapping(source = "sponsorshipCampaignApprovedAt", target = "sponsorshipCampaignApprovedAt")
    @Mapping(source = "sponsorshipCampaignRejectionReason", target = "sponsorshipCampaignRejectionReason", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmCampaign", target = "sponsorshipCampaignUtmCampaign", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmSource", target = "sponsorshipCampaignUtmSource", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignUtmMedium", target = "sponsorshipCampaignUtmMedium", qualifiedByName = "decodeString")
    @Mapping(source = "sponsorshipCampaignTenantId", target = "sponsorshipCampaignTenantId")
    @Mapping(source = "createdAt", target = "sponsorshipCampaignCreatedAt")
    @Mapping(source = "createdBy", target = "sponsorshipCampaignCreatedBy")
    @Mapping(source = "updatedAt", target = "sponsorshipCampaignUpdatedAt")
    @Mapping(source = "updatedBy", target = "sponsorshipCampaignUpdatedBy")
    @Mapping(target = "sponsorshipCampaignStatusDisplayName", expression = "java(getStatusDisplayName(entity.getSponsorshipCampaignStatus()))")
    @Mapping(target = "sponsorshipCampaignTypeDisplayName", expression = "java(getSponsorshipCampaignTypeDisplayName(entity.getSponsorshipCampaignType()))")
    @Mapping(target = "sponsorshipCampaignIsActive", expression = "java(entity.isActive())")
    @Mapping(target = "sponsorshipCampaignIsServingContent", expression = "java(entity.getSponsorshipCampaignStatus() != null && entity.getSponsorshipCampaignStatus().isServingContent())")
    @Mapping(target = "sponsorshipCampaignDaysRemaining", expression = "java(calculateDaysRemaining(entity.getSponsorshipCampaignEndDate()))")
    @Mapping(target = "sponsorshipCampaignTotalDays", expression = "java(calculateTotalDays(entity.getSponsorshipCampaignStartDate(), entity.getSponsorshipCampaignEndDate()))")
    @Mapping(target = "sponsorshipCampaignPercentComplete", expression = "java(calculatePercentComplete(entity))")
    @Mapping(target = "sponsorshipCampaignRemainingBudget", expression = "java(calculateRemainingBudget(entity))")
    @Mapping(target = "sponsorshipCampaignBudgetUtilization", expression = "java(calculateBudgetUtilization(entity))")
    @Mapping(target = "sponsorshipCampaignFormattedBudget", expression = "java(formatCurrency(entity.getSponsorshipCampaignTotalBudget()))")
    @Mapping(target = "sponsorshipCampaignFormattedSpent", expression = "java(formatCurrency(entity.getSponsorshipCampaignAmountSpent()))")
    @Mapping(target = "sponsorshipCampaignClickThroughRate", expression = "java(toDouble(entity.getClickThroughRate()))")
    @Mapping(target = "sponsorshipCampaignConversionRate", expression = "java(calculateConversionRate(entity))")
    @Mapping(target = "sponsorshipCampaignImpressionGoalProgress", expression = "java(calculateGoalProgress(entity.getSponsorshipCampaignImpressionCount(), entity.getSponsorshipCampaignTargetImpressions()))")
    @Mapping(target = "sponsorshipCampaignClickGoalProgress", expression = "java(calculateGoalProgress(entity.getSponsorshipCampaignClickCount(), entity.getSponsorshipCampaignTargetClicks()))")
    @Mapping(target = "sponsorshipCampaignIsPendingApproval", expression = "java(entity.getSponsorshipCampaignStatus() == SponsorshipCampaignStatus.PENDING_APPROVAL)")
    @Mapping(target = "sponsorshipCampaignTrackingUrl", expression = "java(buildTrackingUrl(entity))")
    SponsorshipCampaignResponseDto toResponseDto(SponsorshipCampaign entity);

    /**
     * Maps list of entities to response DTOs.
     */
    List<SponsorshipCampaignResponseDto> toResponseDtoList(List<SponsorshipCampaign> entities);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Gets display name for campaign status.
     */
    default String getStatusDisplayName(SponsorshipCampaignStatus status) {
        if (status == null) {
            return "Unknown";
        }
        return status.getDisplayName();
    }

    /**
     * Gets display name for campaign type.
     */
    default String getSponsorshipCampaignTypeDisplayName(SponsorshipCampaignType type) {
        if (type == null) {
            return "Unknown";
        }
        return type.getDisplayName();
    }

    /**
     * Calculates days remaining until campaign end.
     */
    default Integer calculateDaysRemaining(Instant endDate) {
        if (endDate == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(Instant.now(), endDate);
        return days < 0 ? 0 : (int) days;
    }

    /**
     * Calculates total campaign duration in days.
     */
    default Integer calculateTotalDays(Instant startDate, Instant endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculates campaign completion percentage based on time elapsed.
     */
    default Double calculatePercentComplete(SponsorshipCampaign entity) {
        if (entity.getSponsorshipCampaignStartDate() == null || entity.getSponsorshipCampaignEndDate() == null) {
            return 0.0;
        }

        Instant now = Instant.now();
        if (now.isBefore(entity.getSponsorshipCampaignStartDate())) {
            return 0.0;
        }
        if (now.isAfter(entity.getSponsorshipCampaignEndDate())) {
            return 100.0;
        }

        long total = ChronoUnit.SECONDS.between(entity.getSponsorshipCampaignStartDate(),
                entity.getSponsorshipCampaignEndDate());
        long elapsed = ChronoUnit.SECONDS.between(entity.getSponsorshipCampaignStartDate(), now);

        if (total <= 0) {
            return 0.0;
        }
        return Math.min(100.0, (double) elapsed / total * 100);
    }

    /**
     * Calculates remaining budget.
     */
    default BigDecimal calculateRemainingBudget(SponsorshipCampaign entity) {
        if (entity.getSponsorshipCampaignTotalBudget() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal spent = entity.getSponsorshipCampaignAmountSpent() != null
                ? entity.getSponsorshipCampaignAmountSpent()
                : BigDecimal.ZERO;
        BigDecimal remaining = entity.getSponsorshipCampaignTotalBudget().subtract(spent);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    /**
     * Calculates budget utilization percentage.
     */
    default Double calculateBudgetUtilization(SponsorshipCampaign entity) {
        if (entity.getSponsorshipCampaignTotalBudget() == null ||
                entity.getSponsorshipCampaignTotalBudget().compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal spent = entity.getSponsorshipCampaignAmountSpent() != null
                ? entity.getSponsorshipCampaignAmountSpent()
                : BigDecimal.ZERO;
        return spent.divide(entity.getSponsorshipCampaignTotalBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
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

    /**
     * Calculates conversion rate.
     */
    default Double calculateConversionRate(SponsorshipCampaign entity) {
        Long clicks = entity.getSponsorshipCampaignClickCount();
        Long conversions = entity.getSponsorshipCampaignConversionCount();
        if (clicks == null || clicks == 0 || conversions == null) {
            return 0.0;
        }
        return (double) conversions / clicks * 100;
    }

    /**
     * Calculates goal progress percentage.
     */
    default Double calculateGoalProgress(Long current, Long target) {
        if (target == null || target == 0) {
            return null;
        }
        long actualCurrent = current != null ? current : 0;
        return Math.min(100.0, (double) actualCurrent / target * 100);
    }

    /**
     * Builds tracking URL with UTM parameters.
     */
    default String buildTrackingUrl(SponsorshipCampaign entity) {
        if (entity.getSponsorshipCampaignDestinationUrl() == null
                || entity.getSponsorshipCampaignDestinationUrl().isBlank()) {
            return null;
        }

        StringBuilder url = new StringBuilder(entity.getSponsorshipCampaignDestinationUrl());
        boolean hasParams = entity.getSponsorshipCampaignDestinationUrl().contains("?");

        if (entity.getSponsorshipCampaignUtmCampaign() != null || entity.getSponsorshipCampaignUtmSource() != null
                || entity.getSponsorshipCampaignUtmMedium() != null) {
            if (entity.getSponsorshipCampaignUtmSource() != null) {
                url.append(hasParams ? "&" : "?");
                url.append("utm_source=").append(encodeUtmParam(entity.getSponsorshipCampaignUtmSource()));
                hasParams = true;
            }
            if (entity.getSponsorshipCampaignUtmMedium() != null) {
                url.append(hasParams ? "&" : "?");
                url.append("utm_medium=").append(encodeUtmParam(entity.getSponsorshipCampaignUtmMedium()));
                hasParams = true;
            }
            if (entity.getSponsorshipCampaignUtmCampaign() != null) {
                url.append(hasParams ? "&" : "?");
                url.append("utm_campaign=").append(encodeUtmParam(entity.getSponsorshipCampaignUtmCampaign()));
            }
        }

        return url.toString();
    }

    /**
     * URL encodes a parameter value.
     */
    default String encodeUtmParam(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ========================================
    // Response Mapping Methods (for decoding)
    // ========================================

    /**
     * URL decodes a string value.
     */
    @Named("decodeString")
    default String decodeString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Converts BigDecimal to Double.
     */
    default Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}
