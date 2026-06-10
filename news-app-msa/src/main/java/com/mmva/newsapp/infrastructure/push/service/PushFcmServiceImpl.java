package com.mmva.newsapp.infrastructure.push.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Firebase Cloud Messaging service implementation.
 * 
 * <h3>Configuration:</h3>
 * <p>
 * Requires Firebase service account credentials JSON file.
 * Path configured via {@code app.firebase.credentials-path}.
 * </p>
 * 
 * <h3>Activation:</h3>
 * <p>
 * Only activated when {@code app.firebase.enabled=true}.
 * Use {@link PushFcmServiceMock} for development/testing.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.firebase.enabled", havingValue = "true")
public class PushFcmServiceImpl implements PushFcmService {

    private static final int MAX_BATCH_SIZE = 500;

    @Value("${app.firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @Value("${app.firebase.project-id:}")
    private String projectId;

    private FirebaseMessaging firebaseMessaging;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = new ClassPathResource(credentialsPath).getInputStream();

                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount));

                if (projectId != null && !projectId.isBlank()) {
                    optionsBuilder.setProjectId(projectId);
                }

                FirebaseApp.initializeApp(optionsBuilder.build());
                log.info("FcmService: Firebase initialized successfully");
            }

            firebaseMessaging = FirebaseMessaging.getInstance();
        } catch (IOException e) {
            log.error("FcmService: Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    // ========================================
    // Single Message
    // ========================================

    @Override
    public FcmSendResult sendToDevice(String token, FcmMessage message) {
        try {
            Message fcmMessage = buildMessage(message)
                    .setToken(token)
                    .build();

            String messageId = firebaseMessaging.send(fcmMessage);

            log.debug("FcmService: Sent to device - messageId={}", messageId);
            return FcmSendResult.builder()
                    .success(true)
                    .messageId(messageId)
                    .build();

        } catch (FirebaseMessagingException e) {
            log.warn("FcmService: Failed to send to device - error={}, code={}",
                    e.getMessage(), e.getMessagingErrorCode());
            return handleMessagingException(e);
        }
    }

    // ========================================
    // Batch Messages
    // ========================================

    @Override
    public FcmBatchResult sendToDevices(List<String> tokens, FcmMessage message) {
        if (tokens == null || tokens.isEmpty()) {
            return FcmBatchResult.builder()
                    .successCount(0)
                    .failureCount(0)
                    .results(List.of())
                    .invalidTokens(List.of())
                    .build();
        }

        // Split into batches of MAX_BATCH_SIZE
        List<FcmSendResult> allResults = new ArrayList<>();
        List<String> invalidTokens = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i += MAX_BATCH_SIZE) {
            List<String> batch = tokens.subList(i, Math.min(i + MAX_BATCH_SIZE, tokens.size()));
            FcmBatchResult batchResult = sendBatch(batch, message);
            allResults.addAll(batchResult.getResults());
            invalidTokens.addAll(batchResult.getInvalidTokens());
        }

        int successCount = (int) allResults.stream().filter(FcmSendResult::isSuccess).count();
        int failureCount = allResults.size() - successCount;

        log.info("FcmService: Batch send complete - total={}, success={}, failed={}, invalid={}",
                tokens.size(), successCount, failureCount, invalidTokens.size());

        return FcmBatchResult.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .results(allResults)
                .invalidTokens(invalidTokens)
                .build();
    }

    private FcmBatchResult sendBatch(List<String> tokens, FcmMessage message) {
        try {
            List<Message> messages = tokens.stream()
                    .map(token -> buildMessage(message).setToken(token).build())
                    .collect(Collectors.toList());

            BatchResponse response = firebaseMessaging.sendEach(messages);

            List<FcmSendResult> results = new ArrayList<>();
            List<String> invalidTokens = new ArrayList<>();

            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                if (sendResponse.isSuccessful()) {
                    results.add(FcmSendResult.builder()
                            .success(true)
                            .messageId(sendResponse.getMessageId())
                            .build());
                } else {
                    FcmSendResult result = handleMessagingException(sendResponse.getException());
                    results.add(result);
                    if (result.isTokenInvalid()) {
                        invalidTokens.add(tokens.get(i));
                    }
                }
            }

            return FcmBatchResult.builder()
                    .successCount(response.getSuccessCount())
                    .failureCount(response.getFailureCount())
                    .results(results)
                    .invalidTokens(invalidTokens)
                    .build();

        } catch (FirebaseMessagingException e) {
            log.error("FcmService: Batch send failed - error={}", e.getMessage(), e);
            // Return all as failed
            List<FcmSendResult> failedResults = tokens.stream()
                    .map(t -> handleMessagingException(e))
                    .collect(Collectors.toList());
            return FcmBatchResult.builder()
                    .successCount(0)
                    .failureCount(tokens.size())
                    .results(failedResults)
                    .invalidTokens(List.of())
                    .build();
        }
    }

    // ========================================
    // Topic Messages
    // ========================================

    @Override
    public FcmSendResult sendToTopic(String topic, FcmMessage message) {
        try {
            Message fcmMessage = buildMessage(message)
                    .setTopic(topic)
                    .build();

            String messageId = firebaseMessaging.send(fcmMessage);

            log.info("FcmService: Sent to topic - topic={}, messageId={}", topic, messageId);
            return FcmSendResult.builder()
                    .success(true)
                    .messageId(messageId)
                    .build();

        } catch (FirebaseMessagingException e) {
            log.error("FcmService: Failed to send to topic - topic={}, error={}", topic, e.getMessage());
            return handleMessagingException(e);
        }
    }

    @Override
    public FcmSendResult sendToCondition(String condition, FcmMessage message) {
        try {
            Message fcmMessage = buildMessage(message)
                    .setCondition(condition)
                    .build();

            String messageId = firebaseMessaging.send(fcmMessage);

            log.info("FcmService: Sent to condition - condition={}, messageId={}", condition, messageId);
            return FcmSendResult.builder()
                    .success(true)
                    .messageId(messageId)
                    .build();

        } catch (FirebaseMessagingException e) {
            log.error("FcmService: Failed to send to condition - condition={}, error={}", condition, e.getMessage());
            return handleMessagingException(e);
        }
    }

    // ========================================
    // Topic Subscription Management
    // ========================================

    @Override
    public FcmOperationResult subscribeToTopic(String token, String topic) {
        return subscribeToTopic(List.of(token), topic).getResults().get(0);
    }

    @Override
    public FcmBatchOperationResult subscribeToTopic(List<String> tokens, String topic) {
        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(tokens, topic);

            List<FcmOperationResult> results = buildOperationResults(response, tokens.size());

            log.debug("FcmService: Subscribed to topic - topic={}, success={}, failed={}",
                    topic, response.getSuccessCount(), response.getFailureCount());

            return FcmBatchOperationResult.builder()
                    .successCount(response.getSuccessCount())
                    .failureCount(response.getFailureCount())
                    .results(results)
                    .build();

        } catch (FirebaseMessagingException e) {
            log.error("FcmService: Failed to subscribe to topic - topic={}, error={}", topic, e.getMessage());
            return buildFailedOperationResult(tokens.size(), e.getMessage());
        }
    }

    @Override
    public FcmOperationResult unsubscribeFromTopic(String token, String topic) {
        return unsubscribeFromTopic(List.of(token), topic).getResults().get(0);
    }

    @Override
    public FcmBatchOperationResult unsubscribeFromTopic(List<String> tokens, String topic) {
        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(tokens, topic);

            List<FcmOperationResult> results = buildOperationResults(response, tokens.size());

            log.debug("FcmService: Unsubscribed from topic - topic={}, success={}, failed={}",
                    topic, response.getSuccessCount(), response.getFailureCount());

            return FcmBatchOperationResult.builder()
                    .successCount(response.getSuccessCount())
                    .failureCount(response.getFailureCount())
                    .results(results)
                    .build();

        } catch (FirebaseMessagingException e) {
            log.error("FcmService: Failed to unsubscribe from topic - topic={}, error={}", topic, e.getMessage());
            return buildFailedOperationResult(tokens.size(), e.getMessage());
        }
    }

    // ========================================
    // Token Validation
    // ========================================

    @Override
    public boolean isTokenValid(String token) {
        try {
            // Send a dry-run message to validate token
            Message message = Message.builder()
                    .setToken(token)
                    .putData("validate", "true")
                    .build();

            firebaseMessaging.send(message, true); // dry-run mode
            return true;

        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            return errorCode != MessagingErrorCode.UNREGISTERED
                    && errorCode != MessagingErrorCode.INVALID_ARGUMENT;
        }
    }

    // ========================================
    // Helper Methods
    // ========================================

    private Message.Builder buildMessage(FcmMessage message) {
        Message.Builder builder = Message.builder();

        // Notification payload (visible to user)
        if (message.getTitle() != null || message.getBody() != null) {
            Notification.Builder notificationBuilder = Notification.builder();
            if (message.getTitle() != null) {
                notificationBuilder.setTitle(message.getTitle());
            }
            if (message.getBody() != null) {
                notificationBuilder.setBody(message.getBody());
            }
            if (message.getImageUrl() != null) {
                notificationBuilder.setImage(message.getImageUrl());
            }
            builder.setNotification(notificationBuilder.build());
        }

        // Data payload (passed to app)
        if (message.getData() != null && !message.getData().isEmpty()) {
            builder.putAllData(message.getData());
        }

        // Add click action to data
        if (message.getClickAction() != null) {
            builder.putData("click_action", message.getClickAction());
        }

        // Android configuration
        if (message.getAndroid() != null) {
            AndroidConfig.Builder androidBuilder = AndroidConfig.builder()
                    .setTtl(Duration.ofSeconds(message.getTtlSeconds()).toMillis());

            if ("high".equalsIgnoreCase(message.getPriority())) {
                androidBuilder.setPriority(AndroidConfig.Priority.HIGH);
            }

            AndroidNotification.Builder androidNotification = AndroidNotification.builder();
            FcmMessage.AndroidConfig androidConfig = message.getAndroid();

            if (androidConfig.getChannelId() != null) {
                androidNotification.setChannelId(androidConfig.getChannelId());
            }
            if (androidConfig.getIcon() != null) {
                androidNotification.setIcon(androidConfig.getIcon());
            }
            if (androidConfig.getColor() != null) {
                androidNotification.setColor(androidConfig.getColor());
            }
            if (androidConfig.getSound() != null) {
                androidNotification.setSound(androidConfig.getSound());
            }
            if (androidConfig.getTag() != null) {
                androidNotification.setTag(androidConfig.getTag());
            }

            androidBuilder.setNotification(androidNotification.build());
            builder.setAndroidConfig(androidBuilder.build());
        }

        // iOS (APNS) configuration
        if (message.getApns() != null) {
            FcmMessage.ApnsConfig apnsConfig = message.getApns();
            Aps.Builder apsBuilder = Aps.builder();

            if (apnsConfig.getSound() != null) {
                apsBuilder.setSound(apnsConfig.getSound());
            }
            if (apnsConfig.getBadge() != null) {
                apsBuilder.setBadge(apnsConfig.getBadge());
            }
            if (apnsConfig.getCategory() != null) {
                apsBuilder.setCategory(apnsConfig.getCategory());
            }
            if (apnsConfig.getThreadId() != null) {
                apsBuilder.setThreadId(apnsConfig.getThreadId());
            }

            builder.setApnsConfig(ApnsConfig.builder()
                    .setAps(apsBuilder.build())
                    .build());
        }

        // Web Push configuration
        if (message.getWebPush() != null) {
            FcmMessage.WebPushConfig webConfig = message.getWebPush();
            WebpushNotification.Builder webNotification = WebpushNotification.builder();

            if (webConfig.getIcon() != null) {
                webNotification.setIcon(webConfig.getIcon());
            }
            if (webConfig.getBadge() != null) {
                webNotification.setBadge(webConfig.getBadge());
            }
            if (webConfig.getTag() != null) {
                webNotification.setTag(webConfig.getTag());
            }
            if (webConfig.getRequireInteraction() != null) {
                webNotification.setRequireInteraction(webConfig.getRequireInteraction());
            }

            builder.setWebpushConfig(WebpushConfig.builder()
                    .setNotification(webNotification.build())
                    .build());
        }

        return builder;
    }

    private FcmSendResult handleMessagingException(FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();
        boolean isTokenInvalid = errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT;

        return FcmSendResult.builder()
                .success(false)
                .errorCode(errorCode != null ? errorCode.name() : "UNKNOWN")
                .errorMessage(e.getMessage())
                .isTokenInvalid(isTokenInvalid)
                .build();
    }

    private List<FcmOperationResult> buildOperationResults(TopicManagementResponse response, int count) {
        List<FcmOperationResult> results = new ArrayList<>();
        List<TopicManagementResponse.Error> errors = response.getErrors();

        int successCount = response.getSuccessCount();
        for (int i = 0; i < successCount; i++) {
            results.add(FcmOperationResult.builder().success(true).build());
        }

        for (TopicManagementResponse.Error error : errors) {
            results.add(FcmOperationResult.builder()
                    .success(false)
                    .errorCode(error.getReason())
                    .errorMessage(error.getReason())
                    .build());
        }

        return results;
    }

    private FcmBatchOperationResult buildFailedOperationResult(int count, String error) {
        List<FcmOperationResult> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(FcmOperationResult.builder()
                    .success(false)
                    .errorMessage(error)
                    .build());
        }
        return FcmBatchOperationResult.builder()
                .successCount(0)
                .failureCount(count)
                .results(results)
                .build();
    }

    @Override
    public boolean isHealthy() {
        try {
            // Check if Firebase app is initialized
            return FirebaseApp.getApps() != null && !FirebaseApp.getApps().isEmpty();
        } catch (Exception e) {
            log.error("FCM health check failed", e);
            return false;
        }
    }
}
