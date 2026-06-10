package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for role actions")
public class RoleActionResponseDto {

    @Schema(description = "Role ID that was acted upon", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID roleId;

    @Schema(description = "Action performed", example = "ACTIVATE")
    private String action;

    @Schema(description = "Timestamp of the action")
    private String timestamp;

    @Schema(description = "User who performed the action")
    private UUID performedBy;
}
