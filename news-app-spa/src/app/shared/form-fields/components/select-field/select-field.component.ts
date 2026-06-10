/**
 * SelectFieldComponent
 * Reusable single-select dropdown field with Material Design 3 styling
 *
 * ARCHITECT PATTERN: Dumb/Presentational Component
 * ✅ NO business logic
 * ✅ NO API calls
 * ✅ Display-only component
 * ✅ Pure Input/Output pattern
 * ✅ Reusable across all features
 *
 * USAGE in Parent (Smart Component):
 * ```typescript
 * // Parent has the options and selection logic
 * categoryOptions = [
 *   { value: '1', label: 'Technology' },
 *   { value: '2', label: 'Sports' },
 *   { value: '3', label: 'Politics' }
 * ];
 *
 * <app-select-field
 *   label="Category"
 *   [options]="categoryOptions"
 *   [selectedValue]="categoryIdSelected"
 *   [allowNullOption]="true"
 *   nullOptionLabel="All Categories"
 *   (selectionChanged)="onCategorySelected($event)"
 * ></app-select-field>
 * ```
 *
 * @example
 * // In parent component
 * onCategorySelected(categoryId: string | null) {
 *   this.categoryIdSelected = categoryId;
 *   this.filterNews(); // Business logic here
 * }
 *
 * @category FormFields
 * @module SelectFieldComponent
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
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';

import {
  ISelectFieldProps,
  ISelectOption,
} from '../../models/form-field.models';
import {
  sortOptionsByLabel,
  generateFieldId,
  generateAriaDescribedBy,
} from '../../utils/form-field.utils';

/**
 * SelectFieldComponent
 *
 * Features:
 * - Dropdown with optional "All/None" null option
 * - Sort options alphabetically (optional)
 * - Group options by category (optional)
 * - Disable specific options
 * - Error message display
 * - Accessibility: WCAG AA compliant
 * - Material Design 3: Outline appearance
 *
 * CSS Variables Used (from theme.config.ts):
 * - --components-input-height (44px) - WCAG AA touch target
 * - --color-border (#e5e7eb) - outline color
 * - --option-height (40px) - single line
 * - --option-height-multiline (48px) - multi-line options
 *
 * @component
 */
@Component({
  selector: 'app-select-field',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatDividerModule,
  ],
  templateUrl: './select-field.component.html',
  styleUrl: './select-field.component.css',
})
export class SelectFieldComponent<T = string | number>
  implements OnInit, OnDestroy, OnChanges {
  /**
   * INPUTS: Properties passed from parent (Smart Component)
   * Follow ISelectFieldProps interface
   */

  /** Field label (Material form label) */
  @Input() label: string = '';

  /** Placeholder text (shown when no selection) */
  @Input() placeholder: string = '';

  /** Hint text (shows below select when focused) - Material Design 3 standard */
  @Input() hint: string = '';

  /** Array of options to display in dropdown */
  @Input() options: ISelectOption<T>[] = [];

  /** Currently selected value */
  @Input() selectedValue: T | null = null;

  /** Show "All" or null option at top */
  @Input() allowNullOption: boolean = false;

  /** Label for null option (default: "All") */
  @Input() nullOptionLabel: string = 'All';

  /** Disable field interaction */
  @Input() disabled: boolean = false;

  /** Mark as required field */
  @Input() required: boolean = false;

  /** Error message to display (if any) */
  @Input() hasErrorMessage: string | null = null;

  /** Sort options alphabetically */
  @Input() sortOptions: boolean = false;

  /** CSS class for custom styling */
  @Input() customClass: string = '';

  /** Accessibility: aria-label override */
  @Input() ariaLabel: string = '';

  /**
   * OUTPUTS: Events emitted to parent (Smart Component)
   * Parent handles what to do with these events
   */

  /** Emitted when selection changes */
  @Output() selectionChanged = new EventEmitter<T | null>();

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

  /** Display options (sorted if requested) */
  displayOptions: ISelectOption<T>[] = [];

  /**
   * LIFECYCLE HOOKS
   */

  ngOnInit(): void {
    // Generate unique IDs for accessibility
    this.fieldId = generateFieldId('select-field');
    this.ariaDescribedBy = generateAriaDescribedBy(this.fieldId);

    // Calculate display options
    this.updateDisplayOptions();
  }

  ngOnDestroy(): void {
    // Clean up event emitters
    this.selectionChanged.complete();
    this.focused.complete();
    this.blurred.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Update display options when options input changes
    if (changes['options'] || changes['sortOptions']) {
      this.updateDisplayOptions();
    }
  }

  /**
   * EVENT HANDLERS
   * Called from template, emit events to parent
   */

  /**
   * Handle selection change
   * Parent component handles the business logic
   * @param value - Selected value
   */
  onSelectionChange(value: T | null): void {
    this.selectedValue = value;
    this.selectionChanged.emit(value);
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
    this.blurred.emit();
  }

  /**
   * HELPER METHODS
   */

  /**
   * Calculate display options (with sorting if requested)
   * Update internal list when options or sortOptions changes
   */
  updateDisplayOptions(): void {
    let optionsToDisplay = [...this.options];

    if (this.sortOptions) {
      optionsToDisplay = sortOptionsByLabel(optionsToDisplay);
    }

    this.displayOptions = optionsToDisplay;
  }

  /**
   * Check if option should be displayed (not filtered out)
   * @param option - Option to check
   * @returns True if option should be visible
   */
  isOptionVisible(option: ISelectOption<T>): boolean {
    return !option.disabled;
  }

  /**
   * Get display label for selected value
   * @returns Display text for current selection
   */
  getSelectedLabel(): string {
    if (this.selectedValue === null || this.selectedValue === undefined) {
      return this.nullOptionLabel;
    }

    const selected = this.displayOptions.find(
      (opt) => opt.value === this.selectedValue
    );
    return selected?.label || String(this.selectedValue);
  }

  /**
   * TrackBy function for ngFor (performance optimization)
   * @param index - Index of item
   * @param option - Option item
   * @returns Unique identifier for the option
   */
  trackByOptionValue(index: number, option: ISelectOption<T>): string | number {
    return option.value as string | number;
  }

  /**
   * CompareWith function for mat-select value comparison
   * Uses value equality by default
   * @param option1 - First option
   * @param option2 - Second option
   * @returns True if options are equal
   */
  compareWith(option1: T | null, option2: T | null): boolean {
    if (option1 === null && option2 === null) return true;
    if (option1 === null || option2 === null) return false;
    return option1 === option2;
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
      this.selectedValue !== null && this.selectedValue !== undefined
        ? 'has-value'
        : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  /** Effective aria-label (custom or auto-generated) */
  get effectiveAriaLabel(): string {
    return this.ariaLabel || this.label || this.placeholder || 'Select field';
  }
}
