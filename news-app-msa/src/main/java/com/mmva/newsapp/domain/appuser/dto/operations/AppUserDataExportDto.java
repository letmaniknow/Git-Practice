package com.mmva.newsapp.domain.appuser.dto.operations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for GDPR-compliant user data export.
 * Contains all personal data associated with a user profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUserDataExportDto {

    // Export metadata
    private String exportDate;
    private String exportVersion;
    private UUID appUsersId;

    // Personal Information
    private PersonalInfo personalInfo;

    // Account Information
    private AccountInfo accountInfo;

    // Contact Information
    private ContactInfo contactInfo;

    // Preferences
    private Preferences preferences;

    // Security Information (excluding sensitive data)
    private SecurityInfo securityInfo;

    // Location & Device Data
    private LocationDeviceInfo locationDeviceInfo;

    // GDPR Consent Information
    private GdprInfo gdprInfo;

    // Audit Trail (user actions)
    private List<AuditEntry> auditTrail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PersonalInfo {
        private String appUsersFirstName;
        private String appUsersLastName;
        private String appUsersFullName;
        private String appUsersDateOfBirth;
        private String appUsersGender;
        private String appUsersAvatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountInfo {
        private String appUsersUsername;
        private String appUsersAccountType;
        private AppUserStatus appUsersStatus;
        private String appUsersSegment;
        private Boolean appUsersIsAnonymous;
        private Boolean appUsersProfileCompleted;
        private String appUsersCreatedAt;
        private String appUsersUpdatedAt;
        private String appUsersLastLogin;
        private String appUsersLastActivityAt;
        private String appUsersReferralCode;
        private String appUsersExternalId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactInfo {
        private String appUsersEmail;
        private Boolean appUsersEmailVerified;
        private String appUsersPhoneNumber;
        private Boolean appUsersPhoneVerified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Preferences {
        private String appUsersPreferredLanguage;
        private String appUsersLanguagePreferences;
        private String appUsersTimezone;
        private String appUsersPreferredCurrency;
        private String appUsersTheme;
        private Boolean appUsersNotificationEnabled;
        private Boolean appUsersMarketingOptIn;
        private String appUsersPrivacySettings;
        private String appUsersSocialLinks;
        private String appUsersCustomAttributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SecurityInfo {
        private String appUsersAuthProvider;
        private Boolean appUsersMultiFactorEnabled;
        private String appUsersPasswordUpdatedAt;
        private String appUsersLastPasswordResetAt;
        private Integer appUsersRiskScore;
        // Note: Password hash is NOT included for security
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LocationDeviceInfo {
        private String appUsersCountryCode;
        private String appUsersInstallCountryCode;
        private String appUsersLastLoginCountryCode;
        private String appUsersDeviceId;
        private String appUsersRegisteredIpAddress;
        private String appUsersRegisteredUserAgent;
        private String appUsersLastLoginIpAddress;
        private String appUsersLastLoginUserAgent;
        private String appUsersInstalledIpAddress;
        private String appUsersInstalledUserAgent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GdprInfo {
        private Boolean appUsersGdprConsentGiven;
        private String appUsersGdprConsentDate;
        private String appUsersGdprConsentVersion;
        private String appUsersConsentVersion;
        private String appUsersGdprDataExportRequestedAt;
        private String appUsersGdprDataDeleteRequestedAt;
        private String appUsersDeletedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuditEntry {
        private String appUsersAction;
        private String appUsersDetails;
        private String appUsersCreatedAt;
    }
}
