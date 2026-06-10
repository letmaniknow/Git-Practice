package com.mmva.newsapp.domain.newsletter.mapper.core;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.mmva.newsapp.domain.newsletter.dto.core.*;
import com.mmva.newsapp.domain.newsletter.dto.audit.*;
import com.mmva.newsapp.domain.newsletter.dto.analytics.*;
import com.mmva.newsapp.domain.newsletter.model.core.*;
import com.mmva.newsapp.domain.newsletter.model.audit.*;

/**
 * MapStruct mapper for Newsletter entities.
 *
 * <p>
 * Provides mapping between newsletter entities and DTOs with support for
 * audit fields, relationships, and data transformations.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface NewsletterMapper {

        // ========================================
        // NewsletterSubscriber Mappings
        // ========================================

        /**
         * Converts NewsletterSubscriberRequestDto to NewsletterSubscriber entity.
         *
         * @param dto the request DTO
         * @return the entity
         */
        @Mappings({
                        @Mapping(target = "newsletterSubscriberId", ignore = true),
                        @Mapping(target = "newsletterSubscriberSubscriptionStatus", constant = "PENDING"),
                        @Mapping(target = "newsletterSubscriberPreferredLanguage", defaultValue = "en"),
                        @Mapping(target = "newsletterSubscriberConfirmationToken", ignore = true),
                        @Mapping(target = "newsletterSubscriberIpAddress", ignore = true),
                        @Mapping(target = "newsletterSubscriberUserAgent", ignore = true),
                        @Mapping(target = "newsletterSubscriberConfirmedAt", ignore = true),
                        @Mapping(target = "newsletterSubscriberUnsubscribedAt", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        NewsletterSubscriber toEntity(NewsletterSubscriberRequestDto dto);

        /**
         * Converts NewsletterSubscriber entity to NewsletterSubscriberResponseDto.
         *
         * @param entity the entity
         * @return the response DTO
         */
        @Mappings({
                        @Mapping(target = "createdAt", source = "createdAt"),
                        @Mapping(target = "updatedAt", source = "updatedAt")
        })
        NewsletterSubscriberResponseDto toResponseDto(NewsletterSubscriber entity);

        // ========================================
        // NewsletterCampaign Mappings
        // ========================================

        /**
         * Converts NewsletterCampaignRequestDto to NewsletterCampaign entity.
         *
         * @param dto the request DTO
         * @return the entity
         */
        @Mappings({
                        @Mapping(target = "newsletterCampaignId", ignore = true),
                        @Mapping(target = "newsletterCampaignStatus", constant = "DRAFT"),
                        @Mapping(target = "newsletterCampaignType", defaultValue = "REGULAR"),
                        @Mapping(target = "newsletterCampaignSentAt", ignore = true),
                        @Mapping(target = "newsletterCampaignTotalRecipients", ignore = true),
                        @Mapping(target = "newsletterCampaignSuccessfulDeliveries", ignore = true),
                        @Mapping(target = "newsletterCampaignFailedDeliveries", ignore = true),
                        @Mapping(target = "newsletterCampaignOpenRate", ignore = true),
                        @Mapping(target = "newsletterCampaignClickRate", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        NewsletterCampaign toEntity(NewsletterCampaignRequestDto dto);

        /**
         * Converts NewsletterCampaign entity to NewsletterCampaignResponseDto.
         *
         * @param entity the entity
         * @return the response DTO
         */
        @Mappings({
                        @Mapping(target = "createdAt", source = "createdAt"),
                        @Mapping(target = "updatedAt", source = "updatedAt")
        })
        NewsletterCampaignResponseDto toResponseDto(NewsletterCampaign entity);

        // ========================================
        // NewsletterCampaignContent Mappings
        // ========================================

        /**
         * Converts NewsletterCampaignContentRequestDto to NewsletterCampaignContent
         * entity.
         *
         * @param dto      the request DTO
         * @param campaign the parent campaign entity
         * @return the entity
         */
        @Mappings({
                        @Mapping(target = "newsletterCampaignContentId", ignore = true),
                        @Mapping(target = "newsletterCampaign", source = "campaign"),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        NewsletterCampaignContent toEntity(NewsletterCampaignContentRequestDto dto, NewsletterCampaign campaign);

        /**
         * Converts NewsletterCampaignContent entity to
         * NewsletterCampaignContentResponseDto.
         *
         * @param entity the entity
         * @return the response DTO
         */
        @Mappings({
                        @Mapping(target = "newsletterCampaignId", expression = "java(entity.getNewsletterCampaign() != null ? entity.getNewsletterCampaign().getNewsletterCampaignId() : null)"),
                        @Mapping(target = "hasTextVersion", expression = "java(entity.getNewsletterCampaignContentText() != null && !entity.getNewsletterCampaignContentText().isEmpty())"),
                        @Mapping(target = "newsletterCampaignContentHtml", expression = "java(truncateHtml(entity.getNewsletterCampaignContentHtml()))"),
                        @Mapping(target = "createdAt", source = "createdAt"),
                        @Mapping(target = "updatedAt", source = "updatedAt")
        })
        NewsletterCampaignContentResponseDto toResponseDto(NewsletterCampaignContent entity);

        // ========================================
        // NewsletterDeliveryLog Mappings
        // ========================================

        /**
         * Converts NewsletterDeliveryLog entity to NewsletterDeliveryAnalyticsDto.
         *
         * @param entity the entity
         * @return the analytics DTO
         */
        @Mappings({
                        @Mapping(target = "newsletterCampaignId", expression = "java(entity.getNewsletterCampaign() != null ? entity.getNewsletterCampaign().getNewsletterCampaignId() : null)"),
                        @Mapping(target = "newsletterSubscriberId", expression = "java(entity.getNewsletterSubscriber() != null ? entity.getNewsletterSubscriber().getNewsletterSubscriberId() : null)"),
                        @Mapping(target = "newsletterSubscriberEmail", expression = "java(entity.getNewsletterSubscriber() != null ? entity.getNewsletterSubscriber().getNewsletterSubscriberEmail() : null)"),
                        @Mapping(target = "createdAt", source = "createdAt")
        })
        NewsletterDeliveryAnalyticsDto toAnalyticsDto(NewsletterDeliveryLog entity);

        // ========================================
        // NewsletterUnsubscribe Mappings
        // ========================================

        /**
         * Converts NewsletterUnsubscribeRequestDto to NewsletterUnsubscribe entity.
         *
         * @param dto        the request DTO
         * @param subscriber the subscriber entity
         * @return the entity
         */
        @Mappings({
                        @Mapping(target = "newsletterUnsubscribeId", ignore = true),
                        @Mapping(target = "newsletterSubscriber", source = "subscriber"),
                        @Mapping(target = "newsletterCampaign", ignore = true),
                        @Mapping(target = "newsletterUnsubscribeIpAddress", ignore = true),
                        @Mapping(target = "newsletterUnsubscribeUserAgent", ignore = true),
                        @Mapping(target = "newsletterUnsubscribeCreatedAt", expression = "java(java.time.Instant.now())")
        })
        NewsletterUnsubscribe toEntity(NewsletterUnsubscribeRequestDto dto, NewsletterSubscriber subscriber);

        /**
         * Converts NewsletterUnsubscribe entity to NewsletterUnsubscribeResponseDto.
         *
         * @param entity the entity
         * @return the response DTO
         */
        @Mappings({
                        @Mapping(target = "newsletterSubscriberId", expression = "java(entity.getNewsletterSubscriber() != null ? entity.getNewsletterSubscriber().getNewsletterSubscriberId() : null)"),
                        @Mapping(target = "newsletterSubscriberEmail", expression = "java(entity.getNewsletterSubscriber() != null ? entity.getNewsletterSubscriber().getNewsletterSubscriberEmail() : null)")
        })
        NewsletterUnsubscribeResponseDto toResponseDto(NewsletterUnsubscribe entity);

        // ========================================
        // Helper Methods
        // ========================================

        /**
         * Truncates HTML content for preview.
         *
         * @param html the full HTML content
         * @return truncated HTML (first 200 characters)
         */
        default String truncateHtml(String html) {
                if (html == null || html.length() <= 200) {
                        return html;
                }
                return html.substring(0, 200) + "...";
        }
}