/**
 * Nested Menu Item Component
 * 
 * Handles rendering of hierarchical menu items with:
 * - Expandable/collapsible groups
 * - Active route highlighting
 * - Badge support
 * - Smooth animations
 * - Professional Material Design styling
 * 
 * @author Admin Portal Team
 * @since 1.0.0
 */

import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { filter } from 'rxjs';

import { NavigationItem } from '../../../shared/models/navigation.model';
import { SidebarStateService } from '../../../shared/services/sidebar-state.service';

@Component({
  selector: 'app-nested-menu-item',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatTooltipModule,
    MatBadgeModule,
    MatButtonModule,
  ],
  template: `
    <!-- Item Container -->
    <div class="menu-item-container" [ngClass]="{ expanded: item.expanded && !collapsed }">
      <!-- Parent Item (or standalone link) -->
      <button
        mat-button
        class="menu-item"
        [ngClass]="getItemClasses()"
        (click)="onItemClick($event)"
        [routerLink]="item.route"
        [routerLinkActive]="item.route ? 'active' : ''"
        [routerLinkActiveOptions]="{ exact: true }"
        [matTooltip]="collapsed ? item.label : ''"
        matTooltipPosition="right"
        matTooltipShowDelay="500"
        [matBadge]="item.badge || null"
        [matBadgeColor]="getBadgeColor(item.badgeColor, 'warn')"
        matBadgeSize="small"
      >
        <!-- Icon -->
        <mat-icon class="menu-icon">{{ item.icon }}</mat-icon>

        <!-- Label (shown when not collapsed) -->
        <span *ngIf="!collapsed" class="menu-label">
          {{ item.label }}
        </span>

        <!-- Expand/Collapse Icon (for parent items, only when expanded) -->
        <mat-icon
          *ngIf="item.children && getVisibleChildren().length > 0 && !collapsed"
          class="expand-icon"
          [ngClass]="{ rotated: item.expanded }"
        >
          expand_more
        </mat-icon>
      </button>

      <!-- Divider (optional) -->
      <div *ngIf="item.showDivider && !collapsed" class="menu-divider"></div>

      <!-- Children (nested items) - Only visible and expanded -->
      <div
        *ngIf="item.children && getVisibleChildren().length > 0 && item.expanded && !collapsed"
        class="submenu"
      >
        <ng-container *ngFor="let child of getVisibleChildren()">
          <!-- Nested Group Item (has children) -->
          <div *ngIf="child.children && child.children.length > 0" class="nested-group">
            <button
              mat-button
              class="submenu-group-item"
              [ngClass]="{ 'group-active': isChildRouteActive(child) }"
              (click)="toggleNestedItem(child)"
              [matTooltip]="child.label"
              matTooltipPosition="right"
              matTooltipShowDelay="500"
            >
              <mat-icon class="submenu-icon">{{ child.icon }}</mat-icon>
              <span class="submenu-label">{{ child.label }}</span>
              <mat-icon class="group-expand-icon" [ngClass]="{ rotated: isNestedItemExpanded(child) }">
                expand_more
              </mat-icon>
            </button>

            <!-- Nested Children (Accordion: only one open per parent) -->
            <div *ngIf="isNestedItemExpanded(child)" class="nested-submenu">
              <a
                *ngFor="let grandchild of child.children"
                mat-button
                class="nested-submenu-item"
                [routerLink]="grandchild.route"
                routerLinkActive="active"
                [routerLinkActiveOptions]="{ exact: true }"
                [matTooltip]="grandchild.label"
                matTooltipPosition="right"
                matTooltipShowDelay="500"
              >
                <mat-icon class="nested-icon">{{ grandchild.icon }}</mat-icon>
                <span class="nested-label">{{ grandchild.label }}</span>
              </a>
            </div>
          </div>

          <!-- Simple Leaf Item (no children) -->
          <a
            *ngIf="!child.children || child.children.length === 0"
            mat-button
            class="submenu-item"
            [routerLink]="child.route"
            routerLinkActive="active"
            [routerLinkActiveOptions]="{ exact: true }"
            [matTooltip]="child.label"
            matTooltipPosition="right"
            matTooltipShowDelay="500"
            [matBadge]="child.badge || null"
            [matBadgeColor]="getBadgeColor(child.badgeColor, 'accent')"
            matBadgeSize="small"
          >
            <!-- Submenu Icon -->
            <mat-icon class="submenu-icon">{{ child.icon }}</mat-icon>

            <!-- Submenu Label -->
            <span class="submenu-label">{{ child.label }}</span>
          </a>
        </ng-container>
      </div>
    </div>
  `,
  styles: [
    `
      .menu-item-container {
        width: 100%;
        display: flex;
        flex-direction: column;
      }

      /* Menu Item (Parent/Standalone) */
      .menu-item {
        width: 100% !important;
        height: 44px !important;
        padding: 6px 10px !important;
        margin: 2px 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: flex-start !important;
        gap: 8px !important;
        color: var(--color-text-dark, #111827) !important;
        background: transparent !important;
        border-radius: var(--border-radius-md, 8px) !important;
        transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1) !important;
        text-decoration: none !important;
        font-size: var(--font-size-small, 0.875rem) !important;
        font-weight: 500 !important;
        cursor: pointer !important;
        border: none !important;
        position: relative !important;
        overflow: visible !important;
        pointer-events: auto !important;
        user-select: none !important;
        flex-shrink: 0 !important;

        &:hover {
          background: var(--overlay-hover-light, rgba(79, 70, 229, 0.04)) !important;
          color: var(--color-primary, #4f46e5) !important;

          .menu-icon,
          .expand-icon {
            color: var(--color-primary, #4f46e5) !important;
          }
        }

        &:focus-visible {
          outline: 2px solid var(--color-primary, #4f46e5) !important;
          outline-offset: 2px !important;
        }

        &.active,
        &.parent-active {
          background: var(--overlay-active-light, rgba(79, 70, 229, 0.1)) !important;
          color: var(--color-primary, #4f46e5) !important;
          font-weight: 600 !important;

          .menu-icon,
          .expand-icon {
            color: var(--color-primary, #4f46e5) !important;
          }
        }

        &[routerLinkActive] {
          background: var(--overlay-active-light, rgba(79, 70, 229, 0.1)) !important;
          color: var(--color-primary, #4f46e5) !important;

          .menu-icon {
            color: var(--color-primary, #4f46e5) !important;
          }
        }
      }

      .menu-icon {
        width: var(--icon-md, 24px) !important;
        height: var(--icon-md, 24px) !important;
        font-size: var(--icon-md, 24px) !important;
        line-height: var(--icon-md, 24px) !important;
        color: var(--color-text-dark, #111827) !important;
        transition: color 200ms cubic-bezier(0.4, 0, 0.2, 1) !important;
        flex-shrink: 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        min-width: var(--icon-md, 24px) !important;
      }

      .menu-label {
        flex: 1 1 auto;
        min-width: 0;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: var(--font-size-small, 0.875rem);
        font-weight: 500;
        color: inherit;
        transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
        pointer-events: auto;
      }

      .expand-icon {
        width: var(--icon-md, 24px) !important;
        height: var(--icon-md, 24px) !important;
        font-size: var(--icon-md, 24px) !important;
        line-height: var(--icon-md, 24px) !important;
        color: var(--color-text-gray, #6b7280) !important;
        transition: transform 300ms cubic-bezier(0.4, 0, 0.2, 1), color 200ms cubic-bezier(0.4, 0, 0.2, 1) !important;
        flex-shrink: 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        min-width: var(--icon-md, 24px) !important;
        margin-left: auto;

        &.rotated {
          transform: rotate(180deg) !important;
        }
      }

      /* ═════════════════════════════════════════════════════════════ */
      /* SUBMENU STYLES (Direct Children) */
      /* ═════════════════════════════════════════════════════════════ */

      .submenu {
        display: flex;
        flex-direction: column;
        gap: 2px;
        padding: 4px 8px 4px 16px;
        margin-left: 8px;
        border-left: 2px solid var(--color-border, #e5e7eb);
        animation: slideDown 200ms cubic-bezier(0.4, 0, 0.2, 1);

        @keyframes slideDown {
          from {
            opacity: 0;
            transform: translateY(-8px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
      }

      /* Simple Leaf Item (no children) */
      .submenu-item {
        width: 100% !important;
        height: 38px !important;
        padding: 6px 10px !important;
        margin: 2px 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: flex-start !important;
        gap: 8px !important;
        color: #1f2937 !important;
        background: transparent !important;
        border-radius: var(--border-radius-sm, 6px) !important;
        transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1) !important;
        text-decoration: none !important;
        font-size: var(--font-size-xs, 0.8125rem) !important;
        font-weight: 400 !important;
        cursor: pointer !important;
        border: none !important;
        position: relative !important;
        pointer-events: auto !important;
        user-select: none !important;
        flex-shrink: 0 !important;

        &:hover {
          background: #dbeafe !important;
          color: #1e40af !important;
          font-weight: 500 !important;

          .submenu-icon {
            color: #1e40af !important;
          }
        }

        &:focus-visible {
          outline: 2px solid #2563eb !important;
          outline-offset: 0 !important;
        }

        &.active {
          background: #3b82f6 !important;
          color: #ffffff !important;
          font-weight: 600 !important;

          .submenu-icon {
            color: #ffffff !important;
          }
        }
      }

    .submenu-icon {
      width: 16px !important;
      height: 16px !important;
      font-size: 16px !important;
      line-height: 16px !important;
      color: #4b5563 !important;
      transition: color 150ms cubic-bezier(0.4, 0, 0.2, 1) !important;
      flex-shrink: 0 !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      min-width: 16px !important;
    }

    .submenu-label {
      flex: 1 1 auto;
      min-width: 0;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: var(--font-size-xs, 0.8125rem);
      font-weight: 400;
      color: inherit;
      transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
      pointer-events: auto;
    }

      /* ═════════════════════════════════════════════════════════════ */
      /* NESTED GROUP STYLES (Items with children: e.g., Content, Publishing) */
      /* ═════════════════════════════════════════════════════════════ */

      .nested-group {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }

      .submenu-group-item {
        width: 100% !important;
        height: 38px !important;
        padding: 6px 10px !important;
        margin: 2px 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: flex-start !important;
        gap: 8px !important;
        color: #1f2937 !important;
        background: transparent !important;
        border-radius: var(--border-radius-sm, 6px) !important;
        transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1) !important;
        text-decoration: none !important;
        font-size: var(--font-size-xs, 0.8125rem) !important;
        font-weight: 500 !important;
        cursor: pointer !important;
        border: none !important;
        position: relative !important;
        pointer-events: auto !important;
        user-select: none !important;
        flex-shrink: 0 !important;

        &:hover {
          background: #dbeafe !important;
          color: #1e40af !important;
          font-weight: 600 !important;

          .submenu-icon {
            color: #1e40af !important;
          }

          .group-expand-icon {
            color: #1e40af !important;
          }
        }

        &.group-active {
          background: #eff6ff !important;
          color: #1e40af !important;
          font-weight: 700 !important;
          border-left: 3px solid #3b82f6;
          padding-left: 7px !important;

          .submenu-icon {
            color: #1e40af !important;
          }

          .group-expand-icon {
            color: #1e40af !important;
          }
        }
      }
      .group-expand-icon {
        width: 16px !important;
        height: 16px !important;
        font-size: 16px !important;
        line-height: 16px !important;
        color: var(--color-text-gray, #6b7280) !important;
        transition: transform 200ms cubic-bezier(0.4, 0, 0.2, 1), color 150ms cubic-bezier(0.4, 0, 0.2, 1) !important;
        flex-shrink: 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        min-width: 16px !important;
        margin-left: auto;

        &.rotated {
          transform: rotate(180deg) !important;
        }
      }

      /* ═════════════════════════════════════════════════════════════ */
      /* DEEPLY NESTED ITEMS STYLES (e.g., News List, News Table) */
      /* ═════════════════════════════════════════════════════════════ */

      .nested-submenu {
        display: flex;
        flex-direction: column;
        gap: 2px;
        padding: 4px 8px 4px 28px;
        margin-left: 4px;
        border-left: 2px solid #e5e7eb;
        animation: slideDown 200ms cubic-bezier(0.4, 0, 0.2, 1);
      }

      .nested-submenu-item {
        width: 100% !important;
        height: 36px !important;
        padding: 5px 8px !important;
        margin: 1px 0 !important;
        display: flex !important;
        align-items: center !important;
        justify-content: flex-start !important;
        gap: 8px !important;
        color: #374151 !important;
        background: transparent !important;
        border-radius: var(--border-radius-sm, 6px) !important;
        transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1) !important;
        text-decoration: none !important;
        font-size: var(--font-size-xs, 0.8125rem) !important;
        font-weight: 400 !important;
        cursor: pointer !important;
        border: none !important;
        position: relative !important;
        pointer-events: auto !important;
        user-select: none !important;
        flex-shrink: 0 !important;

        &:hover {
          background: #e0e7ff !important;
          color: #3730a3 !important;
          font-weight: 500 !important;

          .nested-icon {
            color: #3730a3 !important;
          }
        }

        &:focus-visible {
          outline: 2px solid #3b82f6 !important;
          outline-offset: 0 !important;
        }

        &.active {
          background: #3b82f6 !important;
          color: #ffffff !important;
          font-weight: 600 !important;
          border-left: 3px solid #1e40af;
          padding-left: 5px !important;

          .nested-icon {
            color: #ffffff !important;
          }
        }
      }

    .nested-icon {
      width: 15px !important;
      height: 15px !important;
      font-size: 15px !important;
      line-height: 15px !important;
      color: #6b7280 !important;
      transition: color 150ms cubic-bezier(0.4, 0, 0.2, 1) !important;
      flex-shrink: 0 !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      min-width: 15px !important;
    }

    .nested-label {
      flex: 1 1 auto;
      min-width: 0;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: var(--font-size-xs, 0.8125rem);
      font-weight: 400;
      color: inherit;
      transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
      pointer-events: auto;
    }

      /* Divider */
      .menu-divider {
        height: 1px;
        background: var(--color-border, #e5e7eb);
        margin: 4px 0;
        width: 100%;
      }

      /* Collapsed State */
      :host ::ng-deep .sidebar.collapsed {
        .menu-item {
          justify-content: center !important;
          padding: 0 !important;
        }

        .menu-label,
        .expand-icon,
        .submenu {
          display: none !important;
        }

        .menu-badge {
          display: none !important;
        }
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NestedMenuItemComponent implements OnInit {
  @Input() item!: NavigationItem;
  @Input() collapsed = false;
  @Output() itemExpanded = new EventEmitter<string>();

  isActive = false;

  constructor(
    private router: Router,
    private sidebarState: SidebarStateService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Track route changes to update active state
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateActiveState();
      });

    // Initial active state
    this.updateActiveState();
  }

  /**
   * Get visible children (filter by visible property)
   */
  getVisibleChildren(): NavigationItem[] {
    if (!this.item.children) return [];
    return this.item.children.filter((child) => child.visible !== false);
  }

  /**
   * Handle click on menu item
   * Industry-standard behavior:
   * 1. If sidebar is collapsed AND item has children → Auto-expand sidebar first
   * 2. Then toggle the menu item expand/collapse
   * 3. If has route but no children → navigate (handled by routerLink)
   */
  onItemClick(event?: MouseEvent): void {
    if (this.item.children && this.getVisibleChildren().length > 0) {
      // Prevent routerLink navigation when expanding/collapsing
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      
      // Smart expand: If sidebar is collapsed, expand it first to show submenu
      if (this.collapsed) {
        this.sidebarState.setCollapsed(false);
      }
      
      // Toggle the menu item expansion
      this.item.expanded = !this.item.expanded;
      this.itemExpanded.emit(this.item.id);
      // Trigger change detection for OnPush components when object mutates
      this.cdr.markForCheck();
    }
    // If no children, allow routerLink to handle navigation
  }

  /**
   * Toggle nested item expansion (accordion behavior)
   * When one item in a parent is expanded, others close
   */
  toggleNestedItem(child: NavigationItem): void {
    if (child.children && child.children.length > 0) {
      this.sidebarState.toggleNestedItem(this.item.id, child.id);
    }
  }

  /**
   * Check if nested item is expanded (accordion state from service)
   */
  isNestedItemExpanded(child: NavigationItem): boolean {
    return this.sidebarState.isNestedItemExpanded(this.item.id, child.id);
  }

  /**
   * Check if any child route is currently active
   */
  isChildRouteActive(item: NavigationItem): boolean {
    if (!item.children) return false;
    return item.children.some((child: NavigationItem) => this.router.url === child.route);
  }

  /**
   * Get CSS classes for menu item styling
   */
  getItemClasses(): Record<string, boolean> {
    return {
      'has-children': !!(this.item.children && this.getVisibleChildren().length > 0),
      'parent-active': this.isChildActive(),
      active: this.isActive,
      [this.item.cssClass || '']: !!this.item.cssClass,
    };
  }

  /**
   * Get valid badge color for Material Badge directive
   */
  getBadgeColor(color: string | undefined, defaultColor: 'primary' | 'accent' | 'warn'): 'primary' | 'accent' | 'warn' {
    if (color === 'primary' || color === 'accent' || color === 'warn') {
      return color;
    }
    return defaultColor;
  }

  /**
   * Check if this item's route is currently active
   */
  private updateActiveState(): void {
    if (this.item.route) {
      // Exact route match
      this.isActive = this.router.url === this.item.route;
    } else {
      // Check if any child is active (for group items)
      this.isActive = this.isChildActive();
    }
  }

  /**
   * Check if any child route is currently active
   */
  private isChildActive(): boolean {
    if (!this.item.children) return false;
    return this.item.children.some((child: NavigationItem) => this.router.url === child.route);
  }
}
