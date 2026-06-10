/**
 * DateRangeFieldComponent
 * Reusable date range field with FROM and TO date inputs
 *
 * ARCHITECT PATTERN: Dumb/Presentational Component
 * ✅ NO business logic
 * ✅ NO API calls
 * ✅ Display-only component
 * ✅ Pure Input/Output pattern
 * ✅ Leverages Material's mat-date-range-input & mat-date-range-picker
 *
 * USAGE in Parent (Smart Component):
 * ```typescript
 * dateRange = {
 *   start: new Date('2024-01-01'),
 *   end: new Date('2024-12-31')
 * };
 * minDate = new Date('2020-01-01');
 * maxDate = new Date();
 *
 * <app-date-range-field
 *   label="Date Range"
 *   [fromDate]="dateRange.start"
 *   [toDate]="dateRange.end"
 *   [minDate]="minDate"
 *   [maxDate]="maxDate"
 *   [disablePastDates]="false"
 *   (fromDateChanged)="onFromDateChange($event)"
 *   (toDateChanged)="onToDateChange($event)"
 * ></app-date-range-field>
 * ```
 *
 * @example
 * // In parent component
 * onFromDateChange(date: Date | null) {
 *   this.dateRange.start = date;
 *   this.filterNews(); // Business logic here
 * }
 *
 * @category FormFields
 * @module DateRangeFieldComponent
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
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

import {
  generateFieldId,
  generateAriaDescribedBy,
  formatDate,
} from '../../utils/form-field.utils';

/**
 * DateRange interface
 * Represents a date range with start and end dates
 */
export interface IDateRange {
  start: Date | null;
  end: Date | null;
}

/**
 * DateRangeFieldComponent
 *
 * Features:
 * - Inline date range picker (Material's mat-date-range-input)
 * - Separate FROM and TO input fields (44px touch targets)
 * - Min/max date constraints
 * - Optional past/future date disabling
 * - Error message display
 * - Clear button to reset both dates
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
  selector: 'app-date-range-field',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
  ],
  templateUrl: './date-range-field.component.html',
  styleUrl: './date-range-field.component.css',
})
export class DateRangeFieldComponent implements OnInit, OnDestroy, OnChanges {
  /**
   * INPUTS: Properties passed from parent (Smart Component)
   */

  /** Field label (Material form label) */
  @Input() label: string = '';

  /** Hint text (shows below date range when focused) - Material Design 3 standard */
  @Input() hint: string = '';

  /** Placeholder for FROM date input */
  @Input() placeholderFrom: string = 'From date';

  /** Placeholder for TO date input */
  @Input() placeholderTo: string = 'To date';

  /** FROM date value */
  @Input() fromDate: Date | null = null;

  /** TO date value */
  @Input() toDate: Date | null = null;

  /** Minimum selectable date */
  @Input() minDate: Date | null = null;

  /** Maximum selectable date */
  @Input() maxDate: Date | null = null;

  /** Date format display (used for labels) */
  @Input() dateFormat: string = 'MMM d, y';

  /** Disable all dates in the past */
  @Input() disablePastDates: boolean = false;

  /** Disable all dates in the future */
  @Input() disableFutureDates: boolean = false;

  /** Disable field interaction */
  @Input() disabled: boolean = false;

  /** Mark as required field */
  @Input() required: boolean = false;

  /** Error message to display (if any) */
  @Input() hasErrorMessage: string | null = null;

  /** CSS class for custom styling */
  @Input() customClass: string = '';

  /** Accessibility: aria-label override */
  @Input() ariaLabel: string = '';

  /**
   * OUTPUTS: Events emitted to parent (Smart Component)
   * Parent handles what to do with these events
   */

  /** Emitted when FROM date changes */
  @Output() fromDateChanged = new EventEmitter<Date | null>();

  /** Emitted when TO date changes */
  @Output() toDateChanged = new EventEmitter<Date | null>();

  /** Emitted when both dates cleared */
  @Output() cleared = new EventEmitter<void>();

  /** Emitted when range is complete (both dates set) */
  @Output() rangeSelected = new EventEmitter<IDateRange>();

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

  /** FormGroup for mat-date-range-input */
  dateRangeForm!: FormGroup;

  /**
   * LIFECYCLE HOOKS
   */

  constructor(private fb: FormBuilder, private cdr: ChangeDetectorRef) {
    // Initialize empty form - will be populated in ngOnInit
    // NOTE: No validators on start/end because dates are optional in filter contexts
    this.dateRangeForm = this.fb.group({
      start: [null],
      end: [null],
    });
  }

  ngOnInit(): void {
    // Generate unique IDs for accessibility
    this.fieldId = generateFieldId('date-range-field');
    this.ariaDescribedBy = generateAriaDescribedBy(this.fieldId);

    // Initialize form with input values
    this.dateRangeForm.patchValue({
      start: this.fromDate,
      end: this.toDate,
    });

    // Update form when input properties change
    this.dateRangeForm.get('start')?.valueChanges.subscribe((date) => {
      if (date && date !== this.fromDate) {
        this.fromDate = date;
        this.fromDateChanged.emit(date);
      }
    });

    this.dateRangeForm.get('end')?.valueChanges.subscribe((date) => {
      if (date && date !== this.toDate) {
        this.toDate = date;
        this.toDateChanged.emit(date);
      }
    });

    // Set auto minDate to today if disablePastDates=true and minDate not set
    if (this.disablePastDates && !this.minDate) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      this.minDate = today;
    }

    // Set auto maxDate to today if disableFutureDates=true and maxDate not set
    if (this.disableFutureDates && !this.maxDate) {
      const today = new Date();
      today.setHours(23, 59, 59, 999);
      this.maxDate = today;
    }
  }

  /**
   * CRITICAL: Re-sync form values when @Input properties change from parent
   * When parent clears dates (sets to null), this must properly clear the Material inputs
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (!this.dateRangeForm) return;

    // Check if fromDate or toDate actually changed
    if (!changes['fromDate'] && !changes['toDate']) return;

    const startCtrl = this.dateRangeForm.get('start');
    const endCtrl = this.dateRangeForm.get('end');

    if (!startCtrl || !endCtrl) return;

    // Simple direct update - patchValue is safer than setValue for partial updates
    startCtrl.patchValue(this.fromDate, { emitEvent: false });
    endCtrl.patchValue(this.toDate, { emitEvent: false });
  }

  ngOnDestroy(): void {
    // Clean up event emitters
    this.fromDateChanged.complete();
    this.toDateChanged.complete();
    this.cleared.complete();
    this.rangeSelected.complete();
    this.focused.complete();
    this.blurred.complete();
  }

  /**
   * EVENT HANDLERS
   * Called from template, emit events to parent
   */

  /**
   * Handle FROM date change
   * @param date - New FROM date
   */
  onFromDateChange(date: Date | null): void {
    this.fromDate = date;
    this.fromDateChanged.emit(date);

    // Emit rangeSelected if both dates are set
    if (this.fromDate && this.toDate) {
      this.rangeSelected.emit({ start: this.fromDate, end: this.toDate });
    }
  }

  /**
   * Handle TO date change
   * @param date - New TO date
   */
  onToDateChange(date: Date | null): void {
    this.toDate = date;
    this.toDateChanged.emit(date);

    // Emit rangeSelected if both dates are set
    if (this.fromDate && this.toDate) {
      this.rangeSelected.emit({ start: this.fromDate, end: this.toDate });
    }
  }

  /**
   * Clear both dates from range
   */
  onClearDates(): void {
    this.fromDate = null;
    this.toDate = null;
    this.cleared.emit();
    this.fromDateChanged.emit(null);
    this.toDateChanged.emit(null);
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
   * Filter function for date picker
   * Disable dates based on configuration
   * @param date - Date to check
   * @returns True if date should be disabled
   */
  dateFilter(date: Date | null): boolean {
    if (!date) return true;

    // Check min date
    if (this.minDate) {
      const minDate = new Date(this.minDate);
      minDate.setHours(0, 0, 0, 0);
      if (date < minDate) return false;
    }

    // Check max date
    if (this.maxDate) {
      const maxDate = new Date(this.maxDate);
      maxDate.setHours(23, 59, 59, 999);
      if (date > maxDate) return false;
    }

    return true;
  }

  /**
   * Check if range is complete (both dates set)
   */
  isRangeComplete(): boolean {
    return !!(this.fromDate && this.toDate);
  }

  /**
   * Check if range is valid (FROM <= TO)
   */
  isRangeValid(): boolean {
    if (!this.fromDate || !this.toDate) return true;
    return this.fromDate <= this.toDate;
  }

  /**
   * Get display text for FROM date
   */
  getFromDateDisplay(): string {
    if (!this.fromDate) return '';
    return formatDate(this.fromDate, 'short');
  }

  /**
   * Get display text for TO date
   */
  getToDateDisplay(): string {
    if (!this.toDate) return '';
    return formatDate(this.toDate, 'short');
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
      this.isRangeComplete() ? 'has-value' : '',
      !this.isRangeValid() ? 'invalid-range' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  /** Show clear button */
  get showClearButton(): boolean {
    return (
      (this.fromDate !== null || this.toDate !== null) && !this.disabled
    );
  }

  /** Effective aria-label */
  get effectiveAriaLabel(): string {
    return this.ariaLabel || this.label || 'Date range field';
  }

  /** Error message for invalid range */
  get rangeErrorMessage(): string | null {
    if (this.hasErrorMessage) return this.hasErrorMessage;
    if (!this.isRangeValid()) {
      return 'From date must be before To date';
    }
    return null;
  }
}
