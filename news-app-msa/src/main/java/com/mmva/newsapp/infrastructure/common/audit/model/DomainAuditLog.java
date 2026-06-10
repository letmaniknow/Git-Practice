package com.mmva.newsapp.infrastructure.common.audit.model;

import java.util.UUID;

/**
 * Interface for audit log entities with domain-specific fields.
 * 
 * <p>
 * Implementing classes (RbacRoleAuditLog, NewsAuditLog, PushAuditLog, etc.)
 * define their own feature-specific fields (roleId, newsId, pushCampaignId,
 * etc.)
 * and implement this method to populate the primary domain field.
 * </p>
 * 
 * <p>
 * This eliminates manual if-else logic in AuditingUtility and makes the system
 * extensible: adding a new feature audit table requires only implementing this
 * interface, no changes to AuditingUtility.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public interface DomainAuditLog {

    /**
     * Sets the domain-specific field for this audit log entity.
     * 
     * <p>
     * Examples:
     * <ul>
     * <li>RbacRoleAuditLog: sets roleId</li>
     * <li>NewsAuditLog: sets newsId</li>
     * <li>PushAuditLog: sets pushCampaignId</li>
     * </ul>
     * </p>
     * 
     * @param resourceId the UUID of the resource that was affected
     */
    void setDomainSpecificField(UUID resourceId);
}
