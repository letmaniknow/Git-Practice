/**
 * SelectFieldComponent - Unit Tests
 *
 * Tests for SelectFieldComponent - a reusable, presentation-only select dropdown
 * 
 * TEST STRATEGY:
 * ✅ Input/Output contracts
 * ✅ Option rendering and sorting
 * ✅ Selection handling
 * ✅ Null option handling
 * ✅ Accessibility compliance
 * ✅ Edge cases
 *
 * @module SelectFieldComponent.spec
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { SelectFieldComponent } from './select-field.component';
import { ISelectOption } from '../../models/form-field.models';

describe('SelectFieldComponent', () => {
  let component: SelectFieldComponent<string>;
  let fixture: ComponentFixture<SelectFieldComponent<string>>;
  let debugElement: DebugElement;

  const mockOptions: ISelectOption<string>[] = [
    { value: '1', label: 'Technology' },
    { value: '2', label: 'Sports' },
    { value: '3', label: 'Politics' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        SelectFieldComponent,
        MatSelectModule,
        MatFormFieldModule,
        BrowserAnimationsModule,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SelectFieldComponent<string>);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  afterEach(() => {
    fixture.destroy();
  });

  /**
   * BASIC SETUP TESTS
   */
  describe('Component Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.selectedValue).toBeNull();
      expect(component.options).toEqual([]);
      expect(component.disabled).toBe(false);
      expect(component.required).toBe(false);
      expect(component.allowNullOption).toBe(false);
    });

    it('should generate fieldId on init', () => {
      fixture.detectChanges();
      expect(component.fieldId).toBeTruthy();
      expect(component.fieldId).toContain('select-field');
    });

    it('should update display options on init', () => {
      component.options = mockOptions;
      fixture.detectChanges();
      expect(component.displayOptions).toEqual(mockOptions);
    });
  });

  /**
   * INPUT PROPERTIES TESTS
   */
  describe('@Input Properties', () => {
    beforeEach(() => {
      component.label = 'Category';
      component.options = mockOptions;
      component.selectedValue = '2';
      fixture.detectChanges();
    });

    it('should display label', () => {
      const label = debugElement.query(By.css('mat-label'));
      expect(label.nativeElement.textContent).toContain('Category');
    });

    it('should render all options', () => {
      const matSelect = debugElement.query(By.css('mat-select'));
      matSelect.nativeElement.click();
      fixture.detectChanges();

      const options = debugElement.queryAll(By.css('mat-option'));
      expect(options.length).toBe(mockOptions.length);
    });

    it('should show null option when allowNullOption is true', () => {
      component.allowNullOption = true;
      component.nullOptionLabel = 'All Categories';
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      matSelect.nativeElement.click();
      fixture.detectChanges();

      const nullOption = debugElement.query(By.css('mat-option.null-option'));
      expect(nullOption).toBeTruthy();
      expect(nullOption.nativeElement.textContent).toContain('All Categories');
    });

    it('should hide null option when allowNullOption is false', () => {
      component.allowNullOption = false;
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      matSelect.nativeElement.click();
      fixture.detectChanges();

      const nullOption = debugElement.query(By.css('mat-option.null-option'));
      expect(nullOption).toBeFalsy();
    });

    it('should disable field when disabled prop is true', () => {
      component.disabled = true;
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      expect(matSelect.nativeElement.getAttribute('aria-disabled')).toBe('true');
    });

    it('should show required indicator when required is true', () => {
      component.required = true;
      fixture.detectChanges();

      const indicator = debugElement.query(By.css('.required-indicator'));
      expect(indicator).toBeTruthy();
    });
  });

  /**
   * OPTION SORTING TESTS
   */
  describe('Option Sorting', () => {
    it('should not sort options by default', () => {
      const unsortedOptions: ISelectOption<string>[] = [
        { value: '3', label: 'Zebra' },
        { value: '1', label: 'Apple' },
        { value: '2', label: 'Banana' },
      ];

      component.options = unsortedOptions;
      component.sortOptions = false;
      component.updateDisplayOptions();

      expect(component.displayOptions).toEqual(unsortedOptions);
    });

    it('should sort options alphabetically when sortOptions is true', () => {
      const unsortedOptions: ISelectOption<string>[] = [
        { value: '3', label: 'Zebra' },
        { value: '1', label: 'Apple' },
        { value: '2', label: 'Banana' },
      ];

      component.options = unsortedOptions;
      component.sortOptions = true;
      component.updateDisplayOptions();

      const labels = component.displayOptions.map((opt) => opt.label);
      expect(labels).toEqual(['Apple', 'Banana', 'Zebra']);
    });
  });

  /**
   * SELECTION HANDLING TESTS
   */
  describe('Selection Handling', () => {
    beforeEach(() => {
      component.options = mockOptions;
      fixture.detectChanges();
    });

    it('should emit selectionChanged when option selected', (done) => {
      spyOn(component.selectionChanged, 'emit');

      component.onSelectionChange('2');

      expect(component.selectionChanged.emit).toHaveBeenCalledWith('2');
      expect(component.selectedValue).toBe('2');
      done();
    });

    it('should emit selectionChanged with null when null option selected', (done) => {
      spyOn(component.selectionChanged, 'emit');

      component.onSelectionChange(null);

      expect(component.selectionChanged.emit).toHaveBeenCalledWith(null);
      expect(component.selectedValue).toBeNull();
      done();
    });

    it('should update selectedValue when new value provided', (done) => {
      component.selectedValue = null;
      expect(component.selectedValue).toBeNull();

      component.selectedValue = '1';
      fixture.detectChanges();

      expect(component.selectedValue).toBe('1');
      done();
    });
  });

  /**
   * FOCUS/BLUR TESTS
   */
  describe('Focus and Blur Events', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should emit focused when field receives focus', (done) => {
      spyOn(component.focused, 'emit');

      component.onFocus();

      expect(component.focused.emit).toHaveBeenCalled();
      expect(component.isFocused).toBe(true);
      done();
    });

    it('should emit blurred when field loses focus', (done) => {
      component.isFocused = true;

      spyOn(component.blurred, 'emit');

      component.onBlur();

      expect(component.blurred.emit).toHaveBeenCalled();
      expect(component.isFocused).toBe(false);
      done();
    });
  });

  /**
   * ACCESSIBILITY TESTS
   */
  describe('Accessibility (WCAG AA)', () => {
    beforeEach(() => {
      component.label = 'Choose category';
      component.options = mockOptions;
      component.required = true;
      fixture.detectChanges();
    });

    it('should have aria-label on select', () => {
      const matSelect = debugElement.query(By.css('mat-select'));
      expect(matSelect.nativeElement.getAttribute('aria-label')).toBeTruthy();
    });

    it('should set aria-invalid when error exists', (done) => {
      component.hasErrorMessage = 'Selection required';
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      expect(matSelect.nativeElement.getAttribute('aria-invalid')).toBe('true');
      done();
    });

    it('should set aria-invalid to false when no error', (done) => {
      component.hasErrorMessage = null;
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      expect(matSelect.nativeElement.getAttribute('aria-invalid')).toBe('false');
      done();
    });

    it('should link error message via aria-describedby', () => {
      component.hasErrorMessage = 'Error message';
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      const ariaDescribedBy = matSelect.nativeElement.getAttribute(
        'aria-describedby'
      );
      expect(ariaDescribedBy).toBeTruthy();

      const error = debugElement.query(By.css('mat-error'));
      expect(error.nativeElement.id).toBe(ariaDescribedBy);
    });
  });

  /**
   * ERROR HANDLING TESTS
   */
  describe('Error Handling', () => {
    it('should show error message when hasErrorMessage is set', () => {
      component.hasErrorMessage = 'This field is required';
      fixture.detectChanges();

      const error = debugElement.query(By.css('mat-error'));
      expect(error).toBeTruthy();
      expect(error.nativeElement.textContent).toContain('This field is required');
    });

    it('should hide error message when hasErrorMessage is null', () => {
      component.hasErrorMessage = null;
      fixture.detectChanges();

      const error = debugElement.query(By.css('mat-error'));
      expect(error).toBeFalsy();
    });
  });

  /**
   * HELPER METHODS TESTS
   */
  describe('Helper Methods', () => {
    it('getSelectedLabel should return null option label when selectedValue is null', () => {
      component.nullOptionLabel = 'All';
      component.selectedValue = null;

      expect(component.getSelectedLabel()).toBe('All');
    });

    it('getSelectedLabel should return option label when value selected', () => {
      component.options = mockOptions;
      component.displayOptions = mockOptions;
      component.selectedValue = '2';

      expect(component.getSelectedLabel()).toBe('Sports');
    });

    it('trackByOptionValue should return option value', () => {
      const option: ISelectOption<string> = { value: 'test-id', label: 'Test' };
      expect(component.trackByOptionValue(0, option)).toBe('test-id');
    });

    it('compareWith should return true for same values', () => {
      expect(component.compareWith('1', '1')).toBe(true);
      expect(component.compareWith(null, null)).toBe(true);
    });

    it('compareWith should return false for different values', () => {
      expect(component.compareWith('1', '2')).toBe(false);
      expect(component.compareWith('1', null)).toBe(false);
      expect(component.compareWith(null, '1')).toBe(false);
    });
  });

  /**
   * DISABLED OPTIONS TESTS
   */
  describe('Disabled Options', () => {
    it('should mark options as disabled when specified', () => {
      const optionsWithDisabled: ISelectOption<string>[] = [
        { value: '1', label: 'Technology' },
        { value: '2', label: 'Sports', disabled: true },
        { value: '3', label: 'Politics' },
      ];

      component.options = optionsWithDisabled;
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      matSelect.nativeElement.click();
      fixture.detectChanges();

      const disabledOption = debugElement.queryAll(By.css('mat-option'))[1];
      expect(disabledOption.nativeElement.getAttribute('aria-disabled')).toBe('true');
    });
  });

  /**
   * OPTION WITH ICONS TESTS
   */
  describe('Options with Icons', () => {
    it('should display icon when option has icon property', () => {
      const optionsWithIcons: ISelectOption<string>[] = [
        { value: '1', label: 'Technology', icon: 'computer' },
        { value: '2', label: 'Sports', icon: 'sports' },
      ];

      component.options = optionsWithIcons;
      fixture.detectChanges();

      const matSelect = debugElement.query(By.css('mat-select'));
      matSelect.nativeElement.click();
      fixture.detectChanges();

      const icons = debugElement.queryAll(By.css('.option-icon'));
      expect(icons.length).toBe(2);
      expect(icons[0].nativeElement.textContent).toContain('computer');
    });
  });

  /**
   * EDGE CASES TESTS
   */
  describe('Edge Cases', () => {
    it('should handle empty options array', () => {
      component.options = [];
      component.updateDisplayOptions();

      expect(component.displayOptions).toEqual([]);
    });

    it('should handle very long option labels', () => {
      const longLabel = 'a'.repeat(500);
      const optionsWithLongLabels: ISelectOption<string>[] = [
        { value: '1', label: longLabel },
      ];

      component.options = optionsWithLongLabels;
      fixture.detectChanges();

      expect(component.displayOptions[0].label).toBe(longLabel);
    });

    it('should handle numeric values', () => {
      const numericComponent = TestBed.createComponent(SelectFieldComponent<number>);
      const numericFixture = numericComponent.componentInstance;

      numericFixture.options = [
        { value: 1, label: 'One' },
        { value: 2, label: 'Two' },
      ];
      numericFixture.selectedValue = 1;

      expect(numericFixture.selectedValue).toBe(1);
    });
  });
});
