package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for bulk role activation/deactivation")
public class BulkRoleActionRequestDto {

    @NotEmpty(message = "Role IDs list cannot be empty")
    @Schema(description = "List of role IDs to activate or deactivate")
    private List<UUID> roleIds;

    @Schema(description = "Admin user performing the bulk operation (set by server from JWT)", hidden = true)
    private UUID adminId;
}
