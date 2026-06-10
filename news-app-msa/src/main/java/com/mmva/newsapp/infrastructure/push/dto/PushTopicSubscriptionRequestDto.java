package com.mmva.newsapp.infrastructure.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for subscribing/unsubscribing to topics.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushTopicSubscriptionRequestDto {

    /**
     * Topics to subscribe to.
     */
    private List<String> subscribe;

    /**
     * Topics to unsubscribe from.
     */
    private List<String> unsubscribe;
}
