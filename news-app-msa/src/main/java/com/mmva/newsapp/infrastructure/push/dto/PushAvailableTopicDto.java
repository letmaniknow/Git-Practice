package com.mmva.newsapp.infrastructure.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Available topic information for display in UI.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushAvailableTopicDto {

    /**
     * Topic identifier (used for subscribe/unsubscribe).
     */
    private String topic;

    /**
     * Human-readable display name.
     */
    private String displayName;

    /**
     * Topic description.
     */
    private String description;

    /**
     * Topic newscategory for grouping in UI.
     */
    private String category;

    /**
     * Icon name or URL.
     */
    private String icon;

    /**
     * Whether topic is a default subscription.
     */
    private Boolean isDefault;
}
