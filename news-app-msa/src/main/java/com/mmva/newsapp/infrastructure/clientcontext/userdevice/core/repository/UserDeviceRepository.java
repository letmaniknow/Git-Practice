package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.repository;

import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.model.UserDevice;
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
 * Repository for user device management with comprehensive query capabilities.
 * 
 * <p>
 * Provides queries for:
 * </p>
 * <ul>
 * <li>Device lookup and matching</li>
 * <li>Multi-device session management</li>
 * <li>Security monitoring</li>
 * <li>Device analytics</li>
 * <li>Cleanup operations</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

        // ========================================
        // Device Lookup
        // ========================================

        /**
         * Find device by fingerprint and user.
         */
        Optional<UserDevice> findByUserIdAndDeviceFingerprint(UUID userId, String deviceFingerprint);

        /**
         * Find device by fingerprint (across all users).
         */
        List<UserDevice> findByDeviceFingerprint(String deviceFingerprint);

        /**
         * Check if device exists for user.
         */
        boolean existsByUserIdAndDeviceFingerprint(UUID userId, String deviceFingerprint);

        // ========================================
        // User's Devices
        // ========================================

        /**
         * Get all devices for a user, ordered by last use.
         */
        List<UserDevice> findByUserIdOrderByLastUsedAtDesc(UUID userId);

        /**
         * Get active devices for a user.
         */
        @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId " +
                        "AND d.isBlocked = false AND d.deletedAt IS NULL " +
                        "ORDER BY d.lastUsedAt DESC")
        List<UserDevice> findActiveDevicesByUserId(@Param("userId") UUID userId);

        /**
         * Get trusted devices for a user.
         */
        List<UserDevice> findByUserIdAndIsTrustedTrue(UUID userId);

        /**
         * Count devices for a user.
         */
        long countByUserIdAndIsBlockedFalse(UUID userId);

        /**
         * Count devices by type for a user.
         */
        @Query("SELECT d.deviceType, COUNT(d) FROM UserDevice d " +
                        "WHERE d.userId = :userId AND d.isBlocked = false " +
                        "GROUP BY d.deviceType")
        List<Object[]> countDevicesByType(@Param("userId") UUID userId);

        // ========================================
        // Security Queries
        // ========================================

        /**
         * Get blocked devices for a user.
         */
        List<UserDevice> findByUserIdAndIsBlockedTrue(UUID userId);

        /**
         * Find devices with high risk score.
         */
        @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId AND d.riskScore >= :minRisk")
        List<UserDevice> findHighRiskDevices(@Param("userId") UUID userId, @Param("minRisk") int minRisk);

        /**
         * Find devices requiring verification.
         */
        List<UserDevice> findByUserIdAndRequiresVerificationTrue(UUID userId);

        /**
         * Find devices with multiple failed logins.
         */
        @Query("SELECT d FROM UserDevice d WHERE d.failedLoginCount >= :minFailures " +
                        "AND d.isBlocked = false")
        List<UserDevice> findDevicesWithFailedLogins(@Param("minFailures") int minFailures);

        /**
         * Find if device has been used by other users (potential account sharing).
         */
        @Query("SELECT COUNT(DISTINCT d.userId) FROM UserDevice d " +
                        "WHERE d.deviceFingerprint = :fingerprint")
        long countUsersForFingerprint(@Param("fingerprint") String fingerprint);

        /**
         * Find all users who have used a device.
         */
        @Query("SELECT DISTINCT d.userId FROM UserDevice d " +
                        "WHERE d.deviceFingerprint = :fingerprint")
        List<UUID> findUserIdsForFingerprint(@Param("fingerprint") String fingerprint);

        // ========================================
        // Location-based Queries
        // ========================================

        /**
         * Find devices by last known country.
         */
        List<UserDevice> findByUserIdAndLastCountryCode(UUID userId, String countryCode);

        /**
         * Get distinct countries for user's devices.
         */
        @Query("SELECT DISTINCT d.lastCountryCode FROM UserDevice d " +
                        "WHERE d.userId = :userId AND d.lastCountryCode IS NOT NULL")
        List<String> findDistinctCountriesByUserId(@Param("userId") UUID userId);

        /**
         * Check if user has devices in new location (anomaly detection).
         */
        @Query("SELECT CASE WHEN COUNT(d) = 0 THEN true ELSE false END " +
                        "FROM UserDevice d WHERE d.userId = :userId AND d.lastCountryCode = :countryCode")
        boolean isNewLocationForUser(@Param("userId") UUID userId, @Param("countryCode") String countryCode);

        // ========================================
        // Push Notification Queries
        // ========================================

        /**
         * Get devices with push tokens for a user.
         */
        @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId " +
                        "AND d.pushToken IS NOT NULL AND d.isBlocked = false")
        List<UserDevice> findDevicesWithPushToken(@Param("userId") UUID userId);

        /**
         * Get devices with FCM tokens.
         */
        @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId " +
                        "AND d.pushTokenType = 'FCM' AND d.pushToken IS NOT NULL AND d.isBlocked = false")
        List<UserDevice> findDevicesWithFcmToken(@Param("userId") UUID userId);

        /**
         * Get devices with APNS tokens.
         */
        @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId " +
                        "AND d.pushTokenType = 'APNS' AND d.pushToken IS NOT NULL AND d.isBlocked = false")
        List<UserDevice> findDevicesWithApnsToken(@Param("userId") UUID userId);

        // ========================================
        // Analytics Queries
        // ========================================

        /**
         * Get device type distribution.
         */
        @Query("SELECT d.deviceType, COUNT(d) FROM UserDevice d " +
                        "WHERE d.deletedAt IS NULL GROUP BY d.deviceType")
        List<Object[]> getDeviceTypeDistribution();

        /**
         * Get channel distribution.
         */
        @Query("SELECT d.channel, COUNT(d) FROM UserDevice d " +
                        "WHERE d.deletedAt IS NULL GROUP BY d.channel")
        List<Object[]> getChannelDistribution();

        /**
         * Get OS distribution.
         */
        @Query("SELECT d.osName, COUNT(d) FROM UserDevice d " +
                        "WHERE d.deletedAt IS NULL AND d.osName IS NOT NULL GROUP BY d.osName " +
                        "ORDER BY COUNT(d) DESC")
        List<Object[]> getOsDistribution();

        /**
         * Get browser distribution.
         */
        @Query("SELECT d.browserName, COUNT(d) FROM UserDevice d " +
                        "WHERE d.deletedAt IS NULL AND d.browserName IS NOT NULL GROUP BY d.browserName " +
                        "ORDER BY COUNT(d) DESC")
        List<Object[]> getBrowserDistribution();

        /**
         * Get top device brands.
         */
        @Query("SELECT d.deviceBrand, COUNT(d) FROM UserDevice d " +
                        "WHERE d.deletedAt IS NULL AND d.deviceBrand IS NOT NULL GROUP BY d.deviceBrand " +
                        "ORDER BY COUNT(d) DESC")
        List<Object[]> getDeviceBrandDistribution();

        /**
         * Get devices registered per day.
         */
        @Query(value = "SELECT DATE(first_seen_at) as date, COUNT(*) " +
                        "FROM user_devices WHERE first_seen_at >= :since " +
                        "GROUP BY DATE(first_seen_at) ORDER BY date", nativeQuery = true)
        List<Object[]> getDevicesRegisteredPerDay(@Param("since") Instant since);

        // ========================================
        // Cleanup Operations
        // ========================================

        /**
         * Delete devices not used since a certain date.
         */
        @Modifying
        @Query("DELETE FROM UserDevice d WHERE d.lastUsedAt < :before")
        int deleteInactiveDevices(@Param("before") Instant before);

        /**
         * Soft delete inactive devices.
         */
        @Modifying
        @Query("UPDATE UserDevice d SET d.deletedAt = :now " +
                        "WHERE d.lastUsedAt < :before AND d.deletedAt IS NULL")
        int softDeleteInactiveDevices(@Param("before") Instant before, @Param("now") Instant now);

        /**
         * Delete blocked devices older than certain date.
         */
        @Modifying
        @Query("DELETE FROM UserDevice d WHERE d.isBlocked = true AND d.blockedAt < :before")
        int deleteOldBlockedDevices(@Param("before") Instant before);

        /**
         * Reset failed login counts for all devices.
         */
        @Modifying
        @Query("UPDATE UserDevice d SET d.failedLoginCount = 0 " +
                        "WHERE d.failedLoginCount > 0")
        int resetAllFailedLoginCounts();

        // ========================================
        // Device Limits
        // ========================================

        /**
         * Get oldest devices for a user (for enforcing device limits).
         */
        @Query("SELECT d FROM UserDevice d WHERE d.userId = :userId " +
                        "AND d.isBlocked = false ORDER BY d.lastUsedAt ASC")
        List<UserDevice> findOldestDevices(@Param("userId") UUID userId);
}
