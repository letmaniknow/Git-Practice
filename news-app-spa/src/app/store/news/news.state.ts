/**
 * News feature state interface
 * Manages news item selection, filters, and pagination state
 */

import { NewsItem } from '../../features/news-management/models/news-item.model';

export interface NewsState {
  // Selected news IDs for bulk operations
  selectedIds: string[];
  
  // Current filters applied
  filters: {
    status?: string;
    language?: string;
    category?: string;
    searchTerm?: string;
  };
  
  // Pagination state
  pagination: {
    currentPage: number;
    pageSize: number;
    total: number;
  };
  
  // Loading state
  isLoading: boolean;
  error: string | null;
}

export const initialNewsState: NewsState = {
  selectedIds: [],
  filters: {
    status: undefined,
    language: undefined,
    category: undefined,
    searchTerm: undefined,
  },
  pagination: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
  isLoading: false,
  error: null,
};
