/**
 * News Category Management - Routing Configuration
 * Purpose: Define routes for news category management feature
 * Pattern: Follows admin-user-management routing pattern
 */

import { Routes } from '@angular/router';
import { NewsCategoryListPageComponent } from './pages/news-category-list-page.component';

/**
 * News Category Management Routes
 * Base path: /admin/categories (to be configured in main app routing)
 */
export const NEWS_CATEGORY_ROUTES: Routes = [
  {
    path: '',
    component: NewsCategoryListPageComponent,
    data: { breadcrumb: 'Categories', title: 'News Category Management' },
  },
];
