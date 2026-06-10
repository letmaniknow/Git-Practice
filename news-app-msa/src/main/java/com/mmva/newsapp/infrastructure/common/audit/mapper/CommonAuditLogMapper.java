package com.mmva.newsapp.infrastructure.common.audit.mapper;

import com.mmva.newsapp.infrastructure.common.audit.dto.BaseAuditLogDto;
import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditLogEntity;
import com.mmva.newsapp.infrastructure.rbac.role.audit.model.RbacRoleAuditLog;
import com.mmva.newsapp.infrastructure.rbac.permission.audit.model.RbacPermissionAuditLog;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for BaseAuditLogEntity to BaseAuditLogDto.
 * 
 * <p>
 * Converts any audit log entity (RbacRoleAuditLog, RbacPermissionAuditLog,
 * NewsAuditLog, etc.)
 * to the common DTO. Handles all 24 audit fields and polymorphic
 * domain-specific fields.
 * </p>
 * 
 * <p>
 * Supports domain-specific field extraction:
 * - RbacRoleAuditLog → extracts roleId
 * - RbacPermissionAuditLog → extracts permissionId
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Component
public class CommonAuditLogMapper {

    /**
     * Convert BaseAuditLogEntity to BaseAuditLogDto with all 24 fields.
     * 
     * <p>
     * Handles polymorphic types:
     * - RbacRoleAuditLog: sets roleId
     * - RbacPermissionAuditLog: sets permissionId
     * - Future audits: add instanceof checks here
     * </p>
     * 
     * @param auditLog the audit entity to convert
     * @return the DTO with all fields populated, or null if input is null
     */
    public BaseAuditLogDto toDto(BaseAuditLogEntity auditLog) {
        if (auditLog == null) {
            return null;
        }

        UUID roleId = null;
        UUID permissionId = null;

        // Extract domain-specific fields based on concrete type
        if (auditLog instanceof RbacRoleAuditLog) {
            roleId = ((RbacRoleAuditLog) auditLog).getRoleId();
        } else if (auditLog instanceof RbacPermissionAuditLog) {
            permissionId = ((RbacPermissionAuditLog) auditLog).getPermissionId();
        }
        // Future: add more domain-specific types here
        // else if (auditLog instanceof NewsAuditLog) { ... }
        // else if (auditLog instanceof PushAuditLog) { ... }

        // Map all 24 fields
        return BaseAuditLogDto.builder()
                // WHO (2 fields)
                .actorId(auditLog.getActorId())
                .sessionId(auditLog.getSessionId())

                // WHAT (3 fields)
                .action(auditLog.getAction())
                .domain(auditLog.getDomain())
                .source(auditLog.getSource())

                // WHICH (4 fields)
                .resourceId(auditLog.getResourceId())
                .resourceName(auditLog.getResourceName())
                .roleId(roleId)
                .permissionId(permissionId)

                // WHEN (1 field)
                .createdAt(auditLog.getCreatedAt())

                // WHERE (3 fields)
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestUri(auditLog.getRequestUri())

                // WHY (2 fields)
                .reason(auditLog.getReason())
                .details(auditLog.getDetails())

                // HOW (5 fields)
                .isSuccess(auditLog.getIsSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .httpStatus(auditLog.getHttpStatus())
                .requestMethod(auditLog.getRequestMethod())
                .affectedRows(auditLog.getAffectedRows())

                // CORRELATE & RISK (3 fields)
                .transactionId(auditLog.getTransactionId())
                .severity(auditLog.getSeverity())
                .responseTimeMs(auditLog.getResponseTimeMs())

                .build();
    }
}
