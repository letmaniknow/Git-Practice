import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { SidebarComponent } from './sidebar/main-sidebar.component';
import { HeaderComponent } from './header/main-header.component';
import { SessionTimeoutService } from '../../core/services/session-timeout.service';
import { SidebarStateService } from '../../shared/services/sidebar-state.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, HeaderComponent],
  template: `
    <app-header (toggleSidebar)="toggleSidebar()" (logoutEvent)="handleLogout()"></app-header>
    <app-sidebar></app-sidebar>
    <main class="main-content" [class.collapsed]="sidebarCollapsed">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [
    `.main-content {
      margin-left: 280px;
      margin-top: 56px;
      padding: 8px 16px 16px 16px;
      min-height: calc(100vh - 56px);
      background: var(--color-surface, #f5f6fa);
      transition: margin-left 300ms cubic-bezier(0.4, 0, 0.2, 1);
      overflow-x: hidden;
      max-width: 100vw;
      box-sizing: border-box;
    }
    .main-content.collapsed {
      margin-left: 64px;
    }
    @media (max-width: 768px) {
      .main-content {
        margin-left: 0;
        padding: 8px 12px 12px 12px;
      }
    }
    `
  ]
})
export class LayoutComponent implements OnInit, OnDestroy {
  sidebarCollapsed = false;
  private sidebarSubscription: Subscription | null = null;

  constructor(
    private router: Router,
    private sessionTimeout: SessionTimeoutService,
    private sidebarState: SidebarStateService
  ) {}

  ngOnInit(): void {
    // Initialize session monitoring when layout loads
    this.sessionTimeout.initialize();

    // Subscribe to sidebar state changes from service
    this.sidebarSubscription = this.sidebarState.collapsed$.subscribe((collapsed) => {
      this.sidebarCollapsed = collapsed;
    });
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    this.sessionTimeout.destroy();
    if (this.sidebarSubscription) {
      this.sidebarSubscription.unsubscribe();
    }
  }

  toggleSidebar() {
    // Use service to toggle (syncs with sidebar component and persists)
    this.sidebarState.toggleCollapsed();
  }

  handleLogout() {
    this.router.navigate(['/auth/login']);
  }
}
