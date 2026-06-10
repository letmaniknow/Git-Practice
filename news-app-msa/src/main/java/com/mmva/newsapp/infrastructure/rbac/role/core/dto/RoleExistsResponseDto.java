package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for role name existence check")
public class RoleExistsResponseDto {

    @Schema(description = "The role name that was checked", example = "ADMIN")
    private String roleName;

    @Schema(description = "Whether the role name exists", example = "true")
    private Boolean exists;

    @Schema(description = "Message describing the result", example = "Role name 'ADMIN' already exists")
    private String message;
}
