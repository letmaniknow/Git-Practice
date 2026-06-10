/**
 * NewsSearchFiltersComponent - Search & Filter Controls
 * 
 * Responsibility: Manages ALL search and filter UI controls
 * - Search text input with different search modes
 * - Workflow status multi-select filter
 * - Category single-select filter
 * - Date range picker (from/to dates)
 * - Admin user autocomplete dropdown
 * 
 * Communication: Pure Input/Output pattern (no Store injection)
 * - Receives filter state via @Input
 * - Emits filter changes via @Output
 * - Can be tested independently of Store or parent logic
 * 
 * Reusability: Can be used in:
 * - News management dashboard (current use)
 * - Ads management dashboard (future)
 * - Products management dashboard (future)
 */

import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, OnChanges, SimpleChanges, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { Subject, takeUntil } from 'rxjs';

import { NewsAdvancedSearchService, AdminUser } from '../../services/news-advanced-search.service';
import { NewsCategory } from '../../models/news-form.model';
import { FormFieldsModule } from '@shared/form-fields';
import {
  TextInputFieldComponent,
  SelectFieldComponent,
  MultiSelectFieldComponent,
  AutocompleteFieldComponent,
} from '@shared/form-fields/components';
import { ISelectOption } from '@shared/form-fields/models/form-field.models';

/**
 * Interface: Filter state passed from parent
 * Clearly defines all possible filters in one place
 */
export interface INewsSearchFiltersState {
  searchTextInput: string;
  searchModeSelected: 'all' | 'title' | 'content';
  workflowStatusesSelected: string[];
  categoryIdSelected: string | null;
  createdDateFrom: Date | null;
  createdDateTo: Date | null;
  createdByAdminUserIdSelected: string | null;
}

/**
 * Interface: Event emitted when any filter changes
 * Parent receives this and knows what changed
 */
export interface INewsSearchFiltersChangedEvent {
  changedFilterType: 'search' | 'searchMode' | 'status' | 'category' | 'dates' | 'createdBy' | 'clearAll';
  updatedFiltersState: INewsSearchFiltersState;
}

@Component({
  selector: 'app-news-search-filters',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatButtonToggleModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatOptionModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatAutocompleteModule,
    FormFieldsModule,
  ],
  templateUrl: './news-search-filters.component.html',
  styleUrl: './news-search-filters.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewsSearchFiltersComponent implements OnInit, OnDestroy, OnChanges {
  private destroy$ = new Subject<void>();

  // ============================================================================
  // INPUT: Receive filter state from parent (NewsCardListPageComponent)
  // ============================================================================
  @Input() currentFiltersState!: INewsSearchFiltersState;

  // Data to populate form field components
  @Input() availableCategories: NewsCategory[] = [];
  @Input() availableWorkflowStatuses: string[] = [];

  // ============================================================================
  // OUTPUT: Emit when user changes any filter
  // ============================================================================
  @Output() filtersChanged = new EventEmitter<INewsSearchFiltersChangedEvent>();

  // ============================================================================
  // Form Field Component - Option Arrays
  // ============================================================================
  categoryOptions: ISelectOption[] = [];
  workflowStatusOptions: ISelectOption[] = [];
  adminUserOptions: ISelectOption[] = [];

  // ============================================================================
  // Component State - Form Values
  // ============================================================================

  // Search UI state
  searchTextInput: string = '';
  searchModeSelected: 'all' | 'title' | 'content' = 'all';

  // Filter UI state - Statuses
  selectedStatusValues: string[] = []; // String array for template binding
  get workflowStatusesSelected(): string[] {
    return this.selectedStatusValues;
  }

  // Filter UI state - Category
  selectedCategoryValue: string | null = null; 
  get categoryIdSelected(): string | null {
    return this.selectedCategoryValue;
  }

  // Filter UI state - Date Range
  createdDateFrom: Date | null = null;
  createdDateTo: Date | null = null;

  // Filter UI state - Admin User (via autocomplete)
  adminUserSearchText: string = ''; 
  selectedAdminUser: ISelectOption | null = null;
  adminUsersAvailable: AdminUser[] = [];
  adminUsersFiltered: ISelectOption[] = [];
  isAdminUsersLoading: boolean = false;

  // Admin user getters for template binding
  get createdByAdminUserIdSelected(): string | null {
    return this.selectedAdminUser?.value as string || null;
  }

  get createdByAdminUserNameDisplay(): string {
    return this.selectedAdminUser?.label || '';
  }

  // Computed properties
  get activeFilterCount(): number {
    let count = 0;
    if (this.searchTextInput.trim()) count += 1;
    if (this.selectedStatusValues.length > 0) count += this.selectedStatusValues.length;
    if (this.selectedCategoryValue) count += 1;
    if (this.createdDateFrom) count += 1;
    if (this.createdDateTo) count += 1;
    if (this.selectedAdminUser) count += 1;
    return count;
  }

  get hasAnyFiltersApplied(): boolean {
    return this.activeFilterCount > 0;
  }

  constructor(
    private adminSearchService: NewsAdvancedSearchService,
    private changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.initializeFormFromInputState();
    // Build options if they're already available (for initial render)
    if (this.availableCategories.length > 0 || this.availableWorkflowStatuses.length > 0) {
      this.buildFormFieldOptions();
    }
    this.loadDataForDropdowns();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Rebuild form field options when input arrays change
    if (changes['availableCategories'] || changes['availableWorkflowStatuses']) {
      this.buildFormFieldOptions();
      // Mark for check to ensure UI updates with OnPush change detection
      this.changeDetectorRef.markForCheck();
    }
  }

  // ============================================================================
  // Initialization Methods
  // ============================================================================

  /**
   * Copy input state into local form values
   * So UI reflects current filter state from parent
   */
  private initializeFormFromInputState(): void {
    if (this.currentFiltersState) {
      this.searchTextInput = this.currentFiltersState.searchTextInput;
      this.searchModeSelected = this.currentFiltersState.searchModeSelected;
      // Keep workflow status as string array for template
      this.selectedStatusValues = this.currentFiltersState.workflowStatusesSelected;
      // Keep category ID as string for template
      this.selectedCategoryValue = this.currentFiltersState.categoryIdSelected || null;
      this.createdDateFrom = this.currentFiltersState.createdDateFrom;
      this.createdDateTo = this.currentFiltersState.createdDateTo;
    }
  }

  /**
   * Build option arrays for form field components from input data
   */
  private buildFormFieldOptions(): void {
    // Build category options
    this.categoryOptions = this.availableCategories.map(category => ({
      value: category.id,
      label: category.categoryNameEn,
    }));

    // Build workflow status options
    this.workflowStatusOptions = this.availableWorkflowStatuses.map(status => ({
      value: status,
      label: this.getStatusDisplayName(status),
    }));
  }

  /**
   * Load initial data for all dropdowns
   */
  private loadDataForDropdowns(): void {
    this.loadAdminUsersForAutocomplete();
  }

  /**
   * Load list of admin users for autocomplete dropdown
   * GRACEFUL DEGRADATION: If this fails, filter still works (just no autocomplete)
   */
  private loadAdminUsersForAutocomplete(): void {
    this.isAdminUsersLoading = true;
    this.adminSearchService.getAdminUsers().pipe(takeUntil(this.destroy$)).subscribe({
      next: (users: any[]) => {
        this.adminUsersAvailable = users;
        // Convert to ISelectOption format
        this.adminUsersFiltered = users.map(user => ({
          value: user.id,
          label: `${user.name} (${user.email})`,
        }));
        this.isAdminUsersLoading = false;
        // Filter by whatever the user has typed by the time load completes
        if (this.adminUserSearchText.trim()) {
          this.filterAdminUsersBasedOnInput(this.adminUserSearchText);
        }
      },
      error: (error: any) => {
        console.warn('[NewsSearchFilters] ⚠️ Admin users autocomplete load failed (non-critical) - filter still works:', error);
        this.adminUsersAvailable = [];
        this.adminUsersFiltered = [];
        this.isAdminUsersLoading = false;
        // Filter still works, just no autocomplete suggestions
      },
    });
  }

  // ============================================================================
  // Event Handlers - Search Input
  // ============================================================================

  /**
   * User typed in search box: emit filter change
   */
  onSearchTextChanged(searchText: string = this.searchTextInput): void {
    this.searchTextInput = searchText;
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      searchTextInput: this.searchTextInput,
    };
    this.emitFilterChanged('search', updatedState);
  }

  /**
   * User clicked search mode button (All/Title/Content)
   */
  onSearchModeSelected(mode: 'all' | 'title' | 'content'): void {
    this.searchModeSelected = mode;
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      searchModeSelected: mode,
      searchTextInput: this.searchTextInput, // Preserve search text
    };
    this.emitFilterChanged('searchMode', updatedState);
  }

  /**
   * User clicked clear search button
   */
  onClearSearchClicked(): void {
    this.searchTextInput = '';
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      searchTextInput: '',
    };
    this.emitFilterChanged('search', updatedState);
  }

  // ============================================================================
  // Event Handlers - Workflow Status Filter
  // ============================================================================

  /**
   * Form field changed: workflow status multi-select
   * Receives array of raw values (strings) from MultiSelectFieldComponent
   */
  onWorkflowStatusesChanged(selectedValuesOrEvent: (string | number)[] | any): void {
    const selectedValues = selectedValuesOrEvent?.value || selectedValuesOrEvent;
    this.selectedStatusValues = Array.isArray(selectedValues) ? (selectedValues as string[]) : [];
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      workflowStatusesSelected: this.selectedStatusValues,
    };
    this.emitFilterChanged('status', updatedState);
  }

  /**
   * Remove a single status from the active-filter chips row
   */
  onRemoveStatusChip(statusValue: string): void {
    this.selectedStatusValues = this.selectedStatusValues.filter((val) => val !== statusValue);
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      workflowStatusesSelected: this.selectedStatusValues,
    };
    this.emitFilterChanged('status', updatedState);
  }

  // ============================================================================
  // Event Handlers - Category Filter
  // ============================================================================

  /**
   * Form field changed: category single-select
   * Receives raw value (string) or null from SelectFieldComponent
   */
  onCategorySelected(selectedValueOrEvent: string | number | null | any): void {
    const selectedValue = selectedValueOrEvent?.value ?? selectedValueOrEvent;
    this.selectedCategoryValue = selectedValue ? (selectedValue as string) : null;
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      categoryIdSelected: this.selectedCategoryValue,
    };
    this.emitFilterChanged('category', updatedState);
  }

  // ============================================================================
  // Event Handlers - Date Range Filter
  // ============================================================================

  /**
   * Range picker start date changed
   */
  onCreatedDateFromChanged(dateOrEvent: Date | null | any): void {
    const date = dateOrEvent?.value ?? dateOrEvent;
    this.createdDateFrom = date;
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      createdDateFrom: date,
    };
    this.emitFilterChanged('dates', updatedState);
  }

  /**
   * Range picker end date changed — fires when the user completes the range
   */
  onCreatedDateToChanged(dateOrEvent: Date | null | any): void {
    const date = dateOrEvent?.value ?? dateOrEvent;
    this.createdDateTo = date;
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      createdDateTo: date,
    };
    this.emitFilterChanged('dates', updatedState);
  }

  /**
   * Clear both dates at once (× button inside the field, or chip removal)
   */
  onClearDateRangeClicked(): void {
    this.createdDateFrom = null;
    this.createdDateTo = null;
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      createdDateFrom: null,
      createdDateTo: null,
    };
    this.emitFilterChanged('dates', updatedState);
  }

  // ============================================================================
  // Event Handlers - Admin User Autocomplete Filter
  // ============================================================================

  /**
   * Autocomplete input text changed
   * User typed in the search field
   */
  onAdminUserSearchChanged(searchText: string): void {
    this.adminUserSearchText = searchText;
    if (this.adminUsersAvailable.length === 0 && !this.isAdminUsersLoading) {
      this.loadAdminUsersForAutocomplete();
    } else {
      this.filterAdminUsersBasedOnInput(searchText);
    }
  }

  /**
   * Filter admin users list based on search input
   */
  private filterAdminUsersBasedOnInput(searchQuery: string): void {
    if (!searchQuery || searchQuery.trim() === '') {
      this.adminUsersFiltered = this.adminUsersAvailable.map(user => ({
        value: user.id,
        label: `${user.name} (${user.email})`,
      }));
      return;
    }
    const query = searchQuery.toLowerCase();
    this.adminUsersFiltered = this.adminUsersAvailable
      .filter(
        (user) =>
          (user.name?.toLowerCase() ?? '').includes(query) ||
          (user.email?.toLowerCase() ?? '').includes(query)
      )
      .map(user => ({
        value: user.id,
        label: `${user.name} (${user.email})`,
      }));
  }

  /**
   * Form field changed: admin user autocomplete
   * Receives ISelectOption selected by user
   */
  onAdminUserSelected(selectedOptionOrEvent: ISelectOption | null | any): void {
    const selectedOption = selectedOptionOrEvent?.option?.value || selectedOptionOrEvent;
    if (!selectedOption) return;
    
    this.selectedAdminUser = selectedOption;
    const userId = selectedOption.value as string;
    
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      createdByAdminUserIdSelected: userId,
    };
    this.emitFilterChanged('createdBy', updatedState);
  }

  /**
   * User clicked clear button for admin user filter
   */
  onClearAdminUserFilterClicked(): void {
    this.selectedAdminUser = null;
    this.adminUserSearchText = '';
    this.adminUsersFiltered = this.adminUsersAvailable.map(user => ({
      value: user.id,
      label: `${user.name} (${user.email})`,
    }));
    const updatedState: INewsSearchFiltersState = {
      ...this.currentFiltersState,
      createdByAdminUserIdSelected: null,
    };
    this.emitFilterChanged('createdBy', updatedState);
  }

  // ============================================================================
  // Event Handlers - Clear All Filters
  // ============================================================================

  /**
   * User clicked "Clear All Filters" button
   */
  onClearAllFiltersClicked(): void {
    this.searchTextInput = '';
    this.searchModeSelected = 'all';
    this.selectedStatusValues = [];
    this.selectedCategoryValue = null;
    this.createdDateFrom = null;
    this.createdDateTo = null;
    this.selectedAdminUser = null;
    this.adminUserSearchText = '';
    this.adminUsersFiltered = this.adminUsersAvailable.map(user => ({
      value: user.id,
      label: `${user.name} (${user.email})`,
    }));

    const clearedState: INewsSearchFiltersState = {
      searchTextInput: '',
      searchModeSelected: 'all',
      workflowStatusesSelected: [],
      categoryIdSelected: null,
      createdDateFrom: null,
      createdDateTo: null,
      createdByAdminUserIdSelected: null,
    };

    // CRITICAL: Use detectChanges() to immediately update child components with cleared values
    // This must happen BEFORE emitting the event to parent
    this.changeDetectorRef.detectChanges();

    // Emit AFTER a tiny delay to prevent the parent from updating @Input properties
    // which would trigger ngOnChanges again with old values
    setTimeout(() => {
      this.emitFilterChanged('clearAll', clearedState);
    }, 0);
  }

  // ============================================================================
  // Utility Methods
  // ============================================================================

  /**
   * Emit filter change event to parent
   * Called every time user changes any filter
   */
  private emitFilterChanged(
    changedFilterType: 'search' | 'searchMode' | 'status' | 'category' | 'dates' | 'createdBy' | 'clearAll',
    updatedState: INewsSearchFiltersState
  ): void {
    const event: INewsSearchFiltersChangedEvent = {
      changedFilterType,
      updatedFiltersState: updatedState,
    };
    this.filtersChanged.emit(event);
  }

  /**
   * Get display name for category by ID
   * Used in template to show category name instead of ID
   */
  getCategoryNameById(categoryId: string | null): string {
    if (!categoryId) return '';
    const category = this.availableCategories.find((c) => c.id === categoryId);
    return category ? category.categoryNameEn : categoryId;
  }

  /**
   * Get display name for workflow status
   * Used in template to show human-readable status
   */
  getStatusDisplayName(status: string): string {
    // Convert DRAFT → Draft, PUBLISHED → Published
    return status.charAt(0) + status.slice(1).toLowerCase();
  }
}
