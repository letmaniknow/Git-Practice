/**
 * Main Sidebar Navigation Component
 * 
 * Professional, scalable sidebar navigation with:
 * - Hierarchical menu structure (News Management > Scheduler, etc.)
 * - Expandable/collapsible groups
 * - Active route highlighting
 * - Smooth animations
 * - Icon-only mode when collapsed
 * - Industry-standard Material Design
 * - Future-proof architecture for adding menu items
 * 
 * @author Admin Portal Team
 * @since 1.0.0
 */

import { Component, Input, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';

import { NavigationItem, NavigationConfig } from '../../../shared/models/navigation.model';
import { NAVIGATION_CONFIG } from '../../../shared/config/navigation.config';
import { NestedMenuItemComponent } from './nested-menu-item.component';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarStateService } from '../../../shared/services/sidebar-state.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatTooltipModule,
    MatButtonModule,
    NestedMenuItemComponent,
  ],
  template: `
    <aside [class.collapsed]="collapsed" class="sidebar">
      <!-- Main Navigation Menu -->
      <nav class="sidebar-nav">
        <ng-container *ngFor="let item of navigationConfig.mainMenu">
          <app-nested-menu-item
            [item]="item"
            [collapsed]="collapsed"
            (itemExpanded)="onItemExpanded($event)"
          ></app-nested-menu-item>
        </ng-container>
      </nav>

      <!-- Bottom Navigation Menu (Settings, Help, Profile) -->
      <div class="sidebar-bottom">
        <div class="sidebar-divider" *ngIf="navigationConfig.bottomMenu.length > 0"></div>
        <nav class="bottom-nav">
          <ng-container *ngFor="let item of navigationConfig.bottomMenu">
            <app-nested-menu-item
              [item]="item"
              [collapsed]="collapsed"
              (itemExpanded)="onItemExpanded($event)"
            ></app-nested-menu-item>
          </ng-container>
        </nav>
      </div>
    </aside>
  `,
  styles: [
    `
      /* ========================================
         SIDEBAR CONTAINER
         ======================================== */

      .sidebar {
        width: 280px;
        background: var(--color-surface, #ffffff);
        color: var(--color-text-dark, #111827);
        height: calc(100vh - 56px);
        transition: width 300ms cubic-bezier(0.4, 0, 0.2, 1);
        overflow-y: auto;
        overflow-x: hidden;
        position: fixed;
        left: 0;
        top: 56px;
        z-index: 100;
        font-family: var(--font-family, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif);
        border-right: 1px solid var(--color-border, #e5e7eb);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        display: flex;
        flex-direction: column;

        /* Scrollbar styling */
        scrollbar-width: thin;
        scrollbar-color: #d1d5db #f3f4f6;

        &::-webkit-scrollbar {
          width: 8px;
        }

        &::-webkit-scrollbar-track {
          background: transparent;
        }

        &::-webkit-scrollbar-thumb {
          background: #d1d5db;
          border-radius: 4px;

          &:hover {
            background: #9ca3af;
          }
        }
      }

      /* Collapsed State (icon-only) */
      .sidebar.collapsed {
        width: 64px;
        min-width: 64px;
        max-width: 64px;
      }

      /* ========================================
         NAVIGATION MENUS
         ======================================== */

      .sidebar-nav {
        flex: 1 1 auto;
        display: flex;
        flex-direction: column;
        gap: 2px;
        padding: 8px 6px 0 6px;
        min-width: 0;
        width: 100%;
        overflow: visible;
      }

      .sidebar.collapsed .sidebar-nav {
        padding: 8px 2px 0 2px;
      }

      /* ========================================
         BOTTOM MENU (Settings, Help, Profile)
         ======================================== */

      .sidebar-bottom {
        flex: 0 0 auto;
        display: flex;
        flex-direction: column;
        width: 100%;
        margin-top: auto;
      }

      .sidebar-divider {
        height: 1px;
        background: var(--color-border, #e5e7eb);
        margin: 4px 6px;
        width: calc(100% - 12px);
      }

      .bottom-nav {
        display: flex;
        flex-direction: column;
        gap: 2px;
        padding: 4px 6px;
        width: 100%;
      }

      .sidebar.collapsed .bottom-nav {
        padding: 4px 2px;
      }

      /* ========================================
         SCROLLBAR CUSTOMIZATION
         ======================================== */

      .sidebar {
        --scrollbar-background: #f3f4f6;
        --scrollbar-color: #d1d5db;
        --scrollbar-hover: #9ca3af;
      }

      /* ========================================
         RESPONSIVE BREAKPOINTS
         ======================================== */

      @media (max-width: 768px) {
        .sidebar {
          width: var(--sidebar-collapsed-width, 65px);
          padding: var(--spacing-md, 16px) 0;

          .sidebar-nav {
            gap: 2px;
          }

          .bottom-nav {
            gap: 2px;
          }
        }
      }

      @media (prefers-reduced-motion: reduce) {
        .sidebar {
          transition: none;
        }
      }
    `,
  ]
,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarComponent implements OnInit {
  collapsed = false;

  navigationConfig: NavigationConfig = NAVIGATION_CONFIG;

  constructor(
    private authService: AuthService,
    private sidebarState: SidebarStateService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Subscribe to collapsed state from service
    this.sidebarState.collapsed$.subscribe((collapsed) => {
      this.collapsed = collapsed;
      // Trigger change detection for OnPush components
      this.cdr.markForCheck();
    });

    // Role-based filtering disabled for now
    // In future: call this.filterMenuByRole() to restrict by roles
    // this.filterMenuByRole();
  }

  /**
   * Toggle sidebar collapsed/expanded state
   */
  toggleCollapsed(): void {
    this.sidebarState.toggleCollapsed();
  }

  /**
   * Handle item expanded event
   */
  onItemExpanded(itemId: string): void {
    // Handled by SidebarStateService now
  }

  /**
   * Future: Filter menu items based on the current user's roles
   * Uncomment ngOnInit call above when ready to implement role restrictions
   * 
   * ROLE-BASED FILTERING (DISABLED FOR NOW)
   * ────────────────────────────────────
   * Rules:
   * - If no requiredRoles defined: visible to everyone
   * - If requiredRoles defined: visible only if user has ANY of those roles
   * 
   * Usage: Uncomment in ngOnInit when ready
   */
  private filterMenuByRole(): void {
    // TODO: Implement when role restrictions are planned
    const currentRoles = this.authService.getRoles();
    const unfiltered = JSON.parse(JSON.stringify(NAVIGATION_CONFIG)); // Deep clone
    
    this.navigationConfig = {
      mainMenu: this.filterItems(unfiltered.mainMenu, currentRoles),
      bottomMenu: this.filterItems(unfiltered.bottomMenu, currentRoles),
    };
  }

  /**
   * Recursively filter navigation items by role
   * @param items - Items to filter
   * @param userRoles - Current user's roles
   * @returns Filtered items visible to the user
   */
  private filterItems(items: NavigationItem[], userRoles: string[]): NavigationItem[] {
    if (!items) return [];

    return items
      .filter((item) => this.isItemVisible(item, userRoles))
      .map((item) => ({
        ...item,
        children: item.children ? this.filterItems(item.children, userRoles) : undefined,
      }))
      .filter((item) => {
        // Remove parent items if all children were filtered out
        if (item.children && item.children.length === 0 && item.expandable) {
          return false;
        }
        return true;
      });
  }

  /**
   * Check if an item should be visible to the current user
   * @param item - Item to check
   * @param userRoles - Current user's roles
   * @returns true if item should be visible
   */
  private isItemVisible(item: NavigationItem, userRoles: string[]): boolean {
    // If no role requirements specified, show to everyone
    if (!item.requiredRoles || item.requiredRoles.length === 0) {
      return true;
    }

    // If user has no roles (not logged in), hide restricted items
    if (!userRoles || userRoles.length === 0) {
      return false;
    }

    // Check if user has ANY of the required roles
    return item.requiredRoles.some((requiredRole) =>
      userRoles.includes(requiredRole)
    );
  }
}
