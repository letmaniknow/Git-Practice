import { Component, Output, EventEmitter } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { NgIf } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { ConfirmationDialogComponent } from '../../../features/news-management/components/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [MatIconModule, MatTooltipModule, MatMenuModule, NgIf, ConfirmationDialogComponent],
  template: `
    <header class="header">
      <ng-container *ngIf="!isLoginPage">
        <button mat-icon-button (click)="toggleSidebarClick()" aria-label="Toggle sidebar">
          <mat-icon class="hamburger-icon">menu</mat-icon>
        </button>
      </ng-container>
      <div class="header-logo">
        <!-- Replace with <img src="assets/logo.svg" alt="Logo"> for a real logo -->
        <mat-icon>newspaper</mat-icon>
      </div>
      <span class="header-title">Admin Portal</span>
      <span class="header-spacer"></span>
      <div class="header-icon-group">
        <button mat-icon-button matTooltip="Notifications">
          <mat-icon>notifications</mat-icon>
        </button>
        <button mat-icon-button [matMenuTriggerFor]="userMenu" matTooltip="User Menu">
          <mat-icon>account_circle</mat-icon>
        </button>
        <mat-menu #userMenu="matMenu">
          <button mat-menu-item>
            <mat-icon>person</mat-icon>
            <span>Profile</span>
          </button>
          <button mat-menu-item (click)="logout()">
            <mat-icon>logout</mat-icon>
            <span>Logout</span>
          </button>
        </mat-menu>
      </div>
      <app-confirmation-dialog
        [isOpen]="showLogoutDialog"
        title="Confirm Logout"
        message="Are you sure you want to logout?"
        confirmText="Logout"
        cancelText="Cancel"
        type="danger"
        (confirm)="confirmLogout()"
        (cancel)="cancelLogout()"
      ></app-confirmation-dialog>
    </header>
  `,
  styles: [
    `
    :host {
      display: block;
    }

    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: var(--header-height, 56px);
      background: var(--color-primary, #3b82f6);
      color: white;
      padding: 0 var(--spacing-md, 16px);
      position: fixed;
      left: 0;
      top: 0;
      right: 0;
      z-index: 101;
      box-shadow: var(--shadow-2, 0 2px 8px rgba(0,0,0,0.16));
      font-family: var(--font-family, 'Segoe UI, Arial, sans-serif');
      transition: all 0.3s ease;
    }

    /* Left section: Menu button + Logo + Title */
    .header > ng-container:first-of-type {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm, 8px);
    }

    /* Logo section */
    .header-logo {
      display: flex;
      align-items: center;
      justify-content: center;
      width: var(--icon-button-touch-target, 48px);
      height: var(--icon-button-touch-target, 48px);
      flex-shrink: 0;

      mat-icon {
        width: var(--icon-md, 24px) !important;
        height: var(--icon-md, 24px) !important;
        font-size: var(--icon-md, 24px) !important;
        line-height: var(--icon-md, 24px) !important;
        color: white !important;
      }
    }

    /* Title styling */
    .header-title {
      font-size: var(--font-size-h5, 1.125rem);
      font-weight: var(--font-weight-semibold, 600);
      color: white;
      white-space: nowrap;
      margin: 0;
    }

    /* Spacer to push icons to right */
    .header-spacer {
      flex: 1 1 auto;
      min-width: var(--spacing-md, 16px);
    }

    /* Right section: Icons */
    .header-icon-group {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: var(--spacing-xs, 4px);
      flex-shrink: 0;
    }

    /* Icon button styles - ALL buttons */
    button[mat-icon-button] {
      min-width: auto !important;
      min-height: auto !important;
      width: var(--icon-button-touch-target, 48px) !important;
      height: var(--icon-button-touch-target, 48px) !important;
      padding: var(--icon-button-md-padding, 12px) !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
      border-radius: var(--border-radius-md, 8px) !important;
      transition: all 0.2s ease !important;
      margin: 0 !important;
      background: rgba(255, 255, 255, 0) !important;

      &:hover {
        background: rgba(255, 255, 255, 0.12) !important;
      }

      &:focus-visible {
        outline: 2px solid white !important;
        outline-offset: 2px !important;
      }
    }

    /* Icon sizing inside buttons */
    button[mat-icon-button] mat-icon {
      width: var(--icon-md, 24px) !important;
      height: var(--icon-md, 24px) !important;
      font-size: var(--icon-md, 24px) !important;
      line-height: var(--icon-md, 24px) !important;
      color: white !important;
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
    }

    /* Hamburger icon specific */
    .hamburger-icon {
      width: var(--icon-md, 24px) !important;
      height: var(--icon-md, 24px) !important;
      font-size: var(--icon-md, 24px) !important;
      line-height: var(--icon-md, 24px) !important;
      color: white !important;
    }

    /* User menu panel styling */
    ::ng-deep .mat-mdc-menu-panel {
      margin-top: var(--spacing-xs, 4px) !important;
    }

    ::ng-deep .mat-mdc-menu-item {
      display: flex !important;
      align-items: center !important;
      gap: var(--spacing-md, 16px) !important;
      height: auto !important;
      padding: var(--spacing-md, 16px) !important;

      mat-icon {
        width: var(--icon-md, 24px) !important;
        height: var(--icon-md, 24px) !important;
        font-size: var(--icon-md, 24px) !important;
        line-height: var(--icon-md, 24px) !important;
        flex-shrink: 0;
      }

      span {
        flex: 1;
        text-align: left;
      }
    }

    /* Responsive design for smaller screens */
    @media (max-width: 768px) {
      .header {
        padding: 0 var(--spacing-sm, 8px);
      }

      .header-title {
        display: none;
      }

      .header-logo {
        display: none;
      }

      .header-spacer {
        min-width: var(--spacing-sm, 8px);
      }
    }
    `
  ]
})
export class HeaderComponent {
  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() logoutEvent = new EventEmitter<void>();

  showLogoutDialog = false;

  constructor(private authService: AuthService, private router: Router) {}

  get isLoginPage(): boolean {
    // Use Angular Router to check if current route is login
    // This assumes you have injected Router as 'private router: Router' in the constructor
    return this.router.url === '/auth/login';
  }

  toggleSidebarClick() {
    this.toggleSidebar.emit();
  }

  logout() {
    this.showLogoutDialog = true;
  }

  async confirmLogout() {
    this.showLogoutDialog = false;
    this.logoutEvent.emit();
    await this.authService.logoutAsync();
  }

  cancelLogout() {
    this.showLogoutDialog = false;
  }
}
