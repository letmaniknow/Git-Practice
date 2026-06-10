/**
 * Form Fields Component Library - Data Models & Interfaces
 * 
 * ARCHITECT DECISIONS:
 * ✅ Industry Standards: Aligned with Angular best practices, Material Design 3, reactive patterns
 * ✅ Future-Proof: Extensible for future field types (Checkbox, Toggle, TextArea, etc.)
 * ✅ Consistency: Single source of truth for form field contracts
 * ✅ Reusability: Generic types support all field implementations
 * 
 * @module FormFieldModels
 */

/**
 * BASE INTERFACES - Contracts all form fields must implement
 */

/**
 * Generic select/dropdown option definition
 * Extensible for custom properties (icons, descriptions, tags, etc.)
 * 
 * @example
 * const categories: ISelectOption[] = [
 *   { value: 'tech', label: 'Technology', icon: 'computer' },
 *   { value: 'sports', label: 'Sports', icon: 'sports' }
 * ];
 */
export interface ISelectOption<T = string | number> {
  /** Unique identifier (database ID, enum, etc.) */
  value: T;
  
  /** Display label (localized) */
  label: string;
  
  /** Optional icon name for Material Icons */
  icon?: string;
  
  /** Optional: Description shown in hover/tooltip */
  description?: string;
  
  /** Optional: Disable this option */
  disabled?: boolean;
  
  /** Optional: Group name for optgroup support */
  group?: string;
  
  /** Optional: Custom data (extensible for other components) */
  metadata?: Record<string, any>;
}

/**
 * Base properties all form fields share
 * Extracted to reduce code duplication across components
 */
export interface IFormFieldBaseProps {
  /** Field label (Material form field label) */
  label: string;
  
  /** Placeholder text (in input) */
  placeholder?: string;
  
  /** Disable field interaction */
  disabled?: boolean;
  
  /** Mark as required */
  required?: boolean;
  
  /** Display error message */
  hasErrorMessage?: string | null;
  
  /** CSS class for custom styling */
  customClass?: string;
  
  /** Accessibility: aria-label override */
  ariaLabel?: string;
  
  /** Accessibility: aria-describedby for error messages */
  ariaDescribedBy?: string;
  
  /** Internationalization: i18n translation key */
  i18nKey?: string;
}

/**
 * TEXT INPUT FIELD CONTRACT
 * Supports: text, email, search, password, url, etc.
 * 
 * @example
 * export interface ITextInputFieldProps extends IFormFieldBaseProps {
 *   type: 'text' | 'email' | 'search' | 'password';
 *   value: string;
 *   icon?: string;
 *   maxLength?: number;
 * }
 */
export interface ITextInputFieldProps extends IFormFieldBaseProps {
  /** Input type (HTML5) */
  type?: 'text' | 'email' | 'search' | 'password' | 'url' | 'tel' | 'number';
  
  /** Current input value */
  value: string;
  
  /** Material icon name (shown when empty) */
  icon?: string;
  
  /** Show clear button toggle */
  showClearButton?: boolean;
  
  /** Max characters allowed */
  maxLength?: number;
  
  /** Min characters required */
  minLength?: number;
  
  /** Pattern for validation */
  pattern?: string;
}

/**
 * SELECT FIELD CONTRACT (Single selection)
 * Supports: Dropdown, Material select with options
 * 
 * @example
 * const selectProps: ISelectFieldProps = {
 *   label: 'Category',
 *   options: categoryList,
 *   selectedValue: null,
 *   allowNullOption: true,
 *   nullOptionLabel: 'All Categories'
 * };
 */
export interface ISelectFieldProps<T = string | number> extends IFormFieldBaseProps {
  /** Array of select options */
  options: ISelectOption<T>[];
  
  /** Current selected value */
  selectedValue: T | null;
  
  /** Show "All" or null option at top */
  allowNullOption?: boolean;
  
  /** Label for null option (default: "All") */
  nullOptionLabel?: string;
  
  /** Show as Material chips (if multiple selected) */
  showAsChips?: boolean;
  
  /** Sort options by label */
  sortOptions?: boolean;
  
  /** Filter options by text */
  filterPlaceholder?: string;
}

/**
 * MULTI-SELECT FIELD CONTRACT
 * Supports: Multiple selections with checkboxes shown as chips
 * 
 * @example
 * const multiSelectProps: IMultiSelectFieldProps = {
 *   label: 'Filter by Status',
 *   options: statusList,
 *   selectedValues: ['active', 'pending'],
 *   showChips: true,
 *   allowClearAll: true
 * };
 */
export interface IMultiSelectFieldProps<T = string | number> extends IFormFieldBaseProps {
  /** Array of select options */
  options: ISelectOption<T>[];
  
  /** Currently selected values (array) */
  selectedValues: T[];
  
  /** Display selected items as chips */
  showChips?: boolean;
  
  /** Show "Clear All" button */
  allowClearAll?: boolean;
  
  /** Max selections allowed (-1 = unlimited) */
  maxSelections?: number;
  
  /** Sort options by label */
  sortOptions?: boolean;
  
  /** Filter options by text */
  filterPlaceholder?: string;
  
  /** Custom chip color */
  chipColor?: 'primary' | 'accent' | 'warn';
  
  /** Allow deselecting all (min = 0) or require at least one */
  minSelections?: number;
}

/**
 * DATE RANGE FIELD CONTRACT
 * Supports: Material date range picker with From/To inputs
 * 
 * @example
 * const dateRangeProps: IDateRangeFieldProps = {
 *   label: 'Created Date',
 *   fromDate: new Date('2024-01-01'),
 *   toDate: new Date('2024-12-31'),
 *   minDate: new Date('2020-01-01'),
 *   maxDate: new Date()
 * };
 */
export interface IDateRangeFieldProps extends IFormFieldBaseProps {
  /** From date */
  fromDate: Date | null;
  
  /** To date */
  toDate: Date | null;
  
  /** Minimum selectable date */
  minDate?: Date;
  
  /** Maximum selectable date */
  maxDate?: Date;
  
  /** Date format display (default: "MMM d, y") */
  dateFormat?: string;
  
  /** Disable past dates */
  disablePastDates?: boolean;
  
  /** Disable future dates */
  disableFutureDates?: boolean;
  
  /** Show time picker */
  showTimePicker?: boolean;
  
  /** Start week on Sunday (default) or Monday */
  startWeekOn?: 'sunday' | 'monday';
}

/**
 * AUTOCOMPLETE FIELD CONTRACT
 * Supports: Material autocomplete with async filtering (PARENT handles logic)
 * 
 * CRITICAL: Business logic (filtering, API calls) stays in PARENT component
 * This component is PRESENTATION ONLY
 * 
 * @example Business Logic in Parent:
 * ```typescript
 * // Parent: news-search-filters.component.ts
 * onAdminUserInputChange(value: string) {
 *   // ✅ Business logic HERE
 *   this.adminSearchService.searchAdmins(value).subscribe(users => {
 *     this.adminUsersFiltered = users;  // Pass to child
 *     this.isLoading = false;
 *   });
 * }
 * ```
 * 
 * @example Template Usage:
 * ```html
 * <app-autocomplete-field
 *   [filteredItems]="adminUsersFiltered"
 *   [isLoading]="isLoading"
 *   (inputChanged)="onAdminUserInputChange($event)"
 *   (itemSelected)="onAdminUserSelected($event)"
 * ></app-autocomplete-field>
 * ```
 */
export interface IAutocompleteFieldProps<T = any> extends IFormFieldBaseProps {
  /** Items to display in dropdown (filtered by parent) */
  filteredItems: T[];
  
  /** Show loading indicator */
  isLoading?: boolean;
  
  /** Property name to display for each item */
  displayProperty: string;
  
  /** Property name for comparison (unique identifier) */
  compareProperty?: string;
  
  /** Currently selected item */
  selectedItem: T | null;
  
  /** Min characters before showing suggestions */
  minCharsToFilter?: number;
  
  /** Debounce time for input change events (ms) */
  debounceMs?: number;
  
  /** Max items to show in dropdown */
  maxItems?: number;
  
  /** Show when no matches found message */
  showNoMatchesMessage?: boolean;
  
  /** Custom template for displaying each item */
  itemDisplayTemplate?: string;
  
  /** Allow custom value (not in list) */
  allowCustomValue?: boolean;
}

/**
 * TEXTAREA FIELD CONTRACT (Future Tier-2)
 * Supports: Multi-line text input with char count
 */
export interface ITextAreaFieldProps extends IFormFieldBaseProps {
  value: string;
  rows?: number;
  maxLength?: number;
  showCharCount?: boolean;
  resizable?: boolean;
}

/**
 * CHECKBOX FIELD CONTRACT (Future Tier-2)
 * Supports: Single or group checkbox
 */
export interface ICheckboxFieldProps extends IFormFieldBaseProps {
  checked: boolean;
  indeterminate?: boolean;
  color?: 'primary' | 'accent' | 'warn';
}

/**
 * TOGGLE FIELD CONTRACT (Future Tier-2)
 * Supports: On/Off toggle switch
 */
export interface IToggleFieldProps extends IFormFieldBaseProps {
  checked: boolean;
  color?: 'primary' | 'accent' | 'warn';
}

/**
 * RADIO FIELD CONTRACT (Future Tier-2)
 * Supports: Radio button group
 */
export interface IRadioFieldProps<T = string | number> extends IFormFieldBaseProps {
  options: ISelectOption<T>[];
  selectedValue: T | null;
  direction?: 'row' | 'column';
}

/**
 * VALIDATION RESULT
 * Consistent error reporting across all fields
 */
export interface IValidationResult {
  /** Is valid? */
  isValid: boolean;
  
  /** Error message (if invalid) */
  errorMessage?: string;
  
  /** Error code for i18n */
  errorCode?: string;
  
  /** Additional validation metadata */
  metadata?: {
    minLength?: number;
    maxLength?: number;
    pattern?: string;
    customValidations?: string[];
  };
}

/**
 * FORM FIELD EVENT PAYLOAD
 * Standard event structure for consistency
 */
export interface IFormFieldChangeEvent<T = any> {
  /** Field value that changed */
  value: T;
  
  /** Previous value (for undo/history) */
  previousValue?: T;
  
  /** Timestamp */
  timestamp: number;
  
  /** Which property triggered change */
  source?: string;
  
  /** Whether change is valid */
  isValid: boolean;
  
  /** Validation error if present */
  validationError?: IValidationResult;
}

/**
 * FORM FIELD CONFIGURATION
 * Used for dynamic form generation (future feature)
 */
export interface IFormFieldConfig<T = any> {
  /** Unique field identifier */
  id: string;
  
  /** Component type */
  type: 'text' | 'select' | 'multiSelect' | 'dateRange' | 'autocomplete' | 'textarea' | 'checkbox' | 'toggle' | 'radio';
  
  /** Field properties */
  props: IFormFieldBaseProps & Record<string, any>;
  
  /** Validation rules */
  validators?: Array<(value: T) => IValidationResult>;
  
  /** Tab order */
  tabIndex?: number;
  
  /** Show in UI (dynamic show/hide) */
  visible?: boolean;
  
  /** Disable field */
  disabled?: boolean;
  
  /** Read-only mode */
  readOnly?: boolean;
  
  /** CSS classes for styling */
  classes?: string[];
  
  /** Responsive columns (grid layout) */
  colspan?: { xs?: number; sm?: number; md?: number; lg?: number };
}

/**
 * THEME & STYLING CONFIGURATION
 * For future theming support
 */
export interface IFormFieldTheme {
  /** Primary color */
  primary?: string;
  
  /** Accent color */
  accent?: string;
  
  /** Warning color */
  warn?: string;
  
  /** Border color */
  borderColor?: string;
  
  /** Disabled color */
  disabledColor?: string;
  
  /** Error color */
  errorColor?: string;
  
  /** Success color */
  successColor?: string;
  
  /** Custom CSS variables */
  customVariables?: Record<string, string>;
}

/**
 * ACCESSIBILITY CONFIGURATION
 * Ensures WCAG AA compliance
 */
export interface IFormFieldAccessibility {
  /** Aria label */
  ariaLabel?: string;
  
  /** Aria description */
  ariaDescription?: string;
  
  /** Aria live region announcements */
  ariaLive?: 'polite' | 'assertive' | 'off';
  
  /** Role for custom components */
  role?: string;
  
  /** Tab order */
  tabIndex?: number;
  
  /** Read only for screen readers */
  readOnlyForScreenReaders?: boolean;
}

/**
 * COMPONENT STATE (for debugging/monitoring)
 */
export interface IFormFieldState<T = any> {
  value: T;
  isFocused: boolean;
  isDirty: boolean;
  isTouched: boolean;
  isValid: boolean;
  isDisabled: boolean;
  errors?: IValidationResult[];
}

export type FormFieldType = 
  | 'text' 
  | 'email' 
  | 'password' 
  | 'select' 
  | 'multiSelect' 
  | 'dateRange' 
  | 'autocomplete' 
  | 'textarea' 
  | 'checkbox' 
  | 'toggle' 
  | 'radio';
