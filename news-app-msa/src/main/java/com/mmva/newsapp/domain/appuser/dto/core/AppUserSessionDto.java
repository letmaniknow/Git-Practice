package com.mmva.newsapp.domain.appuser.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user session information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserSessionDto {

    @Schema(description = "Session ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID appUsersSessionId;

    @Schema(description = "Device information", example = "Chrome on Windows 10")
    private String appUsersDeviceInfo;

    @Schema(description = "IP address of the session", example = "192.168.1.1")
    private String appUsersIpAddress;

    @Schema(description = "Location of the session", example = "New York, USA")
    private String appUsersLocation;

    @Schema(description = "Session creation timestamp (ISO 8601)", example = "2024-06-01T12:34:56Z")
    private String appUsersSessionCreatedAt;

    @Schema(description = "Last activity timestamp (ISO 8601)", example = "2024-06-01T13:00:00Z")
    private String appUsersLastActivityAt;

    @Schema(description = "Is this the current session?", example = "true")
    private boolean appUsersIsCurrent;
}
