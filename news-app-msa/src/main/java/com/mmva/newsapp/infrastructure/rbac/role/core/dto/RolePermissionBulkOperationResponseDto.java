package com.mmva.newsapp.infrastructure.rbac.role.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for bulk permission operations on a role.
 * 
 * <p>
 * Used when multiple permissions are assigned or removed from a role
 * in a single operation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for bulk permission operations on a role")
public class RolePermissionBulkOperationResponseDto {

    @Schema(description = "Role ID that was acted upon", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID roleId;

    @Schema(description = "Action performed", example = "BULK_ASSIGN")
    private String action;

    @Schema(description = "Number of permissions affected")
    private Integer permissionsCount;

    @Schema(description = "List of permission IDs affected")
    private List<UUID> permissionIds;

    @Schema(description = "Timestamp of the action")
    private String timestamp;

    @Schema(description = "User who performed the action")
    private UUID performedBy;
}
