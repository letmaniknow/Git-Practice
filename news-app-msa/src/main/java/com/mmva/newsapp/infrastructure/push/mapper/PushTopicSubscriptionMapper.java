package com.mmva.newsapp.infrastructure.push.mapper;

import com.mmva.newsapp.infrastructure.push.dto.PushAvailableTopicDto;
import com.mmva.newsapp.infrastructure.push.dto.PushTopicSubscriptionResponseDto;
import com.mmva.newsapp.infrastructure.push.enums.PushTopicCategory;
import com.mmva.newsapp.infrastructure.push.model.PushTopicSubscription;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for push topic subscriptions.
 * 
 * <p>
 * Handles conversions between PushTopicSubscription and DTOs
 * for topic management operations.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PushTopicSubscriptionMapper {

    // ========================================
    // Static Topic Definitions
    // ========================================

    /**
     * Creates an AvailableTopicDto from topic metadata.
     * 
     * @param topic       unique topic identifier
     * @param displayName human-readable name
     * @param description topic description
     * @param category    topic category
     * @param isDefault   whether device auto-subscribes
     * @return the topic DTO
     */
    default PushAvailableTopicDto createTopicDto(
            String topic,
            String displayName,
            String description,
            PushTopicCategory category,
            boolean isDefault) {

        return PushAvailableTopicDto.builder()
                .topic(topic)
                .displayName(displayName)
                .description(description)
                .category(category.name())
                .isDefault(isDefault)
                .build();
    }

    /**
     * Returns list of all available topics for subscription.
     * This is the source of truth for topic definitions.
     * 
     * @return list of available topic DTOs
     */
    default List<PushAvailableTopicDto> getAvailableTopics() {
        return List.of(
                // Global Topics (auto-subscribe)
                createTopicDto("all_news", "All News",
                        "Receive all news updates", PushTopicCategory.GLOBAL, true),
                createTopicDto("breaking_news", "Breaking News",
                        "Urgent breaking news alerts", PushTopicCategory.GLOBAL, true),

                // Category Topics
                createTopicDto("category_politics", "Politics",
                        "Political news and updates", PushTopicCategory.CATEGORY, false),
                createTopicDto("category_sports", "Sports",
                        "Sports news and scores", PushTopicCategory.CATEGORY, false),
                createTopicDto("category_technology", "Technology",
                        "Tech industry news", PushTopicCategory.CATEGORY, false),
                createTopicDto("category_entertainment", "Entertainment",
                        "Entertainment and celebrity news", PushTopicCategory.CATEGORY, false),
                createTopicDto("category_business", "Business",
                        "Business and finance news", PushTopicCategory.CATEGORY, false),
                createTopicDto("category_health", "Health",
                        "Health and medical news", PushTopicCategory.CATEGORY, false),
                createTopicDto("category_science", "Science",
                        "Science discoveries and research", PushTopicCategory.CATEGORY, false),
                createTopicDto("category_world", "World",
                        "International news", PushTopicCategory.CATEGORY, false),

                // Digest Topics
                createTopicDto("digest_daily", "Daily Digest",
                        "Daily news summary", PushTopicCategory.DIGEST, false),
                createTopicDto("digest_weekly", "Weekly Digest",
                        "Weekly news roundup", PushTopicCategory.DIGEST, false),

                // Language Topics
                createTopicDto("lang_en", "English",
                        "News in English", PushTopicCategory.LANGUAGE, false),
                createTopicDto("lang_es", "Spanish",
                        "News in Spanish", PushTopicCategory.LANGUAGE, false),
                createTopicDto("lang_fr", "French",
                        "News in French", PushTopicCategory.LANGUAGE, false));
    }

    /**
     * Extracts topic names from subscription entities.
     * 
     * @param subscriptions list of subscription entities
     * @return list of topic names
     */
    default List<String> toTopicNameList(List<PushTopicSubscription> subscriptions) {
        if (subscriptions == null) {
            return List.of();
        }
        return subscriptions.stream()
                .filter(sub -> sub.getIsActive() != null && sub.getIsActive())
                .map(PushTopicSubscription::getTopic)
                .collect(Collectors.toList());
    }

    /**
     * Gets human-readable display name for a topic.
     * Derives name from topic definition or generates from topic identifier.
     * 
     * @param topic the topic identifier
     * @return human-readable display name
     */
    default String getTopicDisplayName(String topic) {
        // First, try to find from available topics
        return getAvailableTopics().stream()
                .filter(t -> t.getTopic().equals(topic))
                .findFirst()
                .map(PushAvailableTopicDto::getDisplayName)
                .orElseGet(() -> deriveDisplayName(topic));
    }

    /**
     * Derives display name from topic identifier when not in predefined list.
     * 
     * @param topic the topic identifier
     * @return derived display name
     */
    private String deriveDisplayName(String topic) {
        if (topic.startsWith("category_")) {
            return "Category: " + capitalizeFirst(topic.substring(9));
        } else if (topic.startsWith("lang_") || topic.startsWith("language_")) {
            String lang = topic.startsWith("lang_") ? topic.substring(5) : topic.substring(9);
            return "Language: " + lang.toUpperCase();
        } else if (topic.startsWith("platform_")) {
            return "Platform: " + capitalizeFirst(topic.substring(9));
        } else if (topic.startsWith("digest_")) {
            return capitalizeFirst(topic.substring(7)) + " Digest";
        }
        return topic;
    }

    /**
     * Capitalizes the first letter of a string.
     * 
     * @param str input string
     * @return string with first letter capitalized
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Gets the category for a topic.
     * Derives category from topic definition or infers from topic identifier
     * prefix.
     * 
     * @param topic the topic identifier
     * @return the topic category
     */
    default PushTopicCategory getTopicCategory(String topic) {
        // First, try to find from available topics
        return getAvailableTopics().stream()
                .filter(t -> t.getTopic().equals(topic))
                .findFirst()
                .map(t -> PushTopicCategory.valueOf(t.getCategory()))
                .orElseGet(() -> inferCategory(topic));
    }

    /**
     * Infers category from topic identifier prefix when not in predefined list.
     * 
     * @param topic the topic identifier
     * @return inferred category
     */
    private PushTopicCategory inferCategory(String topic) {
        if (topic.equals("all_news") || topic.equals("breaking_news")) {
            return PushTopicCategory.GLOBAL;
        } else if (topic.startsWith("category_")) {
            return PushTopicCategory.CATEGORY;
        } else if (topic.startsWith("lang_") || topic.startsWith("language_")) {
            return PushTopicCategory.LANGUAGE;
        } else if (topic.startsWith("platform_")) {
            return PushTopicCategory.PLATFORM;
        } else if (topic.startsWith("digest_")) {
            return PushTopicCategory.DIGEST;
        }
        return PushTopicCategory.CUSTOM;
    }

    /**
     * Creates a TopicSubscriptionResponseDto for a successful operation.
     * 
     * @param subscribed           list of newly subscribed topics
     * @param unsubscribed         list of unsubscribed topics
     * @param currentSubscriptions all current subscriptions
     * @return the response DTO
     */
    default PushTopicSubscriptionResponseDto createResponse(
            List<String> subscribed,
            List<String> unsubscribed,
            List<String> currentSubscriptions) {

        return PushTopicSubscriptionResponseDto.builder()
                .subscribed(subscribed)
                .unsubscribed(unsubscribed)
                .currentSubscriptions(currentSubscriptions)
                .errors(List.of())
                .build();
    }
}
