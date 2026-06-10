package com.mmva.newsapp.infrastructure.push.mapper;

import com.mmva.newsapp.infrastructure.push.dto.PushNotificationResponseDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationResponseDto.DeliveryStatsDto;
import com.mmva.newsapp.infrastructure.push.dto.PushNotificationSendRequestDto;
import com.mmva.newsapp.infrastructure.push.enums.PushNotificationStatus;
import com.mmva.newsapp.infrastructure.push.model.PushNotification;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper for push notification entities.
 * 
 * <p>
 * Handles conversions between PushNotificationEntity and DTOs for
 * notification sending and viewing.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = {
        Instant.class, UUID.class, PushNotificationStatus.class })
public interface PushNotificationMapper {

    // ========================================
    // Request to Entity
    // ========================================

    /**
     * Creates a new notification entity from send request.
     * Sets default values and initial status.
     * 
     * @param request the send notification request
     * @return new notification entity
     */
    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "idempotencyKey", ignore = true)
    @Mapping(target = "status", expression = "java(PushNotificationStatus.PENDING)")
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "fcmMessageId", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "nextRetryAt", ignore = true)
    @Mapping(target = "sentCount", constant = "0")
    @Mapping(target = "deliveredCount", constant = "0")
    @Mapping(target = "failedCount", constant = "0")
    @Mapping(target = "openedCount", constant = "0")
    @Mapping(target = "payload", ignore = true)
    @Mapping(target = "targetValue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    PushNotification toEntity(PushNotificationSendRequestDto request);

    // ========================================
    // Entity to Response
    // ========================================

    /**
     * Converts notification entity to response DTO.
     * 
     * @param entity the notification entity
     * @return the response DTO
     */
    @Mapping(target = "stats", source = "entity")
    PushNotificationResponseDto toResponse(PushNotification entity);

    /**
     * Maps entity stats to DeliveryStatsDto.
     * 
     * @param entity the notification entity
     * @return delivery stats DTO
     */
    @Mapping(target = "deliveryRate", expression = "java(calculateDeliveryRate(entity))")
    @Mapping(target = "openRate", expression = "java(calculateOpenRate(entity))")
    DeliveryStatsDto toDeliveryStats(PushNotification entity);

    /**
     * Converts list of notification entities to responses.
     * 
     * @param entities the notification entities
     * @return list of response DTOs
     */
    List<PushNotificationResponseDto> toResponseList(List<PushNotification> entities);

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Calculates delivery rate as percentage.
     * 
     * @param entity the notification entity
     * @return delivery rate percentage (0-100)
     */
    default Double calculateDeliveryRate(PushNotification entity) {
        Integer sent = entity.getSentCount();
        if (sent == null || sent == 0) {
            return 0.0;
        }
        int delivered = entity.getDeliveredCount() != null ? entity.getDeliveredCount() : 0;
        return (delivered * 100.0) / sent;
    }

    /**
     * Calculates open rate as percentage of delivered.
     * 
     * @param entity the notification entity
     * @return open rate percentage (0-100)
     */
    default Double calculateOpenRate(PushNotification entity) {
        int delivered = entity.getDeliveredCount() != null ? entity.getDeliveredCount() : 0;
        if (delivered == 0) {
            return 0.0;
        }
        int opened = entity.getOpenedCount() != null ? entity.getOpenedCount() : 0;
        return (opened * 100.0) / delivered;
    }
}
