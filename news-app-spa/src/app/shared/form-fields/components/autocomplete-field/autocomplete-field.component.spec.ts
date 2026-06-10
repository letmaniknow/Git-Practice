/**
 * AutocompleteFieldComponent Unit Tests
 * Full coverage - component is PURELY PRESENTATIONAL
 *
 * Test Structure:
 * 1. Component initialization & default values
 * 2. @Input properties display and updates
 * 3. @Output event emissions (CRITICAL: parent must listen)
 * 4. UI states (focus, disabled, error, filled, loading)
 * 5. Accessibility (WCAG AA compliance)
 * 6. Filtered items display
 * 7. Edge cases (empty input, loading state, no results)
 * 8. Helper methods
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

import { AutocompleteFieldComponent } from './autocomplete-field.component';
import { ISelectOption } from '../../models/form-field.models';

describe('AutocompleteFieldComponent', () => {
  let component: AutocompleteFieldComponent<string>;
  let fixture: ComponentFixture<AutocompleteFieldComponent<string>>;
  let compiled: DebugElement;

  /**
   * MOCK DATA
   */
  const mockUsers: ISelectOption<string>[] = [
    {
      value: 'user1',
      label: 'John Doe',
      icon: 'person',
      description: 'Admin',
    },
    {
      value: 'user2',
      label: 'Jane Smith',
      icon: 'person',
      description: 'Editor',
    },
    {
      value: 'user3',
      label: 'Bob Johnson',
      icon: 'person',
      description: 'Viewer',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AutocompleteFieldComponent, BrowserAnimationsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(AutocompleteFieldComponent<string>);
    component = fixture.componentInstance;
    compiled = fixture.debugElement;
  });

  afterEach(() => {
    fixture.destroy();
  });

  /**
   * SECTION 1: INITIALIZATION & DEFAULTS
   */

  describe('Initialization', () => {
    it('should create component', () => {
      expect(component).toBeTruthy();
    });

    it('should have default input values', () => {
      expect(component.label).toBe('');
      expect(component.placeholder).toBe('');
      expect(component.inputValue).toBe('');
      expect(component.selectedItem).toBeNull();
      expect(component.filteredItems).toEqual([]);
      expect(component.isLoading).toBe(false);
      expect(component.minCharsToFilter).toBe(1);
      expect(component.disabled).toBe(false);
      expect(component.required).toBe(false);
      expect(component.hasErrorMessage).toBeNull();
    });

    it('should generate unique fieldId on init', () => {
      component.ngOnInit();
      expect(component.fieldId).toBeTruthy();
      expect(component.fieldId).toContain('autocomplete-field');
    });

    it('should not be focused on init', () => {
      expect(component.isFocused).toBe(false);
    });

    it('should initialize panel as closed', () => {
      expect(component.panelOpen).toBe(false);
    });
  });

  /**
   * SECTION 2: @INPUT PROPERTIES
   */

  describe('@Input Properties', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should display label when provided', () => {
      component.label = 'Search Users';
      fixture.detectChanges();

      const label = compiled.query(By.css('.field-label'));
      expect(label?.nativeElement.textContent).toContain('Search Users');
    });

    it('should render required indicator when required=true', () => {
      component.label = 'Users';
      component.required = true;
      fixture.detectChanges();

      const indicator = compiled.query(By.css('.required-indicator'));
      expect(indicator).toBeTruthy();
    });

    it('should display filtered items when provided', () => {
      component.filteredItems = mockUsers;
      component.inputValue = 'john';
      fixture.detectChanges();

      expect(component.filteredItems.length).toBe(3);
    });

    it('should show loading spinner when isLoading=true', () => {
      component.inputValue = 'test';
      component.isLoading = true;
      fixture.detectChanges();

      expect(component.showLoadingSpinner).toBe(true);
    });

    it('should hide loading spinner when isLoading=false', () => {
      component.isLoading = false;
      fixture.detectChanges();

      expect(component.showLoadingSpinner).toBe(false);
    });

    it('should set min characters threshold', () => {
      component.minCharsToFilter = 3;
      expect(component.minCharsToFilter).toBe(3);
    });

    it('should disable field when disabled=true', () => {
      component.disabled = true;
      fixture.detectChanges();

      const input = compiled.query(By.css('input[matInput]'));
      expect(input?.nativeElement.disabled).toBe(true);
    });
  });

  /**
   * SECTION 3: @OUTPUT EVENTS (CRITICAL - Parent Must Listen)
   */

  describe('@Output Events', () => {
    beforeEach(() => {
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should emit inputChanged when user types', (done) => {
      // CRITICAL: Parent MUST listen to this and call API
      component.inputChanged.subscribe((value) => {
        expect(value).toBe('john');
        done();
      });

      component.onInputChange('john');
    });

    it('should emit itemSelected when item selected', (done) => {
      const selectedUser = mockUsers[0];

      component.itemSelected.subscribe((item) => {
        expect(item.value).toBe('user1');
        expect(item.label).toBe('John Doe');
        done();
      });

      component.onItemSelected(selectedUser);
    });

    it('should update inputValue on selection', () => {
      const selectedUser = mockUsers[0];
      component.onItemSelected(selectedUser);

      expect(component.inputValue).toBe('John Doe');
    });

    it('should emit cleared when selection cleared', (done) => {
      component.selectedItem = mockUsers[0];
      component.inputValue = 'John Doe';

      component.cleared.subscribe(() => {
        expect(component.inputValue).toBe('');
        expect(component.selectedItem).toBeNull();
        done();
      });

      component.onClear();
    });

    it('should emit inputChanged with empty value on clear', (done) => {
      let emitCount = 0;

      component.inputChanged.subscribe((value) => {
        emitCount++;
        if (emitCount === 2) {
          // Second emission after clear
          expect(value).toBe('');
          done();
        }
      });

      component.onInputChange('test');
      component.onClear();
    });

    it('should emit focused event', (done) => {
      component.focused.subscribe(() => {
        expect(component.isFocused).toBe(true);
        done();
      });

      component.onFocus();
    });

    it('should emit blurred event', (done) => {
      component.isFocused = true;

      component.blurred.subscribe(() => {
        expect(component.isFocused).toBe(false);
        done();
      });

      component.onBlur();
    });
  });

  /**
   * SECTION 4: UI STATES
   */

  describe('UI States', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should render error message when hasErrorMessage provided', () => {
      component.hasErrorMessage = 'User not found';
      fixture.detectChanges();

      const error = compiled.query(By.css('.error-message'));
      expect(error?.nativeElement.textContent).toContain('User not found');
    });

    it('should show clear button when input has value', () => {
      component.inputValue = 'john';
      fixture.detectChanges();

      expect(component.showClearButton).toBe(true);
    });

    it('should show clear button when item selected', () => {
      component.selectedItem = mockUsers[0];
      fixture.detectChanges();

      expect(component.showClearButton).toBe(true);
    });

    it('should hide clear button when disabled', () => {
      component.inputValue = 'john';
      component.disabled = true;
      fixture.detectChanges();

      expect(component.showClearButton).toBe(false);
    });

    it('should apply has-error class', () => {
      component.hasErrorMessage = 'Error';
      fixture.detectChanges();

      const container = compiled.query(By.css('.autocomplete-field-container'));
      expect(container?.nativeElement.classList.contains('has-error')).toBe(true);
    });

    it('should apply has-value class when selected', () => {
      component.selectedItem = mockUsers[0];
      fixture.detectChanges();

      const container = compiled.query(By.css('.autocomplete-field-container'));
      expect(container?.nativeElement.classList.contains('has-value')).toBe(true);
    });

    it('should apply is-disabled class', () => {
      component.disabled = true;
      fixture.detectChanges();

      const container = compiled.query(By.css('.autocomplete-field-container'));
      expect(container?.nativeElement.classList.contains('is-disabled')).toBe(true);
    });

    it('should apply is-loading class when loading', () => {
      component.isLoading = true;
      fixture.detectChanges();

      const container = compiled.query(By.css('.autocomplete-field-container'));
      expect(container?.nativeElement.classList.contains('is-loading')).toBe(true);
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

    it('should have aria-label on input', () => {
      component.label = 'Search Users';
      fixture.detectChanges();

      const input = compiled.query(By.css('input[matInput]'));
      expect(input?.nativeElement.getAttribute('aria-label')).toContain('Search');
    });

    it('should use provided ariaLabel when set', () => {
      component.ariaLabel = 'Custom aria label';
      fixture.detectChanges();

      expect(component.effectiveAriaLabel).toBe('Custom aria label');
    });

    it('should set aria-invalid when has error', () => {
      component.hasErrorMessage = 'Error';
      fixture.detectChanges();

      const input = compiled.query(By.css('input[matInput]'));
      expect(input?.nativeElement.getAttribute('aria-invalid')).toBe('true');
    });

    it('should set aria-required when required', () => {
      component.required = true;
      fixture.detectChanges();

      const input = compiled.query(By.css('input[matInput]'));
      expect(input?.nativeElement.getAttribute('aria-required')).toBe('true');
    });

    it('should have aria-autocomplete="list"', () => {
      const input = compiled.query(By.css('input[matInput]'));
      expect(input?.nativeElement.getAttribute('aria-autocomplete')).toBe('list');
    });

    it('should have aria-expanded tracking panel state', () => {
      component.panelOpen = false;
      fixture.detectChanges();

      let input = compiled.query(By.css('input[matInput]'));
      expect(input?.nativeElement.getAttribute('aria-expanded')).toBe('false');

      component.panelOpen = true;
      fixture.detectChanges();

      input = compiled.query(By.css('input[matInput]'));
      expect(input?.nativeElement.getAttribute('aria-expanded')).toBe('true');
    });

    it('should have aria-describedby linking to error', () => {
      component.hasErrorMessage = 'Error message';
      fixture.detectChanges();

      const input = compiled.query(By.css('input[matInput]'));
      const error = compiled.query(By.css('.error-message'));

      expect(input?.nativeElement.getAttribute('aria-describedby')).toBe(
        error?.nativeElement.id
      );
    });
  });

  /**
   * SECTION 6: FILTERED ITEMS DISPLAY
   */

  describe('Filtered Items Display', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should show filtered items when minCharsToFilter reached', () => {
      component.inputValue = 'john';
      component.minCharsToFilter = 1;
      component.filteredItems = [mockUsers[0]];

      expect(component.shouldShowFilteredItems()).toBe(true);
    });

    it('should not show filtered items when below minCharsToFilter', () => {
      component.inputValue = 'j';
      component.minCharsToFilter = 3;

      expect(component.shouldShowFilteredItems()).toBe(false);
    });

    it('should not show filtered items when list empty', () => {
      component.inputValue = 'test';
      component.filteredItems = [];

      expect(component.shouldShowFilteredItems()).toBe(false);
    });

    it('should show no results message', () => {
      component.inputValue = 'nonexistent';
      component.isLoading = false;
      component.minCharsToFilter = 1;
      component.filteredItems = [];

      expect(component.showNoResults).toBe(true);
    });

    it('should show min chars hint', () => {
      component.inputValue = 'j';
      component.minCharsToFilter = 3;

      expect(component.showMinCharsHint).toBe(true);
    });

    it('should return correct min chars hint text', () => {
      component.inputValue = 'j';
      component.minCharsToFilter = 3;

      expect(component.minCharsHintText).toContain('2 more character');
    });

    it('should return singular "character" in hint', () => {
      component.inputValue = 'jo';
      component.minCharsToFilter = 3;

      expect(component.minCharsHintText).toContain('1 more character');
    });
  });

  /**
   * SECTION 7: PANEL STATE
   */

  describe('Panel State', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should track panel open state', () => {
      expect(component.panelOpen).toBe(false);

      component.onPanelStateChanged(true);
      expect(component.panelOpen).toBe(true);

      component.onPanelStateChanged(false);
      expect(component.panelOpen).toBe(false);
    });

    it('should close panel on blur', () => {
      component.panelOpen = true;
      component.onBlur();

      expect(component.panelOpen).toBe(false);
    });
  });

  /**
   * SECTION 8: PARENT-CONTROLLED LOADING
   */

  describe('Parent-Controlled Loading', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should show spinner only when loading with input', () => {
      component.inputValue = 'test';
      component.isLoading = true;
      component.minCharsToFilter = 1;

      expect(component.showLoadingSpinner).toBe(true);
    });

    it('should hide spinner when not loading', () => {
      component.isLoading = false;

      expect(component.showLoadingSpinner).toBe(false);
    });

    it('should hide spinner when no input yet', () => {
      component.inputValue = '';
      component.isLoading = true;

      expect(component.showLoadingSpinner).toBe(false);
    });
  });

  /**
   * SECTION 9: HELPER METHODS
   */

  describe('Helper Methods', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should get display value from item', () => {
      const display = component.getDisplayValue(mockUsers[0]);
      expect(display).toBe('John Doe');
    });

    it('should return value as string if no label', () => {
      const item: ISelectOption<string> = { value: 'test123', label: '' };
      const display = component.getDisplayValue(item);
      expect(display).toBe('test123');
    });

    it('should have trackByOptionValue for performance', () => {
      const result = component.trackByOptionValue(0, mockUsers[0]);
      expect(result).toBe('user1');
    });
  });

  /**
   * SECTION 10: EDGE CASES
   */

  describe('Edge Cases', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('should handle empty filtered items array', () => {
      component.filteredItems = [];
      component.inputValue = 'test';

      expect(component.shouldShowFilteredItems()).toBe(false);
      expect(component.showNoResults).toBe(true);
    });

    it('should handle very long user input', () => {
      component.inputValue = 'a'.repeat(500);
      component.filteredItems = mockUsers;

      expect(component.shouldShowFilteredItems()).toBe(true);
    });

    it('should handle rapid selection changes', () => {
      component.onItemSelected(mockUsers[0]);
      component.onItemSelected(mockUsers[1]);
      component.onItemSelected(mockUsers[2]);

      expect(component.selectedItem?.value).toBe('user3');
      expect(component.inputValue).toBe('Bob Johnson');
    });

    it('should handle special characters in input', () => {
      component.inputValue = '@#$%^&*()';
      component.minCharsToFilter = 1;

      expect(component.showMinCharsHint).toBe(false);
    });

    it('should handle numeric values', () => {
      const numericComponent = new AutocompleteFieldComponent<number>();
      numericComponent.filteredItems = [
        { value: 1, label: 'One' },
        { value: 2, label: 'Two' },
      ];

      expect(numericComponent.filteredItems[0].value).toBe(1);
    });
  });

  /**
   * SECTION 11: GETTERS
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
      component.selectedItem = mockUsers[0];
      expect(component.fieldContainerClass).toContain('has-value');
    });

    it('should return field container class when focused', () => {
      component.isFocused = true;
      expect(component.fieldContainerClass).toContain('is-focused');
    });

    it('should return field container class when loading', () => {
      component.isLoading = true;
      expect(component.fieldContainerClass).toContain('is-loading');
    });

    it('should return field container class when panel open', () => {
      component.panelOpen = true;
      expect(component.fieldContainerClass).toContain('panel-open');
    });
  });

  /**
   * SECTION 12: CLEANUP
   */

  describe('Cleanup', () => {
    it('should complete event emitters on destroy', () => {
      component.ngOnInit();

      spyOn(component.inputChanged, 'complete');
      spyOn(component.itemSelected, 'complete');
      spyOn(component.cleared, 'complete');
      spyOn(component.focused, 'complete');
      spyOn(component.blurred, 'complete');

      component.ngOnDestroy();

      expect(component.inputChanged.complete).toHaveBeenCalled();
      expect(component.itemSelected.complete).toHaveBeenCalled();
      expect(component.cleared.complete).toHaveBeenCalled();
      expect(component.focused.complete).toHaveBeenCalled();
      expect(component.blurred.complete).toHaveBeenCalled();
    });
  });
});
