import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { StatCard } from '../../models/dashboard-base.model';

@Component({
  selector: 'app-dashboard-stats-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <mat-card class="stats-card">
      <div class="card-content">
        <div class="icon-section">
          <mat-icon [class.icon]="true">{{ stat.icon }}</mat-icon>
        </div>
        <div class="value-section">
          <p class="card-title">{{ stat.title }}</p>
          <p class="card-value">
            {{ stat.value }}
            <span *ngIf="stat.unit" class="unit">{{ stat.unit }}</span>
          </p>
          <p *ngIf="stat.change" class="card-change" [ngClass]="stat.change > 0 ? 'positive' : 'negative'">
            {{ stat.change > 0 ? '+' : '' }}{{ stat.change }}%
          </p>
        </div>
      </div>
    </mat-card>
  `,
  styles: [`
    .stats-card {
      padding: 1.5rem;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }
    }

    .card-content {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
    }

    .icon-section {
      flex-shrink: 0;
    }

    .icon {
      font-size: 2rem;
      width: 2rem;
      height: 2rem;
      color: var(--primary-color, #1976d2);
    }

    .value-section {
      flex: 1;
    }

    .card-title {
      margin: 0;
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--text-secondary, #666);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .card-value {
      margin: 0.5rem 0 0;
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--text-primary, #222);
    }

    .unit {
      font-size: 1rem;
      font-weight: 400;
      color: var(--text-secondary, #666);
      margin-left: 0.25rem;
    }

    .card-change {
      margin: 0.5rem 0 0;
      font-size: 0.875rem;
      font-weight: 500;

      &.positive {
        color: #4caf50;
      }

      &.negative {
        color: #f44336;
      }
    }

    @media (max-width: 768px) {
      .stats-card {
        padding: 1rem;
      }

      .card-value {
        font-size: 1.25rem;
      }

      .icon {
        font-size: 1.5rem;
        width: 1.5rem;
        height: 1.5rem;
      }
    }
  `]
})
export class DashboardStatsCardComponent {
  @Input() stat!: StatCard;
}
