// ✅ V1 NEWS DASHBOARD MODELS (matching v1 requirements)

// Import shared base models
import { PageResponse, BaseAuditLog, StatCard } from '../../shared/models/dashboard-base.model';

// Re-export PageResponse and StatCard for backward compatibility with component imports
export type { PageResponse };
export type { StatCard };

// Main dashboard stats for news
export interface DashboardStats {
  totalArticles: number;
  publishedThisMonth: number;
  draftCount: number;
  scheduledCount: number;
  archivedCount: number;
  systemHealth: 'healthy' | 'warning' | 'critical';
  totalPageViews?: number;
  totalEngagement?: number;
}

// ========================================
// Audit Log DTO (from backend)
// Extends BaseAuditLog with news-specific fields
// ========================================
export interface NewsAuditLogDto extends BaseAuditLog {
  /** Primary key for audit log entry */
  id?: number;

  /** Related news article ID (news-specific) */
  newsId?: string;
}

// Article activity log (legacy, for backward compatibility)
export interface ActivityLog {
  id: string;
  articleId: string;
  articleTitle: string;
  actionType: 'CREATED' | 'PUBLISHED' | 'DELETED' | 'UPDATED' | 'ARCHIVED';
  performedBy: string;
  timestamp: Date;
}

// News dashboard section data
export interface NewsDashboardData {
  stats: DashboardStats;
  recentActivity: NewsAuditLogDto[];
}

// ✅ Frontend view models (internal use)

export interface DashboardState {
  stats: DashboardStats | null;
  recentActivity: NewsAuditLogDto[];
  loading: boolean;
  error: string | null;
  totalActivities: number;
  pageInfo: PageResponse | null;
}
