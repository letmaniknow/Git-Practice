import { createAction, props } from '@ngrx/store';
import { DashboardStats, NewsAuditLogDto } from '../news/models/dashboard-news.model';

// ✅ Load Dashboard Data (NEWS)
export const loadDashboardData = createAction(
  '[Admin Dashboard Page] Load Dashboard Data'
);

export const loadDashboardDataSuccess = createAction(
  '[Admin Dashboard API] Load Dashboard Data Success',
  props<{ stats: DashboardStats; recentActivity: NewsAuditLogDto[] }>()
);

export const loadDashboardDataFailure = createAction(
  '[Admin Dashboard API] Load Dashboard Data Failure',
  props<{ error: string }>()
);

// ✅ Clear data
export const clearDashboardData = createAction(
  '[Admin Dashboard] Clear Data'
);
