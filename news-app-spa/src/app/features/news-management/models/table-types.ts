/**
 * Type-Safe Interfaces for News List Table Component
 * 
 * Provides comprehensive type definitions for:
 * - Table state management
 * - Column configuration
 * - Row context and metadata
 * - Filter state
 * - Action handlers
 * 
 * Ensures type safety across component, template, and services
 * with strong contracts and autocomplete support.
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-04-26
 */

import { NewsItem } from './news-item.model';
import { NewsCategory } from './news-form.model';

/**
 * Column visibility and configuration state
 */
export interface IColumnPreferences {
    visibleColumnIds: string[];
    columnWidths?: Map<string, number | string>;
    frozenColumns?: string[];
}

/**
 * Table sorting state
 */
export interface ISortState {
    column: string;
    direction: 'asc' | 'desc';
}

/**
 * Table pagination state
 */
export interface IPaginationState {
    currentPage: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
    pageSizeOptions: number[];
}

/**
 * Table filter/search state
 */
export interface ITableFilters {
    searchTerm: string;
    searchMode: 'text' | 'all';
    workflowStatuses: string[];
    categoryId: string | null;
    createdBy: string | null;
    createdDateFrom: Date | null;
    createdDateTo: Date | null;
    urgencyLevels?: string[];
}

/**
 * Selected rows state for bulk operations
 */
export interface ISelectionState {
    selectedIds: Set<string>;
    selectedCount: number;
    isAllSelected: boolean;
}

/**
 * Complete table state for state management
 */
export interface ITableState {
    news: NewsItem[];
    loading: boolean;
    error: string | null;
    success: string | null;
    sort: ISortState;
    pagination: IPaginationState;
    filters: ITableFilters;
    selection: ISelectionState;
    columns: IColumnPreferences;
    expandedRowIds?: Set<string>;
}

/**
 * Column metadata configuration
 */
export interface IColumnDefinition {
    id: string;
    header: string;
    field?: string;
    type: 'text' | 'number' | 'date' | 'status' | 'urgency' | 'action' | 'checkbox' | 'badge';
    width?: number | string;
    minWidth?: number | string;
    visible: boolean;
    sortable?: boolean;
    groupName?: string; // For column grouping (Identity, Timeline, Metrics, etc)
    hidden?: boolean; // Hidden from column customization dialog
    responsive?: {
        hideOn?: ('mobile' | 'tablet' | 'desktop')[];
        showOn?: ('mobile' | 'tablet' | 'desktop')[];
    };
    align?: 'left' | 'center' | 'right';
    ariaLabel?: string;
}

/**
 * Row context for template binding
 */
export interface IRowContext {
    index: number;
    isSelected: boolean;
    isExpanded?: boolean;
    item: NewsItem;
    isHovered?: boolean;
}

/**
 * Action handler signatures for bulk and row operations
 */
export interface ITableActions {
    onBulkPublish: (ids: string[]) => void;
    onBulkDelete: (ids: string[]) => void;
    onBulkStatusChange?: (ids: string[], status: string) => void;
    onRowEdit?: (item: NewsItem) => void;
    onRowDelete?: (id: string) => void;
    onRowSelect?: (id: string) => void;
    onRowDeselect?: (id: string) => void;
}

/**
 * Responsive layout configuration based on screen size
 */
export interface IResponsiveConfig {
    breakpoint: 'mobile' | 'tablet' | 'desktop';
    viewportWidth: number;
    compactMode: boolean;
    columnCount: number;
    showExpandButton: boolean;
    singleColumnLayout: boolean;
}

/**
 * Error state with categorization
 */
export interface IErrorState {
    message: string | null;
    category?: string;
    severity?: 'info' | 'warning' | 'error' | 'critical';
    retryable?: boolean;
    action?: 'retry' | 'login' | 'refresh' | 'navigate';
}

/**
 * Success notification state
 */
export interface ISuccessState {
    message: string | null;
    autoDismissMs?: number;
    action?: {
        label: string;
        handler: () => void;
    };
}

/**
 * Keyboard navigation state (for accessibility)
 */
export interface IKeyboardNavigation {
    activeRowIndex: number | null;
    activeCellColumn: string | null;
    isArrowNavigating: boolean;
}

/**
 * Viewport and responsive state
 */
export interface IViewportState {
    width: number;
    height: number;
    breakpoint: 'mobile' | 'tablet' | 'desktop';
    isTouchDevice: boolean;
    densityMode: 'compact' | 'normal' | 'spacious';
}

/**
 * Data export configuration (for future CSV/Excel export)
 */
export interface IExportConfig {
    format: 'csv' | 'xlsx' | 'pdf';
    includeColumns: string[];
    includeHiddenColumns: boolean;
    dateFormat: string;
}

/**
 * Type guard functions for runtime type checking
 */
export const TypeGuards = {
    isColumnDefinition: (obj: any): obj is IColumnDefinition => {
        return obj && typeof obj === 'object' && 'id' in obj && 'header' in obj && 'type' in obj;
    },

    isSortState: (obj: any): obj is ISortState => {
        return obj && typeof obj === 'object' && 'column' in obj && 'direction' in obj;
    },

    isTableState: (obj: any): obj is ITableState => {
        return obj && typeof obj === 'object' && 'news' in obj && 'pagination' in obj && 'filters' in obj;
    },

    isRowContext: (obj: any): obj is IRowContext => {
        return obj && typeof obj === 'object' && 'index' in obj && 'item' in obj;
    },

    isErrorState: (obj: any): obj is IErrorState => {
        return obj && typeof obj === 'object' && 'message' in obj;
    },

    isViewportState: (obj: any): obj is IViewportState => {
        return obj && typeof obj === 'object' && 'width' in obj && 'breakpoint' in obj;
    }
};
