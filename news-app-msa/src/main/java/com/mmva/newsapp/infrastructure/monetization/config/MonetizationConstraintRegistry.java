package com.mmva.newsapp.infrastructure.monetization.config;

import com.mmva.newsapp.infrastructure.common.exception.ConstraintRegistry;
import com.mmva.newsapp.infrastructure.common.exception.ConstraintViolationMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Monetization feature constraint registry.
 * 
 * Registers all constraint mappings specific to monetization:
 * - invoice_number (unique invoice numbers)
 * - agency_code (unique agency codes for news sources)
 * 
 * PRINCIPLE: Feature ownership - Monetization feature owns its constraint definitions
 * LOCATION: Infrastructure tier because monetization is a cross-cutting concern
 */
@Component
public class MonetizationConstraintRegistry implements ConstraintRegistry {

    @Override
    public String getRegistryId() {
        return "monetization";
    }

    @Override
    public Map<String, String> getConstraintToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("invoice_number_uk", "invoice_number");
        mappings.put("agency_code_uk", "agency_code");
        return mappings;
    }

    @Override
    public Map<String, String> getColumnToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("invoice_number", "invoice_number");
        mappings.put("agency_code", "agency_code");
        return mappings;
    }

    @Override
    public Map<String, String> getFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("invoice_number", "invoice number");
        labels.put("agency_code", "agency code");
        return labels;
    }

    @Override
    public Map<String, ConstraintViolationMapper.EntityContext> getEntityContextMappings() {
        Map<String, ConstraintViolationMapper.EntityContext> mappings = new HashMap<>();
        mappings.put("invoice_number", new ConstraintViolationMapper.EntityContext("invoice", "value"));
        mappings.put("agency_code", new ConstraintViolationMapper.EntityContext("agency", "value"));
        return mappings;
    }
}
