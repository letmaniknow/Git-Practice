import { Component, ChangeDetectionStrategy, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { QuickAction } from '../../models/dashboard-quick-actions.model';

/**
 * Generic Quick Actions Component
 * Reusable across all dashboard features
 * Displays configurable action buttons with icons and labels
 * 
 * Supports role-based access control:
 * - Enabled actions: clickable, normal styling
 * - Disabled actions: grayed out, non-clickable (shows reason on hover)
 */
@Component({
  selector: 'app-dashboard-quick-actions',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatTooltipModule],
  template: `
    <div class="quick-actions">
      <button 
        *ngFor="let action of actions"
        mat-raised-button 
        [disabled]="action.disabled"
        [matTooltip]="getTooltip(action)"
        (click)="onActionClick(action.id)"
        [class.action-button]="true"
        [class.disabled]="action.disabled"
      >
        <mat-icon>{{ action.icon }}</mat-icon>
        <span>{{ action.label }}</span>
      </button>
    </div>
  `,
  styles: [`
    .quick-actions {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
      padding: 1rem;
      background: var(--surface-color, #fff);
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .action-button {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: var(--primary-color, #1976d2);
      color: white;
      text-transform: capitalize;
      transition: all 0.2s ease;

      &:hover:not(.disabled) {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &.disabled {
        background-color: #cccccc;
        color: #999999;
        cursor: not-allowed;
        opacity: 0.6;
        box-shadow: none;

        &:hover {
          transform: none;
          box-shadow: none;
        }

        mat-icon {
          opacity: 0.5;
        }
      }

      mat-icon {
        font-size: 1.25rem;
      }

      span {
        font-weight: 500;
      }
    }

    @media (max-width: 768px) {
      .quick-actions {
        gap: 0.5rem;
      }

      .action-button {
        flex: 1;
        min-width: 100px;
        font-size: 0.85rem;

        mat-icon {
          font-size: 1rem;
        }
      }
    }
  `]
})
export class DashboardQuickActionsComponent {
  @Input() actions!: QuickAction[];
  @Output() actionTriggered = new EventEmitter<string>();

  /**
   * Get tooltip text for action
   * Shows permission message for disabled actions
   */
  getTooltip(action: QuickAction): string {
    if (action.disabled) {
      return `${action.label} - You do not have permission to access this feature`;
    }
    return action.label;
  }

  /**
   * Handle action button click
   * Only emit if action is not disabled
   */
  onActionClick(actionId: string): void {
    const action = this.actions.find(a => a.id === actionId);
    if (action && !action.disabled) {
      this.actionTriggered.emit(actionId);
    }
  }
}
