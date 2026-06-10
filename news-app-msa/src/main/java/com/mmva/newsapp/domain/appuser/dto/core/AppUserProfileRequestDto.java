package com.mmva.newsapp.domain.appuser.dto.core;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AppUserProfileRequestDto {
    @Schema(description = "Username (auto-generated if not provided)", example = "john_doe")
    @Size(max = 100, message = "Username must be at most 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Username can only contain letters, numbers, and underscores")
    private String appUsersUsername;

    @Schema(description = "User email address", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String appUsersEmail;

    @Schema(description = "User phone number", example = "+1234567890")
    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String appUsersPhoneNumber;

    @Schema(description = "Password", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character")
    private String appUsersPassword;

    @Schema(description = "First name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String appUsersFirstName;

    @Schema(description = "Last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String appUsersLastName;

    @Schema(description = "Full name (auto-generated from first + last name if not provided)", example = "John Doe")
    @Size(max = 255)
    private String appUsersFullName;

    @Schema(description = "Terms and conditions accepted", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean appUsersTermsAccepted;

    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String appUsersAvatarUrl;

    @Schema(description = "Preferred language", example = "en")
    private String appUsersPreferredLanguage;

    @Schema(description = "Timezone", example = "America/New_York")
    private String appUsersTimezone;

    @Builder.Default
    @Schema(description = "Are notifications enabled?", example = "true")
    private Boolean appUsersNotificationEnabled = true;

    @Schema(description = "Theme", example = "dark")
    private String appUsersTheme;

    @Schema(description = "Authentication provider", example = "local")
    private String appUsersAuthProvider;

    @Builder.Default
    @Schema(description = "Is anonymous user?", example = "false")
    private Boolean appUsersIsAnonymous = false;

    @Schema(description = "Device ID", example = "device-1234")
    private String appUsersDeviceId;

    @Builder.Default
    @Schema(description = "User status", example = "ACTIVE")
    private AppUserStatus appUsersStatus = AppUserStatus.ACTIVE;

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

    // phoneNumber removed, use userPhoneNumber
    @Schema(description = "Is email verified?", example = "true")
    private Boolean appUsersEmailVerified;

    @Schema(description = "Is phone verified?", example = "false")
    private Boolean appUsersPhoneVerified;

    @Schema(description = "Account type", example = "standard")
    private String appUsersAccountType;

    @Schema(description = "Referral code", example = "REF12345")
    private String appUsersReferralCode;

    @Schema(description = "Marketing opt-in", example = "true")
    private Boolean appUsersMarketingOptIn;

    @Schema(description = "Privacy settings (JSON)", example = "{\"shareProfile\":true}")
    private String appUsersPrivacySettings;

    @Schema(description = "External ID", example = "external-abc-123")
    private String appUsersExternalId;

    @Schema(description = "Is profile completed?", example = "true")
    private Boolean appUsersProfileCompleted;

    @Schema(description = "Last activity timestamp", example = "2024-06-01T13:00:00Z")
    private String appUsersLastActivityAt;

    @Schema(description = "Language preferences", example = "en,es")
    private String appUsersLanguagePreferences;

    @Schema(description = "Consent version", example = "v1.0")
    private String appUsersConsentVersion;

    @Schema(description = "Is multi-factor enabled?", example = "false")
    private Boolean appUsersMultiFactorEnabled;

    @Schema(description = "Social links (JSON)", example = "{\"twitter\":\"@john\"}")
    private String appUsersSocialLinks;

    @Schema(description = "Custom attributes (JSON)", example = "{\"attr1\":\"value1\"}")
    private String appUsersCustomAttributes;

    @Schema(description = "Risk score", example = "5")
    private Integer appUsersRiskScore;

    @Schema(description = "Last password reset at", example = "2024-06-01T12:34:56Z")
    private String appUsersLastPasswordResetAt;

    @Schema(description = "Profile picture updated at", example = "2024-06-01T12:34:56Z")
    private String appUsersProfilePictureUpdatedAt;

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
    private String appUsersDeletedAt;
}
