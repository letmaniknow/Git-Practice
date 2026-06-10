package com.mmva.newsapp.domain.appuser.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserProfileResponseDto {
    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID appUsersId;
    @Schema(description = "User email address", example = "john@example.com")
    private String appUsersEmail;
    @Schema(description = "Username", example = "john_doe")
    private String appUsersUsername;
    @Schema(description = "User phone number", example = "+1234567890")
    private String appUsersPhoneNumber;
    @Schema(description = "First name", example = "John")
    private String appUsersFirstName;
    @Schema(description = "Last name", example = "Doe")
    private String appUsersLastName;
    @Schema(description = "Full name", example = "John Doe")
    private String appUsersFullName;
    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String appUsersAvatarUrl;
    @Schema(description = "Preferred language", example = "en")
    private String appUsersPreferredLanguage;
    @Schema(description = "Timezone", example = "America/New_York")
    private String appUsersTimezone;
    @Schema(description = "Are notifications enabled?", example = "true")
    private Boolean appUsersNotificationEnabled;
    @Schema(description = "Theme", example = "dark")
    private String appUsersTheme;
    @Schema(description = "Last login timestamp", example = "2024-06-01T12:34:56Z")
    private String appUsersLastLogin;
    @Schema(description = "Failed login attempts", example = "0")
    private Integer appUsersFailedLoginAttempts;
    @Schema(description = "Password last updated at", example = "2024-06-01T12:34:56Z")
    private String appUsersPasswordUpdatedAt;
    @Schema(description = "Authentication provider", example = "local")
    private String appUsersAuthProvider;
    @Schema(description = "Is anonymous user?", example = "false")
    private Boolean appUsersIsAnonymous;
    @Schema(description = "Device ID", example = "device-1234")
    private String appUsersDeviceId;
    @Schema(description = "User status", example = "ACTIVE")
    private AppUserStatus appUsersStatus;
    @Schema(description = "Account created at", example = "2024-06-01T12:34:56Z")
    private String createdAt;
    @Schema(description = "Account updated at", example = "2024-06-01T12:34:56Z")
    private String updatedAt;
    @Schema(description = "Country code", example = "US")
    private String appUsersCountryCode;
    @Schema(description = "Last login country code", example = "US")
    private String appUsersLastLoginCountryCode;
    @Schema(description = "Install country code", example = "US")
    private String appUsersInstallCountryCode;
    @Schema(description = "Registered IP address", example = "192.168.1.1")
    private String appUsersRegisteredIpAddress;
    @Schema(description = "Registered user agent", example = "Mozilla/5.0 ...")
    private String appUsersRegisteredUserAgent;
    @Schema(description = "Last login IP address", example = "192.168.1.2")
    private String appUsersLastLoginIpAddress;
    @Schema(description = "Last login user agent", example = "Mozilla/5.0 ...")
    private String appUsersLastLoginUserAgent;
    @Schema(description = "Installed IP address", example = "192.168.1.3")
    private String appUsersInstalledIpAddress;
    @Schema(description = "Installed user agent", example = "Mozilla/5.0 ...")
    private String appUsersInstalledUserAgent;
    @Schema(description = "Date of birth (YYYY-MM-DD)", example = "1990-01-01")
    private String appUsersDateOfBirth;
    @Schema(description = "Gender", example = "male")
    private String appUsersGender;
    @Schema(description = "Is email verified?", example = "true")
    private Boolean appUsersEmailVerified;
    @Schema(description = "Is phone verified?", example = "true")
    private Boolean appUsersPhoneVerified;
    @Schema(description = "Account type", example = "standard")
    private String appUsersAccountType;
    @Schema(description = "Referral code", example = "REF123")
    private String appUsersReferralCode;
    @Schema(description = "Marketing opt-in", example = "true")
    private Boolean appUsersMarketingOptIn;
    @Schema(description = "Privacy settings (JSON)", example = "{\"shareProfile\":false}")
    private String appUsersPrivacySettings;
    @Schema(description = "External ID", example = "external-1234")
    private String appUsersExternalId;
    @Schema(description = "Is profile completed?", example = "true")
    private Boolean appUsersProfileCompleted;
    @Schema(description = "Last activity timestamp", example = "2024-06-01T13:00:00Z")
    private String appUsersLastActivityAt;
    @Schema(description = "Language preferences", example = "en,es")
    private String appUsersLanguagePreferences;
    @Schema(description = "Consent version", example = "v1.0")
    private String appUsersConsentVersion;
    @Schema(description = "Is multi-factor enabled?", example = "true")
    private Boolean appUsersMfaEnabled;
    @Schema(description = "Social links (JSON)", example = "{\"twitter\":\"@john\"}")
    private String appUsersSocialLinks;
    @Schema(description = "Custom attributes (JSON)", example = "{\"custom1\":\"value\"}")
    private String appUsersCustomAttributes;
    @Schema(description = "Risk score", example = "5")
    private Integer appUsersRiskScore;
    @Schema(description = "Last password reset at", example = "2024-06-01T12:34:56Z")
    private String appUsersLastPasswordResetAt;
    @Schema(description = "Avatar updated at", example = "2024-06-01T12:34:56Z")
    private String appUsersAvatarUpdatedAt;
    @Schema(description = "Preferred currency", example = "USD")
    private String appUsersPreferredCurrency;
    @Schema(description = "Segment", example = "premium")
    private String appUsersSegment;
    @Schema(description = "GDPR consent given?", example = "true")
    private Boolean appUsersGdprConsentGiven;
    @Schema(description = "GDPR consent date", example = "2024-06-01")
    private String appUsersGdprConsentDate;
    @Schema(description = "GDPR consent version", example = "v1.0")
    private String appUsersGdprConsentVersion;
    @Schema(description = "GDPR data export requested at", example = "2024-06-01T12:34:56Z")
    private String appUsersGdprDataExportRequestedAt;
    @Schema(description = "GDPR data delete requested at", example = "2024-06-01T12:34:56Z")
    private String appUsersGdprDataDeleteRequestedAt;
    @Schema(description = "Deleted at timestamp", example = "2024-06-01T12:34:56Z")
    private String deletedAt;
}
