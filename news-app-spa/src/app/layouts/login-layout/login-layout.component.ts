import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { ThemeService } from '../../core/services/theme.service';

/**
 * 🔐 LOGIN LAYOUT COMPONENT
 * 
 * Wrapper layout for the authentication flow.
 * Provides:
 * - Fixed header bar at top (64px height)
 * - Portal branding (logo + title)
 * - Content area for login component (router-outlet)
 * 
 * Theme Integration:
 * - All colors/spacing use theme variables
 * - Initialized with ThemeService for CSS variable injection
 * - Responsive and accessible design
 */
@Component({
  selector: 'app-login-layout',
  standalone: true,
  imports: [RouterOutlet, MatIconModule],
  template: `
    <div class="login-layout-wrapper">
      <header class="login-header">
        <div class="header-logo">
          <mat-icon>newspaper</mat-icon>
        </div>
        <span class="header-title">The News Portal</span>
      </header>
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .login-layout-wrapper {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      background: var(--color-surface, #f9fafb);
    }

    .login-header {
      display: flex;
      align-items: center;
      justify-content: flex-start;
      height: 64px;
      width: 100vw;
      background: var(--color-primary, #3b82f6);
      color: white;
      padding: 0 var(--spacing-md, 16px);
      position: fixed;
      left: 0;
      top: 0;
      right: 0;
      z-index: 101;
      box-shadow: var(--shadow-1, 0 1px 3px rgba(0, 0, 0, 0.12));
      box-sizing: border-box;
    }

    .header-logo {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      color: white;
    }

    .header-title {
      font-size: var(--font-size-h5, 1.125rem);
      font-weight: 600;
      margin-left: var(--spacing-md, 16px);
      letter-spacing: 0.5px;
      color: white;
      white-space: nowrap;
    }

    mat-icon {
      font-size: 32px !important;
      width: 32px !important;
      height: 32px !important;
      color: white !important;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginLayoutComponent {
  constructor(private themeService: ThemeService) {
    // ThemeService is injected to ensure CSS variables are initialized
    // before this layout renders
  }
}
