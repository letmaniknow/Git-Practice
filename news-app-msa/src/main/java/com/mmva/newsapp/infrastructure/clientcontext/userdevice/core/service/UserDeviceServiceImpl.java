package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.service;

import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.model.UserDevice;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link UserDeviceService} for managing user devices with
 * comprehensive security and analytics features.
 * 
 * <p>
 * Provides:
 * </p>
 * <ul>
 * <li>Device registration and tracking</li>
 * <li>Multi-device session management</li>
 * <li>Anomaly detection (new device, new location)</li>
 * <li>Device trust management</li>
 * <li>Push notification token management</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final ClientContextService clientContextService;

    // ========================================
    // Constants
    // ========================================

    /** Maximum devices per user before enforcing cleanup */
    private static final int MAX_DEVICES_PER_USER = 10;

    /**
     * Days of inactivity before device is considered stale (reserved for future
     * cleanup job)
     */
    @SuppressWarnings("unused")
    private static final int DEVICE_INACTIVE_DAYS = 90;

    /** Trust increase per successful login (reserved for trust scoring feature) */
    @SuppressWarnings("unused")
    private static final int TRUST_INCREMENT_LOGIN = 5;

    /**
     * Trust decrease for suspicious activity (reserved for trust scoring feature)
     */
    @SuppressWarnings("unused")
    private static final int TRUST_DECREMENT_SUSPICIOUS = 10;

    // ========================================
    // Device Registration & Tracking
    // ========================================

    @Override
    @Transactional
    public DeviceRegistrationResult registerOrUpdateDevice(UUID userId) {
        ClientContextDto context = clientContextService.getCurrentContext();
        return registerOrUpdateDevice(userId, context);
    }

    @Override
    @Transactional
    public DeviceRegistrationResult registerOrUpdateDevice(UUID userId, ClientContextDto context) {
        if (userId == null || context == null || context.deviceFingerprint() == null) {
            log.warn("Cannot register device: missing userId or fingerprint");
            return new DeviceRegistrationResult(null, false, false, null);
        }

        String fingerprint = context.deviceFingerprint();
        Optional<UserDevice> existingDevice = userDeviceRepository
                .findByUserIdAndDeviceFingerprint(userId, fingerprint);

        if (existingDevice.isPresent()) {
            // Update existing device
            UserDevice device = existingDevice.get();

            // Check for location change (anomaly detection)
            boolean locationChanged = isLocationChanged(device, context);

            device.updateFromContext(context);
            UserDevice saved = userDeviceRepository.save(device);

            log.debug("Updated device {} for user {}", saved.getDeviceId(), userId);
            return new DeviceRegistrationResult(saved, false, locationChanged,
                    locationChanged ? "New location detected" : null);
        } else {
            // Register new device
            UserDevice newDevice = UserDevice.fromContext(userId, context);

            // Enforce device limit
            enforceDeviceLimit(userId);

            UserDevice saved = userDeviceRepository.save(newDevice);
            log.info("Registered new device {} for user {} ({})",
                    saved.getDeviceId(), userId, saved.getDisplayName());

            return new DeviceRegistrationResult(saved, true, false, "New device registered");
        }
    }

    @Override
    public Optional<UserDevice> getDevice(UUID userId, String fingerprint) {
        return userDeviceRepository.findByUserIdAndDeviceFingerprint(userId, fingerprint);
    }

    @Override
    public List<UserDevice> getUserDevices(UUID userId) {
        return userDeviceRepository.findByUserIdOrderByLastUsedAtDesc(userId);
    }

    @Override
    public List<UserDevice> getActiveDevices(UUID userId) {
        return userDeviceRepository.findActiveDevicesByUserId(userId);
    }

    @Override
    public List<UserDevice> getTrustedDevices(UUID userId) {
        return userDeviceRepository.findByUserIdAndIsTrustedTrue(userId);
    }

    // ========================================
    // Device Trust Management
    // ========================================

    @Override
    @Transactional
    public UserDevice trustDevice(UUID userId, UUID deviceId) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setIsTrusted(true);
        device.setTrustLevel(100);
        device.setRequiresVerification(false);

        log.info("Device {} marked as trusted for user {}", deviceId, userId);
        return userDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public UserDevice untrustDevice(UUID userId, UUID deviceId) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setIsTrusted(false);
        device.setTrustLevel(Math.min(device.getTrustLevel(), 50));

        log.info("Device {} trust removed for user {}", deviceId, userId);
        return userDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public void increaseTrust(UUID deviceId, int amount) {
        userDeviceRepository.findById(deviceId).ifPresent(device -> {
            device.increaseTrust(amount);
            userDeviceRepository.save(device);
        });
    }

    // ========================================
    // Device Security
    // ========================================

    @Override
    @Transactional
    public UserDevice blockDevice(UUID userId, UUID deviceId, String reason) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setIsBlocked(true);
        device.setBlockedReason(reason);
        device.setBlockedAt(Instant.now());
        device.setIsTrusted(false);

        log.warn("Device {} blocked for user {}: {}", deviceId, userId, reason);
        return userDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public UserDevice unblockDevice(UUID userId, UUID deviceId) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setIsBlocked(false);
        device.setBlockedReason(null);
        device.setBlockedAt(null);
        device.setFailedLoginCount(0);
        device.setRequiresVerification(true);

        log.info("Device {} unblocked for user {}", deviceId, userId);
        return userDeviceRepository.save(device);
    }

    @Override
    @Transactional
    public void recordFailedLogin(UUID userId, String fingerprint) {
        userDeviceRepository.findByUserIdAndDeviceFingerprint(userId, fingerprint)
                .ifPresent(device -> {
                    device.recordFailedLogin();
                    userDeviceRepository.save(device);
                    log.debug("Recorded failed login for device {} (count: {})",
                            device.getDeviceId(), device.getFailedLoginCount());
                });
    }

    @Override
    public List<UserDevice> getHighRiskDevices(UUID userId) {
        return userDeviceRepository.findHighRiskDevices(userId, 50);
    }

    @Override
    public boolean isSharedDevice(String fingerprint) {
        return userDeviceRepository.countUsersForFingerprint(fingerprint) > 1;
    }

    // ========================================
    // Anomaly Detection
    // ========================================

    @Override
    public boolean isNewDevice(UUID userId, String fingerprint) {
        return !userDeviceRepository.existsByUserIdAndDeviceFingerprint(userId, fingerprint);
    }

    @Override
    public boolean isNewLocation(UUID userId, String countryCode) {
        if (countryCode == null) {
            return false;
        }
        return userDeviceRepository.isNewLocationForUser(userId, countryCode);
    }

    @Override
    public AnomalyCheckResult checkForAnomalies(UUID userId, ClientContextDto context) {
        boolean newDevice = isNewDevice(userId, context.deviceFingerprint());
        boolean newLocation = isNewLocation(userId, context.countryCode());
        boolean sharedDevice = isSharedDevice(context.deviceFingerprint());
        boolean highRisk = context.riskScore() != null && context.riskScore() >= 70;

        int anomalyScore = 0;
        StringBuilder factors = new StringBuilder();

        if (newDevice) {
            anomalyScore += 20;
            factors.append("NEW_DEVICE;");
        }
        if (newLocation) {
            anomalyScore += 30;
            factors.append("NEW_LOCATION;");
        }
        if (sharedDevice) {
            anomalyScore += 40;
            factors.append("SHARED_DEVICE;");
        }
        if (highRisk) {
            anomalyScore += 30;
            factors.append("HIGH_RISK_CONTEXT;");
        }

        return new AnomalyCheckResult(
                newDevice,
                newLocation,
                sharedDevice,
                highRisk,
                Math.min(anomalyScore, 100),
                factors.length() > 0 ? factors.toString() : null);
    }

    // ========================================
    // Push Notification Management
    // ========================================

    @Override
    @Transactional
    public void updatePushToken(UUID deviceId, String token, String tokenType) {
        userDeviceRepository.findById(deviceId).ifPresent(device -> {
            device.setPushToken(token);
            device.setPushTokenType(tokenType);
            userDeviceRepository.save(device);
            log.debug("Updated push token for device {}", deviceId);
        });
    }

    @Override
    public List<UserDevice> getDevicesWithPushToken(UUID userId) {
        return userDeviceRepository.findDevicesWithPushToken(userId);
    }

    @Override
    public List<UserDevice> getFcmDevices(UUID userId) {
        return userDeviceRepository.findDevicesWithFcmToken(userId);
    }

    @Override
    public List<UserDevice> getApnsDevices(UUID userId) {
        return userDeviceRepository.findDevicesWithApnsToken(userId);
    }

    // ========================================
    // Device Removal
    // ========================================

    @Override
    @Transactional
    public void removeDevice(UUID userId, UUID deviceId) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setDeletedAt(Instant.now());
        userDeviceRepository.save(device);
        log.info("Removed device {} for user {}", deviceId, userId);
    }

    @Override
    @Transactional
    public int removeAllDevices(UUID userId) {
        List<UserDevice> devices = userDeviceRepository.findByUserIdOrderByLastUsedAtDesc(userId);
        Instant now = Instant.now();
        devices.forEach(d -> d.setDeletedAt(now));
        userDeviceRepository.saveAll(devices);
        log.info("Removed {} devices for user {}", devices.size(), userId);
        return devices.size();
    }

    @Override
    @Transactional
    public int logoutOtherDevices(UUID userId, String currentFingerprint) {
        List<UserDevice> devices = userDeviceRepository.findActiveDevicesByUserId(userId);
        int count = 0;
        for (UserDevice device : devices) {
            if (!device.getDeviceFingerprint().equals(currentFingerprint)) {
                device.setRequiresVerification(true);
                userDeviceRepository.save(device);
                count++;
            }
        }
        log.info("Logged out {} other devices for user {}", count, userId);
        return count;
    }

    @Override
    public UserDevice getDeviceById(UUID deviceId, UUID userId) {
        return userDeviceRepository.findById(deviceId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
    }

    @Override
    public List<UserDevice> getBlockedDevices(UUID userId) {
        return userDeviceRepository.findByUserIdAndIsBlockedTrue(userId);
    }

    @Override
    @Transactional
    public UserDevice renameDevice(UUID userId, UUID deviceId, String newName) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .filter(d -> d.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setDeviceName(newName);
        log.info("Device {} renamed to '{}' for user {}", deviceId, newName, userId);
        return userDeviceRepository.save(device);
    }

    // ========================================
    // Private Helpers
    // ========================================

    private void enforceDeviceLimit(UUID userId) {
        long deviceCount = userDeviceRepository.countByUserIdAndIsBlockedFalse(userId);
        if (deviceCount >= MAX_DEVICES_PER_USER) {
            // Remove oldest device
            List<UserDevice> oldestDevices = userDeviceRepository.findOldestDevices(userId);
            if (!oldestDevices.isEmpty()) {
                UserDevice oldest = oldestDevices.get(0);
                oldest.setDeletedAt(Instant.now());
                userDeviceRepository.save(oldest);
                log.info("Auto-removed oldest device {} for user {} (limit reached)",
                        oldest.getDeviceId(), userId);
            }
        }
    }

    private boolean isLocationChanged(UserDevice device, ClientContextDto context) {
        if (context.countryCode() == null || device.getLastCountryCode() == null) {
            return false;
        }
        return !context.countryCode().equals(device.getLastCountryCode());
    }
}
