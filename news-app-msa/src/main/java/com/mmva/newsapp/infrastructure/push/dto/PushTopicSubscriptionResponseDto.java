package com.mmva.newsapp.infrastructure.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for topic subscription operations.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushTopicSubscriptionResponseDto {

    /**
     * Successfully subscribed topics.
     */
    private List<String> subscribed;

    /**
     * Successfully unsubscribed topics.
     */
    private List<String> unsubscribed;

    /**
     * Failed operations with reasons.
     */
    private List<TopicOperationErrorDto> errors;

    /**
     * Current list of all subscribed topics.
     */
    private List<String> currentSubscriptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicOperationErrorDto {
        private String topic;
        private String operation; // "subscribe" or "unsubscribe"
        private String reason;
    }
}
