package com.mmva.newsapp.infrastructure.common.audit.service;

import com.mmva.newsapp.infrastructure.common.audit.model.BaseAuditLogEntity;
import com.mmva.newsapp.infrastructure.common.audit.model.DomainAuditLog;
import com.mmva.newsapp.infrastructure.common.audit.repository.UnifiedAuditLogRepository;
import com.mmva.newsapp.infrastructure.requestanalytics.dto.RequestClientInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Centralized Audit Logging Utility - ONE Method for ALL Features.
 * Builds the correct entity type (RbacRoleAuditLog, NewsAuditLog, etc.) based
 * on repository type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditingUtility {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressWarnings("unchecked")
    public <T extends BaseAuditLogEntity> void audit(
            String domain,
            UUID actorId,
            String action,
            UUID resourceId,
            String resourceName,
            String reason,
            RequestClientInfoDto clientInfo,
            String severity,
            Class<T> entityClass,
            UnifiedAuditLogRepository<T> repository) {

        try {
            log.info("🔍 [AUDIT-START] domain={}, action={}, actorId={}, resourceId={}, entity={}",
                    domain, action, actorId, resourceId, entityClass.getSimpleName());

            // Extract clientInfo safely (record accessors)
            String ipAddress = clientInfo != null ? clientInfo.ipAddress() : null;
            String userAgent = clientInfo != null ? clientInfo.userAgent() : null;
            String requestUri = clientInfo != null ? clientInfo.requestUri() : null;
            String requestMethod = clientInfo != null ? clientInfo.requestMethod() : null;

            log.debug("📋 [CLIENT-INFO] ip={}, ua={}, uri={}, method={}",
                    ipAddress, userAgent, requestUri, requestMethod);

            log.info("📦 [ENTITY-CLASS] using: {}", entityClass.getSimpleName());

            // Create instance using no-arg constructor (requires @NoArgsConstructor on
            // entity)
            T auditLog = entityClass.getDeclaredConstructor().newInstance();
            log.debug("✨ [INSTANCE-CREATED] entity type: {}", entityClass.getName());

            // Set all 24 fields directly via setters (from Lombok @Data/@SuperBuilder)
            auditLog.setActorId(actorId);
            auditLog.setAction(action);
            auditLog.setDomain(domain);
            auditLog.setSource("REST_API");
            auditLog.setResourceId(resourceId);
            auditLog.setResourceName(resourceName);
            auditLog.setReason(reason);
            auditLog.setDetails("");
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setRequestUri(requestUri != null ? requestUri : "/api");
            auditLog.setIsSuccess(true);
            auditLog.setErrorMessage(null);
            auditLog.setHttpStatus(200);
            auditLog.setRequestMethod(requestMethod != null ? requestMethod : "POST");
            auditLog.setAffectedRows(1);
            auditLog.setTransactionId(UUID.randomUUID());
            auditLog.setSeverity(severity);
            auditLog.setResponseTimeMs(0L);
            auditLog.setCreatedAt(Instant.now());

            // Set domain-specific field if entity implements DomainAuditLog
            if (auditLog instanceof DomainAuditLog) {
                ((DomainAuditLog) auditLog).setDomainSpecificField(resourceId);
                log.debug("🎯 [DOMAIN-FIELD-SET] {} for resourceId={}",
                        entityClass.getSimpleName(), resourceId);
            }

            log.debug("📝 [FIELDS-POPULATED] All audit fields set successfully");

            // Persist to feature-specific repository (polymorphic - correct table)
            T saved = repository.save(auditLog);
            log.info("✅ [AUDIT-SAVED] entity saved to repository for table: {}",
                    entityClass.getAnnotation(jakarta.persistence.Table.class).name());
            log.debug("💾 [SAVED-ENTITY] id: {}, domain: {}, action: {}",
                    saved, domain, action);

        } catch (Exception e) {
            log.error("❌ [AUDIT-FAILED] domain={}, action={}, error_msg={}, error_type={}",
                    domain, action, e.getMessage(), e.getClass().getSimpleName());
            log.error("[STACK-TRACE]", e);
        }
    }
}
