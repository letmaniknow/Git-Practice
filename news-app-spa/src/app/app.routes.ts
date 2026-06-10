import { Routes } from '@angular/router';

import { LayoutComponent } from './layouts/main-layout/layout.component';
import { LoginLayoutComponent } from './layouts/login-layout/login-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { unsavedChangesGuard } from './core/guards/unsaved-changes.guard';

export const routes: Routes = [
  {
    path: 'auth',
    component: LoginLayoutComponent,
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      }
    ]
  },
    {
      path: '',
      component: LayoutComponent,
      canActivate: [authGuard],
      children: [
        { path: '', redirectTo: 'admin/dashboard', pathMatch: 'full' },
        { path: 'admin/dashboard', loadComponent: () => import('./features/dashboard-management/news/pages/dashboard-news-page.component').then(m => m.DashboardNewsPageComponent) },
        { path: 'news', loadComponent: () => import('./features/news-management/pages/news-browse-page/news-browse-page.component').then(m => m.NewsBrowsePageComponent) },
        { path: 'news/admin', loadComponent: () => import('./features/news-management/pages/news-table-list-page/news-table-list-page.component').then(m => m.NewsTableListPageComponent) },
        { path: 'news/create', loadComponent: () => import('./features/news-management/pages/news-create-page/news-create-page.component').then(m => m.NewsCreatePageComponent), canDeactivate: [unsavedChangesGuard] },
        { path: 'news/scheduler', loadComponent: () => import('./features/news-management/pages/news-scheduler-management-page/news-scheduler-management-page.component').then(m => m.NewsSchedulerManagementPageComponent) },
        { path: 'news/:id/edit', loadComponent: () => import('./features/news-management/pages/news-edit-page/news-edit-page.component').then(m => m.NewsEditPageComponent), canDeactivate: [unsavedChangesGuard] },
        { path: 'news/:newsId/audit-trail', loadComponent: () => import('./features/news-management/pages/news-audit-trail-page/news-audit-trail-page.component').then(m => m.NewsAuditTrailPageComponent) },
        { path: 'news/:id', loadComponent: () => import('./features/news-management/pages/news-detail-page/news-detail-page.component').then(m => m.NewsDetailPageComponent) },
        // Admin User Management
        { path: 'admin/users', loadComponent: () => import('./features/admin-user-management/pages/admin-user-list-page.component').then(m => m.AdminUserListPageComponent) },
        { path: 'admin/users/:id/profile', loadComponent: () => import('./features/admin-user-management/components/admin-user-detail/admin-user-detail.component').then(m => m.AdminUserDetailComponent) },
        // News Category Management
        { path: 'admin/categories', loadComponent: () => import('./features/news-category-management/pages/news-category-list-page.component').then(m => m.NewsCategoryListPageComponent) },
        { path: 'users', loadComponent: () => import('./features/users/users.component').then(m => m.UsersComponent) },
        { path: 'ads', loadComponent: () => import('./features/ads/ads.component').then(m => m.AdsComponent) },
        { path: 'settings', loadComponent: () => import('./features/settings/settings.component').then(m => m.SettingsComponent) },
        // Add more feature routes as needed
      ]
    },
    // Legacy Routes
    { path: 'user-form', redirectTo: '/news/create' },
    // Global Wildcard Route
    { path: '**', redirectTo: '/admin/dashboard' }
  ];