/**
 * AutocompleteFieldComponent
 * Reusable autocomplete field with parent-controlled filtering
 *
 * ARCHITECT PATTERN: Dumb/Presentational Component
 * ✅ ZERO business logic - parent handles EVERYTHING
 * ✅ ZERO API calls - parent fetches & filters
 * ✅ ZERO debouncing - parent controls input timing
 * ✅ Display-only component
 * ✅ Pure Input/Output pattern
 * ✅ Reusable across all features
 *
 * CRITICAL: This component is PURELY PRESENTATIONAL
 * ========================================================
 * PARENT (Smart Component) is responsible for:
 * ✅ Listening to inputChanged events
 * ✅ Making API calls to fetch data
 * ✅ Filtering the results
 * ✅ Debouncing input (if needed)
 * ✅ Managing loading state
 * ✅ Passing filtered items via @Input
 *
 * EXAMPLE PARENT IMPLEMENTATION:
 * ```typescript
 * export class NewsSearchFiltersComponent {
 *   filteredUsers: IUserOption[] = [];
 *   userSearchLoading = false;
 *   userSearchTimeout: any;
 *
 *   constructor(private adminService: AdminService) {}
 *
 *   onUserInputChanged(inputValue: string) {
 *     // Parent handles debouncing
 *     clearTimeout(this.userSearchTimeout);
 *     this.userSearchTimeout = setTimeout(() => {
 *       if (inputValue.length >= 3) {
 *         this.userSearchLoading = true;
 *         // Parent makes API call
 *         this.adminService.searchUsers(inputValue).subscribe(
 *           (users) => {
 *             // Parent filters/transforms results
 *             this.filteredUsers = users.map(u => ({
 *               value: u.id,
 *               label: u.fullName,
 *               icon: 'person'
 *             }));
 *             this.userSearchLoading = false;
 *           }
 *         );
 *       }
 *     }, 300);
 *   }
 * }
 *
 * // Template:
 * <app-autocomplete-field
 *   label="Search Users"
 *   placeholder="Type user name"
 *   [filteredItems]="filteredUsers"
 *   [isLoading]="userSearchLoading"
 *   [minCharsToFilter]="3"
 *   (inputChanged)="onUserInputChanged($event)"
 *   (itemSelected)="onUserSelected($event)"
 * ></app-autocomplete-field>
 * ```
 *
 * @category FormFields
 * @module AutocompleteFieldComponent
 */

import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
  OnChanges,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ISelectOption } from '../../models/form-field.models';
import {
  generateFieldId,
  generateAriaDescribedBy,
} from '../../utils/form-field.utils';

/**
 * AutocompleteFieldComponent
 *
 * Features:
 * - Input field with autocomplete suggestions
 * - Material autocomplete panel
 * - Loading indicator (shows parent's async state)
 * - Minimum characters to filter threshold
 * - Optional icon support for items
 * - Optional item descriptions
 * - Error message display
 * - Clear button to reset selection
 * - Accessibility: WCAG AA compliant
 * - Material Design 3: Outline appearance
 *
 * CSS Variables Used (from theme.config.ts):
 * - --components-input-height (44px) - WCAG AA touch target
 * - --color-border (#e5e7eb) - outline color
 *
 * @component
 */
@Component({
  selector: 'app-autocomplete-field',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './autocomplete-field.component.html',
  styleUrl: './autocomplete-field.component.css',
})
export class AutocompleteFieldComponent<T = string | number>
  implements OnInit, OnDestroy, OnChanges {
  /**
   * INPUTS: Properties passed from parent (Smart Component)
   * Parent controls EVERYTHING: API calls, filtering, debouncing
   */

  /** Field label (Material form label) */
  @Input() label: string = '';

  /** Placeholder text */
  @Input() placeholder: string = '';

  /** Hint text (shows below input when focused) - Material Design 3 standard */
  @Input() hint: string = '';

  /** Current input value in search field */
  @Input() inputValue: string = '';

  /** Currently selected item (after selection) */
  @Input() selectedItem: ISelectOption<T> | null = null;

  /** Filtered items to display (PARENT provides this via API) */
  @Input() filteredItems: ISelectOption<T>[] = [];

  /** Parent-controlled loading state (showing spinner) */
  @Input() isLoading: boolean = false;

  /** Property to display from item object */
  @Input() displayProperty: string = 'label';

  /** Property to match against when comparing items */
  @Input() compareProperty: string = 'value';

  /** Minimum characters to start filtering */
  @Input() minCharsToFilter: number = 1;

  /** Disable field interaction */
  @Input() disabled: boolean = false;

  /** Mark as required field */
  @Input() required: boolean = false;

  /** Error message to display (if any) */
  @Input() hasErrorMessage: string | null = null;

  /** Show description under display value */
  @Input() showItemDescription: boolean = true;

  /** CSS class for custom styling */
  @Input() customClass: string = '';

  /** Accessibility: aria-label override */
  @Input() ariaLabel: string = '';

  /**
   * OUTPUTS: Events emitted to parent (Smart Component)
   * Parent MUST listen and handle all business logic
   */

  /** CRITICAL: Emitted when user types - parent must listen and call API */
  @Output() inputChanged = new EventEmitter<string>();

  /** Emitted when item is selected from suggestions */
  @Output() itemSelected = new EventEmitter<ISelectOption<T>>();

  /** Emitted when selection cleared */
  @Output() cleared = new EventEmitter<void>();

  /** Emitted when field receives focus */
  @Output() focused = new EventEmitter<void>();

  /** Emitted when field loses focus */
  @Output() blurred = new EventEmitter<void>();

  /**
   * COMPONENT STATE
   * Internal state for rendering
   */

  /** Generated unique ID for field + label binding */
  fieldId: string = '';

  /** Track if field is focused (for styling) */
  isFocused: boolean = false;

  /** Generated aria-describedby for error messages */
  ariaDescribedBy: string = '';

  /** Track if autocomplete panel is open */
  panelOpen: boolean = false;

  /** @ViewChild reference to autocomplete trigger */
  @ViewChild('autocomplete') autocomplete: any;

  /**
   * LIFECYCLE HOOKS
   */

  ngOnInit(): void {
    // Generate unique IDs for accessibility
    this.fieldId = generateFieldId('autocomplete-field');
    this.ariaDescribedBy = generateAriaDescribedBy(this.fieldId);
  }

  /**
   * CRITICAL: Sync component state when @Input properties change from parent
   * This ensures that when parent sets selectedItem to null or inputValue to empty string,
   * the component updates immediately
   */
  ngOnChanges(changes: SimpleChanges): void {
    // Monitor selectedItem input changes
    if (changes['selectedItem'] && !changes['selectedItem'].firstChange) {
      // When selectedItem is cleared (set to null), ensure inputValue is also cleared
      if (!this.selectedItem) {
        this.inputValue = '';
      }
    }
  }

  ngOnDestroy(): void {
    // Clean up event emitters
    this.inputChanged.complete();
    this.itemSelected.complete();
    this.cleared.complete();
    this.focused.complete();
    this.blurred.complete();
  }

  /**
   * EVENT HANDLERS
   * Called from template, emit events to parent
   */

  /**
   * CRITICAL: When user types, emit to parent
   * Parent MUST handle API call, filtering, debouncing
   * @param value - Input value
   */
  onInputChange(value: string): void {
    this.inputValue = value;
    // Parent listens to this and makes API call
    this.inputChanged.emit(value);
  }

  /**
   * Handle item selection from autocomplete panel
   * @param item - Selected ISelectOption
   */
  onItemSelected(item: ISelectOption<T>): void {
    this.selectedItem = item;
    this.inputValue = item.label;
    this.itemSelected.emit(item);
  }

  /**
   * Clear selection and input
   */
  onClear(): void {
    this.selectedItem = null;
    this.inputValue = '';
    this.cleared.emit();

    // Re-emit inputChanged with empty value
    // Parent should reset filtered items
    this.inputChanged.emit('');
  }

  /**
   * Handle field focus
   */
  onFocus(): void {
    this.isFocused = true;
    this.focused.emit();
  }

  /**
   * Handle field blur
   */
  onBlur(): void {
    this.isFocused = false;
    this.panelOpen = false;
    this.blurred.emit();
  }

  /**
   * Handle autocomplete panel open/close
   * @param isOpen - Panel open state
   */
  onPanelStateChanged(isOpen: boolean): void {
    this.panelOpen = isOpen;
  }

  /**
   * HELPER METHODS
   */

  /**
   * Check if should show filtered items
   * Items only shown if enough characters typed
   */
  shouldShowFilteredItems(): boolean {
    return (
      this.inputValue.length >= this.minCharsToFilter &&
      this.filteredItems.length > 0
    );
  }

  /**
   * Get display value for item
   * @param item - Item to display
   * @returns Display text
   */
  getDisplayValue(item: ISelectOption<T>): string {
    return item.label || String(item.value);
  }

  /**
   * TrackBy function for ngFor (performance optimization)
   * @param index - Index of item
   * @param item - Item being tracked
   * @returns Unique identifier
   */
  trackByOptionValue(index: number, item: ISelectOption<T>): string | number {
    return item.value as string | number;
  }

  /**
   * GETTERS
   * Computed properties for template
   */

  /** CSS class list for form field container */
  get fieldContainerClass(): string {
    return [
      this.customClass,
      this.hasErrorMessage ? 'has-error' : '',
      this.isFocused ? 'is-focused' : '',
      this.disabled ? 'is-disabled' : '',
      this.selectedItem ? 'has-value' : '',
      this.isLoading ? 'is-loading' : '',
      this.panelOpen ? 'panel-open' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  /** Show clear button */
  get showClearButton(): boolean {
    return (this.inputValue !== '' || this.selectedItem !== null) && !this.disabled;
  }

  /** Show loading spinner */
  get showLoadingSpinner(): boolean {
    return this.isLoading && this.inputValue.length >= this.minCharsToFilter;
  }

  /** Show "no results" message */
  get showNoResults(): boolean {
    return (
      !this.isLoading &&
      this.inputValue.length >= this.minCharsToFilter &&
      this.filteredItems.length === 0 &&
      this.inputValue !== ''
    );
  }

  /** Show "type more characters" hint */
  get showMinCharsHint(): boolean {
    return this.inputValue.length > 0 && this.inputValue.length < this.minCharsToFilter;
  }

  /** Effective aria-label */
  get effectiveAriaLabel(): string {
    return this.ariaLabel || this.label || this.placeholder || 'Autocomplete field';
  }

  /** Min characters hint text */
  get minCharsHintText(): string {
    const remaining = this.minCharsToFilter - this.inputValue.length;
    return `Type ${remaining} more character${remaining > 1 ? 's' : ''} to search`;
  }
}
