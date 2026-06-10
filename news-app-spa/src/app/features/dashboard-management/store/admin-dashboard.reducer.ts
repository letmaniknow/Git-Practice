import { createReducer, on } from '@ngrx/store';
import { DashboardState } from '../news/models/dashboard-news.model';
import * as AdminDashboardActions from './admin-dashboard.actions';

const initialState: DashboardState = {
  stats: null,
  recentActivity: [],
  loading: false,
  error: null,
  totalActivities: 0,
  pageInfo: null,
};

export const adminDashboardReducer = createReducer(
  initialState,

  // ✅ Load Dashboard Data (NEWS)
  on(AdminDashboardActions.loadDashboardData, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(AdminDashboardActions.loadDashboardDataSuccess, (state, { stats, recentActivity }) => ({
    ...state,
    stats,
    recentActivity,
    loading: false,
    error: null,
  })),

  on(AdminDashboardActions.loadDashboardDataFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  // ✅ Clear Data
  on(AdminDashboardActions.clearDashboardData, () => initialState)
);
