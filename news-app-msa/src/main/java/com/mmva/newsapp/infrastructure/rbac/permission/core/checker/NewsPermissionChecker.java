package com.mmva.newsapp.infrastructure.rbac.permission.core.checker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity;
import com.mmva.newsapp.domain.news.model.core.NewsMasterEntity.WorkflowStatus;
import com.mmva.newsapp.infrastructure.rbac.config.PermissionConfigService;
import com.mmva.newsapp.infrastructure.rbac.RbacPermissionConstants;
import com.mmva.newsapp.infrastructure.security.userdetails.AdminUserDetails;

/**
 * NewsPermissionChecker: Centralized permission validation for news operations
 * 
 * Purpose:
 * - Validate permissions for news CRUD operations
 * - Check both permission AND context (status, ownership, role)
 * - Eliminate duplicate permission checks across controllers
 * - Provide clear, reusable permission checking methods
 * 
 * Architecture:
 * - Inject into AdminNewsController
 * - Call before performing sensitive operations
 * - Returns boolean for controller to decide response
 * - Logs all permission checks for audit trail
 * 
 * Patterns:
 * - canUser{Action}(user, article) - Always validate context + permission
 * - throw IllegalArgumentException for null parameters
 * - Log at DEBUG level for success, WARN level for failures
 */
@Slf4j
@Service
public class NewsPermissionChecker {

    private final PermissionConfigService permissionConfigService;

    public NewsPermissionChecker(PermissionConfigService permissionConfigService) {
        this.permissionConfigService = permissionConfigService;
    }

    /**
     * Check if user can publish a news article
     * 
     * Business Rules:
     * - User must have NEWS_PUBLISH permission
     * - Article must be in DRAFT status (can only publish drafts)
     * - Context: Publishing means moving from DRAFT to PUBLISHED
     * 
     * @param user    AdminUserDetails (must have roles and permissions)
     * @param article NewsMasterEntity to publish
     * @return true if user can publish this article
     * @throws IllegalArgumentException if user or article is null
     */
    public boolean canUserPublish(AdminUserDetails user, NewsMasterEntity article) {
        if (user == null) {
            log.warn("canUserPublish called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        if (article == null) {
            log.warn("canUserPublish called with null article by user: {}", user.getAdminId());
            throw new IllegalArgumentException("Article cannot be null");
        }

        // Check if user has NEWS_PUBLISH permission
        boolean hasPermission = permissionConfigService.hasPermission(user, RbacPermissionConstants.NEWS_PUBLISH);

        // Check article status - can only publish DRAFT articles
        boolean isDraft = WorkflowStatus.DRAFT.equals(article.getNewsWorkflowStatus());

        boolean result = hasPermission && isDraft;

        log.debug("canUserPublish check for user {} on article {}: permission={}, isDraft={}, result={}",
                user.getAdminId(), article.getNewsNewsId(), hasPermission, isDraft, result);

        if (!result) {
            if (!hasPermission) {
                log.warn("User {} lacks NEWS_PUBLISH permission", user.getAdminId());
            }
            if (!isDraft) {
                log.warn("Article {} is not DRAFT (status: {}), cannot publish",
                        article.getNewsNewsId(), article.getNewsWorkflowStatus());
            }
        }

        return result;
    }

    /**
     * Check if user can soft delete a news article
     * 
     * Business Rules:
     * - User must have NEWS_DELETE_SOFT permission
     * - Article must not already be deleted
     * - Soft delete: Mark as deleted but keep in DB
     * 
     * @param user    AdminUserDetails
     * @param article NewsMasterEntity to delete
     * @return true if user can soft delete this article
     * @throws IllegalArgumentException if user or article is null
     */
    public boolean canUserSoftDelete(AdminUserDetails user, NewsMasterEntity article) {
        if (user == null) {
            log.warn("canUserSoftDelete called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        if (article == null) {
            log.warn("canUserSoftDelete called with null article by user: {}", user.getAdminId());
            throw new IllegalArgumentException("Article cannot be null");
        }

        // Check if user has NEWS_DELETE permission
        boolean hasPermission = permissionConfigService.hasPermission(user, RbacPermissionConstants.NEWS_DELETE);

        // Check article is not already deleted
        boolean notAlreadyDeleted = article.getDeletedAt() == null;

        boolean result = hasPermission && notAlreadyDeleted;

        log.debug("canUserSoftDelete check for user {} on article {}: permission={}, notDeleted={}, result={}",
                user.getAdminId(), article.getNewsNewsId(), hasPermission, notAlreadyDeleted, result);

        if (!result) {
            if (!hasPermission) {
                log.warn("User {} lacks NEWS_DELETE_SOFT permission", user.getAdminId());
            }
            if (!notAlreadyDeleted) {
                log.warn("Article {} is already deleted", article.getNewsNewsId());
            }
        }

        return result;
    }

    /**
     * Check if user can permanently delete a news article (hard delete)
     * 
     * Business Rules:
     * - User must have NEWS_DELETE_PERMANENT permission
     * - Only SUPER_ADMIN typically has this permission
     * - Hard delete: Remove completely from database
     * 
     * @param user AdminUserDetails
     * @return true if user can permanently delete
     * @throws IllegalArgumentException if user is null
     */
    public boolean canUserPermanentlyDelete(AdminUserDetails user) {
        if (user == null) {
            log.warn("canUserPermanentlyDelete called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        boolean hasPermission = permissionConfigService.hasPermission(user,
                RbacPermissionConstants.NEWS_DELETE);

        log.debug("canUserPermanentlyDelete check for user {}: permission={}", user.getAdminId(), hasPermission);

        if (!hasPermission) {
            log.warn("User {} lacks NEWS_DELETE_PERMANENT permission", user.getAdminId());
        }

        return hasPermission;
    }

    /**
     * Check if user can reschedule/schedule a news article
     * 
     * Business Rules:
     * - User must have NEWS_RESCHEDULE permission
     * - Article must be in DRAFT or SCHEDULED status
     * - Can reschedule SCHEDULED articles to new time
     * - Can schedule DRAFT articles for future publishing
     * 
     * @param user    AdminUserDetails
     * @param article NewsMasterEntity to reschedule
     * @return true if user can reschedule this article
     * @throws IllegalArgumentException if user or article is null
     */
    public boolean canUserReschedule(AdminUserDetails user, NewsMasterEntity article) {
        if (user == null) {
            log.warn("canUserReschedule called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        if (article == null) {
            log.warn("canUserReschedule called with null article by user: {}", user.getAdminId());
            throw new IllegalArgumentException("Article cannot be null");
        }

        // Check if user has NEWS_PUBLISH permission (for rescheduling)
        boolean hasPermission = permissionConfigService.hasPermission(user, RbacPermissionConstants.NEWS_PUBLISH);

        // Check article status - can reschedule DRAFT or SCHEDULED articles
        WorkflowStatus status = article.getNewsWorkflowStatus();
        boolean canReschedule = WorkflowStatus.DRAFT.equals(status) || WorkflowStatus.SCHEDULED.equals(status);

        boolean result = hasPermission && canReschedule;

        log.debug("canUserReschedule check for user {} on article {}: permission={}, canReschedule={}, result={}",
                user.getAdminId(), article.getNewsNewsId(), hasPermission, canReschedule, result);

        if (!result) {
            if (!hasPermission) {
                log.warn("User {} lacks NEWS_RESCHEDULE permission", user.getAdminId());
            }
            if (!canReschedule) {
                log.warn("Article {} cannot be rescheduled (status: {}, only DRAFT or SCHEDULED allowed)",
                        article.getNewsNewsId(), status);
            }
        }

        return result;
    }

    /**
     * Check if user can edit a news article
     * 
     * Business Rules:
     * - User must have NEWS_UPDATE permission
     * - Article must not be PUBLISHED or ARCHIVED
     * - Can edit DRAFT, SUBMITTED, REVIEWED, APPROVED, SCHEDULED
     * - Published content requires new revision, not edit
     * 
     * @param user    AdminUserDetails
     * @param article NewsMasterEntity to edit
     * @return true if user can edit this article
     * @throws IllegalArgumentException if user or article is null
     */
    public boolean canUserEditArticle(AdminUserDetails user, NewsMasterEntity article) {
        if (user == null) {
            log.warn("canUserEditArticle called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        if (article == null) {
            log.warn("canUserEditArticle called with null article by user: {}", user.getAdminId());
            throw new IllegalArgumentException("Article cannot be null");
        }

        // Check if user has NEWS_UPDATE permission
        boolean hasPermission = permissionConfigService.hasPermission(user, RbacPermissionConstants.NEWS_UPDATE);

        // Check article status - cannot edit PUBLISHED
        WorkflowStatus status = article.getNewsWorkflowStatus();
        boolean canEdit = !WorkflowStatus.PUBLISHED.equals(status);

        boolean result = hasPermission && canEdit;

        log.debug("canUserEditArticle check for user {} on article {}: permission={}, canEdit={}, result={}",
                user.getAdminId(), article.getNewsNewsId(), hasPermission, canEdit, result);

        if (!result) {
            if (!hasPermission) {
                log.warn("User {} lacks NEWS_UPDATE permission", user.getAdminId());
            }
            if (!canEdit) {
                log.warn("Article {} cannot be edited (status: {}, PUBLISHED and ARCHIVED cannot be edited)",
                        article.getNewsNewsId(), status);
            }
        }

        return result;
    }

    /**
     * Check if user can view audit history for an article
     * 
     * Business Rules:
     * - User must have NEWS_VIEW_AUDIT permission
     * - Audit logs show who did what and when
     * - Typically EDITOR+, not AUTHOR
     * 
     * @param user AdminUserDetails
     * @return true if user can view audit history
     * @throws IllegalArgumentException if user is null
     */
    public boolean canUserViewAuditHistory(AdminUserDetails user) {
        if (user == null) {
            log.warn("canUserViewAuditHistory called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        boolean hasPermission = permissionConfigService.hasPermission(user, RbacPermissionConstants.NEWS_VIEW_AUDIT);

        log.debug("canUserViewAuditHistory check for user {}: permission={}", user.getAdminId(), hasPermission);

        if (!hasPermission) {
            log.warn("User {} lacks NEWS_VIEW_AUDIT permission", user.getAdminId());
        }

        return hasPermission;
    }

    /**
     * Check if user can create a new article (and potentially duplicate existing)
     * 
     * Business Rules:
     * - User must have NEWS_CREATE permission
     * - This allows both new article creation and duplication
     * 
     * @param user AdminUserDetails
     * @return true if user can create articles
     * @throws IllegalArgumentException if user is null
     */
    public boolean canUserCreateArticle(AdminUserDetails user) {
        if (user == null) {
            log.warn("canUserCreateArticle called with null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        boolean hasPermission = permissionConfigService.hasPermission(user, RbacPermissionConstants.NEWS_CREATE);

        log.debug("canUserCreateArticle check for user {}: permission={}", user.getAdminId(), hasPermission);

        if (!hasPermission) {
            log.warn("User {} lacks NEWS_CREATE permission", user.getAdminId());
        }

        return hasPermission;
    }

    /**
     * Check if user has permission by name (generic check)
     * 
     * @param user           AdminUserDetails
     * @param permissionName Permission constant name
     * @return true if user has permission
     * @throws IllegalArgumentException if user is null
     */
    public boolean hasPermission(AdminUserDetails user, String permissionName) {
        if (user == null) {
            log.warn("hasPermission called with null user for permission: {}", permissionName);
            throw new IllegalArgumentException("User cannot be null");
        }

        if (permissionName == null) {
            log.warn("hasPermission called with null permission name for user: {}", user.getAdminId());
            return false;
        }

        return permissionConfigService.hasPermission(user, permissionName);
    }

    /**
     * Get cache statistics (for debugging)
     * 
     * @return String with cache info
     */
    public String getCacheStats() {
        return permissionConfigService.getCacheStats();
    }
}
