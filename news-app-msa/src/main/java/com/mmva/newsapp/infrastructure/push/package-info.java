/**
 * Push notification feature - manages device registration, topic subscriptions,
 * and notification delivery via Firebase Cloud Messaging (FCM).
 * 
 * <h2>Package Structure:</h2>
 * 
 * <pre>
 * com.mmva.newsapp.infrastructure.push/
 * ├── model/
 * │   ├── PushDevice.java                    # Device registration
 * │   ├── PushNotification.java              # Notification log
 * │   ├── PushNotificationDelivery.java      # Per-device delivery
 * │   ├── PushTopicSubscription.java         # Topic subscriptions
 * │   └── PushNotificationAuditLog.java      # Admin audit trail
 * ├── repository/
 * │   ├── PushDeviceRepository.java
 * │   ├── PushNotificationRepository.java
 * │   ├── PushNotificationDeliveryRepository.java
 * │   ├── PushTopicSubscriptionRepository.java
 * │   └── PushNotificationAuditLogRepository.java
 * ├── dto/
 * │   ├── PushDeviceRegistrationRequestDto.java
 * │   ├── PushDeviceRegistrationResponseDto.java
 * │   ├── PushDeviceSettingsUpdateRequestDto.java
 * │   ├── PushNotificationSendRequestDto.java
 * │   ├── PushNotificationResponseDto.java
 * │   ├── PushTopicSubscriptionRequestDto.java
 * │   ├── PushTopicSubscriptionResponseDto.java
 * │   └── PushAvailableTopicDto.java
 * ├── mapper/
 * │   ├── PushDeviceMapper.java
 * │   ├── PushNotificationMapper.java
 * │   └── PushTopicSubscriptionMapper.java
 * ├── enums/
 * │   ├── PushDevicePlatform.java            # Device OS type
 * │   ├── PushNotificationType.java          # Notification categories
 * │   ├── PushNotificationStatus.java        # Delivery lifecycle
 * │   ├── PushNotificationPriority.java      # FCM priority
 * │   ├── PushNotificationTargetType.java    # Target selection
 * │   ├── PushNotificationDeliveryStatus.java # Per-device status
 * │   └── PushTopicCategory.java             # Topic grouping
 * ├── exception/
 * │   ├── PushDeviceNotFoundException.java
 * │   ├── PushNotificationNotFoundException.java
 * │   └── PushTopicSubscriptionNotFoundException.java
 * └── service/
 *     ├── PushDeviceService.java             # Device management
 *     ├── PushDeviceServiceImpl.java
 *     ├── PushNotificationService.java       # Notification sending
 *     ├── PushNotificationServiceImpl.java
 *     ├── PushNotificationAuditLogService.java # Audit logging
 *     ├── PushFcmService.java                # FCM integration
 *     ├── PushFcmServiceImpl.java
 *     └── PushFcmServiceMock.java            # Mock for testing
 * </pre>
 * 
 * <h2>Key Entities:</h2>
 * <ul>
 * <li><b>PushDevice</b> - Device registration with FCM token</li>
 * <li><b>PushNotification</b> - Notification log and status</li>
 * <li><b>PushNotificationDelivery</b> - Per-device delivery tracking</li>
 * <li><b>PushTopicSubscription</b> - Device-to-topic subscriptions</li>
 * </ul>
 * 
 * <h2>Controllers:</h2>
 * <ul>
 * <li>{@code controller/publicunauthenticated/PublicPushController} - Device
 * registration</li>
 * <li>{@code controller/admindashboard/push/AdminPushController} - Admin
 * send/manage</li>
 * </ul>
 * 
 * <h2>Design Philosophy:</h2>
 * <p>
 * Device-centric approach: The primary entity is the DEVICE, not the user.
 * This allows anonymous push notifications and device-user association is
 * optional.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 * @see com.mmva.newsapp.infrastructure.push.model.PushDevice
 * @see com.mmva.newsapp.infrastructure.push.service.PushNotificationService
 */
package com.mmva.newsapp.infrastructure.push;
