package com.mmva.newsapp.infrastructure.push.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock FCM service for development and testing.
 * 
 * <h3>Activation:</h3>
 * <p>
 * Active when {@code app.firebase.enabled=false} (default).
 * Logs all operations without actually sending to Firebase.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.firebase.enabled", havingValue = "false", matchIfMissing = true)
public class PushFcmServiceMock implements PushFcmService {

    @Override
    public FcmSendResult sendToDevice(String token, FcmMessage message) {
        log.info("[MOCK FCM] Send to device - token={}..., title='{}', body='{}'",
                token.substring(0, Math.min(20, token.length())),
                message.getTitle(),
                truncate(message.getBody(), 50));

        return FcmSendResult.builder()
                .success(true)
                .messageId("mock-" + UUID.randomUUID())
                .build();
    }

    @Override
    public FcmBatchResult sendToDevices(List<String> tokens, FcmMessage message) {
        log.info("[MOCK FCM] Send to {} devices - title='{}', body='{}'",
                tokens.size(),
                message.getTitle(),
                truncate(message.getBody(), 50));

        List<FcmSendResult> results = new ArrayList<>();
        for (String token : tokens) {
            log.debug("[MOCK FCM] Simulating send to token: {}", truncate(token, 20));
            results.add(FcmSendResult.builder()
                    .success(true)
                    .messageId("mock-" + UUID.randomUUID())
                    .build());
        }

        return FcmBatchResult.builder()
                .successCount(tokens.size())
                .failureCount(0)
                .results(results)
                .invalidTokens(List.of())
                .build();
    }

    @Override
    public FcmSendResult sendToTopic(String topic, FcmMessage message) {
        log.info("[MOCK FCM] Send to topic - topic='{}', title='{}', body='{}'",
                topic,
                message.getTitle(),
                truncate(message.getBody(), 50));

        return FcmSendResult.builder()
                .success(true)
                .messageId("mock-topic-" + UUID.randomUUID())
                .build();
    }

    @Override
    public FcmSendResult sendToCondition(String condition, FcmMessage message) {
        log.info("[MOCK FCM] Send to condition - condition='{}', title='{}', body='{}'",
                condition,
                message.getTitle(),
                truncate(message.getBody(), 50));

        return FcmSendResult.builder()
                .success(true)
                .messageId("mock-condition-" + UUID.randomUUID())
                .build();
    }

    @Override
    public FcmOperationResult subscribeToTopic(String token, String topic) {
        log.info("[MOCK FCM] Subscribe to topic - topic='{}', token={}...",
                topic,
                token.substring(0, Math.min(20, token.length())));

        return FcmOperationResult.builder()
                .success(true)
                .build();
    }

    @Override
    public FcmBatchOperationResult subscribeToTopic(List<String> tokens, String topic) {
        log.info("[MOCK FCM] Subscribe {} devices to topic '{}'", tokens.size(), topic);

        List<FcmOperationResult> results = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            results.add(FcmOperationResult.builder().success(true).build());
        }

        return FcmBatchOperationResult.builder()
                .successCount(tokens.size())
                .failureCount(0)
                .results(results)
                .build();
    }

    @Override
    public FcmOperationResult unsubscribeFromTopic(String token, String topic) {
        log.info("[MOCK FCM] Unsubscribe from topic - topic='{}', token={}...",
                topic,
                token.substring(0, Math.min(20, token.length())));

        return FcmOperationResult.builder()
                .success(true)
                .build();
    }

    @Override
    public FcmBatchOperationResult unsubscribeFromTopic(List<String> tokens, String topic) {
        log.info("[MOCK FCM] Unsubscribe {} devices from topic '{}'", tokens.size(), topic);

        List<FcmOperationResult> results = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            results.add(FcmOperationResult.builder().success(true).build());
        }

        return FcmBatchOperationResult.builder()
                .successCount(tokens.size())
                .failureCount(0)
                .results(results)
                .build();
    }

    @Override
    public boolean isTokenValid(String token) {
        log.debug("[MOCK FCM] Validate token - token={}...",
                token.substring(0, Math.min(20, token.length())));
        return true;
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
    }

    @Override
    public boolean isHealthy() {
        // Mock is always healthy
        return true;
    }
}
