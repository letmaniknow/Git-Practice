package com.mmva.newsapp.domain.adminuser.mapper;

import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserRequestDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserResponseDto;
import com.mmva.newsapp.domain.adminuser.dto.core.AdminUserUpdateDto;
import com.mmva.newsapp.domain.adminuser.model.core.AdminUser;
import org.mapstruct.*;

/**
 * MapStruct mapper for AdminUser entity.
 * 
 * <p>
 * Handles conversions between AdminUser entity and DTOs.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AdminUserMapper {

        // ========================================
        // Entity → Response DTO
        // ========================================

        @Mappings({
                        @Mapping(target = "adminUsersRoleId", expression = "java(entity.getRole() != null ? entity.getRole().getRoleId() : null)"),
                        @Mapping(target = "adminUsersRoleName", expression = "java(entity.getRole() != null ? entity.getRole().getRoleName() : null)"),
                        @Mapping(target = "adminUsersEmailVerificationExpiresAt", expression = "java(entity.getAdminUsersEmailVerificationExpiresAt() != null ? entity.getAdminUsersEmailVerificationExpiresAt().toString() : null)"),
                        @Mapping(target = "adminUsersAccountLockExpiresAt", expression = "java(entity.getAdminUsersAccountLockExpiresAt() != null ? entity.getAdminUsersAccountLockExpiresAt().toString() : null)"),
                        @Mapping(target = "adminUsersLastLogin", expression = "java(entity.getAdminUsersLastLogin() != null ? entity.getAdminUsersLastLogin().toString() : null)"),
                        @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)"),
                        @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)"),
                        @Mapping(target = "deletedAt", expression = "java(entity.getDeletedAt() != null ? entity.getDeletedAt().toString() : null)")
        })
        AdminUserResponseDto toResponseDto(AdminUser entity);

        // ========================================
        // Request DTO → Entity
        // ========================================

        @Mappings({
                        @Mapping(target = "adminUsersId", ignore = true),
                        @Mapping(target = "role", ignore = true),
                        @Mapping(target = "adminUsersPasswordHash", ignore = true),
                        @Mapping(target = "adminUsersPasswordSalt", ignore = true),
                        @Mapping(target = "adminUsersLastPasswordChangeAt", ignore = true),
                        @Mapping(target = "adminUsersResetPasswordToken", ignore = true),
                        @Mapping(target = "adminUsersResetPasswordExpiresAt", ignore = true),
                        @Mapping(target = "adminUsersFailedLoginAttempts", constant = "0"),
                        @Mapping(target = "adminUsersAccountLocked", constant = "false"),
                        @Mapping(target = "adminUsersAccountLockExpiresAt", ignore = true),
                        @Mapping(target = "adminUsersMfaEnabled", constant = "false"),
                        @Mapping(target = "adminUsersMfaSecret", ignore = true),
                        @Mapping(target = "adminUsersEmailVerified", constant = "false"),
                        @Mapping(target = "adminUsersEmailVerificationCode", ignore = true),
                        @Mapping(target = "adminUsersEmailVerificationExpiresAt", ignore = true),
                        @Mapping(target = "adminUsersLastLogin", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        AdminUser toEntity(AdminUserRequestDto dto);

        // ========================================
        // Update Entity from DTO (null values ignored)
        // ========================================

        @Mappings({
                        @Mapping(target = "adminUsersId", ignore = true),
                        @Mapping(target = "role", ignore = true),
                        @Mapping(target = "adminUsersPasswordHash", ignore = true),
                        @Mapping(target = "adminUsersPasswordSalt", ignore = true),
                        @Mapping(target = "adminUsersLastPasswordChangeAt", ignore = true),
                        @Mapping(target = "adminUsersResetPasswordToken", ignore = true),
                        @Mapping(target = "adminUsersResetPasswordExpiresAt", ignore = true),
                        @Mapping(target = "adminUsersFailedLoginAttempts", ignore = true),
                        @Mapping(target = "adminUsersAccountLockExpiresAt", ignore = true),
                        @Mapping(target = "adminUsersMfaEnabled", ignore = true),
                        @Mapping(target = "adminUsersMfaSecret", ignore = true),
                        @Mapping(target = "adminUsersEmailVerified", ignore = true),
                        @Mapping(target = "adminUsersEmailVerificationCode", ignore = true),
                        @Mapping(target = "adminUsersEmailVerificationExpiresAt", ignore = true),
                        @Mapping(target = "adminUsersLastLogin", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        void updateEntityFromDto(AdminUserUpdateDto dto, @MappingTarget AdminUser entity);
}
