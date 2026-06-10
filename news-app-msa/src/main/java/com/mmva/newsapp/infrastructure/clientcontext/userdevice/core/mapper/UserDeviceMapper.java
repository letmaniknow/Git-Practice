package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.mapper;

import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.dto.UserDeviceResponseDto;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.model.UserDevice;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for UserDevice entity.
 * 
 * <p>
 * Handles conversions between UserDevice entity and DTOs.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserDeviceMapper {

    // ========================================
    // Entity → Response DTO
    // ========================================

    /**
     * Maps UserDevice entity to response DTO.
     * 
     * @param device the device entity
     * @return the response DTO
     */
    @Mappings({
            @Mapping(target = "deviceType", expression = "java(device.getDeviceType() != null ? device.getDeviceType().name() : null)"),
            @Mapping(target = "channel", expression = "java(device.getChannel() != null ? device.getChannel().name() : null)"),
            @Mapping(target = "operatingSystem", expression = "java(formatOperatingSystem(device.getOsName(), device.getOsVersion()))"),
            @Mapping(target = "lastIpAddress", expression = "java(maskIpAddress(device.getLastIpAddress()))"),
            @Mapping(target = "isCurrentDevice", constant = "false")
    })
    UserDeviceResponseDto toResponseDto(UserDevice device);

    /**
     * Maps a list of UserDevice entities to response DTOs.
     * 
     * @param devices the list of device entities
     * @return the list of response DTOs
     */
    List<UserDeviceResponseDto> toResponseDtoList(List<UserDevice> devices);

    // ========================================
    // Entity → Response DTO (with current device check)
    // ========================================

    /**
     * Maps UserDevice entity to response DTO with current device identification.
     * 
     * @param device             the device entity
     * @param currentFingerprint the current session's device fingerprint
     * @return the response DTO with isCurrentDevice set appropriately
     */
    default UserDeviceResponseDto toResponseDto(UserDevice device, String currentFingerprint) {
        UserDeviceResponseDto dto = toResponseDto(device);
        if (dto != null) {
            boolean isCurrent = device.getDeviceFingerprint() != null
                    && device.getDeviceFingerprint().equals(currentFingerprint);
            dto.setIsCurrentDevice(isCurrent);
        }
        return dto;
    }

    /**
     * Maps a list of UserDevice entities to response DTOs with current device
     * identification.
     * 
     * @param devices            the list of device entities
     * @param currentFingerprint the current session's device fingerprint
     * @return the list of response DTOs
     */
    default List<UserDeviceResponseDto> toResponseDtoList(List<UserDevice> devices, String currentFingerprint) {
        return devices.stream()
                .map(device -> toResponseDto(device, currentFingerprint))
                .toList();
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Formats operating system name and version.
     * 
     * @param osName    the OS name
     * @param osVersion the OS version
     * @return formatted OS string (e.g., "iOS 17", "Windows 11")
     */
    default String formatOperatingSystem(String osName, String osVersion) {
        if (osName == null) {
            return null;
        }
        if (osVersion != null) {
            return osName + " " + osVersion;
        }
        return osName;
    }

    /**
     * Masks IP address for privacy display.
     * 
     * <p>
     * Examples:
     * <ul>
     * <li>IPv4: "192.168.1.100" → "192.168.1.xxx"</li>
     * <li>IPv6: "2001:0db8:85a3:0000:0000:8a2e:0370:7334" →
     * "2001:0db8:85a3:xxxx:xxxx:xxxx"</li>
     * </ul>
     * </p>
     * 
     * @param ipAddress the original IP address
     * @return the masked IP address
     */
    default String maskIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return null;
        }
        // For IPv4
        if (ipAddress.contains(".")) {
            String[] parts = ipAddress.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".xxx";
            }
        }
        // For IPv6, show first half
        if (ipAddress.contains(":")) {
            String[] parts = ipAddress.split(":");
            if (parts.length >= 4) {
                return parts[0] + ":" + parts[1] + ":" + parts[2] + ":xxxx:xxxx:xxxx";
            }
        }
        return ipAddress.substring(0, Math.min(ipAddress.length() / 2, 10)) + "...";
    }
}
