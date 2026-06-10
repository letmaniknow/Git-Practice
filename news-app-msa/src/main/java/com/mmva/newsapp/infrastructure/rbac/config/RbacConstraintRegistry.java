package com.mmva.newsapp.infrastructure.rbac.config;

import com.mmva.newsapp.infrastructure.common.exception.ConstraintRegistry;
import com.mmva.newsapp.infrastructure.common.exception.ConstraintViolationMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RBAC (Role-Based Access Control) feature constraint registry.
 * 
 * Registers all constraint mappings specific to RBAC:
 * - role_name (unique role names)
 * - permission_name (unique permission names)
 * 
 * PRINCIPLE: Feature ownership - RBAC feature owns its constraint definitions
 * LOCATION: Infrastructure tier because RBAC is cross-cutting infrastructure concern
 */
@Component
public class RbacConstraintRegistry implements ConstraintRegistry {

    @Override
    public String getRegistryId() {
        return "rbac";
    }

    @Override
    public Map<String, String> getConstraintToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("role_name_uk", "role_name");
        mappings.put("permission_name_uk", "permission_name");
        return mappings;
    }

    @Override
    public Map<String, String> getColumnToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("role_name", "role_name");
        mappings.put("permission_name", "permission_name");
        return mappings;
    }

    @Override
    public Map<String, String> getFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("role_name", "role name");
        labels.put("permission_name", "permission name");
        return labels;
    }

    @Override
    public Map<String, ConstraintViolationMapper.EntityContext> getEntityContextMappings() {
        Map<String, ConstraintViolationMapper.EntityContext> mappings = new HashMap<>();
        mappings.put("role_name", new ConstraintViolationMapper.EntityContext("role", "value"));
        mappings.put("permission_name", new ConstraintViolationMapper.EntityContext("permission", "value"));
        return mappings;
    }
}
