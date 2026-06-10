package com.mmva.newsapp.infrastructure.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Coordinator for all constraint registries.
 * 
 * Manages feature-specific constraint registry implementations and provides
 * unified access to constraint mappings across all features.
 * 
 * ARCHITECTURE: Central coordinator that discovers all ConstraintRegistry implementations
 * and aggregates their mappings. This allows features to add their constraints without
 * modifying the core mapper.
 * 
 * PRINCIPLE: Inversion of Control - Features register themselves, manager doesn't need to know about them
 */
@Slf4j
@Component
public class ConstraintRegistryManager {

    private final List<ConstraintRegistry> registries;
    private final Map<String, String> aggregatedConstraintToField = new HashMap<>();
    private final Map<String, String> aggregatedColumnToField = new HashMap<>();
    private final Map<String, String> aggregatedFieldLabels = new HashMap<>();
    private final Map<String, ConstraintViolationMapper.EntityContext> aggregatedEntityContext = new HashMap<>();

    /**
     * Constructor: Receives all ConstraintRegistry implementations via Spring dependency injection.
     * Spring automatically wires all beans implementing ConstraintRegistry.
     * 
     * @param registries All ConstraintRegistry implementations (auto-discovered by Spring)
     */
    public ConstraintRegistryManager(List<ConstraintRegistry> registries) {
        this.registries = registries;
        this.aggregateRegistries();
    }

    /**
     * Aggregate all registries into unified maps.
     * Called during initialization to consolidate all constraint mappings.
     */
    private void aggregateRegistries() {
        log.info("Aggregating {} constraint registries", registries.size());

        for (ConstraintRegistry registry : registries) {
            String registryId = registry.getRegistryId();
            log.debug("Processing registry: {}", registryId);

            // Merge constraint mappings
            registry.getConstraintToFieldMappings().forEach((key, value) -> {
                if (aggregatedConstraintToField.containsKey(key)) {
                    log.warn("Duplicate constraint mapping for key: {} in registry: {} (existing: {}→{}, new: {}→{})",
                        key, registryId,
                        key, aggregatedConstraintToField.get(key),
                        key, value);
                }
                aggregatedConstraintToField.put(key, value);
            });

            // Merge column mappings
            registry.getColumnToFieldMappings().forEach((key, value) -> {
                if (aggregatedColumnToField.containsKey(key)) {
                    log.warn("Duplicate column mapping for key: {} in registry: {} (existing: {}→{}, new: {}→{})",
                        key, registryId,
                        key, aggregatedColumnToField.get(key),
                        key, value);
                }
                aggregatedColumnToField.put(key, value);
            });

            // Merge field labels
            registry.getFieldLabels().forEach((key, value) -> {
                if (aggregatedFieldLabels.containsKey(key)) {
                    log.warn("Duplicate field label for key: {} in registry: {} (existing: {}→{}, new: {}→{})",
                        key, registryId,
                        key, aggregatedFieldLabels.get(key),
                        key, value);
                }
                aggregatedFieldLabels.put(key, value);
            });

            // Merge entity context
            registry.getEntityContextMappings().forEach((key, value) -> {
                if (aggregatedEntityContext.containsKey(key)) {
                    log.warn("Duplicate entity context for key: {} in registry: {}", key, registryId);
                }
                aggregatedEntityContext.put(key, value);
            });
        }

        log.info("✅ Registry aggregation complete:");
        log.info("   Constraint mappings: {}", aggregatedConstraintToField.size());
        log.info("   Column mappings: {}", aggregatedColumnToField.size());
        log.info("   Field labels: {}", aggregatedFieldLabels.size());
        log.info("   Entity contexts: {}", aggregatedEntityContext.size());
    }

    // Accessors for aggregated maps

    public Map<String, String> getAggregatedConstraintToField() {
        return Collections.unmodifiableMap(aggregatedConstraintToField);
    }

    public Map<String, String> getAggregatedColumnToField() {
        return Collections.unmodifiableMap(aggregatedColumnToField);
    }

    public Map<String, String> getAggregatedFieldLabels() {
        return Collections.unmodifiableMap(aggregatedFieldLabels);
    }

    public Map<String, ConstraintViolationMapper.EntityContext> getAggregatedEntityContext() {
        return Collections.unmodifiableMap(aggregatedEntityContext);
    }

    /**
     * Get information about all registered features (for debugging/monitoring).
     * 
     * @return List of registry IDs
     */
    public List<String> getRegisteredFeatures() {
        return registries.stream()
            .map(ConstraintRegistry::getRegistryId)
            .sorted()
            .toList();
    }
}
