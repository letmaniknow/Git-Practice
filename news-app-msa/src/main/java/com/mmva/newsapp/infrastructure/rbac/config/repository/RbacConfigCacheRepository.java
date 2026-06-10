package com.mmva.newsapp.infrastructure.rbac.config.repository;

import com.mmva.newsapp.infrastructure.rbac.config.model.RbacConfigCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RbacConfigCacheRepository - Data access for RBAC configuration
 * 
 * Purpose:
 * - Load configuration from database
 * - Support version tracking for multi-server sync
 * - Find active configuration values
 */
@Repository
public interface RbacConfigCacheRepository extends JpaRepository<RbacConfigCache, Integer> {

    /**
     * Find configuration by key
     * Used to fetch specific configuration values
     */
    Optional<RbacConfigCache> findByConfigKey(String configKey);

    /**
     * Find all active configurations
     * Excludes soft-deleted (configIsActive = false) configurations
     */
    @Query("SELECT r FROM RbacConfigCache r WHERE r.configIsActive = true ORDER BY r.configKey ASC")
    List<RbacConfigCache> findAllActive();

    /**
     * Find all configurations of a specific type
     * Examples: Find all INT configs, all BOOLEAN configs
     */
    @Query("SELECT r FROM RbacConfigCache r WHERE r.configType = :configType AND r.configIsActive = true")
    List<RbacConfigCache> findByConfigType(@Param("configType") String configType);

    /**
     * Check if a configuration key exists and is active
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RbacConfigCache r WHERE r.configKey = :configKey AND r.configIsActive = true")
    boolean existsActiveConfig(@Param("configKey") String configKey);

    /**
     * Count total active configurations
     * Used for version tracking
     */
    @Query("SELECT COUNT(r) FROM RbacConfigCache r WHERE r.configIsActive = true")
    long countActiveConfigs();
}
