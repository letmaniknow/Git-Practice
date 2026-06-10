package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for blocking a device request.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceBlockRequestDto {

    /**
     * Reason for blocking the device.
     * Examples: "Stolen device", "Lost device", "Suspicious activity", "Not my
     * device"
     */
    @NotBlank(message = "Block reason is required")
    @Size(min = 3, max = 255, message = "Block reason must be between 3 and 255 characters")
    private String reason;
}
