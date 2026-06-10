import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';

import { NewsFormService } from '../../services/news-form.service';
import { NewsAuditLogDto } from '../../../dashboard-management/news/models/dashboard-news.model';
import { NewsItem } from '../../models/news-item.model';
import { AdminUserService } from '../../../admin-user-management/services/admin-user.service';
import { NewsCategory } from '../../models/news-form.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-news-audit-trail-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule,
    MatDividerModule
  ],
  templateUrl: './news-audit-trail-page.component.html',
  styleUrls: ['./news-audit-trail-page.component.scss']
})
export class NewsAuditTrailPageComponent implements OnInit {
  newsId!: string;
  newsItem: NewsItem | null = null;
  auditLogs: NewsAuditLogDto[] = [];
  loading = false;
  error: string | null = null;

  // Cache for admin users and categories
  private adminUserCache = new Map<string, { name: string; email: string }>();
  private categoryCache = new Map<string, string>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private newsFormService: NewsFormService,
    private adminUserService: AdminUserService
  ) {}

  ngOnInit(): void {
    this.newsId = this.route.snapshot.paramMap.get('newsId') || '';
    if (this.newsId) {
      this.loadAuditTrail();
      this.loadNewsItem();
    } else {
      this.error = 'Invalid news ID';
    }
  }

  loadAuditTrail(): void {
    this.loading = true;
    this.error = null;

    this.newsFormService.getNewsAuditLogs(this.newsId).subscribe({
      next: (logs: NewsAuditLogDto[]) => {
        console.log('🔍 Audit logs received:', logs);
        if (logs && logs.length > 0) {
          console.log('🔍 First log createdAt:', logs[0].createdAt, 'Type:', typeof logs[0].createdAt);
        }
        // Sort by timestamp descending (newest first) - Industry standard
        this.auditLogs = this.sortLogsDescending(logs);
        this.loadAdditionalData();
      },
      error: (err: Error) => {
        this.error = err.message || 'Failed to load audit trail';
        this.loading = false;
      }
    });
  }

  /**
   * Load admin user names and category names
   */
  loadAdditionalData(): void {
    // Get unique actor IDs
    const actorIds = [...new Set(this.auditLogs.map(log => log.actorId).filter(id => id))];

    // Get unique category IDs from details
    const categoryIds = new Set<string>();
    this.auditLogs.forEach(log => {
      if (log.details) {
        // Try to extract category ID from details
        const categoryMatch = log.details.match(/categoryId["']?\s*:\s*["']([a-f0-9\-]+)["']/i) ||
                             log.details.match(/([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})/g);
        if (categoryMatch && categoryMatch[1]) {
          categoryIds.add(categoryMatch[1]);
        } else if (log.action === 'CATEGORY_ASSIGNED' && log.reason) {
          // Extract from reason field
          const reasonMatch = log.reason.match(/([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})/);
          if (reasonMatch && reasonMatch[1]) {
            categoryIds.add(reasonMatch[1]);
          }
        }
      }
    });

    // Load admin users and categories in parallel
    const adminUserRequests = actorIds.map(id => 
      this.adminUserService.getAdminUser(id).pipe(
        catchError(() => of(null))
      )
    );

    const categoryRequest = this.newsFormService.getActiveCategoriesForNewsCreation().pipe(
      catchError(() => of([]))
    );

    if (adminUserRequests.length === 0) {
      // No admin users to load, just load categories
      categoryRequest.subscribe({
        next: (categories: NewsCategory[]) => {
          categories.forEach(cat => {
            this.categoryCache.set(cat.id, cat.categoryNameEn || cat.categoryNameEs);
          });
          this.loading = false;
        },
        error: (err) => {
          console.error('Failed to load categories:', err);
          this.loading = false;
        }
      });
      return;
    }

    forkJoin([
      ...adminUserRequests,
      categoryRequest
    ]).subscribe({
      next: (results) => {
        console.log('🔍 Admin user results:', results);
        // Process admin users (all but last result)
        results.slice(0, -1).forEach((adminUser: any, index) => {
          console.log('🔍 Processing admin user:', adminUser);
          if (adminUser) {
            const actorId = actorIds[index];
            // Use correct field names from AdminUserResponseDto
            const fullName = adminUser.adminUsersFullName || 
                           (adminUser.adminUsersFirstName && adminUser.adminUsersLastName 
                             ? `${adminUser.adminUsersFirstName} ${adminUser.adminUsersLastName}` 
                             : adminUser.adminUsersUsername);
            const email = adminUser.adminUsersEmail || '';
            console.log('🔍 Setting cache for', actorId, ':', fullName, email);
            this.adminUserCache.set(actorId, {
              name: fullName || 'Unknown User',
              email: email
            });
          }
        });

        // Process categories (last result)
        const categories = results[results.length - 1] as NewsCategory[];
        categories.forEach(cat => {
          this.categoryCache.set(cat.id, cat.categoryNameEn || cat.categoryNameEs);
        });

        console.log('✅ Admin user cache:', this.adminUserCache);
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Failed to load additional data:', err);
        this.loading = false;
      }
    });
  }

  loadNewsItem(): void {
    this.newsFormService.getNewsById(this.newsId).subscribe({
      next: (news) => {
        this.newsItem = news;
      },
      error: (err) => {
        console.error('Failed to load news item:', err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/news/table']);
  }

  /**
   * Get icon based on action type (ServiceNow-inspired)
   */
  getActionIcon(action: string): string {
    const icons: { [key: string]: string } = {
      'CREATED': 'add_circle',
      'UPDATED': 'edit',
      'PUBLISHED': 'publish',
      'DELETED': 'delete',
      'ARCHIVED': 'archive',
      'RESTORED': 'restore_from_trash',
      'SCHEDULED_PUBLISHED': 'schedule',
      'UNPUBLISHED': 'unpublished'
    };
    return icons[action] || 'change_history';
  }

  /**
   * Get color class based on action type
   */
  getActionColor(action: string): string {
    const colors: { [key: string]: string } = {
      'CREATED': 'action-created',
      'UPDATED': 'action-updated',
      'PUBLISHED': 'action-published',
      'DELETED': 'action-deleted',
      'ARCHIVED': 'action-archived',
      'RESTORED': 'action-restored',
      'SCHEDULED_PUBLISHED': 'action-scheduled',
      'UNPUBLISHED': 'action-unpublished'
    };
    return colors[action] || 'action-default';
  }

  /**
   * Format action name for display
   */
  formatAction(action: string): string {
    return action
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }

  /**
   * Format timestamp to relative time (fix for ISO 8601 format and Java LocalDateTime array)
   */
  formatTime(timestamp: string | number | any[]): string {
    if (!timestamp) return '';
    
    const date = this.parseTimestamp(timestamp);
    if (!date) return String(timestamp);
    
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Format timestamp for display in header (with full date and time)
   */
  formatFullDateTime(timestamp: string | number | any[]): string {
    if (!timestamp) return '';
    
    const date = this.parseTimestamp(timestamp);
    if (!date) return String(timestamp);
    
    return date.toLocaleDateString('en-US', { 
      month: 'short', 
      day: 'numeric', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  /**
   * Parse timestamp from various formats:
   * - ISO 8601 string
   * - Unix timestamp (seconds or milliseconds)
   * - Java LocalDateTime array [year, month, day, hour, minute, second, nano]
   */
  private parseTimestamp(timestamp: string | number | any[]): Date | null {
    try {
      // Handle Java LocalDateTime array format: [2026, 6, 2, 9, 32, 52, 123456789]
      if (Array.isArray(timestamp)) {
        console.log('🔍 Parsing LocalDateTime array:', timestamp);
        const [year, month, day, hour = 0, minute = 0, second = 0] = timestamp;
        // Month in Date constructor is 0-indexed, Java sends 1-indexed
        const date = new Date(year, month - 1, day, hour, minute, second);
        console.log('✅ Parsed array to date:', date.toISOString());
        return date;
      }
      
      // Handle number (Unix timestamp)
      if (typeof timestamp === 'number') {
        // If timestamp < 10000000000, it's in seconds (before year 2286), multiply by 1000
        const ms = timestamp < 10000000000 ? timestamp * 1000 : timestamp;
        const date = new Date(ms);
        console.log('🔍 Parsed number timestamp:', timestamp, '→', date.toISOString());
        return date;
      }
      
      // Handle string
      if (typeof timestamp === 'string') {
        // Check if it's a numeric string (Unix timestamp)
        if (/^\d+$/.test(timestamp)) {
          const num = parseInt(timestamp, 10);
          const ms = num < 10000000000 ? num * 1000 : num;
          const date = new Date(ms);
          console.log('🔍 Parsed string number:', timestamp, '→', date.toISOString());
          return date;
        }
        
        // Parse as ISO string
        const date = new Date(timestamp);
        if (!isNaN(date.getTime())) {
          console.log('🔍 Parsed ISO string:', timestamp, '→', date.toISOString());
          return date;
        }
      }
      
      console.warn('⚠️ Could not parse timestamp:', timestamp);
      return null;
    } catch (err) {
      console.error('❌ Error parsing timestamp:', timestamp, err);
      return null;
    }
  }

  /**
   * Get formatted actor name with email
   */
  getActorDisplay(log: NewsAuditLogDto): string {
    console.log('🔍 Getting actor display for:', log.actorId);
    console.log('🔍 Cache has:', this.adminUserCache.get(log.actorId));
    const cached = this.adminUserCache.get(log.actorId);
    if (cached && cached.name && cached.name !== 'Unknown User') {
      return cached.email ? `${cached.name} (${cached.email})` : cached.name;
    }
    // Fallback to actorDisplayName from log
    return log.actorDisplayName || 'Unknown User';
  }

  /**
   * Get actor name only (without email)
   */
  getActorName(log: NewsAuditLogDto): string {
    const cached = this.adminUserCache.get(log.actorId);
    if (cached && cached.name && cached.name !== 'Unknown User') {
      return cached.name;
    }
    return log.actorDisplayName || 'Unknown User';
  }

  /**
   * Get actor email
   */
  getActorEmail(log: NewsAuditLogDto): string {
    const cached = this.adminUserCache.get(log.actorId);
    return cached?.email || '';
  }

  /**
   * Get category name from details
   */
  getCategoryFromDetails(details: string | undefined): string {
    if (!details) return '';

    // Try to extract category ID
    const categoryMatch = details.match(/categoryId["']?\s*:\s*["']([a-f0-9\-]+)["']/i) ||
                         details.match(/([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})/);
    
    if (categoryMatch && categoryMatch[1]) {
      const categoryId = categoryMatch[1];
      const categoryName = this.categoryCache.get(categoryId);
      if (categoryName) {
        return categoryName;
      }
      return categoryId; // Return ID if name not found
    }

    return '';
  }

  /**
   * Format details with category name replacement
   */
  formatDetails(details: string | undefined): string {
    if (!details) return '';

    let formatted = details;

    // Replace category IDs with names
    this.categoryCache.forEach((name, id) => {
      formatted = formatted.replace(new RegExp(id, 'g'), `${name} (${id.substring(0, 8)}...)`);
    });

    return formatted;
  }

  /**
   * Sort logs by timestamp descending (newest first)
   * Industry standard: GitHub, JIRA, ServiceNow, Azure DevOps
   */
  sortLogsDescending(logs: NewsAuditLogDto[]): NewsAuditLogDto[] {
    return logs.sort((a, b) => {
      const dateA = this.parseTimestamp(a.createdAt);
      const dateB = this.parseTimestamp(b.createdAt);
      
      if (!dateA && !dateB) return 0;
      if (!dateA) return 1;  // Put invalid dates at the end
      if (!dateB) return -1; // Put invalid dates at the end
      
      // Descending order: newest first (b - a)
      return dateB.getTime() - dateA.getTime();
    });
  }

  /**
   * Get severity badge color
   */
  getSeverityColor(severity: string): string {
    const colors: { [key: string]: string } = {
      'CRITICAL': 'severity-critical',
      'HIGH': 'severity-high',
      'MEDIUM': 'severity-medium',
      'LOW': 'severity-low'
    };
    return colors[severity] || 'severity-medium';
  }
}
