/**
 * TextInputFieldComponent
 * Reusable text input field with Material Design 3 styling
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
 * <app-text-input-field
 *   label="Search articles"
 *   placeholder="Enter search term"
 *   type="search"
 *   icon="search"
 *   [value]="searchTextInput"
 *   [disabled]="isLoading"
 *   [hasErrorMessage]="validationError"
 *   (valueChanged)="onSearchChanged($event)"
 *   (cleared)="onClearClicked()"
 * ></app-text-input-field>
 * ```
 *
 * @example
 * // In parent component
 * searchTextInput = '';
 *
 * onSearchChanged(newValue: string) {
 *   this.searchTextInput = newValue;
 *   this.filterResults();
 * }
 *
 * onClearClicked() {
 *   this.searchTextInput = '';
 *   this.filterResults();
 * }
 *
 * @category FormFields
 * @module TextInputFieldComponent
 */

import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ITextInputFieldProps } from '../../models/form-field.models';
import { generateFieldId, generateAriaDescribedBy } from '../../utils/form-field.utils';

/**
 * TextInputFieldComponent
 *
 * Flexible text input supporting multiple input types:
 * - text (default)
 * - email
 * - password
 * - search
 * - url
 * - tel
 * - number
 *
 * Features:
 * - Optional leading icon (shown when empty)
 * - Optional clear button (shown when has value)
 * - Min/max length validation
 * - Pattern validation
 * - Error message display
 * - Accessibility: WCAG AA compliant
 * - Material Design 3: Outline appearance
 * - Keyboard shortcuts: Enter, Escape
 *
 * CSS Variables Used (from theme.config.ts):
 * - --spacing-sm (8px) - input padding
 * - --spacing-xs (4px) - prefix/suffix padding
 * - --components-input-height (44px) - WCAG AA touch target
 * - --color-border (#e5e7eb) - outline color
 * - --color-text-secondary - placeholder color
 *
 * @component
 */
@Component({
  selector: 'app-text-input-field',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './text-input-field.component.html',
  styleUrl: './text-input-field.component.css',
})
export class TextInputFieldComponent implements OnInit, OnDestroy {
  /**
   * INPUTS: Properties passed from parent (Smart Component)
   * Follow ITextInputFieldProps interface
   */

  /** Field label (Material form label) */
  @Input() label: string = '';

  /** Placeholder text inside input */
  @Input() placeholder: string = '';

  /** Hint text (shows below input when focused) - Material Design 3 standard */
  @Input() hint: string = '';

  /** Input value - two-way binding with parent */
  @Input() value: string = '';

  /** Input type: text, email, password, search, url, tel, number */
  @Input() type: 'text' | 'email' | 'password' | 'search' | 'url' | 'tel' | 'number' = 'text';

  /** Material icon name shown when empty (e.g., 'search', 'email') */
  @Input() icon: string = '';

  /** Show clear button when value exists */
  @Input() showClearButton: boolean = true;

  /** Disable field interaction */
  @Input() disabled: boolean = false;

  /** Mark as required field */
  @Input() required: boolean = false;

  /** Error message to display (if any) */
  @Input() hasErrorMessage: string | null = null;

  /** Maximum characters allowed */
  @Input() maxLength: number = 255;

  /** Minimum characters required */
  @Input() minLength: number = 0;

  /** Pattern for validation (HTML5 pattern attribute) */
  @Input() pattern: string = '';

  /** CSS class for custom styling */
  @Input() customClass: string = '';

  /** Accessibility: aria-label override */
  @Input() ariaLabel: string = '';

  /**
   * OUTPUTS: Events emitted to parent (Smart Component)
   * Parent handles what to do with these events
   */

  /** Emitted when input value changes */
  @Output() valueChanged = new EventEmitter<string>();

  /** Emitted when clear button clicked */
  @Output() cleared = new EventEmitter<void>();

  /** Emitted when field receives focus */
  @Output() focused = new EventEmitter<void>();

  /** Emitted when field loses focus */
  @Output() blurred = new EventEmitter<void>();

  /** Emitted when Enter key pressed */
  @Output() enterPressed = new EventEmitter<string>();

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

  /** View reference to input element */
  @ViewChild('inputElement') inputElement!: ElementRef<HTMLInputElement>;

  /**
   * LIFECYCLE HOOKS
   */

  ngOnInit(): void {
    // Generate unique IDs for accessibility
    this.fieldId = generateFieldId('text-input');
    this.ariaDescribedBy = generateAriaDescribedBy(this.fieldId);
  }

  ngOnDestroy(): void {
    // Clean up event emitters
    this.valueChanged.complete();
    this.cleared.complete();
    this.focused.complete();
    this.blurred.complete();
    this.enterPressed.complete();
  }

  /**
   * EVENT HANDLERS
   * Called from template, emit events to parent
   */

  /**
   * Handle input value change
   * Parent component handles the business logic
   * @param event - Input change event
   */
  onInputChange(value: string): void {
    this.value = value;
    this.valueChanged.emit(value);
  }

  /**
   * Handle clear button click
   * Reset value and notify parent
   */
  onClear(): void {
    this.value = '';
    this.cleared.emit();
    this.valueChanged.emit('');
    // Focus input after clear (better UX)
    setTimeout(() => {
      this.inputElement?.nativeElement.focus();
    }, 0);
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
   * Handle keyboard events
   * @param event - Keyboard event
   */
  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !this.disabled) {
      this.enterPressed.emit(this.value);
    } else if (event.key === 'Escape') {
      this.onClear();
    }
  }

  /**
   * GETTERS
   * Computed properties for template
   */

  /** Should show icon when input is empty */
  get shouldShowIcon(): boolean {
    return !!this.icon && !this.value;
  }

  /** Should show clear button when input has value */
  get shouldShowClearButton(): boolean {
    return this.showClearButton && !!this.value && !this.disabled;
  }

  /** CSS class list for form field container */
  get fieldContainerClass(): string {
    return [
      this.customClass,
      this.hasErrorMessage ? 'has-error' : '',
      this.isFocused ? 'is-focused' : '',
      this.disabled ? 'is-disabled' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  /** Calculate character count for display */
  get charCount(): { current: number; max: number } {
    return {
      current: this.value.length,
      max: this.maxLength,
    };
  }

  /** Is character at max limit */
  get isCharLimitReached(): boolean {
    return this.value.length >= this.maxLength;
  }

  /** Effective aria-label (custom or auto-generated) */
  get effectiveAriaLabel(): string {
    return this.ariaLabel || this.label || this.placeholder || 'Text input';
  }
}
