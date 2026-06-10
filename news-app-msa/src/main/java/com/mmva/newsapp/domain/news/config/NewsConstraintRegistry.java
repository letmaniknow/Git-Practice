package com.mmva.newsapp.domain.news.config;

import com.mmva.newsapp.infrastructure.common.exception.ConstraintRegistry;
import com.mmva.newsapp.infrastructure.common.exception.ConstraintViolationMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * News feature constraint registry.
 * 
 * Registers all constraint mappings specific to the NEWS domain:
 * - news_title_en, news_title_es (title uniqueness)
 * - news_slug (URL slug)
 * - news_categories_slug (category slug)
 * 
 * PRINCIPLE: Feature ownership - News feature owns its constraint definitions
 * SCALABLE: New news constraints are added here, not in central mapper
 * 
 * This implementation ensures that error messages for news constraints are:
 * - Professional: "An article with this English title already exists..."
 * - Consistent: Same messages across application and database layers
 * - Maintainable: All news constraints in one place
 */
@Component
public class NewsConstraintRegistry implements ConstraintRegistry {

    @Override
    public String getRegistryId() {
        return "news";
    }

    @Override
    public Map<String, String> getConstraintToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // NEWS constraints - but title constraints are now handled by application layer
        // No constraint mappings needed for titles (handled by application validation)
        // Keep this for reference if database constraints are needed in future
        
        // news_slug constraint (if needed in future)
        // mappings.put("news_slug_uk", "news_slug");
        
        // news_categories_slug constraint
        mappings.put("news_categories_slug_uk", "news_categories_slug");
        
        return mappings;
    }

    @Override
    public Map<String, String> getColumnToFieldMappings() {
        Map<String, String> mappings = new HashMap<>();
        
        // NEWS column mappings - for direct extraction from error messages
        // Title columns: uniqueness enforced at BOTH layers (application validation + database constraints)
        mappings.put("news_title_en", "news_title_en");
        mappings.put("news_title_es", "news_title_es");
        
        // Slug column (currently optional, for future use)
        mappings.put("news_slug", "news_slug");
        
        // Category slug column
        mappings.put("news_categories_slug", "news_categories_slug");
        
        return mappings;
    }

    @Override
    public Map<String, String> getFieldLabels() {
        Map<String, String> labels = new HashMap<>();
        
        // NEWS field labels - human-friendly names for error messages
        labels.put("news_title_en", "English title");
        labels.put("news_title_es", "Spanish title");
        labels.put("news_slug", "URL slug");
        labels.put("news_categories_slug", "category slug");
        
        return labels;
    }

    @Override
    public Map<String, ConstraintViolationMapper.EntityContext> getEntityContextMappings() {
        Map<String, ConstraintViolationMapper.EntityContext> mappings = new HashMap<>();
        
        // NEWS entity contexts - used to generate professional error messages
        // Example output: "An article with this English title already exists. Please use a different title."
        
        // Title constraints
        mappings.put("news_title_en", new ConstraintViolationMapper.EntityContext("article", "title"));
        mappings.put("news_title_es", new ConstraintViolationMapper.EntityContext("article", "title"));
        
        // Slug constraint
        mappings.put("news_slug", new ConstraintViolationMapper.EntityContext("article", "slug"));
        
        // Category slug constraint
        mappings.put("news_categories_slug", new ConstraintViolationMapper.EntityContext("article", "value"));
        
        return mappings;
    }
}
