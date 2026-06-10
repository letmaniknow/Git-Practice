package com.mmva.newsapp.infrastructure.push.service;

import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.push.dto.*;
import com.mmva.newsapp.infrastructure.push.enums.PushDevicePlatform;
import com.mmva.newsapp.infrastructure.push.mapper.PushDeviceMapper;
import com.mmva.newsapp.infrastructure.push.mapper.PushTopicSubscriptionMapper;
import com.mmva.newsapp.infrastructure.push.model.PushDevice;
import com.mmva.newsapp.infrastructure.push.model.PushTopicSubscription;
import com.mmva.newsapp.infrastructure.push.repository.PushDeviceRepository;
import com.mmva.newsapp.infrastructure.push.repository.PushTopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Push device management service implementation.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushDeviceServiceImpl implements PushDeviceService {

    private static final List<String> DEFAULT_TOPICS = List.of("all_news", "breaking_news");

    private final PushDeviceRepository deviceRepository;
    private final PushTopicSubscriptionRepository subscriptionRepository;
    private final PushFcmService fcmService;
    private final PushDeviceMapper deviceMapper;
    private final PushTopicSubscriptionMapper topicMapper;

    // ========================================
    // Device Registration
    // ========================================

    @Override
    @Transactional
    public PushDeviceRegistrationResponseDto registerDevice(PushDeviceRegistrationRequestDto request) {
        log.info("PushDeviceService: Registering device - infrastructure={}, fingerprint={}",
                request.getPlatform(), request.getDeviceFingerprint());

        // Check if device already exists by token
        Optional<PushDevice> existingByToken = deviceRepository.findByFcmToken(request.getFcmToken());

        if (existingByToken.isPresent()) {
            // Update existing device
            PushDevice device = existingByToken.get();
            deviceMapper.updateEntityFromRequest(request, device);
            device.setLastActiveAt(Instant.now());
            device.setIsActive(true);
            device = deviceRepository.save(device);

            log.info("PushDeviceService: Device updated - deviceId={}", device.getDeviceId());
            return buildResponseWithTopics(device, "Device updated successfully");
        }

        // Check by fingerprint (token refresh scenario)
        if (request.getDeviceFingerprint() != null) {
            Optional<PushDevice> existingByFingerprint = deviceRepository
                    .findByDeviceFingerprint(request.getDeviceFingerprint());

            if (existingByFingerprint.isPresent()) {
                PushDevice device = existingByFingerprint.get();
                String oldToken = device.getFcmToken();

                device.setFcmToken(request.getFcmToken());
                deviceMapper.updateEntityFromRequest(request, device);
                device.setLastActiveAt(Instant.now());
                device.setIsActive(true);
                device = deviceRepository.save(device);

                // Re-subscribe to topics with new token
                resubscribeToTopics(device, oldToken);

                log.info("PushDeviceService: Device token refreshed - deviceId={}", device.getDeviceId());
                return buildResponseWithTopics(device, "Device token refreshed successfully");
            }
        }

        // Create new device
        PushDevice device = new PushDevice();
        device.setFcmToken(request.getFcmToken());
        device.setPlatform(request.getPlatform());
        device.setDeviceFingerprint(request.getDeviceFingerprint());
        device.setAppVersion(request.getAppVersion());
        device.setOsVersion(request.getOsVersion());
        device.setDeviceModel(request.getDeviceModel());
        device.setDeviceManufacturer(request.getDeviceManufacturer());
        device.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        device.setTimezone(request.getTimezone());
        device.setCountryCode(request.getCountryCode());
        device.setNotificationsEnabled(
                request.getNotificationsEnabled() != null ? request.getNotificationsEnabled() : true);
        device.setBreakingNewsEnabled(true);
        device.setDailyDigestEnabled(false);
        device.setPromotionalEnabled(false);
        device.setIsActive(true);
        device.setLastActiveAt(Instant.now());
        device.setFailedDeliveryCount(0);

        device = deviceRepository.save(device);

        // Subscribe to default topics
        subscribeToDefaultTopics(device);

        log.info("PushDeviceService: Device registered - deviceId={}", device.getDeviceId());
        return buildResponseWithTopics(device, "Device registered successfully");
    }

    @Override
    @Transactional
    public PushDeviceRegistrationResponseDto refreshToken(UUID deviceId, String newToken) {
        PushDevice device = findDeviceById(deviceId);
        String oldToken = device.getFcmToken();

        device.setFcmToken(newToken);
        device.setLastActiveAt(Instant.now());
        device = deviceRepository.save(device);

        // Re-subscribe to topics with new token
        resubscribeToTopics(device, oldToken);

        log.info("PushDeviceService: Token refreshed - deviceId={}", deviceId);
        return buildResponseWithTopics(device, "Token refreshed successfully");
    }

    @Override
    @Transactional
    public void unregisterDevice(UUID deviceId) {
        PushDevice device = findDeviceById(deviceId);

        // Unsubscribe from all topics
        List<String> topics = subscriptionRepository.findActiveTopicsByDeviceId(deviceId);
        for (String topic : topics) {
            fcmService.unsubscribeFromTopic(device.getFcmToken(), topic);
        }
        subscriptionRepository.deactivateAllForDevice(deviceId);

        // Soft delete device
        device.setIsActive(false);
        device.setDeletedAt(Instant.now());
        deviceRepository.save(device);

        log.info("PushDeviceService: Device unregistered - deviceId={}", deviceId);
    }

    @Override
    @Transactional
    public void unregisterByToken(String fcmToken) {
        Optional<PushDevice> device = deviceRepository.findByFcmToken(fcmToken);
        device.ifPresent(d -> unregisterDevice(d.getDeviceId()));
    }

    // ========================================
    // User Association
    // ========================================

    @Override
    @Transactional
    public void linkToUser(UUID deviceId, UUID userId) {
        deviceRepository.linkDeviceToUser(deviceId, userId);
        log.info("PushDeviceService: Device linked to user - deviceId={}, userId={}", deviceId, userId);
    }

    @Override
    @Transactional
    public void unlinkFromUser(UUID deviceId) {
        deviceRepository.unlinkDeviceFromUser(deviceId);
        log.info("PushDeviceService: Device unlinked from user - deviceId={}", deviceId);
    }

    // ========================================
    // Device Settings
    // ========================================

    @Override
    @Transactional
    public PushDeviceRegistrationResponseDto updateSettings(UUID deviceId, PushDeviceSettingsUpdateRequestDto request) {
        PushDevice device = findDeviceById(deviceId);

        if (request.getNotificationsEnabled() != null) {
            device.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getBreakingNewsEnabled() != null) {
            device.setBreakingNewsEnabled(request.getBreakingNewsEnabled());
            updateTopicSubscription(device, "breaking_news", request.getBreakingNewsEnabled());
        }
        if (request.getDailyDigestEnabled() != null) {
            device.setDailyDigestEnabled(request.getDailyDigestEnabled());
            updateTopicSubscription(device, "digest_daily", request.getDailyDigestEnabled());
        }
        if (request.getPromotionalEnabled() != null) {
            device.setPromotionalEnabled(request.getPromotionalEnabled());
        }
        if (request.getLanguage() != null) {
            String oldLang = device.getLanguage();
            device.setLanguage(request.getLanguage());
            // Update language topic
            if (oldLang != null) {
                updateTopicSubscription(device, "language_" + oldLang, false);
            }
            updateTopicSubscription(device, "language_" + request.getLanguage(), true);
        }
        if (request.getTimezone() != null) {
            device.setTimezone(request.getTimezone());
        }

        device = deviceRepository.save(device);
        log.info("PushDeviceService: Settings updated - deviceId={}", deviceId);
        return buildResponseWithTopics(device, "Settings updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PushDeviceRegistrationResponseDto getDevice(UUID deviceId) {
        PushDevice device = findDeviceById(deviceId);
        return buildResponseWithTopics(device, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PushDeviceRegistrationResponseDto getDeviceByToken(String fcmToken) {
        PushDevice device = deviceRepository.findByFcmToken(fcmToken)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "fcmToken", fcmToken));
        return buildResponseWithTopics(device, null);
    }

    // ========================================
    // Topic Subscriptions
    // ========================================

    @Override
    @Transactional
    public PushTopicSubscriptionResponseDto updateSubscriptions(UUID deviceId,
            PushTopicSubscriptionRequestDto request) {
        PushDevice device = findDeviceById(deviceId);

        List<String> subscribed = new ArrayList<>();
        List<String> unsubscribed = new ArrayList<>();
        List<PushTopicSubscriptionResponseDto.TopicOperationErrorDto> errors = new ArrayList<>();

        // Process subscriptions
        if (request.getSubscribe() != null) {
            for (String topic : request.getSubscribe()) {
                try {
                    subscribeToTopic(device, topic);
                    subscribed.add(topic);
                } catch (Exception e) {
                    errors.add(PushTopicSubscriptionResponseDto.TopicOperationErrorDto.builder()
                            .topic(topic)
                            .operation("subscribe")
                            .reason(e.getMessage())
                            .build());
                }
            }
        }

        // Process unsubscriptions
        if (request.getUnsubscribe() != null) {
            for (String topic : request.getUnsubscribe()) {
                try {
                    unsubscribeFromTopic(device, topic);
                    unsubscribed.add(topic);
                } catch (Exception e) {
                    errors.add(PushTopicSubscriptionResponseDto.TopicOperationErrorDto.builder()
                            .topic(topic)
                            .operation("unsubscribe")
                            .reason(e.getMessage())
                            .build());
                }
            }
        }

        List<String> currentSubscriptions = subscriptionRepository.findActiveTopicsByDeviceId(deviceId);

        log.info("PushDeviceService: Subscriptions updated - deviceId={}, subscribed={}, unsubscribed={}",
                deviceId, subscribed.size(), unsubscribed.size());

        return PushTopicSubscriptionResponseDto.builder()
                .subscribed(subscribed)
                .unsubscribed(unsubscribed)
                .errors(errors)
                .currentSubscriptions(currentSubscriptions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSubscriptions(UUID deviceId) {
        return subscriptionRepository.findActiveTopicsByDeviceId(deviceId);
    }

    @Override
    public List<PushAvailableTopicDto> getAvailableTopics() {
        // Use mapper to get predefined available topics
        return topicMapper.getAvailableTopics();
    }

    // ========================================
    // Activity Tracking
    // ========================================

    @Override
    @Transactional
    public void recordActivity(UUID deviceId) {
        deviceRepository.updateLastActiveAt(deviceId, Instant.now());
    }

    @Override
    @Transactional
    public void recordActivityByToken(String fcmToken) {
        deviceRepository.findByFcmToken(fcmToken)
                .ifPresent(device -> deviceRepository.updateLastActiveAt(device.getDeviceId(), Instant.now()));
    }

    // ========================================
    // Admin Operations
    // ========================================

    @Override
    @Transactional(readOnly = true)
    public List<PushDeviceRegistrationResponseDto> getDevicesForUser(UUID userId) {
        return deviceRepository.findActiveDevicesByUserId(userId).stream()
                .map(device -> buildResponseWithTopics(device, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceStatistics getDeviceStatistics() {
        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countActiveDevices();
        long androidDevices = deviceRepository.countByPlatform(PushDevicePlatform.ANDROID);
        long iosDevices = deviceRepository.countByPlatform(PushDevicePlatform.IOS);
        long webDevices = deviceRepository.countByPlatform(PushDevicePlatform.WEB);

        Map<String, Long> devicesByLanguage = deviceRepository.countDevicesByLanguage().stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]));

        return new DeviceStatistics(
                totalDevices,
                activeDevices,
                androidDevices,
                iosDevices,
                webDevices,
                devicesByLanguage);
    }

    @Override
    @Transactional
    public int cleanupStaleDevices(int daysInactive) {
        Instant cutoff = Instant.now().minus(daysInactive, ChronoUnit.DAYS);
        List<PushDevice> staleDevices = deviceRepository.findStaleDevices(cutoff);

        for (PushDevice device : staleDevices) {
            device.setIsActive(false);
            device.setDeletedAt(Instant.now());
            subscriptionRepository.deactivateAllForDevice(device.getDeviceId());
        }
        deviceRepository.saveAll(staleDevices);

        log.info("PushDeviceService: Cleaned up stale devices - count={}", staleDevices.size());
        return staleDevices.size();
    }

    // ========================================
    // Helper Methods
    // ========================================

    private PushDevice findDeviceById(UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "deviceId", deviceId.toString()));
    }

    private void subscribeToDefaultTopics(PushDevice device) {
        for (String topic : DEFAULT_TOPICS) {
            subscribeToTopic(device, topic);
        }

        // Subscribe to language topic
        if (device.getLanguage() != null) {
            subscribeToTopic(device, "language_" + device.getLanguage());
        }

        // Subscribe to infrastructure topic
        subscribeToTopic(device, "platform_" + device.getPlatform().name().toLowerCase());
    }

    private void subscribeToTopic(PushDevice device, String topic) {
        // Check if already subscribed
        if (subscriptionRepository.existsByDeviceIdAndTopicAndIsActiveTrue(device.getDeviceId(), topic)) {
            return;
        }

        // Subscribe with FCM
        PushFcmService.FcmOperationResult result = fcmService.subscribeToTopic(device.getFcmToken(), topic);

        // Create subscription record
        PushTopicSubscription subscription = PushTopicSubscription.builder()
                .deviceId(device.getDeviceId())
                .topic(topic)
                .topicDisplayName(topicMapper.getTopicDisplayName(topic))
                .topicCategory(topicMapper.getTopicCategory(topic))
                .isActive(true)
                .isSynced(result.isSuccess())
                .syncedAt(result.isSuccess() ? Instant.now() : null)
                .syncError(result.isSuccess() ? null : result.getErrorMessage())
                .build();

        subscriptionRepository.save(subscription);
    }

    private void unsubscribeFromTopic(PushDevice device, String topic) {
        fcmService.unsubscribeFromTopic(device.getFcmToken(), topic);
        subscriptionRepository.deactivateSubscription(device.getDeviceId(), topic);
    }

    private void updateTopicSubscription(PushDevice device, String topic, boolean subscribe) {
        if (subscribe) {
            subscribeToTopic(device, topic);
        } else {
            unsubscribeFromTopic(device, topic);
        }
    }

    private void resubscribeToTopics(PushDevice device, String oldToken) {
        List<String> topics = subscriptionRepository.findActiveTopicsByDeviceId(device.getDeviceId());

        // Unsubscribe old token
        for (String topic : topics) {
            fcmService.unsubscribeFromTopic(oldToken, topic);
        }

        // Subscribe new token
        for (String topic : topics) {
            PushFcmService.FcmOperationResult result = fcmService.subscribeToTopic(device.getFcmToken(), topic);
            if (result.isSuccess()) {
                subscriptionRepository.findByDeviceIdAndTopic(device.getDeviceId(), topic)
                        .ifPresent(sub -> subscriptionRepository.markAsSynced(sub.getSubscriptionId(), Instant.now()));
            }
        }
    }

    /**
     * Builds response using mapper with subscribed topics.
     * 
     * @param device  the device entity
     * @param message optional status message
     * @return the complete response DTO
     */
    private PushDeviceRegistrationResponseDto buildResponseWithTopics(PushDevice device, String message) {
        List<String> topics = subscriptionRepository.findActiveTopicsByDeviceId(device.getDeviceId());
        return deviceMapper.toResponseWithDetails(device, message, topics);
    }
}
