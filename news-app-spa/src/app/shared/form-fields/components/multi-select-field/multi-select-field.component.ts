/**
 * MultiSelectFieldComponent
 * Reusable multi-select field where selections display as chips
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
 * availableStatuses = ['draft', 'published', 'archived'];
 * selectedStatuses = ['draft', 'published'];
 *
 * <app-multi-select-field
 *   label="Filter by Status"
 *   [options]="statusOptions"
 *   [selectedValues]="selectedStatuses"
 *   [showChips]="true"
 *   [allowClearAll]="true"
 *   (selectionChanged)="onStatusesChanged($event)"
 * ></app-multi-select-field>
 * ```
 *
 * @example
 * // In parent component
 * onStatusesChanged(statuses: string[]) {
 *   this.selectedStatuses = statuses;
 *   this.filterNews(); // Business logic here
 * }
 *
 * @category FormFields
 * @module MultiSelectFieldComponent
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
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import {
  IMultiSelectFieldProps,
  ISelectOption,
} from '../../models/form-field.models';
import {
  sortOptionsByLabel,
  generateFieldId,
  generateAriaDescribedBy,
  arraysEqual,
} from '../../utils/form-field.utils';

/**
 * MultiSelectFieldComponent
 *
 * Features:
 * - Multiple checkboxes in Material select
 * - Display selected items as removable chips
 * - Optional "Select All" / "Clear All" buttons
 * - Min/max selection limits
 * - Option sorting
 * - Error message display
 * - Accessibility: WCAG AA compliant
 * - Material Design 3: Outline appearance
 *
 * CSS Variables Used (from theme.config.ts):
 * - --components-input-height (44px) - WCAG AA touch target
 * - --color-border (#e5e7eb) - outline color
 * - --spacing-xl (32px) - chips height
 * - --chip-color - chip background color
 *
 * @component
 */
@Component({
  selector: 'app-multi-select-field',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './multi-select-field.component.html',
  styleUrl: './multi-select-field.component.css',
})
export class MultiSelectFieldComponent<T = string | number>
  implements OnInit, OnDestroy, OnChanges {
  /**
   * INPUTS: Properties passed from parent (Smart Component)
   * Follow IMultiSelectFieldProps interface
   */

  /** Field label (Material form label) */
  @Input() label: string = '';

  /** Placeholder text */
  @Input() placeholder: string = '';

  /** Hint text (shows below select when focused) - Material Design 3 standard */
  @Input() hint: string = '';

  /** Array of options to display in dropdown */
  @Input() options: ISelectOption<T>[] = [];

  /** Currently selected values (array) */
  @Input() selectedValues: T[] = [];

  /** Display selected items as chips */
  @Input() showChips: boolean = true;

  /** Show "Clear All" button */
  @Input() allowClearAll: boolean = true;

  /** Max selections allowed (-1 = unlimited) */
  @Input() maxSelections: number = -1;

  /** Min selections required (0 = optional) */
  @Input() minSelections: number = 0;

  /** Disable field interaction */
  @Input() disabled: boolean = false;

  /** Mark as required field */
  @Input() required: boolean = false;

  /** Error message to display (if any) */
  @Input() hasErrorMessage: string | null = null;

  /** Sort options alphabetically */
  @Input() sortOptions: boolean = false;

  /** Custom chip color */
  @Input() chipColor: 'primary' | 'accent' | 'warn' = 'primary';

  /** CSS class for custom styling */
  @Input() customClass: string = '';

  /** Accessibility: aria-label override */
  @Input() ariaLabel: string = '';

  /**
   * OUTPUTS: Events emitted to parent (Smart Component)
   * Parent handles what to do with these events
   */

  /** Emitted when selection changes */
  @Output() selectionChanged = new EventEmitter<T[]>();

  /** Emitted when selections cleared */
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

  /** Display options (sorted if requested) */
  displayOptions: ISelectOption<T>[] = [];

  /**
   * LIFECYCLE HOOKS
   */

  ngOnInit(): void {
    // Generate unique IDs for accessibility
    this.fieldId = generateFieldId('multi-select-field');
    this.ariaDescribedBy = generateAriaDescribedBy(this.fieldId);

    // Calculate display options
    this.updateDisplayOptions();
  }

  ngOnDestroy(): void {
    // Clean up event emitters
    this.selectionChanged.complete();
    this.cleared.complete();
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
   * Handle selection changes
   * Parent component handles the business logic
   * @param values - Array of selected values
   */
  onSelectionChange(values: T[]): void {
    // Validate against max selections
    if (this.maxSelections > 0 && values.length > this.maxSelections) {
      // Remove the last one if max exceeded
      values = values.slice(0, this.maxSelections);
    }

    // Only emit if actually changed
    if (!arraysEqual(this.selectedValues, values)) {
      this.selectedValues = values;
      this.selectionChanged.emit(values);
    }
  }

  /**
   * Handle chip removal (X button on chip)
   * @param value - Value to remove
   */
  onChipRemove(value: T): void {
    const newValues = this.selectedValues.filter((v) => v !== value);
    this.onSelectionChange(newValues);
  }

  /**
   * Clear all selections
   */
  onClearAll(): void {
    // Only allow clear if minSelections = 0
    if (this.minSelections === 0) {
      this.selectedValues = [];
      this.selectionChanged.emit([]);
      this.cleared.emit();
    }
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
   */
  updateDisplayOptions(): void {
    let optionsToDisplay = [...this.options];

    if (this.sortOptions) {
      optionsToDisplay = sortOptionsByLabel(optionsToDisplay);
    }

    this.displayOptions = optionsToDisplay;
  }

  /**
   * Check if value is currently selected
   * @param value - Value to check
   * @returns True if selected
   */
  isValueSelected(value: T): boolean {
    return this.selectedValues.includes(value);
  }

  /**
   * Get display label for a selected value
   * @param value - Value to get label for
   * @returns Display label
   */
  getLabel(value: T): string {
    const option = this.displayOptions.find((opt) => opt.value === value);
    return option?.label || String(value);
  }

  /**
   * Check if at max selections limit
   */
  isAtMaxSelections(): boolean {
    return this.maxSelections > 0 && this.selectedValues.length >= this.maxSelections;
  }

  /**
   * Check if below min selections
   */
  isBelowMinSelections(): boolean {
    return this.selectedValues.length < this.minSelections;
  }

  /**
   * TrackBy function for ngFor (performance optimization)
   * @param index - Index of item
   * @param value - Value being tracked
   * @returns Unique identifier
   */
  trackByValue(index: number, value: T): string | number {
    return value as string | number;
  }

  /**
   * TrackBy for options
   * @param index - Index of item
   * @param option - Option item
   * @returns Unique identifier
   */
  trackByOptionValue(index: number, option: ISelectOption<T>): string | number {
    return option.value as string | number;
  }

  /**
   * Compare function for mat-select value comparison
   * Required for proper value matching in multi-select
   * @param c1 - First value
   * @param c2 - Second value
   * @returns True if equal
   */
  compareWith(c1: T, c2: T): boolean {
    return c1 && c2 ? c1 === c2 : c1 === c2;
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
      this.selectedValues.length > 0 ? 'has-value' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  /** Show chips (selected items) */
  get showSelectedChips(): boolean {
    return this.showChips && this.selectedValues.length > 0;
  }

  /** Show Clear All button */
  get showClearAllButton(): boolean {
    return (
      this.allowClearAll &&
      this.selectedValues.length > 0 &&
      this.minSelections === 0 &&
      !this.disabled
    );
  }

  /** Selection count display */
  get selectionCountText(): string {
    if (this.selectedValues.length === 0) return 'None selected';
    if (this.maxSelections > 0) {
      return `${this.selectedValues.length} / ${this.maxSelections} selected`;
    }
    return `${this.selectedValues.length} selected`;
  }

  /** Effective aria-label */
  get effectiveAriaLabel(): string {
    return this.ariaLabel || this.label || this.placeholder || 'Multi-select field';
  }

  /** Hint text for min/max selections */
  get selectionHintText(): string {
    const hints: string[] = [];

    if (this.minSelections > 0) {
      hints.push(`Minimum ${this.minSelections} required`);
    }

    if (this.maxSelections > 0) {
      hints.push(`Maximum ${this.maxSelections} allowed`);
    }

    return hints.join(' | ') || '';
  }
}
