package com.mmva.newsapp.infrastructure.common.api.dto;

import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response data for subscription conflicts, providing existing subscription
 * details
 * and available upgrade options for graceful user experience.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionConflictData {

    /**
     * The user's current active subscription
     */
    private UserSubscriptionResponseDto existingSubscription;

    /**
     * Available actions the user can take
     */
    private List<String> availableActions;

    /**
     * Suggested next steps for the user
     */
    private String suggestion;
}