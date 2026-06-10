package com.mmva.newsapp.domain.adminuser.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for admindashboard user audit log responses.
 * 
 * <p>
 * Table: admin_users_audit_log
 * </p>
 * <p>
 * Naming convention: {tableName}_{fieldName}
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserAuditLogDto {

    private Long id;
    private UUID adminUserId;
    private String action;
    private String details;
    private UUID createdBy;
    private String createdAt;
}
