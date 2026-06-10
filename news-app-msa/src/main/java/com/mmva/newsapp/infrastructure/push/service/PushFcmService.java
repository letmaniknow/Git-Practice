package com.mmva.newsapp.infrastructure.push.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging service interface.
 * 
 * <h3>Purpose:</h3>
 * <p>
 * Low-level interface for Firebase Cloud Messaging operations.
 * Abstracts FCM SDK details from business logic.
 * </p>
 * 
 * <h3>Implementation Options:</h3>
 * <ul>
 * <li>{@code FcmServiceImpl} - Production implementation using Firebase Admin
 * SDK</li>
 * <li>{@code FcmServiceMock} - Mock implementation for testing/development</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface PushFcmService {

    // ========================================
    // Single Message
    // ========================================

    /**
     * Send notification to a single device.
     * 
     * @param token   FCM registration token
     * @param message notification message
     * @return send result
     */
    FcmSendResult sendToDevice(String token, FcmMessage message);

    // ========================================
    // Batch Messages
    // ========================================

    /**
     * Send notification to multiple devices.
     * FCM supports up to 500 tokens per batch.
     * 
     * @param tokens  list of FCM registration tokens
     * @param message notification message
     * @return batch send result
     */
    FcmBatchResult sendToDevices(List<String> tokens, FcmMessage message);

    // ========================================
    // Topic Messages
    // ========================================

    /**
     * Send notification to a topic.
     * All devices subscribed to the topic will receive.
     * 
     * @param topic   topic name
     * @param message notification message
     * @return send result
     */
    FcmSendResult sendToTopic(String topic, FcmMessage message);

    /**
     * Send notification to topic with condition.
     * Example: "'sports' in topics && 'newsapp' in topics"
     * 
     * @param condition topic condition expression
     * @param message   notification message
     * @return send result
     */
    FcmSendResult sendToCondition(String condition, FcmMessage message);

    // ========================================
    // Topic Subscription Management
    // ========================================

    /**
     * Subscribe device to a topic.
     * 
     * @param token FCM registration token
     * @param topic topic name
     * @return operation result
     */
    FcmOperationResult subscribeToTopic(String token, String topic);

    /**
     * Subscribe multiple devices to a topic.
     * 
     * @param tokens list of FCM tokens
     * @param topic  topic name
     * @return batch operation result
     */
    FcmBatchOperationResult subscribeToTopic(List<String> tokens, String topic);

    /**
     * Unsubscribe device from a topic.
     * 
     * @param token FCM registration token
     * @param topic topic name
     * @return operation result
     */
    FcmOperationResult unsubscribeFromTopic(String token, String topic);

    /**
     * Unsubscribe multiple devices from a topic.
     * 
     * @param tokens list of FCM tokens
     * @param topic  topic name
     * @return batch operation result
     */
    FcmBatchOperationResult unsubscribeFromTopic(List<String> tokens, String topic);

    // ========================================
    // Token Validation
    // ========================================

    /**
     * Validate if an FCM token is still valid.
     * 
     * @param token FCM registration token
     * @return true if token is valid
     */
    boolean isTokenValid(String token);

    // ========================================
    // DTOs
    // ========================================

    /**
     * FCM message payload.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FcmMessage {
        /**
         * Notification title.
         */
        private String title;

        /**
         * Notification body.
         */
        private String body;

        /**
         * Image URL for rich notifications.
         */
        private String imageUrl;

        /**
         * Click action URL (deep link).
         */
        private String clickAction;

        /**
         * Custom data payload.
         */
        private Map<String, String> data;

        /**
         * Android-specific configuration.
         */
        private AndroidConfig android;

        /**
         * iOS-specific configuration.
         */
        private ApnsConfig apns;

        /**
         * Web push-specific configuration.
         */
        private WebPushConfig webPush;

        /**
         * Time-to-live in seconds.
         */
        @Builder.Default
        private Integer ttlSeconds = 86400;

        /**
         * Priority: "high" or "normal".
         */
        @Builder.Default
        private String priority = "normal";

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AndroidConfig {
            private String channelId;
            private String icon;
            private String color;
            private String sound;
            private String tag;
            private Boolean sticky;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ApnsConfig {
            private String sound;
            private Integer badge;
            private String category;
            private String threadId;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WebPushConfig {
            private String icon;
            private String badge;
            private String tag;
            private Boolean requireInteraction;
        }
    }

    /**
     * Result of single send operation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FcmSendResult {
        private boolean success;
        private String messageId;
        private String errorCode;
        private String errorMessage;
        private boolean isTokenInvalid;
    }

    /**
     * Result of batch send operation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FcmBatchResult {
        private int successCount;
        private int failureCount;
        private List<FcmSendResult> results;
        private List<String> invalidTokens;
    }

    /**
     * Result of single topic operation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FcmOperationResult {
        private boolean success;
        private String errorCode;
        private String errorMessage;
    }

    /**
     * Result of batch topic operation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class FcmBatchOperationResult {
        private int successCount;
        private int failureCount;
        private List<FcmOperationResult> results;
    }

    /**
     * Check if the FCM service is healthy.
     *
     * @return true if healthy
     */
    boolean isHealthy();
}
