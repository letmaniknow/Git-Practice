import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { DatePipe } from '@angular/common';

interface DatetimePickerDialogData {
  selectedDateTime: Date | null;
  min?: Date;
  max?: Date;
}

interface TimeFormModel {
  hour: FormControl<number>;
  minute: FormControl<number>;
  period: FormControl<'AM' | 'PM'>;
}

/**
 * Dialog component for the unified datetime picker
 * Provides a professional interface with:
 * - Calendar on the left (Material datepicker)
 * - Time controls on the right (hour, minute, AM/PM)
 * - Live preview
 * - Confirm/Cancel buttons
 */
@Component({
  selector: 'app-unified-datetime-picker-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './unified-datetime-picker-dialog.component.html',
  styleUrls: ['./unified-datetime-picker-dialog.component.scss'],
  providers: [DatePipe],
})
export class UnifiedDatetimePickerDialogComponent implements OnInit {
  dateControl = new FormControl<Date | null>(null, { nonNullable: true });
  timeForm = new FormGroup<TimeFormModel>({
    hour: new FormControl(12, { nonNullable: true, validators: [Validators.min(1), Validators.max(12)] }) as FormControl<number>,
    minute: new FormControl(0, { nonNullable: true, validators: [Validators.min(0), Validators.max(59)] }) as FormControl<number>,
    period: new FormControl<'AM' | 'PM'>('AM', { nonNullable: true }) as FormControl<'AM' | 'PM'>,
  });

  previewDateTime: string = '';
  previewTime: string = '';
  minDate: Date = new Date(); // Minimum date (today)
  private initialDateTime: Date;

  // Getters for template access with proper typing
  get hourControl(): FormControl<number> {
    return this.timeForm.get('hour') as FormControl<number>;
  }

  get minuteControl(): FormControl<number> {
    return this.timeForm.get('minute') as FormControl<number>;
  }

  get periodControl(): FormControl<'AM' | 'PM'> {
    return this.timeForm.get('period') as FormControl<'AM' | 'PM'>;
  }

  constructor(
    private dialogRef: MatDialogRef<UnifiedDatetimePickerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data: DatetimePickerDialogData,
    private datePipe: DatePipe
  ) {
    this.initialDateTime = this.data?.selectedDateTime ? new Date(this.data.selectedDateTime) : new Date();
  }

  ngOnInit(): void {
    // Set minimum date to today (cannot schedule for past dates)
    this.minDate = new Date();
    this.minDate.setHours(0, 0, 0, 0);
    
    this.initializeForm();
    this.setupPreviewUpdates();
  }

  /**
   * Initialize form with current datetime values
   */
  private initializeForm(): void {
    // Set initial date
    const initialDate = this.data?.selectedDateTime ? new Date(this.data.selectedDateTime) : new Date();
    this.dateControl.setValue(initialDate, { emitEvent: false });

    // Extract and set time components
    const hours24 = initialDate.getHours();
    const minutes = initialDate.getMinutes();
    const is24HourMode = true;

    const { hour12, period } = this.convert24to12(hours24);

    this.timeForm.patchValue(
      {
        hour: hour12,
        minute: minutes,
        period: period,
      },
      { emitEvent: false }
    );

    this.updatePreview();
  }

  /**
   * Setup listeners for form changes to update preview
   */
  private setupPreviewUpdates(): void {
    this.dateControl.valueChanges.subscribe(() => this.updatePreview());
    this.timeForm.get('hour')?.valueChanges.subscribe(() => this.updatePreview());
    this.timeForm.get('minute')?.valueChanges.subscribe(() => this.updatePreview());
    this.timeForm.get('period')?.valueChanges.subscribe(() => this.updatePreview());
  }

  /**
   * Convert 24-hour format to 12-hour format with AM/PM
   */
  private convert24to12(hours24: number): { hour12: number; period: 'AM' | 'PM' } {
    const period: 'AM' | 'PM' = hours24 >= 12 ? 'PM' : 'AM';
    let hour12 = hours24 % 12;
    if (hour12 === 0) hour12 = 12;
    return { hour12, period };
  }

  /**
   * Convert 12-hour format with AM/PM to 24-hour format
   */
  private convert12to24(hour12: number, period: 'AM' | 'PM'): number {
    let hours24 = hour12;
    if (period === 'AM' && hour12 === 12) {
      hours24 = 0; // 12 AM = 00:00 (midnight)
    } else if (period === 'PM' && hour12 !== 12) {
      hours24 += 12; // 1-11 PM = 13-23
    }
    // 12 PM stays as 12
    return hours24;
  }

  /**
   * Update preview display of selected datetime
   */
  private updatePreview(): void {
    const selectedDate = this.dateControl.value;
    if (!selectedDate) return;

    const hour = this.timeForm.get('hour')?.value || 12;
    const minute = this.timeForm.get('minute')?.value || 0;
    const period = this.timeForm.get('period')?.value as 'AM' | 'PM' || 'AM';

    // Format date preview
    this.previewDateTime = this.datePipe.transform(selectedDate, 'EEEE, MMMM d, yyyy', undefined, 'en-US') || '';

    // Format time preview
    const formattedMinute = String(minute).padStart(2, '0');
    this.previewTime = `${hour}:${formattedMinute} ${period}`;
  }

  /**
   * Confirms the selected datetime and closes dialog
   */
  confirmDatetime(): void {
    const selectedDate = this.dateControl.value;
    if (!selectedDate) return;

    const hour = this.timeForm.get('hour')?.value || 12;
    const minute = this.timeForm.get('minute')?.value || 0;
    const period = this.timeForm.get('period')?.value as 'AM' | 'PM' || 'AM';

    // Convert to 24-hour format
    const hours24 = this.convert12to24(hour, period);

    // Create datetime object
    const resultDate = new Date(selectedDate);
    resultDate.setHours(hours24, minute, 0, 0);

    // Validate against min/max
    if (this.data?.min && resultDate < this.data.min) {
      alert('Selected date/time is before the minimum allowed date/time');
      return;
    }

    if (this.data?.max && resultDate > this.data.max) {
      alert('Selected date/time is after the maximum allowed date/time');
      return;
    }

    // Validate that selected time is in the future (for scheduling)
    const now = new Date();
    
    // Get the selected date without time for comparison
    const selectedDateOnly = new Date(selectedDate);
    selectedDateOnly.setHours(0, 0, 0, 0);
    
    // Get today's date without time
    const todayOnly = new Date(now);
    todayOnly.setHours(0, 0, 0, 0);
    
    // If selected date is in the past, reject
    if (selectedDateOnly < todayOnly) {
      alert('Cannot schedule for past dates. Please select today or a future date.');
      return;
    }
    
    // If selected date is today, check if time is in the future
    if (selectedDateOnly.getTime() === todayOnly.getTime()) {
      // Compare time in milliseconds for accuracy
      const resultTimeMs = resultDate.getTime();
      const nowTimeMs = now.getTime();
      
      if (resultTimeMs <= nowTimeMs) {
        const currentTimeStr = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true });
        alert(`Selected time must be in the future. Current time is ${currentTimeStr}. Please select a later time.`);
        return;
      }
    }

    this.dialogRef.close(resultDate);
  }

  /**
   * Cancels the selection
   */
  cancelSelection(): void {
    this.dialogRef.close(null);
  }

  /**
   * Sets time to current time
   */
  setToNow(): void {
    const now = new Date();
    this.dateControl.setValue(now, { emitEvent: false });

    const { hour12, period } = this.convert24to12(now.getHours());
    this.timeForm.patchValue(
      {
        hour: hour12,
        minute: now.getMinutes(),
        period: period,
      },
      { emitEvent: false }
    );

    this.updatePreview();
  }

  /**
   * Increments the hour
   */
  incrementHour(): void {
    const current = this.timeForm.get('hour')?.value || 1;
    const next = current === 12 ? 1 : current + 1;
    this.timeForm.get('hour')?.setValue(next, { emitEvent: false });
    this.updatePreview();
  }

  /**
   * Decrements the hour
   */
  decrementHour(): void {
    const current = this.timeForm.get('hour')?.value || 1;
    const next = current === 1 ? 12 : current - 1;
    this.timeForm.get('hour')?.setValue(next, { emitEvent: false });
    this.updatePreview();
  }

  /**
   * Increments the minutes
   */
  incrementMinute(): void {
    const current = this.timeForm.get('minute')?.value || 0;
    const next = current === 59 ? 0 : current + 1;
    this.timeForm.get('minute')?.setValue(next, { emitEvent: false });
    this.updatePreview();
  }

  /**
   * Decrements the minutes
   */
  decrementMinute(): void {
    const current = this.timeForm.get('minute')?.value || 0;
    const next = current === 0 ? 59 : current - 1;
    this.timeForm.get('minute')?.setValue(next, { emitEvent: false });
    this.updatePreview();
  }

  /**
   * Toggles between AM and PM
   */
  togglePeriod(): void {
    const current = this.timeForm.get('period')?.value;
    const next = current === 'AM' ? 'PM' : 'AM';
    this.timeForm.get('period')?.setValue(next, { emitEvent: true });
    this.updatePreview();
  }

  /**
   * Filter for custom styled minute input
   */
  minuteFilter(value: string): number | null {
    const num = parseInt(value, 10);
    return !isNaN(num) && num >= 0 && num <= 59 ? num : null;
  }
}
