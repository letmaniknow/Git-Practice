package com.mmva.newsapp.infrastructure.push.mapper;

import com.mmva.newsapp.infrastructure.push.dto.PushDeviceRegistrationRequestDto;
import com.mmva.newsapp.infrastructure.push.dto.PushDeviceRegistrationResponseDto;
import com.mmva.newsapp.infrastructure.push.dto.PushDeviceRegistrationResponseDto.DeviceNotificationSettingsDto;
import com.mmva.newsapp.infrastructure.push.dto.PushDeviceSettingsUpdateRequestDto;
import com.mmva.newsapp.infrastructure.push.model.PushDevice;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

/**
 * MapStruct mapper for push device entities.
 * 
 * <p>
 * Handles conversions between PushDeviceEntity and DTOs for device
 * registration and settings management.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = {
        Instant.class })
public interface PushDeviceMapper {

    // ========================================
    // Request to Entity
    // ========================================

    /**
     * Creates a new device entity from registration request.
     * Sets default values for new devices.
     * 
     * @param request the registration request
     * @return new device entity
     */
    @Mapping(target = "deviceId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "lastActiveAt", expression = "java(Instant.now())")
    @Mapping(target = "failedDeliveryCount", constant = "0")
    @Mapping(target = "lastNotificationAt", ignore = true)
    @Mapping(target = "breakingNewsEnabled", constant = "true")
    @Mapping(target = "dailyDigestEnabled", constant = "false")
    @Mapping(target = "promotionalEnabled", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    PushDevice toEntity(PushDeviceRegistrationRequestDto request);

    /**
     * Updates existing device entity from registration request.
     * Only updates non-null values.
     * 
     * @param request the registration request
     * @param entity  the existing entity to update
     */
    @Mapping(target = "deviceId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "lastActiveAt", ignore = true)
    @Mapping(target = "failedDeliveryCount", ignore = true)
    @Mapping(target = "lastNotificationAt", ignore = true)
    @Mapping(target = "breakingNewsEnabled", ignore = true)
    @Mapping(target = "dailyDigestEnabled", ignore = true)
    @Mapping(target = "promotionalEnabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromRequest(PushDeviceRegistrationRequestDto request, @MappingTarget PushDevice entity);

    /**
     * Updates device entity settings from settings update request.
     * Only updates non-null values.
     * 
     * @param request the settings update request
     * @param entity  the existing entity to update
     */
    @Mapping(target = "deviceId", ignore = true)
    @Mapping(target = "fcmToken", ignore = true)
    @Mapping(target = "platform", ignore = true)
    @Mapping(target = "deviceFingerprint", ignore = true)
    @Mapping(target = "appVersion", ignore = true)
    @Mapping(target = "osVersion", ignore = true)
    @Mapping(target = "deviceModel", ignore = true)
    @Mapping(target = "deviceManufacturer", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "lastActiveAt", ignore = true)
    @Mapping(target = "failedDeliveryCount", ignore = true)
    @Mapping(target = "lastNotificationAt", ignore = true)
    @Mapping(target = "countryCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateSettingsFromRequest(PushDeviceSettingsUpdateRequestDto request, @MappingTarget PushDevice entity);

    // ========================================
    // Entity to Response
    // ========================================

    /**
     * Converts device entity to registration response.
     * 
     * @param entity the device entity
     * @return the response DTO
     */
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "subscribedTopics", ignore = true)
    @Mapping(target = "registeredAt", source = "createdAt")
    @Mapping(target = "settings", source = "entity")
    PushDeviceRegistrationResponseDto toResponse(PushDevice entity);

    /**
     * Maps device entity to notification settings.
     * 
     * @param entity the device entity
     * @return the settings DTO
     */
    @Mapping(target = "notificationsEnabled", source = "notificationsEnabled")
    @Mapping(target = "breakingNewsEnabled", source = "breakingNewsEnabled")
    @Mapping(target = "dailyDigestEnabled", source = "dailyDigestEnabled")
    @Mapping(target = "promotionalEnabled", source = "promotionalEnabled")
    DeviceNotificationSettingsDto toSettings(PushDevice entity);

    /**
     * Converts list of device entities to responses.
     * 
     * @param entities the device entities
     * @return list of response DTOs
     */
    List<PushDeviceRegistrationResponseDto> toResponseList(List<PushDevice> entities);

    // ========================================
    // Response with Additional Data
    // ========================================

    /**
     * Creates a complete response with message and subscribed topics.
     * This method should be used by services to build the full response.
     * 
     * @param entity           the device entity
     * @param message          optional status message
     * @param subscribedTopics list of subscribed topic names
     * @return the complete response DTO
     */
    default PushDeviceRegistrationResponseDto toResponseWithDetails(
            PushDevice entity,
            String message,
            List<String> subscribedTopics) {

        PushDeviceRegistrationResponseDto response = toResponse(entity);
        response.setMessage(message);
        response.setSubscribedTopics(subscribedTopics);
        return response;
    }

    // ========================================
    // After Mapping - Custom Logic
    // ========================================

    /**
     * Sets default language if null after mapping.
     * 
     * @param entity the mapped entity
     */
    @AfterMapping
    default void setDefaults(@MappingTarget PushDevice entity) {
        if (entity.getLanguage() == null) {
            entity.setLanguage("en");
        }
    }
}
