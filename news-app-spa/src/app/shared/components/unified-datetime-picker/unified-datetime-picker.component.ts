import {
  Component,
  Input,
  Output,
  EventEmitter,
  Injector,
  OnInit,
  forwardRef,
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { DatePipe } from '@angular/common';
import { UnifiedDatetimePickerDialogComponent } from './unified-datetime-picker-dialog.component';

/**
 * Professional unified datetime picker component
 * Implements ControlValueAccessor for seamless form integration
 * Encapsulates all datetime logic in a reusable component
 *
 * Usage in forms:
 * <app-unified-datetime-picker
 *   [formControl]="myDateTimeControl"
 *   label="Select Date and Time"
 *   [min]="minDateTime"
 *   [max]="maxDateTime">
 * </app-unified-datetime-picker>
 */
@Component({
  selector: 'app-unified-datetime-picker',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule,
  ],
  templateUrl: './unified-datetime-picker.component.html',
  styleUrls: ['./unified-datetime-picker.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => UnifiedDatetimePickerComponent),
      multi: true,
    },
    DatePipe,
  ],
})
export class UnifiedDatetimePickerComponent implements ControlValueAccessor, OnInit {
  @Input() label = 'Date & Time';
  @Input() placeholder = 'Select date and time';
  @Input() min?: Date;
  @Input() max?: Date;
  @Input() disabled = false;
  @Input() required = false;
  @Input() errorMessage: string | null = null;
  @Input() hint: string | null = null;

  @Output() dateTimeChange = new EventEmitter<string>();

  displayControl = new FormControl<string>('', { nonNullable: true });
  private selectedDateTime: Date | null = null;
  private onChange: (value: string | null) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(
    private dialog: MatDialog,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    this.displayControl.disable({ emitEvent: false });
  }

  /**
   * Opens the unified datetime picker dialog
   * Launches Material Dialog with calendar + time picker interface
   */
  openDatetimePickerDialog(): void {
    if (this.disabled) return;

    const dialogRef = this.dialog.open(UnifiedDatetimePickerDialogComponent, {
      width: '500px',
      maxWidth: '95vw',
      maxHeight: '95vh',
      data: {
        selectedDateTime: this.selectedDateTime,
        min: this.min,
        max: this.max,
      },
      panelClass: 'unified-datetime-picker-dialog',
      disableClose: false,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result instanceof Date) {
        this.setDateTime(result);
        this.onTouched();
      }
    });
  }

  /**
   * Sets the selected datetime and updates the form control
   */
  private setDateTime(date: Date): void {
    this.selectedDateTime = date;
    const isoString = date.toISOString();
    this.displayControl.setValue(this.formatDateTimeForDisplay(date), { emitEvent: false });
    this.onChange(isoString);
    this.dateTimeChange.emit(isoString);
  }

  /**
   * Formats date for display in UI
   * Format: "Mar 4, 2026, 2:30 PM"
   */
  private formatDateTimeForDisplay(date: Date): string {
    const formatted = this.datePipe.transform(date, 'MMM d, yyyy, h:mm a', undefined, 'en-US');
    return formatted || '';
  }

  /**
   * Clears the selected datetime
   */
  clearDateTime(): void {
    this.selectedDateTime = null;
    this.displayControl.setValue('', { emitEvent: false });
    this.onChange(null);
    this.dateTimeChange.emit('');
  }

  /**
   * ControlValueAccessor implementation - writes value from form
   */
  writeValue(value: string | null): void {
    if (value) {
      try {
        const date = new Date(value);
        if (!isNaN(date.getTime())) {
          this.selectedDateTime = date;
          this.displayControl.setValue(this.formatDateTimeForDisplay(date), { emitEvent: false });
        }
      } catch (error) {
        console.warn('Invalid datetime value:', value);
      }
    } else {
      this.selectedDateTime = null;
      this.displayControl.setValue('', { emitEvent: false });
    }
  }

  /**
   * ControlValueAccessor implementation - registers onChange callback
   */
  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  /**
   * ControlValueAccessor implementation - registers onTouched callback
   */
  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  /**
   * ControlValueAccessor implementation - sets disabled state
   */
  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (isDisabled) {
      this.displayControl.disable({ emitEvent: false });
    } else {
      this.displayControl.enable({ emitEvent: false });
    }
  }

  /**
   * Getter for template - check if datetime is selected
   */
  get hasValue(): boolean {
    return this.selectedDateTime !== null;
  }

  /**
   * Getter for template - format selected datetime for display
   */
  get formattedDateTime(): string {
    if (!this.selectedDateTime) return '';
    return this.formatDateTimeForDisplay(this.selectedDateTime);
  }
}
