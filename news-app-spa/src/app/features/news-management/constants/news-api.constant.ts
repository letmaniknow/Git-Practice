import { environment } from '@environments/environment';

/**
 * News Feature API Endpoints
 * 
 * Centralized management of all news-related API endpoints.
 * Self-contained and maintainable per feature.
 * 
 * Usage in services:
 * import { NEWS_API_ENDPOINTS } from '../constants/news-api.constant';
 * 
 * Example:
 * this.http.get(NEWS_API_ENDPOINTS.news.searchAdvanced({ params }))
 */

const BASE_URL = environment.apiBaseUrl;

export const NEWS_API_ENDPOINTS = {
  // News CRUD endpoints
  news: {
    list: `${BASE_URL}/api/v1/admin/news`,
    create: `${BASE_URL}/api/v1/admin/news`,
    getById: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}`,
    update: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}`,
    // Delete operations (explicit soft vs permanent)
    softDelete: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}/softdelete`,
    permanentDelete: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}/permanentdelete`,
    delete: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}/softdelete`, // Alias for backward compatibility
    
    // Search endpoints
    search: {
      multiField: `${BASE_URL}/api/v1/admin/news/search-multi-field`,
      byTitle: `${BASE_URL}/api/v1/admin/news/search-by-title`,
      byContent: `${BASE_URL}/api/v1/admin/news/search-by-content`,
      advanced: `${BASE_URL}/api/v1/admin/news/search-advanced`,
    },
    
    // Publishing & status management (using workflow endpoint)
    publish: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}/workflow?newStatus=PUBLISHED`,
    unpublish: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}/workflow?newStatus=DRAFT`,
    schedule: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}/schedule`,
    workflowStatuses: `${BASE_URL}/api/v1/admin/news/workflow-statuses`,
    
    // Bulk operations (explicit soft vs permanent delete)
    bulk: {
      publish: `${BASE_URL}/api/v1/admin/news/bulk/publish`,
      unpublish: `${BASE_URL}/api/v1/admin/news/bulk/unpublish`,
      softDelete: `${BASE_URL}/api/v1/admin/news/bulksoftdelete`,
      permanentDelete: `${BASE_URL}/api/v1/admin/news/bulk/permanentdelete`,
      delete: `${BASE_URL}/api/v1/admin/news/bulksoftdelete`, // Alias for backward compatibility
      updateStatus: `${BASE_URL}/api/v1/admin/news/bulk/status`,
    },
    
    // Media management
    media: `${BASE_URL}/api/v1/public/news/media`,
    mediaUpload: `${BASE_URL}/api/v1/admin/news/media/upload`,
    mediaFile: (filename: string) => `${BASE_URL}/api/v1/public/news/media/${encodeURIComponent(filename)}`,
    
    // Audit trail
    auditLogs: (id: string) => `${BASE_URL}/api/v1/admin/news/${id}/audit-logs`,
  },

  // Category endpoints
  categories: {
    list: `${BASE_URL}/api/v1/admin/news-categories`,
    getById: (id: string) => `${BASE_URL}/api/v1/admin/news-categories/${id}`,
    create: `${BASE_URL}/api/v1/admin/news-categories`,
    update: (id: string) => `${BASE_URL}/api/v1/admin/news-categories/${id}`,
    delete: (id: string) => `${BASE_URL}/api/v1/admin/news-categories/${id}`,
  },

  // Staff/User management (used in news form autocomplete)
  staff: {
    autocomplete: `${BASE_URL}/api/v1/admin/staff/autocomplete`,
    list: `${BASE_URL}/api/v1/admin/staff`,
    getById: (id: string) => `${BASE_URL}/api/v1/admin/staff/${id}`,
  },
};
