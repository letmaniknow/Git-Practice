package com.mmva.newsapp.infrastructure.rbac.audit.constants;

/**
 * Domain constant for RBAC audit logs.
 * 
 * Used to identify which feature/domain an audit log belongs to when using
 * the unified audit system. The domain column in the audit table allows
 * filtering and analyzing audit logs by feature.
 * 
 * <h2>Unified Audit System</h2>
 * 
 * In the unified audit architecture, all features use the same underlying
 * UnifiedAuditLogRepository interface, but each feature has its own audit
 * table:
 * 
 * <pre>
 * Domain          Table                   Volume
 * ──────────────────────────────────────────────────────
 * RBAC       → rbac_general_audit_log    ~18K rows/year
 * NEWS       → news_audit_log            ~365K rows/year
 * PUSH       → push_audit_log            ~3.65M rows/year
 * ADMIN      → admin_audit_log           ~73K rows/year
 * </pre>
 * 
 * The domain column helps with:
 * ✅ Cross-domain analytics: "Which features have the most audit failures?"
 * ✅ Debugging: "Was this action performed in RBAC or NEWS?"
 * ✅ Retention policies: "Keep RBAC forever, archive NEWS after 1 year"
 * ✅ Performance analysis: "Which domain generates the most audit logs?"
 * 
 * <h2>Usage</h2>
 * 
 * When creating an audit log through AuditingUtility:
 * 
 * <pre>
 * auditingUtility.audit(
 *         RbacAuditDomain.RBAC, // Domain constant
 *         actorId,
 *         RbacAuditActions.ROLE_CREATED,
 *         roleId,
 *         roleName,
 *         reason,
 *         clientInfo,
 *         "CRITICAL",
 *         rbacAuditLogRepository);
 * </pre>
 * 
 * Or for other features (future):
 * 
 * <pre>
 * auditingUtility.audit(
 *         NewsAuditDomain.NEWS,
 *         actorId,
 *         NewsAuditActions.NEWS_PUBLISHED,
 *         newsId,
 *         headline,
 *         reason,
 *         clientInfo,
 *         "HIGH",
 *         newsAuditLogRepository);
 * </pre>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public final class RbacAuditDomain {

    // Prevent instantiation
    private RbacAuditDomain() {
        throw new AssertionError("Cannot instantiate RbacAuditDomain");
    }

    /** Domain identifier for Role-Based Access Control audit logs */
    public static final String RBAC = "RBAC";
}
