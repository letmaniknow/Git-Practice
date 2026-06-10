/**
 * DateRangeFieldComponent Unit Tests
 * Full coverage of component functionality, accessibility, and edge cases
 *
 * Test Structure:
 * 1. Component initialization & default values
 * 2. @Input properties display and updates
 * 3. @Output event emissions
 * 4. UI states (focus, disabled, error, filled)
 * 5. Accessibility (WCAG AA compliance)
 * 6. Date validation (range validity, min/max)
 * 7. Edge cases (invalid ranges, dates out of bounds)
 * 8. Helper methods
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

import { DateRangeFieldComponent } from './date-range-field.component';

describe('DateRangeFieldComponent', () => {
  let component: DateRangeFieldComponent;
  let fixture: ComponentFixture<DateRangeFieldComponent>;
  let compiled: DebugElement;

  /**
   * MOCK DATA
   */
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const tomorrow = new Date(today);
  tomorrow.setDate(tomorrow.getDate() + 1);

  const nextWeek = new Date(today);
  nextWeek.setDate(nextWeek.getDate() + 7);

  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  const nextMonth = new Date(today);
  nextMonth.setMonth(nextMonth.getMonth() + 1);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DateRangeFieldComponent, BrowserAnimationsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(DateRangeFieldComponent);
    component = fixture.componentInstance;
    compiled = fixture.debugElement;
  });

  afterEach(() => {
    fixture.destroy();
  });

  /**
   * SECTION 1: COMPONENT INITIALIZATION & DEFAULT VALUES
   */

  describe('Initialization', () => {
    it('should create component', () => {
      expect(component).toBeTruthy();
    });

    it('should have default input values', () => {
      expect(component.label).toBe('');
      expect(component.placeholderFrom).toBe('From date');
      expect(component.placeholderTo).toBe('To date');
      expect(component.fromDate).toBeNull();
      expect(component.toDate).toBeNull();
      expect(component.minDate).toBeNull();
      expect(component.maxDate).toBeNull();
      expect(component.disabled).toBe(false);
      expect(component.required).toBe(false);
      expect(component.hasErrorMessage).toBeNull();
      expect(component.disablePastDates).toBe(false);
      expect(component.disableFutureDates).toBe(false);
    });

    it('should generate unique fieldId on init', () => {
      component.ngOnInit();
      expect(component.fieldId).toBeTruthy();
      expect(component.fieldId).toContain('date-range-field');
    });

    it('should generate ariaDescribedBy on init', () => {
      component.ngOnInit();
      expect(component.ariaDescribedBy).toBeTruthy();
    });

    it('should not be focused on init', () => {
      expect(component.isFocused).toBe(false);
    });

    it('should set minDate to today when disablePastDates=true', () => {
      component.disablePastDates = true;
      component.ngOnInit();

      expect(component.minDate).toBeTruthy();
      const minDate = component.minDate as Date;
      expect(minDate.getHours()).toBe(0);
      expect(minDate.getMinutes()).toBe(0);
    });

    it('should set maxDate to today when disableFutureDates=true', () => {
      component.disableFutureDates = true;
      component.ngOnInit();

      expect(component.maxDate).toBeTruthy();
      const maxDate = component.maxDate as Date;
      expect(maxDate.getHours()).toBe(23);
      expect(maxDate.getMinutes()).toBe(59);
    });

    it('should not override minDate if already set and disablePastDates=true', () => {
      const customMinDate = new Date(2020, 0, 1);
      component.minDate = customMinDate;
      component.disablePastDates = true;
      component.ngOnInit();

      expect(component.minDate).toBe(customMinDate);
    });
  });

  /**
   * SECTION 2: @INPUT PROPERTIES DISPLAY
   */

  describe('@Input Properties', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should display label when provided', () => {
      component.label = 'Date Range';
      fixture.detectChanges();

      const label = compiled.query(By.css('.field-label'));
      expect(label?.nativeElement.textContent).toContain('Date Range');
    });

    it('should render required indicator when required=true', () => {
      component.label = 'Date Range';
      component.required = true;
      fixture.detectChanges();

      const indicator = compiled.query(By.css('.required-indicator'));
      expect(indicator).toBeTruthy();
      expect(indicator?.nativeElement.textContent).toContain('*');
    });

    it('should not render required indicator when required=false', () => {
      component.required = false;
      fixture.detectChanges();

      const indicator = compiled.query(By.css('.required-indicator'));
      expect(indicator).toBeFalsy();
    });

    it('should display FROM placeholder text', () => {
      component.placeholderFrom = 'Start Date';
      fixture.detectChanges();

      expect(component.placeholderFrom).toBe('Start Date');
    });

    it('should display TO placeholder text', () => {
      component.placeholderTo = 'End Date';
      fixture.detectChanges();

      expect(component.placeholderTo).toBe('End Date');
    });

    it('should set min attribute on date inputs', () => {
      component.minDate = yesterday;
      fixture.detectChanges();

      expect(component.minDate).toBe(yesterday);
    });

    it('should set max attribute on date inputs', () => {
      component.maxDate = nextMonth;
      fixture.detectChanges();

      expect(component.maxDate).toBe(nextMonth);
    });

    it('should disable field when disabled=true', () => {
      component.disabled = true;
      fixture.detectChanges();

      const clearbtn = compiled.query(By.css('.clear-dates-button'));
      expect(clearbtn?.nativeElement.disabled).toBe(true);
    });
  });

  /**
   * SECTION 3: @OUTPUT EVENT EMISSIONS
   */

  describe('@Output Events', () => {
    beforeEach(() => {
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should emit fromDateChanged when FROM date changes', (done) => {
      component.fromDateChanged.subscribe((date) => {
        expect(date).toBe(today);
        done();
      });

      component.onFromDateChange(today);
    });

    it('should emit toDateChanged when TO date changes', (done) => {
      component.toDateChanged.subscribe((date) => {
        expect(date).toBe(nextWeek);
        done();
      });

      component.onToDateChange(nextWeek);
    });

    it('should emit rangeSelected when both dates set', (done) => {
      component.fromDate = today;

      component.rangeSelected.subscribe((range) => {
        expect(range.start).toBe(today);
        expect(range.end).toBe(nextWeek);
        done();
      });

      component.onToDateChange(nextWeek);
    });

    it('should not emit rangeSelected when only FROM set', (done) => {
      let rangeEmitted = false;

      component.rangeSelected.subscribe(() => {
        rangeEmitted = true;
      });

      component.onFromDateChange(today);

      setTimeout(() => {
        expect(rangeEmitted).toBe(false);
        done();
      }, 100);
    });

    it('should emit cleared when dates cleared', (done) => {
      component.fromDate = today;
      component.toDate = nextWeek;

      component.cleared.subscribe(() => {
        expect(component.fromDate).toBeNull();
        expect(component.toDate).toBeNull();
        done();
      });

      component.onClearDates();
    });

    it('should emit focused when field receives focus', (done) => {
      component.focused.subscribe(() => {
        expect(component.isFocused).toBe(true);
        done();
      });

      component.onFocus();
    });

    it('should emit blurred when field loses focus', (done) => {
      component.isFocused = true;

      component.blurred.subscribe(() => {
        expect(component.isFocused).toBe(false);
        done();
      });

      component.onBlur();
    });
  });

  /**
   * SECTION 4: UI STATES & RENDERING
   */

  describe('UI States', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should render error message when hasErrorMessage provided', () => {
      component.hasErrorMessage = 'Please select a valid date range';
      fixture.detectChanges();

      const error = compiled.query(By.css('.error-message'));
      expect(error?.nativeElement.textContent).toContain(
        'Please select a valid date range'
      );
    });

    it('should not render error message when hasErrorMessage is null', () => {
      component.hasErrorMessage = null;
      fixture.detectChanges();

      const error = compiled.query(By.css('.error-message'));
      expect(error).toBeFalsy();
    });

    it('should show clear button when dates set', () => {
      component.fromDate = today;
      component.toDate = nextWeek;
      fixture.detectChanges();

      const clearBtn = compiled.query(By.css('.clear-dates-button'));
      expect(clearBtn).toBeTruthy();
    });

    it('should hide clear button when no dates set', () => {
      component.fromDate = null;
      component.toDate = null;
      fixture.detectChanges();

      const clearBtn = compiled.query(By.css('.clear-dates-button'));
      expect(clearBtn).toBeFalsy();
    });

    it('should hide clear button when disabled', () => {
      component.fromDate = today;
      component.disabled = true;
      fixture.detectChanges();

      const clearBtn = compiled.query(By.css('.clear-dates-button'));
      expect(clearBtn).toBeFalsy();
    });

    it('should apply has-error class when hasErrorMessage provided', () => {
      component.hasErrorMessage = 'Error';
      fixture.detectChanges();

      const container = compiled.query(By.css('.date-range-field-container'));
      expect(container?.nativeElement.classList.contains('has-error')).toBe(true);
    });

    it('should apply has-value class when dates set', () => {
      component.fromDate = today;
      component.toDate = nextWeek;
      fixture.detectChanges();

      const container = compiled.query(By.css('.date-range-field-container'));
      expect(container?.nativeElement.classList.contains('has-value')).toBe(true);
    });

    it('should apply is-disabled class when disabled=true', () => {
      component.disabled = true;
      fixture.detectChanges();

      const container = compiled.query(By.css('.date-range-field-container'));
      expect(container?.nativeElement.classList.contains('is-disabled')).toBe(true);
    });

    it('should show range complete hint when both dates set', () => {
      component.fromDate = today;
      component.toDate = nextWeek;
      fixture.detectChanges();

      const hint = compiled.query(By.css('.range-complete-hint'));
      expect(hint).toBeTruthy();
      expect(hint?.nativeElement.textContent).toContain('to');
    });

    it('should show incomplete hint when only one date set', () => {
      component.fromDate = today;
      component.toDate = null;
      fixture.detectChanges();

      const hint = compiled.query(By.css('.range-incomplete-hint'));
      expect(hint).toBeTruthy();
    });
  });

  /**
   * SECTION 5: ACCESSIBILITY (WCAG AA)
   */

  describe('Accessibility (WCAG AA)', () => {
    beforeEach(() => {
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should have aria-label on clear button', () => {
      component.fromDate = today;
      fixture.detectChanges();

      const clearBtn = compiled.query(By.css('.clear-dates-button'));
      expect(clearBtn?.nativeElement.getAttribute('aria-label')).toContain('Clear');
    });

    it('should have aria-label on date inputs', () => {
      const inputs = compiled.queryAll(By.css('input[matInput]'));
      expect(inputs[0]?.nativeElement.getAttribute('aria-label')).toContain('From');
      expect(inputs[1]?.nativeElement.getAttribute('aria-label')).toContain('To');
    });

    it('should set aria-invalid when range invalid', () => {
      component.fromDate = nextWeek; // From is after To
      component.toDate = today;
      fixture.detectChanges();

      const inputs = compiled.queryAll(By.css('input[matInput]'));
      expect(inputs[0]?.nativeElement.getAttribute('aria-invalid')).toBe('true');
      expect(inputs[1]?.nativeElement.getAttribute('aria-invalid')).toBe('true');
    });

    it('should use provided ariaLabel when set', () => {
      component.ariaLabel = 'Custom aria label';
      fixture.detectChanges();

      expect(component.effectiveAriaLabel).toBe('Custom aria label');
    });

    it('should link error message with aria-describedby', () => {
      component.hasErrorMessage = 'Error message';
      fixture.detectChanges();

      const error = compiled.query(By.css('.error-message'));
      expect(error?.nativeElement.id).toBe(component.ariaDescribedBy);
    });
  });

  /**
   * SECTION 6: DATE VALIDATION
   */

  describe('Date Validation', () => {
    beforeEach(() => {
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should check if range is complete', () => {
      component.fromDate = null;
      component.toDate = null;
      expect(component.isRangeComplete()).toBe(false);

      component.fromDate = today;
      expect(component.isRangeComplete()).toBe(false);

      component.toDate = nextWeek;
      expect(component.isRangeComplete()).toBe(true);
    });

    it('should check if range is valid (FROM <= TO)', () => {
      component.fromDate = today;
      component.toDate = nextWeek;
      expect(component.isRangeValid()).toBe(true);

      // TO before FROM is invalid
      component.toDate = yesterday;
      expect(component.isRangeValid()).toBe(false);

      // Same date is valid
      component.toDate = today;
      expect(component.isRangeValid()).toBe(true);
    });

    it('should show error when range is invalid', () => {
      component.fromDate = nextWeek;
      component.toDate = today;
      fixture.detectChanges();

      expect(component.rangeErrorMessage).toContain('From date must be before To date');
    });

    it('should apply invalid-range class when range invalid', () => {
      component.fromDate = nextWeek;
      component.toDate = today;
      fixture.detectChanges();

      const container = compiled.query(By.css('.date-range-field-container'));
      expect(container?.nativeElement.classList.contains('invalid-range')).toBe(true);
    });

    it('should filter dates based on minDate', () => {
      const minDate = new Date(today);
      minDate.setDate(minDate.getDate() + 5);
      component.minDate = minDate;

      // Date before min should be filtered out
      expect(component.dateFilter(today)).toBe(false);

      // Date after min should be allowed
      expect(component.dateFilter(nextMonth)).toBe(true);
    });

    it('should filter dates based on maxDate', () => {
      const maxDate = new Date(today);
      maxDate.setDate(maxDate.getDate() + 5);
      component.maxDate = maxDate;

      // Date before max should be allowed
      expect(component.dateFilter(today)).toBe(true);

      // Date after max should be filtered out
      expect(component.dateFilter(nextMonth)).toBe(false);
    });

    it('should handle null dates in filter', () => {
      expect(component.dateFilter(null)).toBe(true);
    });
  });

  /**
   * SECTION 7: HELPER METHODS
   */

  describe('Helper Methods', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should get FROM date display text', () => {
      component.fromDate = today;
      const display = component.getFromDateDisplay();
      expect(display).toBeTruthy();
      expect(display.length).toBeGreaterThan(0);
    });

    it('should get TO date display text', () => {
      component.toDate = nextWeek;
      const display = component.getToDateDisplay();
      expect(display).toBeTruthy();
      expect(display.length).toBeGreaterThan(0);
    });

    it('should return empty string when FROM date is null', () => {
      component.fromDate = null;
      expect(component.getFromDateDisplay()).toBe('');
    });

    it('should return empty string when TO date is null', () => {
      component.toDate = null;
      expect(component.getToDateDisplay()).toBe('');
    });
  });

  /**
   * SECTION 8: EDGE CASES
   */

  describe('Edge Cases', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should handle same date for FROM and TO', () => {
      component.fromDate = today;
      component.toDate = today;

      expect(component.isRangeValid()).toBe(true);
      expect(component.isRangeComplete()).toBe(true);
    });

    it('should handle clearing dates multiple times', () => {
      component.fromDate = today;
      component.toDate = nextWeek;

      component.onClearDates();
      expect(component.fromDate).toBeNull();
      expect(component.toDate).toBeNull();

      component.onClearDates();
      expect(component.fromDate).toBeNull();
      expect(component.toDate).toBeNull();
    });

    it('should handle rapid date changes', () => {
      component.onFromDateChange(today);
      component.onToDateChange(tomorrow);
      component.onFromDateChange(yesterday);

      expect(component.fromDate).toBe(yesterday);
      expect(component.toDate).toBe(tomorrow);
    });

    it('should handle changing FROM after TO is set', () => {
      component.toDate = nextWeek;
      component.fromDate = today;

      expect(component.isRangeValid()).toBe(true);
    });

    it('should handle disabled dates in range', () => {
      component.minDate = tomorrow;
      component.maxDate = nextMonth;

      // Date before min is disabled
      expect(component.dateFilter(yesterday)).toBe(false);

      // Date after max is disabled
      const afterMax = new Date(nextMonth);
      afterMax.setDate(afterMax.getDate() + 1);
      expect(component.dateFilter(afterMax)).toBe(false);
    });
  });

  /**
   * SECTION 9: GETTERS
   */

  describe('Getters', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should return field container class with error', () => {
      component.hasErrorMessage = 'Error';
      expect(component.fieldContainerClass).toContain('has-error');
    });

    it('should return field container class with value', () => {
      component.fromDate = today;
      expect(component.fieldContainerClass).toContain('has-value');
    });

    it('should return field container class when focused', () => {
      component.isFocused = true;
      expect(component.fieldContainerClass).toContain('is-focused');
    });

    it('should return field container class when disabled', () => {
      component.disabled = true;
      expect(component.fieldContainerClass).toContain('is-disabled');
    });

    it('should return field container class when invalid range', () => {
      component.fromDate = nextWeek;
      component.toDate = today;
      expect(component.fieldContainerClass).toContain('invalid-range');
    });

    it('should show clear button only when dates exist', () => {
      component.fromDate = today;
      component.disabled = false;
      expect(component.showClearButton).toBe(true);

      component.disabled = true;
      expect(component.showClearButton).toBe(false);
    });
  });

  /**
   * SECTION 10: CLEANUP
   */

  describe('Cleanup', () => {
    it('should complete event emitters on destroy', () => {
      component.ngOnInit();

      spyOn(component.fromDateChanged, 'complete');
      spyOn(component.toDateChanged, 'complete');
      spyOn(component.cleared, 'complete');
      spyOn(component.rangeSelected, 'complete');
      spyOn(component.focused, 'complete');
      spyOn(component.blurred, 'complete');

      component.ngOnDestroy();

      expect(component.fromDateChanged.complete).toHaveBeenCalled();
      expect(component.toDateChanged.complete).toHaveBeenCalled();
      expect(component.cleared.complete).toHaveBeenCalled();
      expect(component.rangeSelected.complete).toHaveBeenCalled();
      expect(component.focused.complete).toHaveBeenCalled();
      expect(component.blurred.complete).toHaveBeenCalled();
    });
  });
});
