import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NewsAuditLogDto } from '../../models/dashboard-news.model';

@Component({
  selector: 'app-dashboard-news-activity',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="activity-container">
      <h3 class="section-title">📰 Recent Activity</h3>
      
      <div *ngIf="!activities || activities.length === 0" class="empty-state">
        <p>No recent activity</p>
      </div>

      <div *ngIf="activities && activities.length > 0" class="activity-list">
        <div *ngFor="let activity of activities" class="activity-item" [ngClass]="'action-' + (activity.action || '').toLowerCase()">
          <div class="activity-icon">
            <mat-icon [matTooltip]="activity.action || 'Action'">
              {{ getIcon(activity.action || '') }}
            </mat-icon>
          </div>
          
          <div class="activity-content">
            <p class="activity-title">{{ activity.resourceName || activity.newsId }}</p>
            <p class="activity-meta">
              <span class="action">{{ formatAction(activity.action || '') }}</span>
              <span class="by">by {{ activity.actorDisplayName || (activity.actorId | slice:0:8) }}</span>
              <span class="time">{{ formatTime(activity.createdAt || '') }}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .activity-container {
      padding: 1.5rem;
      background: var(--surface-color, #fff);
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .section-title {
      margin: 0 0 1.5rem;
      font-size: 1.125rem;
      font-weight: 600;
      color: var(--text-primary, #222);
    }

    .empty-state {
      text-align: center;
      padding: 2rem;
      color: var(--text-secondary, #999);
    }

    .activity-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .activity-item {
      display: grid;
      grid-template-columns: auto 1fr;
      gap: 1rem;
      align-items: start;
      padding: 1rem;
      background: var(--background-color, #f5f5f5);
      border-radius: 6px;
      border-left: 4px solid var(--primary-color, #1976d2);
      transition: all 0.2s ease;

      &:hover {
        background: var(--background-hover, #efefef);
      }

      &.action-created {
        border-left-color: #4caf50;
      }

      &.action-published {
        border-left-color: #2196f3;
      }

      &.action-deleted {
        border-left-color: #f44336;
      }

      &.action-updated {
        border-left-color: #ff9800;
      }

      &.action-archived {
        border-left-color: #9c27b0;
      }
    }

    .activity-icon {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      background: var(--primary-color-light, #e3f2fd);
      border-radius: 50%;

      mat-icon {
        color: var(--primary-color, #1976d2);
      }
    }

    .activity-content {
      min-width: 0;
    }

    .activity-title {
      margin: 0;
      font-weight: 500;
      color: var(--text-primary, #222);
      font-size: 0.95rem;
      word-break: break-word;
    }

    .activity-meta {
      margin: 0.5rem 0 0;
      font-size: 0.8rem;
      color: var(--text-secondary, #999);
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;

      span {
        white-space: nowrap;
      }

      .action {
        font-weight: 600;
        background: rgba(0, 0, 0, 0.05);
        padding: 0.25rem 0.5rem;
        border-radius: 3px;
      }

      .by::before {
        content: '';
      }

      .time::before {
        content: '•';
        margin-right: 0.5rem;
      }
    }

    @media (max-width: 768px) {
      .activity-container {
        padding: 1rem;
      }

      .activity-item {
        gap: 0.75rem;
        padding: 0.75rem;
      }

      .activity-icon {
        width: 32px;
        height: 32px;
      }

      .activity-title {
        font-size: 0.875rem;
      }

      .activity-meta {
        font-size: 0.75rem;
      }
    }
  `]
})
export class DashboardNewsActivityComponent {
  @Input() activities: NewsAuditLogDto[] = [];

  getIcon(type: string): string {
    const icons: Record<string, string> = {
      CREATE: 'add_circle_outline',
      PUBLISH: 'publish',
      DELETE: 'delete',
      UPDATE: 'edit',
      ARCHIVE: 'archive',
      READ: 'visibility',
      LIKE: 'favorite',
      COMMENT: 'comment',
      SHARE: 'share',
      BOOKMARK: 'bookmark',
    };
    return icons[type] || 'info';
  }

  formatAction(type: string): string {
    return type.charAt(0) + type.slice(1).toLowerCase();
  }

  formatTime(date: Date | string | number): string {
    let d: Date;
    
    // Handle different date formats
    if (typeof date === 'number') {
      // If it's epoch seconds (less than year 2100 in seconds), convert to milliseconds
      if (date < 4102444800) {
        d = new Date(date * 1000);
      } else {
        d = new Date(date);
      }
    } else if (typeof date === 'string') {
      d = new Date(date);
    } else {
      d = date;
    }
    
    // Validate date
    if (isNaN(d.getTime())) {
      return 'unknown date';
    }
    
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }
}
