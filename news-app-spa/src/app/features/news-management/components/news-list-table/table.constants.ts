/**
 * News List Table - Configuration Constants
 * 
 * Single source of truth for all hardcoded values, timeouts, and UI configuration.
 * Centralizes magic numbers to improve maintainability and reduce duplication.
 * 
 * @version 1.0
 * @since 2026-04-26
 */

// ============================================================================
// PAGINATION & DATA LOADING
// ============================================================================
export const TABLE_CONFIG = {
    // Default and available page sizes for pagination
    DEFAULT_PAGE_SIZE: 20,
    PAGE_SIZE_OPTIONS: [5, 10, 20, 50, 100],

    // Search and filtering configuration
    SEARCH_CACHE_MAX_SIZE: 50,
    MAX_SEARCH_LENGTH: 500, // Prevent XSS and DoS attacks
    MAX_CATEGORY_ID_LENGTH: 36, // UUID length

    // Timing configuration (milliseconds)
    DEBOUNCE_TIME_MS: 150,
    REQUEST_TIMEOUT_MS: 5000, // 5 second timeout for hung requests
    SUCCESS_MESSAGE_AUTO_DISMISS_MS: 4000, // Auto-dismiss success after 4 seconds
    SMOOTH_SCROLL_BEHAVIOR: 'smooth' as const,
} as const;

// ============================================================================
// SPINNER & LOADING STATES
// ============================================================================
export const SPINNER_CONFIG = {
    DIAMETER: 50,
    STROKE_WIDTH: 4,
} as const;

// ============================================================================
// COLUMN CONFIGURATION
// ============================================================================
export const COLUMN_WIDTHS = {
    CHECKBOX: 50,
    TITLE: '30%',
    CATEGORY: '15%',
    STATUS: '12%',
    CREATED_BY: '12%',
    DATE: '12%',
    VIEWS: '8%',
    LIKES: '8%',
    COMMENTS: '10%',
    SHARES: '8%',
    BOOKMARKS: '10%',
    FEATURED: '8%',
    PUBLISHED: '10%',
    WORDCOUNT: '10%',
    READTIME: '10%',
    URGENCY: '10%',
    PRIORITY: '8%',
    SOURCE: '12%',
    UPDATED: '12%',
    BADGES: 120,
    ACTIONS: 80,
} as const;

export const COLUMN_MIN_WIDTHS = {
    TITLE: 250,
    CATEGORY: 120,
    STATUS: 110,
    CREATED_BY: 100,
    DATE: 140,
    VIEWS: 70,
    LIKES: 70,
    COMMENTS: 90,
    SHARES: 80,
    BOOKMARKS: 100,
    FEATURED: 70,
    PUBLISHED: 120,
    WORDCOUNT: 100,
    READTIME: 100,
    URGENCY: 100,
    PRIORITY: 80,
    SOURCE: 110,
    UPDATED: 130,
} as const;

// ============================================================================
// RESPONSIVE DESIGN BREAKPOINTS
// ============================================================================
export const RESPONSIVE_BREAKPOINTS = {
    TABLET: 768,
    TABLET_LANDSCAPE: 1024,
    DESKTOP: 1920,
} as const;

// ============================================================================
// DIALOG CONFIGURATION
// ============================================================================
export const DIALOG_CONFIG = {
    COLUMN_CUSTOMIZATION: {
        WIDTH: '90vw',
        MAX_WIDTH: '700px',
        MIN_WIDTH: '500px',
        MAX_HEIGHT: '85vh',
        DISABLE_CLOSE: false,
        PANEL_CLASS: 'column-customization-dialog-panel',
        HAS_BACKDROP: true,
        BACKDROP_CLASS: 'column-customization-backdrop',
    },
} as const;

// ============================================================================
// SORTING CONFIGURATION
// ============================================================================
export const SORT_CONFIG = {
    DEFAULT_COLUMN: 'newsNewsId' as string,
    DEFAULT_DIRECTION: 'desc' as const,
    DIRECTIONS: ['asc', 'desc'] as const,
} as const;

// ============================================================================
// ANIMATION TIMING
// ============================================================================
export const ANIMATION_TIMING = {
    SLIDE_DOWN_ENTER_MS: 300,
    SLIDE_DOWN_LEAVE_MS: 200,
    SLIDE_IN_OUT_ENTER_MS: 300,
    SLIDE_IN_OUT_LEAVE_MS: 200,
    HOVER_TRANSFORM_MS: 300,
} as const;

// ============================================================================
// ACCESSIBILITY & SEMANTICS
// ============================================================================
export const ACCESSIBILITY_CONFIG = {
    DEFAULT_LANGUAGE: 'en',
    SUPPORTED_LANGUAGES: ['en', 'es'],
} as const;

// ============================================================================
// ICON SIZES (Material Icons)
// ============================================================================
export const ICON_SIZES = {
    SORT_ICON: 14,
    COL_GROUP_ICON: 16,
    BADGE_ICON: 16,
    EMPTY_ICON: 64,
    ERROR_ICON: 64,
    BUTTON_ICON: 24,
    MENU_ICON: 24,
} as const;

// ============================================================================
// SORTING & DISPLAY OPTIONS
// ============================================================================
export const SEARCH_MODES = {
    ALL: 'all',
    TITLE: 'title',
    CONTENT: 'content',
} as const;

// Type for search mode
export type SearchMode = typeof SEARCH_MODES[keyof typeof SEARCH_MODES];
