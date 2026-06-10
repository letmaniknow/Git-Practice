/**
 * News state selectors
 * Functions to extract and derive data from the store
 * These are memoized and efficient
 */

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AppState } from '../app.state';
import { NewsState } from './news.state';

// Feature selector
export const selectNewsFeature = createFeatureSelector<NewsState>('news');

// Selection selectors
export const selectAllSelectedIds = createSelector(
  selectNewsFeature,
  (state: NewsState) => state.selectedIds
);

export const selectSelectedCount = createSelector(
  selectAllSelectedIds,
  (ids) => ids.length
);

export const selectIsItemSelected = (id: string) =>
  createSelector(
    selectAllSelectedIds,
    (ids) => ids.includes(id)
  );

export const selectHasAnySelection = createSelector(
  selectSelectedCount,
  (count) => count > 0
);

// Filter selectors
export const selectFilters = createSelector(
  selectNewsFeature,
  (state: NewsState) => state.filters
);

export const selectStatusFilter = createSelector(
  selectFilters,
  (filters) => filters.status
);

export const selectLanguageFilter = createSelector(
  selectFilters,
  (filters) => filters.language
);

export const selectCategoryFilter = createSelector(
  selectFilters,
  (filters) => filters.category
);

export const selectSearchTerm = createSelector(
  selectFilters,
  (filters) => filters.searchTerm
);

export const selectHasActiveFilters = createSelector(
  selectFilters,
  (filters) =>
    filters.status !== undefined ||
    filters.language !== undefined ||
    filters.category !== undefined ||
    filters.searchTerm !== undefined
);

// Pagination selectors
export const selectPagination = createSelector(
  selectNewsFeature,
  (state: NewsState) => state.pagination
);

export const selectCurrentPage = createSelector(
  selectPagination,
  (pagination) => pagination.currentPage
);

export const selectPageSize = createSelector(
  selectPagination,
  (pagination) => pagination.pageSize
);

export const selectTotal = createSelector(
  selectPagination,
  (pagination) => pagination.total
);

export const selectTotalPages = createSelector(
  selectPagination,
  (pagination) => Math.ceil(pagination.total / pagination.pageSize)
);

// Loading state selectors
export const selectIsLoading = createSelector(
  selectNewsFeature,
  (state: NewsState) => state.isLoading
);

export const selectError = createSelector(
  selectNewsFeature,
  (state: NewsState) => state.error
);

// Combined selectors useful for the component
export const selectNewsListState = createSelector(
  selectAllSelectedIds,
  selectSelectedCount,
  selectHasAnySelection,
  selectFilters,
  selectPagination,
  selectIsLoading,
  selectError,
  (selectedIds, selectedCount, hasSelection, filters, pagination, isLoading, error) => ({
    selectedIds,
    selectedCount,
    hasSelection,
    filters,
    pagination,
    isLoading,
    error,
  })
);
