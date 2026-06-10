package com.mmva.newsapp.domain.news.dto.realtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for WebSocket-based real-time news subscriptions.
 *
 * <p>
 * Used by clients to subscribe to specific types of real-time updates
 * and configure their notification preferences.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsRealTimeWebSocketRequestDto {

    /**
     * Type of subscription request.
     * Values: subscribe, unsubscribe, update_preferences
     */
    @NotBlank(message = "Subscription action is required")
    @Size(max = 50, message = "Action must not exceed 50 characters")
    private String action;

    /**
     * User ID for user-specific subscriptions.
     * Required for personal notifications and recommendations.
     */
    private UUID userId;

    /**
     * Types of notifications to subscribe to.
     * Examples: breaking_news, news_updates, comments, analytics
     */
    private String[] notificationTypes;

    /**
     * Specific news categories to follow.
     * If null, subscribe to all categories.
     */
    private UUID[] categoryIds;

    /**
     * Preferred language for notifications.
     * Values: en, es (English, Spanish)
     */
    @Size(max = 2, message = "Language code must not exceed 2 characters")
    private String language;

    /**
     * Enable/disable sound notifications.
     */
    @Builder.Default
    private Boolean soundEnabled = true;

    /**
     * Enable/disable vibration for mobile clients.
     */
    @Builder.Default
    private Boolean vibrationEnabled = true;

    /**
     * Client platform type.
     * Values: web, mobile, desktop
     */
    @Size(max = 20, message = "Platform must not exceed 20 characters")
    private String platform;

    /**
     * Client application version for compatibility checks.
     */
    @Size(max = 20, message = "Version must not exceed 20 characters")
    private String clientVersion;

    /**
     * Request timestamp for tracking and debugging.
     */
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
}