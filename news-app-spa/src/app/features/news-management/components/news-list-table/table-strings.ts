/**
 * News List Table - Localized Strings & UI Text
 * 
 * Centralized text content for the News List Table component.
 * Organized by functional area for easy maintenance and future i18n migration.
 * 
 * Future Enhancement: Migrate to i18n service for multi-language support
 * 
 * @version 1.0
 * @since 2026-04-26
 */

// ============================================================================
// LOADING & EMPTY STATES
// ============================================================================
export const LOADING_STRINGS = {
    LOADING_MESSAGE: 'Loading news...',
} as const;

export const EMPTY_STATE_STRINGS = {
    EMPTY_HEADING: 'No news found',
    EMPTY_DESCRIPTION: 'Start by creating your first news',
    EMPTY_ICON: 'newspaper',
} as const;

export const ERROR_STATE_STRINGS = {
    ERROR_TITLE: 'Failed to Load News',
    RETRY_LABEL: 'Reload News',
    RETRY_LOADING_LABEL: 'Reloading...',
    DISMISS_LABEL: 'Dismiss',
} as const;

// ============================================================================
// TOOLBAR & ACTIONS
// ============================================================================
export const TOOLBAR_STRINGS = {
    CUSTOMIZE_COLUMNS_TOOLTIP: 'Customize columns',
    CUSTOMIZE_COLUMNS_ARIA_LABEL: 'Customize table columns',
} as const;

export const BULK_ACTIONS_STRINGS = {
    PUBLISH_BUTTON: 'Publish',
    DELETE_BUTTON: 'Delete',
    CLEAR_BUTTON: 'Clear',
    ITEMS_SELECTED_SINGULAR: 'news',
    ITEMS_SELECTED_PLURAL: 'newss', // Grammatically incorrect but matches template pattern
    SELECTED_SUFFIX: 'selected',
    PUBLISH_ARIA_LABEL: 'Publish selected news',
    DELETE_ARIA_LABEL: 'Delete selected news',
    CLEAR_ARIA_LABEL: 'Clear selection',
} as const;

// ============================================================================
// TABLE CONTENT & DATE DISPLAY
// ============================================================================
export const DATE_STRINGS = {
    NOT_PUBLISHED: 'Not published',
    NOT_SCHEDULED: '—', // Em dash for scheduled publish not set
    EMPTY_PLACEHOLDER: '—', // Em dash for null/empty values
    READ_TIME_UNIT: 'min', // Minutes suffix for read time
} as const;

export const TABLE_STRINGS = {
    NO_URGENCY: '—', // Em dash when no urgency level set
} as const;

// ============================================================================
// ACCESSIBILITY & ARIA LABELS
// ============================================================================
export const ACCESSIBILITY_STRINGS = {
    LOADING_ARIA_LABEL: 'Loading news',
    LOADING_ARIA_BUSY: true,
    BULK_ACTIONS_ROLE: 'toolbar',
    BULK_ACTIONS_ARIA_LABEL: 'Bulk actions',
    ERROR_ROLE: 'alert',
    ERROR_ARIA_LIVE: 'polite',
    SUCCESS_ROLE: 'status',
    SUCCESS_ARIA_LIVE: 'polite',
    TABLE_HEADER_TABINDEX: 0,
    COLUMN_HEADER_ROLE: 'columnheader',
    CHECKBOX_ARIA_LABEL: 'Select all',
} as const;

// ============================================================================
// COLUMN HEADERS & ICONS
// ============================================================================
export const COLUMN_HEADER_ICONS = {
    TITLE_ICON: 'title',
    CATEGORY_ICON: 'category',
    STATUS_ICON: 'published_with_changes',
    FEATURED_ICON: 'star',
    PUBLISHED_ICON: 'calendar_today',
    SCHEDULED_ICON: 'schedule_send',
    AUTHOR_ICON: 'person',
    VIEWS_ICON: 'visibility',
    LIKES_ICON: 'thumb_up',
    COMMENTS_ICON: 'comment',
    SHARES_ICON: 'share',
    BOOKMARKS_ICON: 'bookmark',
    EMPTY_ICON: 'newspaper',
    ERROR_ICON: 'error_outline',
    SUCCESS_ICON: 'check_circle',
    CLOSE_ICON: 'close',
    MENU_ICON: 'more_vert',
    SPINNER_ICON: 'refresh',
} as const;

// ============================================================================
// ACTION MENU & DIALOGS
// ============================================================================
export const ACTION_MENU_STRINGS = {
    EDIT_LABEL: 'Edit',
    VIEW_LABEL: 'View',
    DELETE_LABEL: 'Delete',
    PUBLISH_LABEL: 'Publish',
    ARCHIVE_LABEL: 'Archive',
    RESTORE_LABEL: 'Restore',
} as const;

export const DIALOG_STRINGS = {
    COLUMN_CUSTOMIZATION_DIALOG_TITLE: 'Customize Columns',
    DIALOG_CANCEL: 'Cancel',
    DIALOG_APPLY: 'Apply',
    DIALOG_SAVE: 'Save',
} as const;

// ============================================================================
// CONFIRMATION & WARNINGS
// ============================================================================
export const CONFIRMATION_STRINGS = {
    CONFIRM_DELETE_TITLE: 'Delete News',
    CONFIRM_DELETE_MESSAGE: 'Are you sure you want to delete this news item? This action cannot be undone.',
    CONFIRM_BUTTON: 'Confirm',
    CANCEL_BUTTON: 'Cancel',
} as const;

// ============================================================================
// CONSOLE & DEBUG MESSAGES (Development only)
// ============================================================================
export const DEBUG_STRINGS = {
    COMPONENT_INIT: '[NewsListTableComponent] Initializing table with best practices',
    COLUMN_PREFERENCES_CHANGED: '🔄 Column preferences changed:',
    OPENING_CUSTOMIZATION_DIALOG: '🔧 Opening column customization dialog',
    DIALOG_CLOSED: '📋 Dialog closed with result:',
    COLUMN_PREFERENCES_UPDATED: '✅ Column preferences updated, triggering change detection',
    DIALOG_CLOSED_WITHOUT_SAVING: 'ℹ️ Dialog closed without saving',
    FILTER_CHANGED: '[NewsListTableComponent] onFiltersChanged()',
    CLEARING_CACHE: '[NewsListTableComponent] Clearing search cache (size=%d)',
    EMITTING_FILTER_CHANGE: '[NewsListTableComponent] Emitting filterChange$ event',
    NO_ACTIVE_FILTERS: '[NewsListTableComponent] No active filters, loading all articles',
    ACTIVE_FILTERS_DETECTED: '[NewsListTableComponent] Active filters detected at init, skipping loadArticles()',
} as const;

// ============================================================================
// TYPE DEFINITIONS FOR AUTOCOMPLETE & IDE SUPPORT
// ============================================================================
export type LoadingStrings = typeof LOADING_STRINGS;
export type EmptyStateStrings = typeof EMPTY_STATE_STRINGS;
export type ErrorStateStrings = typeof ERROR_STATE_STRINGS;
export type ToolbarStrings = typeof TOOLBAR_STRINGS;
export type BulkActionsStrings = typeof BULK_ACTIONS_STRINGS;
export type DateStrings = typeof DATE_STRINGS;
export type AccessibilityStrings = typeof ACCESSIBILITY_STRINGS;
export type ColumnHeaderIcons = typeof COLUMN_HEADER_ICONS;
export type ActionMenuStrings = typeof ACTION_MENU_STRINGS;
export type DialogStrings = typeof DIALOG_STRINGS;
export type ConfirmationStrings = typeof CONFIRMATION_STRINGS;
