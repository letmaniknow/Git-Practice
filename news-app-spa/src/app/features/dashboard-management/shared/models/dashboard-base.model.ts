/**
 * Base Models for Dashboard Features
 * Shared interfaces and types used across all dashboard phases
 */

/**
 * Pagination information for activity feeds
 * Used by all features to paginate results
 */
export interface PageResponse {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

/**
 * Base Dashboard Statistics Interface
 * Core metrics common to all dashboard features
 * Each feature extends this with feature-specific metrics
 */
export interface BaseDashboardStats {
  /** Total count of primary entities for this feature */
  totalCount: number;
  /** Count of active entities */
  activeCount: number;
  /** Entities created in current month */
  createdThisMonth: number;
}

/**
 * Base Audit Log Interface
 * Industry-standard audit trail with WHO, WHAT, WHEN, WHERE, WHY, HOW sections
 * Extended by feature-specific implementations
 */
export interface BaseAuditLog {
  // ===== WHO: Actor Information =====
  /** User ID who performed the action */
  actorId: string;
  /** Display name of the user */
  actorDisplayName: string;
  /** Session ID for tracking user session */
  sessionId?: string;

  // ===== WHAT: Action Information =====
  /** Type of action performed (CREATE, READ, UPDATE, DELETE, PUBLISH, ARCHIVE, etc.) */
  action: string;
  /** Domain/Module affected (NEWS, USER, CATEGORY, etc.) */
  domain: string;
  /** Source of action (UI, API, SYSTEM) */
  source?: string;

  // ===== WHICH: Resource Information =====
  /** Unique identifier of affected resource */
  resourceId?: string;
  /** Human-readable name of resource (article title, user name, category name) */
  resourceName?: string;

  // ===== WHEN: Timestamp =====
  /** ISO 8601 formatted timestamp of when action occurred */
  createdAt: string;

  // ===== WHERE: Context Information =====
  /** IP address of requester */
  ipAddress?: string;
  /** User agent/browser information */
  userAgent?: string;
  /** Request URI/endpoint */
  requestUri?: string;

  // ===== WHY: Reason Information =====
  /** Reason for the action (optional explanation) */
  reason?: string;
  /** Detailed notes about the action */
  details?: string;

  // ===== HOW: Result Information =====
  /** Whether action succeeded */
  isSuccess: boolean;
  /** Error message if action failed */
  errorMessage?: string;
  /** HTTP status code of request */
  httpStatus?: number;
  /** HTTP method used */
  requestMethod?: string;
  /** Number of records affected */
  affectedRows?: number;

  // ===== CORRELATE: Tracing Information =====
  /** Unique transaction ID for correlation across services */
  transactionId?: string;
  /** Severity level (INFO, WARNING, ERROR) */
  severity?: string;
  /** Response time in milliseconds */
  responseTimeMs?: number;
}

/**
 * Generic Stat Card Interface
 * Used to display statistics in card format across all dashboard features
 * Flexible structure suitable for any phase to display metrics
 */
export interface StatCard {
  /** Display title/label for the stat */
  title: string;
  /** The statistic value (number or formatted string) */
  value: number | string;
  /** Material Icon name to display */
  icon: string;
  /** Percentage change from previous period (optional, positive or negative) */
  change?: number;
  /** Unit label for the value (e.g., "articles", "views", "%") */
  unit?: string;
}
