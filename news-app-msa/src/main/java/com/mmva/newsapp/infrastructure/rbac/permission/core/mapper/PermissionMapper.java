package com.mmva.newsapp.infrastructure.rbac.permission.core.mapper;

import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionRequestDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.dto.PermissionResponseDto;
import com.mmva.newsapp.infrastructure.rbac.permission.core.model.RbacPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
        PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

        @Mappings({
                        @Mapping(target = "permissionId", ignore = true),
                        @Mapping(target = "createdAt", ignore = true),
                        @Mapping(target = "updatedAt", ignore = true),
                        @Mapping(target = "createdBy", ignore = true),
                        @Mapping(target = "updatedBy", ignore = true),
                        @Mapping(target = "deletedAt", ignore = true),
                        @Mapping(target = "deletedBy", ignore = true)
        })
        RbacPermission toEntity(PermissionRequestDto dto);

        PermissionResponseDto toResponseDto(RbacPermission entity);
}
