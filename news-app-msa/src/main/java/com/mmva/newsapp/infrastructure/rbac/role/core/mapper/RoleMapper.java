package com.mmva.newsapp.infrastructure.rbac.role.core.mapper;

import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleRequestDto;
import com.mmva.newsapp.infrastructure.rbac.role.core.dto.RoleResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.mapper.PermissionMapper;
import com.mmva.newsapp.infrastructure.rbac.role.core.model.RbacRole;
import org.mapstruct.*;

/**
 * MapStruct mapper for Role entity.
 * 
 * <p>
 * Handles conversions between Role entity and DTOs.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = { PermissionMapper.class })
public interface RoleMapper {

        // ========================================
        // Entity → Response DTO
        // ========================================

        @Mappings({
                        @Mapping(target = "deletedAt", expression = "java(entity.getDeletedAt() != null ? entity.getDeletedAt().toString() : null)"),
                        @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null)"),
                        @Mapping(target = "updatedAt", expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null)")
        })
        RoleResponseDto toResponseDto(RbacRole entity);

        // ========================================
        // Request DTO → Entity
        // ========================================

        @Mappings({
                        @Mapping(target = "roleId", ignore = true),
                        @Mapping(target = "isActive", defaultValue = "true"),
                        @Mapping(target = "permissions", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        RbacRole toEntity(RoleRequestDto dto);

        // ========================================
        // Update Entity from DTO
        // ========================================

        @Mappings({
                        @Mapping(target = "roleId", ignore = true),
                        @Mapping(target = "permissions", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        void updateEntityFromDto(RoleRequestDto dto, @MappingTarget RbacRole entity);
}
