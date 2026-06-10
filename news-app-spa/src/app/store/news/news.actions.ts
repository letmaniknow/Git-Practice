/**
 * News state actions
 * Define all possible actions that can be dispatched to update news state
 */

import { createAction, props } from '@ngrx/store';
import { NewsItem } from '../../features/news-management/models/news-item.model';

// Selection actions
export const selectNewsItem = createAction(
  '[News] Select News Item',
  props<{ id: string }>()
);

export const deselectNewsItem = createAction(
  '[News] Deselect News Item',
  props<{ id: string }>()
);

export const selectMultipleNewsItems = createAction(
  '[News] Select Multiple News Items',
  props<{ ids: string[] }>()
);

export const clearSelection = createAction(
  '[News] Clear Selection'
);

export const toggleNewsItemSelection = createAction(
  '[News] Toggle News Item Selection',
  props<{ id: string }>()
);

// Filter actions
export const setStatusFilter = createAction(
  '[News] Set Status Filter',
  props<{ status: string | undefined }>()
);

export const setLanguageFilter = createAction(
  '[News] Set Language Filter',
  props<{ language: string | undefined }>()
);

export const setCategoryFilter = createAction(
  '[News] Set Category Filter',
  props<{ category: string | undefined }>()
);

export const setSearchTerm = createAction(
  '[News] Set Search Term',
  props<{ searchTerm: string | undefined }>()
);

export const clearFilters = createAction(
  '[News] Clear Filters'
);

// Pagination actions
export const setCurrentPage = createAction(
  '[News] Set Current Page',
  props<{ page: number }>()
);

export const setPageSize = createAction(
  '[News] Set Page Size',
  props<{ pageSize: number }>()
);

export const updateTotal = createAction(
  '[News] Update Total Count',
  props<{ total: number }>()
);

// Loading actions
export const setLoading = createAction(
  '[News] Set Loading',
  props<{ isLoading: boolean }>()
);

export const setError = createAction(
  '[News] Set Error',
  props<{ error: string | null }>()
);

// Bulk operation actions
export const publishSelectedNews = createAction(
  '[News] Publish Selected News'
);

export const unpublishSelectedNews = createAction(
  '[News] Unpublish Selected News'
);

export const softDeleteSelectedNews = createAction(
  '[News] Soft Delete Selected News'
);

export const permanentDeleteSelectedNews = createAction(
  '[News] Permanent Delete Selected News'
);

export const restoreSelectedNews = createAction(
  '[News] Restore Selected News'
);

export const changeStatusForSelected = createAction(
  '[News] Change Status For Selected',
  props<{ newStatus: string }>()
);
