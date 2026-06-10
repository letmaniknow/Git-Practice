package com.mmva.newsapp.domain.appuser.mapper.core;

import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileRequestDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileResponseDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileUpdateDto;
import com.mmva.newsapp.domain.appuser.model.core.AppUsers;
import org.mapstruct.*;

import java.time.Instant;

/**
 * MapStruct mapper for AppUserProfile entity.
 * 
 * <p>
 * Handles conversions between AppUserProfile entity and DTOs.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AppUserMapper {

    // ========================================
    // Entity → Response DTO
    // ========================================

    @Mappings({
            @Mapping(target = "appUsersLastLogin", expression = "java(instantToString(entity.getAppUsersLastLogin()))"),
            @Mapping(target = "appUsersPasswordUpdatedAt", expression = "java(instantToString(entity.getAppUsersPasswordUpdatedAt()))"),
            @Mapping(target = "createdAt", expression = "java(instantToString(entity.getCreatedAt()))"),
            @Mapping(target = "updatedAt", expression = "java(instantToString(entity.getUpdatedAt()))"),
            @Mapping(target = "appUsersLastActivityAt", expression = "java(instantToString(entity.getAppUsersLastActivityAt()))"),
            @Mapping(target = "appUsersLastPasswordResetAt", expression = "java(instantToString(entity.getAppUsersLastPasswordResetAt()))"),
            @Mapping(target = "appUsersAvatarUpdatedAt", expression = "java(instantToString(entity.getAppUsersAvatarUpdatedAt()))"),
            @Mapping(target = "appUsersGdprConsentDate", expression = "java(instantToString(entity.getAppUsersGdprConsentDate()))"),
            @Mapping(target = "appUsersGdprDataExportRequestedAt", expression = "java(instantToString(entity.getAppUsersGdprDataExportRequestedAt()))"),
            @Mapping(target = "appUsersGdprDataDeleteRequestedAt", expression = "java(instantToString(entity.getAppUsersGdprDataDeleteRequestedAt()))"),
            @Mapping(target = "deletedAt", expression = "java(instantToString(entity.getDeletedAt()))")
    })
    AppUserProfileResponseDto toResponseDto(AppUsers entity);

    // ========================================
    // Request DTO → Entity
    // ========================================

    @Mappings({
            @Mapping(target = "appUsersId", ignore = true),
            @Mapping(target = "appUsersPasswordHash", ignore = true),
            @Mapping(target = "appUsersPasswordHistory", ignore = true),
            @Mapping(target = "appUsersPasswordUpdatedAt", ignore = true),
            @Mapping(target = "appUsersPasswordResetCode", ignore = true),
            @Mapping(target = "appUsersPasswordResetExpiresAt", ignore = true),
            @Mapping(target = "appUsersFailedLoginAttempts", constant = "0"),
            @Mapping(target = "appUsersAccountLocked", ignore = true),
            @Mapping(target = "appUsersAccountLockedAt", ignore = true),
            @Mapping(target = "appUsersAccountLockExpiresAt", ignore = true),
            @Mapping(target = "appUsersLastLogin", ignore = true),
            @Mapping(target = "appUsersOauthProvider", ignore = true),
            @Mapping(target = "appUsersOauthProviderId", ignore = true),
            @Mapping(target = "appUsersOauthAccessToken", ignore = true),
            @Mapping(target = "appUsersOauthRefreshToken", ignore = true),
            @Mapping(target = "appUsersOauthTokenExpiresAt", ignore = true),
            @Mapping(target = "appUsersEmailVerificationCode", ignore = true),
            @Mapping(target = "appUsersEmailVerificationExpiresAt", ignore = true),
            @Mapping(target = "appUsersPhoneVerificationCode", ignore = true),
            @Mapping(target = "appUsersPhoneVerificationExpiresAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedBy", ignore = true),
            // Registration context fields (set by service layer during registration)
            @Mapping(target = "appUsersRegisteredCountryCode", ignore = true),
            @Mapping(target = "appUsersRegisteredCity", ignore = true),
            @Mapping(target = "appUsersRegisteredPlatform", ignore = true),
            @Mapping(target = "appUsersRegisteredDeviceType", ignore = true),
            @Mapping(target = "appUsersRegisteredBrowser", ignore = true),
            @Mapping(target = "appUsersRegisteredReferer", ignore = true),
            @Mapping(target = "appUsersRegisteredChannel", ignore = true),
            @Mapping(target = "appUsersRegisteredDeviceFingerprint", ignore = true),
            @Mapping(target = "appUsersDetectedLanguage", ignore = true),
            // Last login tracking fields (set by service layer during login)
            @Mapping(target = "appUsersLastLoginDeviceType", ignore = true),
            @Mapping(target = "appUsersLastLoginDeviceFingerprint", ignore = true),
            @Mapping(target = "appUsersLastActivityAt", expression = "java(stringToInstant(dto.getAppUsersLastActivityAt()))"),
            @Mapping(target = "appUsersLastPasswordResetAt", expression = "java(stringToInstant(dto.getAppUsersLastPasswordResetAt()))"),
            @Mapping(target = "appUsersAvatarUpdatedAt", ignore = true),
            @Mapping(target = "appUsersGdprConsentDate", expression = "java(stringToInstant(dto.getAppUsersGdprConsentDate()))"),
            @Mapping(target = "appUsersGdprDataExportRequestedAt", expression = "java(stringToInstant(dto.getAppUsersGdprDataExportRequestedAt()))"),
            @Mapping(target = "appUsersGdprDataDeleteRequestedAt", expression = "java(stringToInstant(dto.getAppUsersGdprDataDeleteRequestedAt()))"),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "appUsersMfaEnabled", ignore = true)
    })
    AppUsers toEntity(AppUserProfileRequestDto dto);

    // ========================================
    // Update Entity from DTO (null values ignored)
    // ========================================

    @Mappings({
            @Mapping(target = "appUsersId", ignore = true),
            @Mapping(target = "appUsersEmail", ignore = true),
            @Mapping(target = "appUsersPasswordHash", ignore = true),
            @Mapping(target = "appUsersPasswordHistory", ignore = true),
            @Mapping(target = "appUsersPasswordUpdatedAt", ignore = true),
            @Mapping(target = "appUsersPasswordResetCode", ignore = true),
            @Mapping(target = "appUsersPasswordResetExpiresAt", ignore = true),
            @Mapping(target = "appUsersFailedLoginAttempts", ignore = true),
            @Mapping(target = "appUsersAccountLocked", ignore = true),
            @Mapping(target = "appUsersAccountLockedAt", ignore = true),
            @Mapping(target = "appUsersAccountLockExpiresAt", ignore = true),
            @Mapping(target = "appUsersLastLogin", ignore = true),
            @Mapping(target = "appUsersAuthProvider", ignore = true),
            @Mapping(target = "appUsersOauthProvider", ignore = true),
            @Mapping(target = "appUsersOauthProviderId", ignore = true),
            @Mapping(target = "appUsersOauthAccessToken", ignore = true),
            @Mapping(target = "appUsersOauthRefreshToken", ignore = true),
            @Mapping(target = "appUsersOauthTokenExpiresAt", ignore = true),
            @Mapping(target = "appUsersIsAnonymous", ignore = true),
            @Mapping(target = "appUsersEmailVerificationCode", ignore = true),
            @Mapping(target = "appUsersEmailVerificationExpiresAt", ignore = true),
            @Mapping(target = "appUsersPhoneVerificationCode", ignore = true),
            @Mapping(target = "appUsersPhoneVerificationExpiresAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "updatedBy", ignore = true),
            @Mapping(target = "deletedBy", ignore = true),
            // Registration context fields (immutable after registration)
            @Mapping(target = "appUsersRegisteredCountryCode", ignore = true),
            @Mapping(target = "appUsersRegisteredCity", ignore = true),
            @Mapping(target = "appUsersRegisteredPlatform", ignore = true),
            @Mapping(target = "appUsersRegisteredDeviceType", ignore = true),
            @Mapping(target = "appUsersRegisteredBrowser", ignore = true),
            @Mapping(target = "appUsersRegisteredReferer", ignore = true),
            @Mapping(target = "appUsersRegisteredChannel", ignore = true),
            @Mapping(target = "appUsersRegisteredDeviceFingerprint", ignore = true),
            @Mapping(target = "appUsersDetectedLanguage", ignore = true),
            // Last login tracking fields (set by service layer during login)
            @Mapping(target = "appUsersLastLoginDeviceType", ignore = true),
            @Mapping(target = "appUsersLastLoginDeviceFingerprint", ignore = true),
            @Mapping(target = "appUsersLastActivityAt", expression = "java(stringToInstant(dto.getAppUsersLastActivityAt()))"),
            @Mapping(target = "appUsersLastPasswordResetAt", expression = "java(stringToInstant(dto.getAppUsersLastPasswordResetAt()))"),
            @Mapping(target = "appUsersAvatarUpdatedAt", ignore = true),
            @Mapping(target = "appUsersGdprConsentDate", expression = "java(stringToInstant(dto.getAppUsersGdprConsentDate()))"),
            @Mapping(target = "appUsersGdprDataExportRequestedAt", expression = "java(stringToInstant(dto.getAppUsersGdprDataExportRequestedAt()))"),
            @Mapping(target = "appUsersGdprDataDeleteRequestedAt", expression = "java(stringToInstant(dto.getAppUsersGdprDataDeleteRequestedAt()))"),
            @Mapping(target = "appUsersMfaEnabled", ignore = true),
            @Mapping(target = "deletedAt", ignore = true)
    })
    void updateEntityFromDto(AppUserProfileUpdateDto dto, @MappingTarget AppUsers entity);

    // ========================================
    // Helper Methods for Instant conversion
    // ========================================

    default String instantToString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    default Instant stringToInstant(String str) {
        return str != null ? Instant.parse(str) : null;
    }
}
