import { QuickAction } from '../../shared/models/dashboard-quick-actions.model';

/**
 * Dashboard Quick Actions Configuration
 * Contains all actions available across dashboard features (Phases 1-5)
 * 
 * Phase 1: News (✅ Active)
 * Phase 2: Admin User Management
 * Phase 3: Category Management
 * Phase 4: Recycle Bin
 * Phase 5: Settings
 */
export const DASHBOARD_QUICK_ACTIONS: QuickAction[] = [
  {
    id: 'dashboard-news',
    label: 'News',
    icon: 'newspaper',
    color: '#1976d2'
  },
  {
    id: 'manage-users',
    label: 'Users',
    icon: 'people',
    color: '#388e3c'
  },
  {
    id: 'manage-categories',
    label: 'Categories',
    icon: 'folder',
    color: '#f57c00'
  },
  {
    id: 'recycle-bin',
    label: 'Recycle Bin',
    icon: 'delete_outline',
    color: '#d32f2f'
  },
  {
    id: 'settings',
    label: 'Settings',
    icon: 'settings',
    color: '#7b1fa2'
  }
];
