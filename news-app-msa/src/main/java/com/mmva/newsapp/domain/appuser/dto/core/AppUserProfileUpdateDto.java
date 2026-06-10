package com.mmva.newsapp.domain.appuser.dto.core;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserProfileUpdateDto {
    @Schema(description = "Username", example = "john_doe")
    @Size(max = 100, message = "Username must be at most 100 characters")
    private String appUsersUsername;

    @Schema(description = "User email address", example = "john@example.com")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String appUsersEmail;

    @Schema(description = "First name", example = "John")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String appUsersFirstName;

    @Schema(description = "Last name", example = "Doe")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String appUsersLastName;

    @Schema(description = "Full name", example = "John Doe")
    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String appUsersFullName;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    @Size(max = 500, message = "Avatar URL must be at most 500 characters")
    private String appUsersAvatarUrl;

    @Schema(description = "Preferred language", example = "en")
    @Size(max = 10, message = "Preferred language must be at most 10 characters")
    private String appUsersPreferredLanguage;

    @Schema(description = "Timezone", example = "America/New_York")
    @Size(max = 100, message = "Timezone must be at most 100 characters")
    private String appUsersTimezone;

    @Schema(description = "Are notifications enabled for the user", example = "true")
    private Boolean appUsersNotificationEnabled;

    @Schema(description = "UI theme preference", example = "dark")
    @Size(max = 20, message = "Theme must be at most 20 characters")
    private String appUsersTheme;

    @Schema(description = "Device ID", example = "device-12345")
    @Size(max = 255, message = "Device ID must be at most 255 characters")
    private String appUsersDeviceId;

    @Schema(description = "Country code", example = "US")
    @Size(max = 3, message = "Country code must be at most 3 characters")
    private String appUsersCountryCode;

    @Schema(description = "Last login country code", example = "US")
    @Size(max = 3, message = "Country code must be at most 3 characters")
    private String appUsersLastLoginCountryCode;

    @Schema(description = "Install country code", example = "US")
    @Size(max = 3, message = "Country code must be at most 3 characters")
    private String appUsersInstallCountryCode;

    @Schema(description = "Registered IP address", example = "192.168.1.1")
    @Size(max = 45, message = "IP address must be at most 45 characters")
    private String appUsersRegisteredIpAddress;

    @Schema(description = "Registered user agent", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    @Size(max = 255)
    private String appUsersRegisteredUserAgent;

    @Schema(description = "Last login IP address", example = "192.168.1.2")
    @Size(max = 45)
    private String appUsersLastLoginIpAddress;

    @Schema(description = "Last login user agent", example = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
    @Size(max = 255)
    private String appUsersLastLoginUserAgent;

    @Schema(description = "Installed IP address", example = "192.168.1.3")
    @Size(max = 45)
    private String appUsersInstalledIpAddress;

    @Schema(description = "Installed user agent", example = "Mozilla/5.0 (Linux; Android 10)")
    @Size(max = 255)
    private String appUsersInstalledUserAgent;

    @Schema(description = "Date of birth (YYYY-MM-DD)", example = "1990-01-01")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    private String appUsersDateOfBirth;

    @Schema(description = "Gender", example = "male")
    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String appUsersGender;

    @Schema(description = "User phone number", example = "+1234567890")
    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String appUsersPhoneNumber;

    // These should not be updatable directly by user
    // emailVerified and phoneVerified should only be set through verification flow
    @Schema(description = "Is email verified?", example = "true")
    private Boolean appUsersEmailVerified;

    @Schema(description = "Is phone verified?", example = "false")
    private Boolean appUsersPhoneVerified;

    @Schema(description = "Account type", example = "standard")
    @Size(max = 50, message = "Account type must be at most 50 characters")
    private String appUsersAccountType;

    @Schema(description = "Referral code", example = "REF12345")
    @Size(max = 50, message = "Referral code must be at most 50 characters")
    private String appUsersReferralCode;

    @Schema(description = "Marketing opt-in status", example = "true")
    private Boolean appUsersMarketingOptIn;

    @Schema(description = "Privacy settings (JSON string)", example = "{\"shareProfile\":true}")
    private String appUsersPrivacySettings;

    @Schema(description = "External ID", example = "external-abc-123")
    @Size(max = 255, message = "External ID must be at most 255 characters")
    private String appUsersExternalId;

    @Schema(description = "Is profile completed?", example = "true")
    private Boolean appUsersProfileCompleted;

    @Schema(description = "Last activity timestamp (ISO 8601)", example = "2024-06-01T13:00:00Z")
    private String appUsersLastActivityAt;

    @Schema(description = "Language preferences (comma-separated)", example = "en,es")
    private String appUsersLanguagePreferences;

    @Schema(description = "Consent version", example = "v1.0")
    @Size(max = 20, message = "Consent version must be at most 20 characters")
    private String appUsersConsentVersion;

    @Schema(description = "Is multi-factor authentication enabled?", example = "false")
    private Boolean appUsersMultiFactorEnabled;

    @Schema(description = "Social links (JSON string)", example = "{\"twitter\":\"@john\"}")
    private String appUsersSocialLinks;

    @Schema(description = "Custom attributes (JSON string)", example = "{\"attr1\":\"value1\"}")
    private String appUsersCustomAttributes;

    @Schema(description = "Risk score", example = "5")
    private Integer appUsersRiskScore;

    @Schema(description = "Last password reset timestamp (ISO 8601)", example = "2024-06-01T14:00:00Z")
    private String appUsersLastPasswordResetAt;

    @Schema(description = "Profile picture updated timestamp (ISO 8601)", example = "2024-06-01T15:00:00Z")
    private String appUsersProfilePictureUpdatedAt;

    @Schema(description = "Preferred currency code", example = "USD")
    @Size(max = 10, message = "Currency code must be at most 10 characters")
    private String appUsersPreferredCurrency;

    @Schema(description = "Status (ACTIVE, INACTIVE, SUSPENDED, PENDING, DELETED, BANNED)", example = "ACTIVE")
    private AppUserStatus appUsersStatus;

    @Schema(description = "Segment", example = "premium")
    @Size(max = 50, message = "Segment must be at most 50 characters")
    private String appUsersSegment;

    // GDPR fields
    @Schema(description = "GDPR consent given?", example = "true")
    private Boolean appUsersGdprConsentGiven;

    @Schema(description = "GDPR consent date (ISO 8601)", example = "2024-06-01T16:00:00Z")
    private String appUsersGdprConsentDate;

    @Schema(description = "GDPR consent version", example = "v2.0")
    @Size(max = 20)
    private String appUsersGdprConsentVersion;

    @Schema(description = "GDPR data export requested at (ISO 8601)", example = "2024-06-01T17:00:00Z")
    private String appUsersGdprDataExportRequestedAt;

    @Schema(description = "GDPR data delete requested at (ISO 8601)", example = "2024-06-01T18:00:00Z")
    private String appUsersGdprDataDeleteRequestedAt;

    // Should not be settable by regular users
    @Schema(description = "Deleted at timestamp (ISO 8601)", example = "2024-06-01T19:00:00Z")
    private String appUsersDeletedAt;
}
