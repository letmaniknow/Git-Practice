package com.mmva.newsapp.domain.adminuser.config;

import com.mmva.newsapp.infrastructure.common.exception.ConstraintRegistry;
import com.mmva.newsapp.infrastructure.common.exception.ConstraintViolationMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin User feature constraint registry.
 * 
 * Registers all constraint mappings specific to the ADMIN USER domain:
 * - admin_users_username (unique username for login)
 * - admin_users_email (unique email for notifications)
 * 
 * PRINCIPLE: Feature ownership - Admin User feature owns its constraint definitions
 * SCALABLE: New admin constraints are added here, not in central mapper
 * 
 * This implementation ensures that error messages for admin constraints are:
 * - Professional: "An administrator with this email already exists..."
 * - Consistent: Same messages across application and database layers
 * - Maintainable: All admin constraints in one place
 */
@Component
public class AdminUserConstraintRegistry implements ConstraintRegistry {

    @Override
    public String getRegistryId() {
        return "admin-user";
    }

    @Override
    public Map<String, String> getConstraintToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // ADMIN USER constraints - unique constraints on username and email
        mappings.put("admin_users_username_uk", "admin_users_username");
        mappings.put("admin_users_email_uk", "admin_users_email");
        
        return mappings;
    }

    @Override
    public Map<String, String> getColumnToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // ADMIN USER column mappings - for direct extraction from error messages
        mappings.put("admin_users_username", "admin_users_username");
        mappings.put("admin_users_email", "admin_users_email");
        
        return mappings;
    }

    @Override
    public Map<String, String> getFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        
        // ADMIN USER field labels - human-friendly names for error messages
        labels.put("admin_users_username", "username");
        labels.put("admin_users_email", "email");
        
        return labels;
    }

    @Override
    public Map<String, ConstraintViolationMapper.EntityContext> getEntityContextMappings() {
        Map<String, ConstraintViolationMapper.EntityContext> mappings = new HashMap<>();
        
        // ADMIN USER entity contexts - used to generate professional error messages
        // Example output: "An administrator with this email already exists. Please use a different value."
        
        mappings.put("admin_users_username", new ConstraintViolationMapper.EntityContext("administrator", "value"));
        mappings.put("admin_users_email", new ConstraintViolationMapper.EntityContext("administrator", "value"));
        
        return mappings;
    }
}
