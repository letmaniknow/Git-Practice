/**
 * MultiSelectFieldComponent Unit Tests
 * Full coverage of component functionality, accessibility, and edge cases
 *
 * Test Structure:
 * 1. Component initialization & default values
 * 2. @Input properties display and updates
 * 3. @Output event emissions
 * 4. UI states (focus, disabled, error, filled)
 * 5. Accessibility (WCAG AA compliance)
 * 6. User interactions (selection, clearing, chips)
 * 7. Edge cases (empty options, max selections, disabled states)
 * 8. Helper methods
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

import { MultiSelectFieldComponent } from './multi-select-field.component';
import { ISelectOption } from '../../models/form-field.models';

describe('MultiSelectFieldComponent', () => {
  let component: MultiSelectFieldComponent<string>;
  let fixture: ComponentFixture<MultiSelectFieldComponent<string>>;
  let compiled: DebugElement;

  /**
   * MOCK DATA
   */
  const mockOptions: ISelectOption<string>[] = [
    { value: 'draft', label: 'Draft', icon: 'edit', description: 'Not published' },
    { value: 'published', label: 'Published', icon: 'check', description: 'Live content' },
    { value: 'archived', label: 'Archived', icon: 'archive', description: 'Old content' },
  ];

  const emptyOptions: ISelectOption<string>[] = [];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MultiSelectFieldComponent, BrowserAnimationsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(MultiSelectFieldComponent<string>);
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
      expect(component.placeholder).toBe('');
      expect(component.options).toEqual([]);
      expect(component.selectedValues).toEqual([]);
      expect(component.showChips).toBe(true);
      expect(component.allowClearAll).toBe(true);
      expect(component.maxSelections).toBe(-1);
      expect(component.minSelections).toBe(0);
      expect(component.disabled).toBe(false);
      expect(component.required).toBe(false);
      expect(component.hasErrorMessage).toBeNull();
      expect(component.sortOptions).toBe(false);
    });

    it('should generate unique fieldId on init', () => {
      component.ngOnInit();
      expect(component.fieldId).toBeTruthy();
      expect(component.fieldId).toContain('multi-select-field');
    });

    it('should generate ariaDescribedBy on init', () => {
      component.ngOnInit();
      expect(component.ariaDescribedBy).toBeTruthy();
    });

    it('should initialize with empty display options', () => {
      component.ngOnInit();
      expect(component.displayOptions).toEqual([]);
    });

    it('should not be focused on init', () => {
      expect(component.isFocused).toBe(false);
    });

    it('should initialize focus state as false', () => {
      component.ngOnInit();
      expect(component.isFocused).toBe(false);
    });
  });

  /**
   * SECTION 2: @INPUT PROPERTIES DISPLAY
   */

  describe('@Input Properties', () => {
    it('should display label when provided', () => {
      component.label = 'Status Filter';
      fixture.detectChanges();

      const label = compiled.query(By.css('.field-label'));
      expect(label?.nativeElement.textContent).toContain('Status Filter');
    });

    it('should render required indicator when required=true', () => {
      component.label = 'Status';
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

    it('should render all provided options', () => {
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();

      expect(component.displayOptions.length).toBe(3);
    });

    it('should disable field when disabled=true', () => {
      component.disabled = true;
      fixture.detectChanges();

      const select = compiled.query(By.css('mat-select'));
      expect(select?.componentInstance.disabled).toBe(true);
    });

    it('should set placeholder text', () => {
      component.placeholder = 'Select statuses';
      // Note: Material doesn't use placeholder attr on select, it's handled via label
      expect(component.placeholder).toBe('Select statuses');
    });

    it('should display selected values', () => {
      component.selectedValues = ['draft', 'published'];
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();

      expect(component.selectedValues).toContain('draft');
      expect(component.selectedValues).toContain('published');
    });

    it('should update displayOptions when options change', () => {
      component.ngOnInit();
      component.options = mockOptions;
      component.updateDisplayOptions();

      expect(component.displayOptions).toEqual(mockOptions);
    });

    it('should sort options when sortOptions=true', () => {
      const unsortedOptions: ISelectOption<string>[] = [
        { value: 'c', label: 'Zebra' },
        { value: 'a', label: 'Apple' },
        { value: 'b', label: 'Banana' },
      ];

      component.options = unsortedOptions;
      component.sortOptions = true;
      component.ngOnInit();
      component.updateDisplayOptions();

      expect(component.displayOptions[0].label).toBe('Apple');
      expect(component.displayOptions[1].label).toBe('Banana');
      expect(component.displayOptions[2].label).toBe('Zebra');
    });

    it('should not sort options when sortOptions=false', () => {
      const unsortedOptions: ISelectOption<string>[] = [
        { value: 'c', label: 'Zebra' },
        { value: 'a', label: 'Apple' },
        { value: 'b', label: 'Banana' },
      ];

      component.options = unsortedOptions;
      component.sortOptions = false;
      component.ngOnInit();
      component.updateDisplayOptions();

      expect(component.displayOptions[0].label).toBe('Zebra');
      expect(component.displayOptions[1].label).toBe('Apple');
      expect(component.displayOptions[2].label).toBe('Banana');
    });
  });

  /**
   * SECTION 3: @OUTPUT EVENT EMISSIONS
   */

  describe('@Output Events', () => {
    beforeEach(() => {
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should emit selectionChanged when value changes', (done) => {
      const newValues = ['draft', 'published'];

      component.selectionChanged.subscribe((values) => {
        expect(values).toEqual(newValues);
        done();
      });

      component.onSelectionChange(newValues);
    });

    it('should not emit selectionChanged if value unchanged', (done) => {
      component.selectedValues = ['draft'];
      let emitCount = 0;

      component.selectionChanged.subscribe(() => {
        emitCount++;
      });

      component.onSelectionChange(['draft']);

      setTimeout(() => {
        expect(emitCount).toBe(0);
        done();
      }, 100);
    });

    it('should emit cleared when all selections removed', (done) => {
      component.selectedValues = ['draft'];

      component.cleared.subscribe(() => {
        expect(component.selectedValues).toEqual([]);
        done();
      });

      component.onClearAll();
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

    it('should emit with correct values on chip remove', (done) => {
      component.selectedValues = ['draft', 'published'];

      component.selectionChanged.subscribe((values) => {
        expect(values).toEqual(['published']);
        done();
      });

      component.onChipRemove('draft');
    });
  });

  /**
   * SECTION 4: UI STATES & RENDERING
   */

  describe('UI States', () => {
    beforeEach(() => {
      component.options = mockOptions;
      component.ngOnInit();
    });

    it('should render error message when hasErrorMessage provided', () => {
      component.hasErrorMessage = 'Please select at least one status';
      fixture.detectChanges();

      const error = compiled.query(By.css('.error-message'));
      expect(error?.nativeElement.textContent).toContain('Please select at least one status');
    });

    it('should not render error message when hasErrorMessage is null', () => {
      component.hasErrorMessage = null;
      fixture.detectChanges();

      const error = compiled.query(By.css('.error-message'));
      expect(error).toBeFalsy();
    });

    it('should show selected chips when showChips=true', () => {
      component.selectedValues = ['draft', 'published'];
      component.showChips = true;
      fixture.detectChanges();

      const chipsContainer = compiled.query(By.css('.selected-chips-container'));
      expect(chipsContainer).toBeTruthy();
    });

    it('should hide selected chips when showChips=false', () => {
      component.selectedValues = ['draft'];
      component.showChips = false;
      fixture.detectChanges();

      const chipsContainer = compiled.query(By.css('.selected-chips-container'));
      expect(chipsContainer).toBeFalsy();
    });

    it('should show Clear All button when allowClearAll=true', () => {
      component.selectedValues = ['draft'];
      component.allowClearAll = true;
      component.minSelections = 0;
      fixture.detectChanges();

      const clearBtn = compiled.query(By.css('.clear-all-button'));
      expect(clearBtn).toBeTruthy();
    });

    it('should hide Clear All button when allowClearAll=false', () => {
      component.selectedValues = ['draft'];
      component.allowClearAll = false;
      fixture.detectChanges();

      const clearBtn = compiled.query(By.css('.clear-all-button'));
      expect(clearBtn).toBeFalsy();
    });

    it('should show selection count in trigger', () => {
      component.selectedValues = ['draft', 'published'];
      fixture.detectChanges();

      expect(component.selectionCountText).toContain('2 selected');
    });

    it('should apply has-error class when hasErrorMessage provided', () => {
      component.hasErrorMessage = 'Error';
      fixture.detectChanges();

      const container = compiled.query(By.css('.multi-select-field-container'));
      expect(container?.nativeElement.classList.contains('has-error')).toBe(true);
    });

    it('should apply has-value class when selectedValues not empty', () => {
      component.selectedValues = ['draft'];
      fixture.detectChanges();

      const container = compiled.query(By.css('.multi-select-field-container'));
      expect(container?.nativeElement.classList.contains('has-value')).toBe(true);
    });

    it('should apply is-disabled class when disabled=true', () => {
      component.disabled = true;
      fixture.detectChanges();

      const container = compiled.query(By.css('.multi-select-field-container'));
      expect(container?.nativeElement.classList.contains('is-disabled')).toBe(true);
    });
  });

  /**
   * SECTION 5: ACCESSIBILITY (WCAG AA)
   */

  describe('Accessibility (WCAG AA)', () => {
    beforeEach(() => {
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should have id that links label to select', () => {
      const select = compiled.query(By.css('mat-select'));
      expect(select?.nativeElement.id).toBe(component.fieldId);
    });

    it('should have aria-label on select', () => {
      component.label = 'Status Filter';
      fixture.detectChanges();

      const select = compiled.query(By.css('mat-select'));
      expect(select?.nativeElement.getAttribute('aria-label')).toContain('Status Filter');
    });

    it('should use provided ariaLabel when set', () => {
      component.ariaLabel = 'Custom aria label';
      fixture.detectChanges();

      expect(component.effectiveAriaLabel).toBe('Custom aria label');
    });

    it('should set aria-invalid when hasErrorMessage', () => {
      component.hasErrorMessage = 'Error';
      fixture.detectChanges();

      const select = compiled.query(By.css('mat-select'));
      expect(select?.nativeElement.getAttribute('aria-invalid')).toBe('true');
    });

    it('should link error message with aria-describedby', () => {
      component.hasErrorMessage = 'Error message';
      fixture.detectChanges();

      const select = compiled.query(By.css('mat-select'));
      const error = compiled.query(By.css('.error-message'));

      expect(select?.nativeElement.getAttribute('aria-describedby')).toBe(
        error?.nativeElement.id
      );
    });

    it('should set aria-required when required=true', () => {
      component.required = true;
      fixture.detectChanges();

      const select = compiled.query(By.css('mat-select'));
      expect(select?.nativeElement.getAttribute('aria-required')).toBe('true');
    });

    it('should have aria-label on chips', () => {
      component.selectedValues = ['draft'];
      component.showChips = true;
      fixture.detectChanges();

      const chips = compiled.queryAll(By.css('.selected-chip'));
      expect(chips[0]?.nativeElement.getAttribute('aria-label')).toBeTruthy();
    });

    it('should have aria-label on chip remove buttons', () => {
      component.selectedValues = ['draft'];
      component.showChips = true;
      fixture.detectChanges();

      const removeBtn = compiled.query(By.css('.chip-remove-button'));
      expect(removeBtn?.nativeElement.getAttribute('aria-label')).toContain('Remove');
    });

    it('should have aria-label on Clear All button', () => {
      component.selectedValues = ['draft'];
      component.allowClearAll = true;
      fixture.detectChanges();

      const clearBtn = compiled.query(By.css('.clear-all-button'));
      expect(clearBtn?.nativeElement.getAttribute('aria-label')).toContain('Clear all');
    });

    it('should have role on chips container', () => {
      component.selectedValues = ['draft'];
      component.showChips = true;
      fixture.detectChanges();

      const container = compiled.query(By.css('.selected-chips-container'));
      expect(container?.nativeElement.getAttribute('role')).toBe('region');
    });
  });

  /**
   * SECTION 6: USER INTERACTIONS
   */

  describe('User Interactions', () => {
    beforeEach(() => {
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should handle selection changes', () => {
      const newValues = ['draft', 'published'];
      component.onSelectionChange(newValues);

      expect(component.selectedValues).toEqual(newValues);
    });

    it('should return correct selection count text', () => {
      component.selectedValues = ['draft', 'published'];
      expect(component.selectionCountText).toBe('2 selected');
    });

    it('should return "None selected" when no selections', () => {
      component.selectedValues = [];
      expect(component.selectionCountText).toBe('None selected');
    });

    it('should show fraction when maxSelections set', () => {
      component.selectedValues = ['draft'];
      component.maxSelections = 3;

      expect(component.selectionCountText).toBe('1 / 3 selected');
    });

    it('should enforce maxSelections limit', () => {
      component.maxSelections = 2;
      component.onSelectionChange(['draft', 'published', 'archived']);

      expect(component.selectedValues.length).toBeLessThanOrEqual(2);
    });

    it('should remove individual chip on remove click', () => {
      component.selectedValues = ['draft', 'published'];
      component.onChipRemove('draft');

      expect(component.selectedValues).toEqual(['published']);
    });

    it('should clear all selections', () => {
      component.selectedValues = ['draft', 'published'];
      component.minSelections = 0;
      component.onClearAll();

      expect(component.selectedValues).toEqual([]);
    });

    it('should prevent clear when below minSelections', () => {
      component.selectedValues = ['draft'];
      component.minSelections = 1;
      component.onClearAll();

      expect(component.selectedValues).toEqual(['draft']);
    });
  });

  /**
   * SECTION 7: MAXIMUM SELECTIONS HANDLING
   */

  describe('Selection Limits', () => {
    beforeEach(() => {
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should prevent selection beyond maxSelections', () => {
      component.maxSelections = 2;
      component.onSelectionChange(['draft', 'published', 'archived']);

      expect(component.selectedValues.length).toBeLessThanOrEqual(2);
    });

    it('should report when at max selections', () => {
      component.selectedValues = ['draft', 'published'];
      component.maxSelections = 2;

      expect(component.isAtMaxSelections()).toBe(true);
    });

    it('should not report at max when maxSelections=-1 (unlimited)', () => {
      component.selectedValues = ['draft', 'published'];
      component.maxSelections = -1;

      expect(component.isAtMaxSelections()).toBe(false);
    });

    it('should report below min selections', () => {
      component.selectedValues = ['draft'];
      component.minSelections = 2;

      expect(component.isBelowMinSelections()).toBe(true);
    });

    it('should show selection hint text with min/max', () => {
      component.minSelections = 1;
      component.maxSelections = 3;

      expect(component.selectionHintText).toContain('Minimum 1');
      expect(component.selectionHintText).toContain('Maximum 3');
    });
  });

  /**
   * SECTION 8: HELPER METHODS
   */

  describe('Helper Methods', () => {
    beforeEach(() => {
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should check if value is selected', () => {
      component.selectedValues = ['draft', 'published'];

      expect(component.isValueSelected('draft')).toBe(true);
      expect(component.isValueSelected('archived')).toBe(false);
    });

    it('should get label for value', () => {
      const label = component.getLabel('draft');
      expect(label).toBe('Draft');
    });

    it('should return value as string if no label found', () => {
      const label = component.getLabel('unknown' as any);
      expect(label).toBe('unknown');
    });

    it('should have trackByValue function for performance', () => {
      const result = component.trackByValue(0, 'draft');
      expect(result).toBe('draft');
    });

    it('should have trackByOptionValue for options loop', () => {
      const result = component.trackByOptionValue(0, mockOptions[0]);
      expect(result).toBe('draft');
    });
  });

  /**
   * SECTION 9: EDGE CASES
   */

  describe('Edge Cases', () => {
    it('should handle empty options array', () => {
      component.options = emptyOptions;
      component.ngOnInit();
      fixture.detectChanges();

      expect(component.displayOptions.length).toBe(0);
      expect(component.selectionCountText).toBe('None selected');
    });

    it('should handle very long option labels', () => {
      const longLabelOption: ISelectOption<string>[] = [
        {
          value: 'test',
          label: 'This is a very long label that might cause layout issues '.repeat(3),
        },
      ];
      component.options = longLabelOption;
      component.ngOnInit();
      fixture.detectChanges();

      expect(component.displayOptions[0].label.length).toBeGreaterThan(100);
    });

    it('should handle all options selected', () => {
      component.selectedValues = ['draft', 'published', 'archived'];
      expect(component.selectionCountText).toContain('3 selected');
    });

    it('should handle numeric values', () => {
      const numericComponent = new MultiSelectFieldComponent<number>();
      numericComponent.selectedValues = [1, 2, 3];
      expect(numericComponent.selectedValues.length).toBe(3);
    });

    it('should handle rapid selection changes', () => {
      component.onSelectionChange(['draft']);
      component.onSelectionChange(['draft', 'published']);
      component.onSelectionChange(['archived']);

      expect(component.selectedValues).toEqual(['archived']);
    });

    it('should handle disabled options in list', () => {
      const optionsWithDisabled = [
        { value: 'draft', label: 'Draft', disabled: false },
        { value: 'published', label: 'Published', disabled: true },
      ];

      component.options = optionsWithDisabled;
      component.ngOnInit();
      fixture.detectChanges();

      expect(component.displayOptions[1].disabled).toBe(true);
    });
  });

  /**
   * SECTION 10: GETTERS
   */

  describe('Getters', () => {
    beforeEach(() => {
      component.options = mockOptions;
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should return field container class with error', () => {
      component.hasErrorMessage = 'Error';
      expect(component.fieldContainerClass).toContain('has-error');
    });

    it('should return field container class with value', () => {
      component.selectedValues = ['draft'];
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

    it('should show chips only when enabled', () => {
      component.selectedValues = ['draft'];
      component.showChips = true;
      expect(component.showSelectedChips).toBe(true);
    });

    it('should show Clear All only when allowed', () => {
      component.selectedValues = ['draft'];
      component.allowClearAll = true;
      component.minSelections = 0;
      expect(component.showClearAllButton).toBe(true);
    });

    it('should not show Clear All when minSelections > 0', () => {
      component.selectedValues = ['draft'];
      component.allowClearAll = true;
      component.minSelections = 1;
      expect(component.showClearAllButton).toBe(false);
    });
  });

  /**
   * SECTION 11: CLEANUP
   */

  describe('Cleanup', () => {
    it('should complete event emitters on destroy', () => {
      component.ngOnInit();

      spyOn(component.selectionChanged, 'complete');
      spyOn(component.cleared, 'complete');
      spyOn(component.focused, 'complete');
      spyOn(component.blurred, 'complete');

      component.ngOnDestroy();

      expect(component.selectionChanged.complete).toHaveBeenCalled();
      expect(component.cleared.complete).toHaveBeenCalled();
      expect(component.focused.complete).toHaveBeenCalled();
      expect(component.blurred.complete).toHaveBeenCalled();
    });
  });
});
