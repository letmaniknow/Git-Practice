package com.mmva.newsapp.infrastructure.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating device notification settings.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushDeviceSettingsUpdateRequestDto {

    /**
     * Master toggle for notifications.
     */
    private Boolean notificationsEnabled;

    /**
     * Breaking news alerts.
     */
    private Boolean breakingNewsEnabled;

    /**
     * Daily digest notifications.
     */
    private Boolean dailyDigestEnabled;

    /**
     * Promotional notifications.
     */
    private Boolean promotionalEnabled;

    /**
     * Preferred language for notifications.
     */
    private String language;

    /**
     * Timezone for scheduled notifications.
     */
    private String timezone;
}
