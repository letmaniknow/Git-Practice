/**
 * News state reducer
 * Pure functions that handle state mutations based on dispatched actions
 */

import { createReducer, on } from '@ngrx/store';
import { NewsState, initialNewsState } from './news.state';
import * as NewsActions from './news.actions';

export const newsReducer = createReducer(
  initialNewsState,

  // Selection handlers
  on(NewsActions.selectNewsItem, (state, { id }) => ({
    ...state,
    selectedIds: state.selectedIds.includes(id)
      ? state.selectedIds
      : [...state.selectedIds, id],
  })),

  on(NewsActions.deselectNewsItem, (state, { id }) => ({
    ...state,
    selectedIds: state.selectedIds.filter(selectedId => selectedId !== id),
  })),

  on(NewsActions.selectMultipleNewsItems, (state, { ids }) => ({
    ...state,
    selectedIds: Array.from(new Set([...state.selectedIds, ...ids])),
  })),

  on(NewsActions.clearSelection, (state) => ({
    ...state,
    selectedIds: [],
  })),

  on(NewsActions.toggleNewsItemSelection, (state, { id }) => ({
    ...state,
    selectedIds: state.selectedIds.includes(id)
      ? state.selectedIds.filter(selectedId => selectedId !== id)
      : [...state.selectedIds, id],
  })),

  // Filter handlers
  on(NewsActions.setStatusFilter, (state, { status }) => ({
    ...state,
    filters: { ...state.filters, status },
  })),

  on(NewsActions.setLanguageFilter, (state, { language }) => ({
    ...state,
    filters: { ...state.filters, language },
  })),

  on(NewsActions.setCategoryFilter, (state, { category }) => ({
    ...state,
    filters: { ...state.filters, category },
  })),

  on(NewsActions.setSearchTerm, (state, { searchTerm }) => ({
    ...state,
    filters: { ...state.filters, searchTerm },
  })),

  on(NewsActions.clearFilters, (state) => ({
    ...state,
    filters: {
      status: undefined,
      language: undefined,
      category: undefined,
      searchTerm: undefined,
    },
  })),

  // Pagination handlers
  on(NewsActions.setCurrentPage, (state, { page }) => ({
    ...state,
    pagination: { ...state.pagination, currentPage: page },
  })),

  on(NewsActions.setPageSize, (state, { pageSize }) => ({
    ...state,
    pagination: { ...state.pagination, pageSize, currentPage: 1 },
  })),

  on(NewsActions.updateTotal, (state, { total }) => ({
    ...state,
    pagination: { ...state.pagination, total },
  })),

  // Loading handlers
  on(NewsActions.setLoading, (state, { isLoading }) => ({
    ...state,
    isLoading,
  })),

  on(NewsActions.setError, (state, { error }) => ({
    ...state,
    error,
  })),
);
