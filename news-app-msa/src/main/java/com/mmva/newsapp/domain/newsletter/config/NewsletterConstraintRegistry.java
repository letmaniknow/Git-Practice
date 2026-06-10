package com.mmva.newsapp.domain.newsletter.config;

import com.mmva.newsapp.infrastructure.common.exception.ConstraintRegistry;
import com.mmva.newsapp.infrastructure.common.exception.ConstraintViolationMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Newsletter feature constraint registry.
 * 
 * Registers all constraint mappings specific to the NEWSLETTER domain:
 * - newsletter_subscriber_email (unique email for subscriptions)
 * 
 * PRINCIPLE: Feature ownership - Newsletter feature owns its constraint definitions
 * SCALABLE: New newsletter constraints are added here, not in central mapper
 */
@Component
public class NewsletterConstraintRegistry implements ConstraintRegistry {

    @Override
    public String getRegistryId() {
        return "newsletter";
    }

    @Override
    public Map<String, String> getConstraintToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("newsletter_subscriber_email_uk", "newsletter_subscriber_email");
        return mappings;
    }

    @Override
    public Map<String, String> getColumnToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("newsletter_subscriber_email", "newsletter_subscriber_email");
        return mappings;
    }

    @Override
    public Map<String, String> getFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("newsletter_subscriber_email", "subscriber email");
        return labels;
    }

    @Override
    public Map<String, ConstraintViolationMapper.EntityContext> getEntityContextMappings() {
        Map<String, ConstraintViolationMapper.EntityContext> mappings = new HashMap<>();
        // Example: "A subscription with this email already exists. Please use a different value."
        mappings.put("newsletter_subscriber_email", new ConstraintViolationMapper.EntityContext("subscription", "value"));
        return mappings;
    }
}
