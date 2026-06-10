package com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for renaming a device request.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceRenameRequestDto {

    /**
     * Custom name for the device.
     * Examples: "My Work Laptop", "John's iPhone", "Living Room TV"
     */
    @NotBlank(message = "Device name is required")
    @Size(min = 1, max = 100, message = "Device name must be between 1 and 100 characters")
    private String deviceName;
}
