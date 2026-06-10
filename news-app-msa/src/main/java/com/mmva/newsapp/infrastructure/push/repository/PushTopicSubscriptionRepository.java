package com.mmva.newsapp.infrastructure.push.repository;

import com.mmva.newsapp.infrastructure.push.enums.PushTopicCategory;
import com.mmva.newsapp.infrastructure.push.model.PushTopicSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for push notification topic subscriptions.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface PushTopicSubscriptionRepository
        extends JpaRepository<PushTopicSubscription, UUID> {

    // ========================================
    // Find by Device
    // ========================================

    /**
     * Find all subscriptions for a device.
     */
    List<PushTopicSubscription> findByDeviceIdAndIsActiveTrue(UUID deviceId);

    /**
     * Find active topics for device.
     */
    @Query("SELECT s.topic FROM PushTopicSubscription s WHERE s.deviceId = :deviceId AND s.isActive = true AND s.deletedAt IS NULL")
    List<String> findActiveTopicsByDeviceId(@Param("deviceId") UUID deviceId);

    /**
     * Find subscription for device and topic.
     */
    Optional<PushTopicSubscription> findByDeviceIdAndTopic(UUID deviceId, String topic);

    /**
     * Check if device is subscribed to topic.
     */
    boolean existsByDeviceIdAndTopicAndIsActiveTrue(UUID deviceId, String topic);

    // ========================================
    // Find by Topic
    // ========================================

    /**
     * Find all subscribers for a topic.
     */
    List<PushTopicSubscription> findByTopicAndIsActiveTrue(String topic);

    /**
     * Count subscribers for topic.
     */
    long countByTopicAndIsActiveTrue(String topic);

    /**
     * Get device IDs subscribed to topic.
     */
    @Query("SELECT s.deviceId FROM PushTopicSubscription s WHERE s.topic = :topic AND s.isActive = true AND s.deletedAt IS NULL")
    List<UUID> findDeviceIdsByTopic(@Param("topic") String topic);

    // ========================================
    // Find by Category
    // ========================================

    /**
     * Find subscriptions by topic newscategory.
     */
    List<PushTopicSubscription> findByTopicCategoryAndIsActiveTrue(PushTopicCategory category);

    // ========================================
    // Sync Status
    // ========================================

    /**
     * Find unsynced subscriptions.
     */
    @Query("SELECT s FROM PushTopicSubscription s WHERE s.isSynced = false AND s.isActive = true AND s.deletedAt IS NULL")
    List<PushTopicSubscription> findUnsyncedSubscriptions();

    /**
     * Find pending unsubscribes (inactive but synced).
     */
    @Query("SELECT s FROM PushTopicSubscription s WHERE s.isActive = false AND s.isSynced = true AND s.deletedAt IS NULL")
    List<PushTopicSubscription> findPendingUnsubscribes();

    // ========================================
    // Update Operations
    // ========================================

    /**
     * Mark subscription as synced.
     */
    @Modifying
    @Query("UPDATE PushTopicSubscription s SET s.isSynced = true, s.syncedAt = :syncedAt, s.syncError = null WHERE s.subscriptionId = :id")
    void markAsSynced(@Param("id") UUID id, @Param("syncedAt") Instant syncedAt);

    /**
     * Mark subscription sync failed.
     */
    @Modifying
    @Query("UPDATE PushTopicSubscription s SET s.isSynced = false, s.syncError = :error WHERE s.subscriptionId = :id")
    void markSyncFailed(@Param("id") UUID id, @Param("error") String error);

    /**
     * Deactivate subscription (unsubscribe).
     */
    @Modifying
    @Query("UPDATE PushTopicSubscription s SET s.isActive = false WHERE s.deviceId = :deviceId AND s.topic = :topic")
    void deactivateSubscription(@Param("deviceId") UUID deviceId, @Param("topic") String topic);

    /**
     * Deactivate all subscriptions for device.
     */
    @Modifying
    @Query("UPDATE PushTopicSubscription s SET s.isActive = false WHERE s.deviceId = :deviceId")
    void deactivateAllForDevice(@Param("deviceId") UUID deviceId);

    // ========================================
    // Delete Operations
    // ========================================

    /**
     * Delete subscriptions for device.
     */
    void deleteByDeviceId(UUID deviceId);

    // ========================================
    // Statistics
    // ========================================

    /**
     * Count subscriptions per topic.
     */
    @Query("SELECT s.topic, COUNT(s) FROM PushTopicSubscription s WHERE s.isActive = true AND s.deletedAt IS NULL GROUP BY s.topic ORDER BY COUNT(s) DESC")
    List<Object[]> countSubscriptionsByTopic();

    /**
     * Get popular topics.
     */
    @Query("SELECT s.topic, s.topicDisplayName, COUNT(s) FROM PushTopicSubscription s WHERE s.isActive = true AND s.deletedAt IS NULL GROUP BY s.topic, s.topicDisplayName ORDER BY COUNT(s) DESC")
    List<Object[]> getPopularTopics();
}
