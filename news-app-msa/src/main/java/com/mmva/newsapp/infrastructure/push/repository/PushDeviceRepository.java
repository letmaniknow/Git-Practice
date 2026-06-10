package com.mmva.newsapp.infrastructure.push.repository;

import com.mmva.newsapp.infrastructure.push.enums.PushDevicePlatform;
import com.mmva.newsapp.infrastructure.push.model.PushDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for push notification device management.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface PushDeviceRepository
        extends JpaRepository<PushDevice, UUID>, JpaSpecificationExecutor<PushDevice> {

    // ========================================
    // Find by Token
    // ========================================

    /**
     * Find device by FCM token.
     */
    Optional<PushDevice> findByFcmToken(String fcmToken);

    /**
     * Check if FCM token exists.
     */
    boolean existsByFcmToken(String fcmToken);

    // ========================================
    // Find by Device Fingerprint
    // ========================================

    /**
     * Find device by fingerprint (for token refresh scenarios).
     */
    Optional<PushDevice> findByDeviceFingerprint(String deviceFingerprint);

    /**
     * Find all devices with same fingerprint.
     */
    List<PushDevice> findAllByDeviceFingerprint(String deviceFingerprint);

    // ========================================
    // Find by User
    // ========================================

    /**
     * Find all devices for a user.
     */
    List<PushDevice> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find all active devices for a user.
     */
    @Query("SELECT d FROM PushDevice d WHERE d.userId = :userId AND d.isActive = true AND d.deletedAt IS NULL")
    List<PushDevice> findActiveDevicesByUserId(@Param("userId") UUID userId);

    /**
     * Find active devices for multiple users.
     * Used for segment-targeted push notifications.
     * 
     * @param userIds list of user IDs
     * @return list of active devices for those users
     */
    @Query("SELECT d FROM PushDevice d WHERE d.userId IN :userIds AND d.isActive = true AND d.notificationsEnabled = true AND d.deletedAt IS NULL")
    List<PushDevice> findActiveDevicesByUserIdIn(@Param("userIds") List<UUID> userIds);

    /**
     * Count devices per user.
     */
    long countByUserIdAndIsActiveTrue(UUID userId);

    // ========================================
    // Find Active Devices
    // ========================================

    /**
     * Find all active devices (for broadcast notifications).
     */
    @Query("SELECT d FROM PushDevice d WHERE d.isActive = true AND d.notificationsEnabled = true AND d.deletedAt IS NULL")
    List<PushDevice> findAllActiveDevices();

    /**
     * Find active devices with pagination.
     */
    @Query("SELECT d FROM PushDevice d WHERE d.isActive = true AND d.notificationsEnabled = true AND d.deletedAt IS NULL")
    Page<PushDevice> findAllActiveDevices(Pageable pageable);

    /**
     * Count all active devices.
     */
    @Query("SELECT COUNT(d) FROM PushDevice d WHERE d.isActive = true AND d.notificationsEnabled = true AND d.deletedAt IS NULL")
    long countActiveDevices();

    // ========================================
    // Find by Platform
    // ========================================

    /**
     * Find active devices by platform.
     */
    @Query("SELECT d FROM PushDevice d WHERE d.platform = :platform AND d.isActive = true AND d.notificationsEnabled = true AND d.deletedAt IS NULL")
    List<PushDevice> findActiveDevicesByPlatform(@Param("platform") PushDevicePlatform platform);

    /**
     * Count devices by platform.
     */
    @Query("SELECT COUNT(d) FROM PushDevice d WHERE d.platform = :platform AND d.isActive = true AND d.deletedAt IS NULL")
    long countByPlatform(@Param("platform") PushDevicePlatform platform);

    // ========================================
    // Find by Language
    // ========================================

    /**
     * Find active devices by language.
     */
    @Query("SELECT d FROM PushDevice d WHERE d.language = :language AND d.isActive = true AND d.notificationsEnabled = true AND d.deletedAt IS NULL")
    List<PushDevice> findActiveDevicesByLanguage(@Param("language") String language);

    // ========================================
    // Breaking News Enabled
    // ========================================

    /**
     * Find devices with breaking newsapp enabled.
     */
    @Query("SELECT d FROM PushDevice d WHERE d.isActive = true AND d.notificationsEnabled = true AND d.breakingNewsEnabled = true AND d.deletedAt IS NULL")
    List<PushDevice> findDevicesWithBreakingNewsEnabled();

    /**
     * Find devices with breaking newsapp enabled (paginated).
     */
    @Query("SELECT d FROM PushDevice d WHERE d.isActive = true AND d.notificationsEnabled = true AND d.breakingNewsEnabled = true AND d.deletedAt IS NULL")
    Page<PushDevice> findDevicesWithBreakingNewsEnabled(Pageable pageable);

    // ========================================
    // Daily Digest Enabled
    // ========================================

    /**
     * Find devices with daily digest enabled.
     */
    @Query("SELECT d FROM PushDevice d WHERE d.isActive = true AND d.notificationsEnabled = true AND d.dailyDigestEnabled = true AND d.deletedAt IS NULL")
    List<PushDevice> findDevicesWithDailyDigestEnabled();

    // ========================================
    // Stale Device Cleanup
    // ========================================

    /**
     * Find stale devices (no activity for given period).
     */
    @Query("SELECT d FROM PushDevice d WHERE d.isActive = true AND d.lastActiveAt < :cutoffTime AND d.deletedAt IS NULL")
    List<PushDevice> findStaleDevices(@Param("cutoffTime") Instant cutoffTime);

    /**
     * Find devices with high failure count.
     */
    @Query("SELECT d FROM PushDevice d WHERE d.isActive = true AND d.failedDeliveryCount >= :threshold AND d.deletedAt IS NULL")
    List<PushDevice> findDevicesWithHighFailureCount(@Param("threshold") int threshold);

    // ========================================
    // Update Operations
    // ========================================

    /**
     * Update last active time for device.
     */
    @Modifying
    @Query("UPDATE PushDevice d SET d.lastActiveAt = :lastActiveAt WHERE d.deviceId = :deviceId")
    void updateLastActiveAt(@Param("deviceId") UUID deviceId, @Param("lastActiveAt") Instant lastActiveAt);

    /**
     * Update last notification time.
     */
    @Modifying
    @Query("UPDATE PushDevice d SET d.lastNotificationAt = :time, d.failedDeliveryCount = 0 WHERE d.deviceId = :deviceId")
    void updateLastNotificationSuccess(@Param("deviceId") UUID deviceId, @Param("time") Instant time);

    /**
     * Increment failure count.
     */
    @Modifying
    @Query("UPDATE PushDevice d SET d.failedDeliveryCount = d.failedDeliveryCount + 1 WHERE d.deviceId = :deviceId")
    void incrementFailureCount(@Param("deviceId") UUID deviceId);

    /**
     * Deactivate device (e.g., invalid token).
     */
    @Modifying
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.deviceId = :deviceId")
    void deactivateDevice(@Param("deviceId") UUID deviceId);

    /**
     * Deactivate by FCM token.
     */
    @Modifying
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.fcmToken = :fcmToken")
    void deactivateByFcmToken(@Param("fcmToken") String fcmToken);

    /**
     * Link device to user.
     */
    @Modifying
    @Query("UPDATE PushDevice d SET d.userId = :userId WHERE d.deviceId = :deviceId")
    void linkDeviceToUser(@Param("deviceId") UUID deviceId, @Param("userId") UUID userId);

    /**
     * Unlink device from user (on logout).
     */
    @Modifying
    @Query("UPDATE PushDevice d SET d.userId = null WHERE d.deviceId = :deviceId")
    void unlinkDeviceFromUser(@Param("deviceId") UUID deviceId);

    // ========================================
    // Statistics
    // ========================================

    /**
     * Get device count by platform.
     */
    @Query("SELECT d.platform, COUNT(d) FROM PushDevice d WHERE d.isActive = true AND d.deletedAt IS NULL GROUP BY d.platform")
    List<Object[]> countDevicesByPlatform();

    /**
     * Get device count by language.
     */
    @Query("SELECT d.language, COUNT(d) FROM PushDevice d WHERE d.isActive = true AND d.deletedAt IS NULL GROUP BY d.language")
    List<Object[]> countDevicesByLanguage();
}
