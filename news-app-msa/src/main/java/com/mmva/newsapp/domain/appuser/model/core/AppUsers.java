package com.mmva.newsapp.domain.appuser.model.core;

import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Public/Customer user profile entity.
 * 
 * <p>
 * Represents end-users of the newsapp infrastructure (readers, commenters,
 * etc.).
 * Maps to the {@code app_users} table.
 * </p>
 * 
 * <p>
 * Soft-delete filtering is handled via {@code SoftDeleteSpec} in repository
 * queries.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "app_users", indexes = {
        @Index(name = "idx_app_users_deleted_at", columnList = "deleted_at"),
        @Index(name = "idx_app_users_email", columnList = "app_users_email"),
        @Index(name = "idx_app_users_status", columnList = "app_users_status"),
        @Index(name = "idx_app_users_phone_number", columnList = "app_users_phone_number")
})
public class AppUsers extends BaseAuditEntity {

    // ========================================
    // Primary Key
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "app_users_id", nullable = false, updatable = false)
    private UUID appUsersId;

    // ========================================
    // Identity Fields
    // ========================================

    @Column(name = "app_users_email", unique = true, length = 255, nullable = false)
    private String appUsersEmail;

    @Column(name = "app_users_username", length = 255)
    private String appUsersUsername;

    // ========================================
    // Name Fields
    // ========================================

    @Column(name = "app_users_first_name", length = 100)
    private String appUsersFirstName;

    @Column(name = "app_users_last_name", length = 100)
    private String appUsersLastName;

    @Column(name = "app_users_full_name", length = 255)
    private String appUsersFullName;

    // ========================================
    // Contact Information
    // ========================================

    @Column(name = "app_users_phone_number", unique = true, length = 20, nullable = false)
    private String appUsersPhoneNumber;

    @Column(name = "app_users_avatar_url", length = 2000)
    private String appUsersAvatarUrl;

    // ========================================
    // Personal Info
    // ========================================

    @Column(name = "app_users_date_of_birth", length = 10)
    private String appUsersDateOfBirth;

    @Column(name = "app_users_gender", length = 20)
    private String appUsersGender;

    // ========================================
    // Credentials & Security
    // ========================================

    @Column(name = "app_users_password_hash")
    private String appUsersPasswordHash;

    @Column(name = "app_users_password_updated_at")
    private Instant appUsersPasswordUpdatedAt;

    @Column(name = "app_users_password_history")
    private String appUsersPasswordHistory;

    @Column(name = "app_users_password_reset_code", length = 10)
    private String appUsersPasswordResetCode;

    @Column(name = "app_users_password_reset_expires_at")
    private Instant appUsersPasswordResetExpiresAt;

    @Column(name = "app_users_last_password_reset_at")
    private Instant appUsersLastPasswordResetAt;

    // ========================================
    // Account Lockout
    // ========================================

    @Column(name = "app_users_failed_login_attempts")
    private Integer appUsersFailedLoginAttempts = 0;

    @Column(name = "app_users_account_locked")
    private Boolean appUsersAccountLocked = false;

    @Column(name = "app_users_account_locked_at")
    private Instant appUsersAccountLockedAt;

    @Column(name = "app_users_account_lock_expires_at")
    private Instant appUsersAccountLockExpiresAt;

    // ========================================
    // Email Verification
    // ========================================

    @Column(name = "app_users_email_verified")
    private Boolean appUsersEmailVerified;

    @Column(name = "app_users_email_verification_code", length = 10)
    private String appUsersEmailVerificationCode;

    @Column(name = "app_users_email_verification_expires_at")
    private Instant appUsersEmailVerificationExpiresAt;

    // ========================================
    // Phone Verification
    // ========================================

    @Column(name = "app_users_phone_verified")
    private Boolean appUsersPhoneVerified;

    @Column(name = "app_users_phone_verification_code", length = 10)
    private String appUsersPhoneVerificationCode;

    @Column(name = "app_users_phone_verification_expires_at")
    private Instant appUsersPhoneVerificationExpiresAt;

    // ========================================
    // Multi-Factor Authentication
    // ========================================

    @Column(name = "app_users_mfa_enabled")
    private Boolean appUsersMfaEnabled;

    // ========================================
    // Status & Account Type
    // ========================================

    @Enumerated(EnumType.STRING)
    @Column(name = "app_users_status", length = 50, nullable = false)
    private AppUserStatus appUsersStatus = AppUserStatus.ACTIVE;

    @Column(name = "app_users_account_type", length = 50)
    private String appUsersAccountType;

    @Column(name = "app_users_segment", length = 50)
    private String appUsersSegment;

    @Column(name = "app_users_profile_completed")
    private Boolean appUsersProfileCompleted;

    // ========================================
    // Activity Tracking
    // ========================================

    @Column(name = "app_users_last_login")
    private Instant appUsersLastLogin;

    @Column(name = "app_users_last_activity_at")
    private Instant appUsersLastActivityAt;

    @Column(name = "app_users_avatar_updated_at")
    private Instant appUsersAvatarUpdatedAt;

    // ========================================
    // Localization & Preferences
    // ========================================

    @Column(name = "app_users_preferred_language", length = 10)
    private String appUsersPreferredLanguage;

    @Column(name = "app_users_timezone", length = 100)
    private String appUsersTimezone;

    @Column(name = "app_users_preferred_currency", length = 10)
    private String appUsersPreferredCurrency;

    @Column(name = "app_users_theme", length = 10)
    private String appUsersTheme;

    @Column(name = "app_users_language_preferences")
    private String appUsersLanguagePreferences;

    @Column(name = "app_users_notification_enabled")
    private Boolean appUsersNotificationEnabled = true;

    // ========================================
    // Auth Provider & OAuth
    // ========================================

    @Column(name = "app_users_auth_provider", length = 100)
    private String appUsersAuthProvider;

    @Column(name = "app_users_oauth_provider", length = 50)
    private String appUsersOauthProvider;

    @Column(name = "app_users_oauth_provider_id", length = 255)
    private String appUsersOauthProviderId;

    @Column(name = "app_users_oauth_access_token", length = 2000)
    private String appUsersOauthAccessToken;

    @Column(name = "app_users_oauth_refresh_token", length = 2000)
    private String appUsersOauthRefreshToken;

    @Column(name = "app_users_oauth_token_expires_at")
    private Instant appUsersOauthTokenExpiresAt;

    // ========================================
    // Anonymous & Device Info
    // ========================================

    @Column(name = "app_users_is_anonymous")
    private Boolean appUsersIsAnonymous = false;

    @Column(name = "app_users_device_id", length = 255)
    private String appUsersDeviceId;

    @Column(name = "app_users_external_id", length = 255)
    private String appUsersExternalId;

    // ========================================
    // Location & Device Tracking
    // ========================================

    @Column(name = "app_users_country_code", length = 3)
    private String appUsersCountryCode;

    @Column(name = "app_users_registered_ip_address", length = 45)
    private String appUsersRegisteredIpAddress;

    @Column(name = "app_users_registered_user_agent", length = 512)
    private String appUsersRegisteredUserAgent;

    @Column(name = "app_users_registered_country_code", length = 3)
    private String appUsersRegisteredCountryCode;

    @Column(name = "app_users_registered_city", length = 100)
    private String appUsersRegisteredCity;

    @Column(name = "app_users_registered_platform", length = 50)
    private String appUsersRegisteredPlatform;

    @Column(name = "app_users_registered_device_type", length = 20)
    private String appUsersRegisteredDeviceType;

    @Column(name = "app_users_registered_browser", length = 50)
    private String appUsersRegisteredBrowser;

    @Column(name = "app_users_registered_referer", length = 2048)
    private String appUsersRegisteredReferer;

    @Column(name = "app_users_registered_channel", length = 20)
    private String appUsersRegisteredChannel;

    @Column(name = "app_users_registered_device_fingerprint", length = 64)
    private String appUsersRegisteredDeviceFingerprint;

    @Column(name = "app_users_detected_language", length = 10)
    private String appUsersDetectedLanguage;

    @Column(name = "app_users_install_country_code", length = 3)
    private String appUsersInstallCountryCode;

    @Column(name = "app_users_installed_ip_address", length = 45)
    private String appUsersInstalledIpAddress;

    @Column(name = "app_users_installed_user_agent", length = 512)
    private String appUsersInstalledUserAgent;

    @Column(name = "app_users_last_login_country_code", length = 3)
    private String appUsersLastLoginCountryCode;

    @Column(name = "app_users_last_login_ip_address", length = 45)
    private String appUsersLastLoginIpAddress;

    @Column(name = "app_users_last_login_user_agent", length = 512)
    private String appUsersLastLoginUserAgent;

    @Column(name = "app_users_last_login_device_type", length = 20)
    private String appUsersLastLoginDeviceType;

    @Column(name = "app_users_last_login_device_fingerprint", length = 64)
    private String appUsersLastLoginDeviceFingerprint;

    // ========================================
    // Marketing & Referral
    // ========================================

    @Column(name = "app_users_referral_code", length = 50)
    private String appUsersReferralCode;

    @Column(name = "app_users_marketing_opt_in")
    private Boolean appUsersMarketingOptIn;

    // ========================================
    // GDPR Compliance
    // ========================================

    @Column(name = "app_users_gdpr_consent_given")
    private Boolean appUsersGdprConsentGiven;

    @Column(name = "app_users_gdpr_consent_date")
    private Instant appUsersGdprConsentDate;

    @Column(name = "app_users_gdpr_consent_version", length = 20)
    private String appUsersGdprConsentVersion;

    @Column(name = "app_users_gdpr_data_export_requested_at")
    private Instant appUsersGdprDataExportRequestedAt;

    @Column(name = "app_users_gdpr_data_delete_requested_at")
    private Instant appUsersGdprDataDeleteRequestedAt;

    @Column(name = "app_users_consent_version", length = 20)
    private String appUsersConsentVersion;

    // ========================================
    // Privacy & Custom Settings
    // ========================================

    @Column(name = "app_users_privacy_settings")
    private String appUsersPrivacySettings;

    @Column(name = "app_users_custom_attributes")
    private String appUsersCustomAttributes;

    // ========================================
    // Social & Analytics
    // ========================================

    @Column(name = "app_users_social_links")
    private String appUsersSocialLinks;

    @Column(name = "app_users_risk_score")
    private Integer appUsersRiskScore;
}
