package com.mmva.newsapp.infrastructure.push.config;

import com.mmva.newsapp.infrastructure.common.exception.ConstraintRegistry;
import com.mmva.newsapp.infrastructure.common.exception.ConstraintViolationMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Push Notification feature constraint registry.
 * 
 * Registers all constraint mappings specific to push notifications:
 * - fcm_token (unique Firebase Cloud Messaging tokens)
 * - idempotency_key (unique idempotency keys for notification deduplication)
 * 
 * PRINCIPLE: Feature ownership - Push feature owns its constraint definitions
 * LOCATION: Infrastructure tier because push notifications are a cross-cutting concern
 */
@Component
public class PushConstraintRegistry implements ConstraintRegistry {

    @Override
    public String getRegistryId() {
        return "push";
    }

    @Override
    public Map<String, String> getConstraintToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("push_device_fcm_token_uk", "fcm_token");
        mappings.put("push_notification_idempotency_key_uk", "idempotency_key");
        return mappings;
    }

    @Override
    public Map<String, String> getColumnToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("fcm_token", "fcm_token");
        mappings.put("idempotency_key", "idempotency_key");
        return mappings;
    }

    @Override
    public Map<String, String> getFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("fcm_token", "FCM token");
        labels.put("idempotency_key", "idempotency key");
        return labels;
    }

    @Override
    public Map<String, ConstraintViolationMapper.EntityContext> getEntityContextMappings() {
        Map<String, ConstraintViolationMapper.EntityContext> mappings = new HashMap<>();
        mappings.put("fcm_token", new ConstraintViolationMapper.EntityContext("device", "value"));
        mappings.put("idempotency_key", new ConstraintViolationMapper.EntityContext("notification", "value"));
        return mappings;
    }
}
