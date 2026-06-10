package com.mmva.newsapp.infrastructure.common.exception;

import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionResponseDto;

import java.util.List;

/**
 * Exception thrown when a user attempts to subscribe but already has an active subscription.
 * Includes details about the existing subscription and available actions for graceful error handling.
 *
 * Industry best practice: Exception names should describe the business condition clearly,
 * not imply specific solutions. This exception represents the state where a user already
 * has an active subscription, providing options for what they can do next.
 */
public class ActiveSubscriptionExistsException extends RuntimeException {

    private final UserSubscriptionResponseDto existingSubscription;
    private final List<String> availableActions;

    public ActiveSubscriptionExistsException(String message, UserSubscriptionResponseDto existingSubscription) {
        super(message);
        this.existingSubscription = existingSubscription;
        this.availableActions = List.of();
    }

    public ActiveSubscriptionExistsException(String message, UserSubscriptionResponseDto existingSubscription, List<String> availableActions) {
        super(message);
        this.existingSubscription = existingSubscription;
        this.availableActions = availableActions != null ? availableActions : List.of();
    }

    public UserSubscriptionResponseDto getExistingSubscription() {
        return existingSubscription;
    }

    public List<String> getAvailableActions() {
        return availableActions;
    }
}