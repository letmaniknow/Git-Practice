import { createSelector, createFeatureSelector } from '@ngrx/store';
import { DashboardState } from '../news/models/dashboard-news.model';

// ✅ Feature selector
export const selectAdminDashboardState = createFeatureSelector<DashboardState>(
  'adminDashboard'
);

// ✅ Stats (NEWS)
export const selectDashboardStats = createSelector(
  selectAdminDashboardState,
  (state: DashboardState) => state.stats
);

// ✅ Recent Activity (NEWS)
export const selectRecentActivity = createSelector(
  selectAdminDashboardState,
  (state: DashboardState) => state.recentActivity
);

// ✅ Loading & Error
export const selectDashboardLoading = createSelector(
  selectAdminDashboardState,
  (state: DashboardState) => state.loading
);

export const selectDashboardError = createSelector(
  selectAdminDashboardState,
  (state: DashboardState) => state.error
);
