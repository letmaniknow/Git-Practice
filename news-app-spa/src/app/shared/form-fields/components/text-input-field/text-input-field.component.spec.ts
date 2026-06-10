/**
 * TextInputFieldComponent - Unit Tests
 *
 * Tests for TextInputFieldComponent - a reusable, presentation-only text input
 * 
 * TEST STRATEGY:
 * ✅ Input/Output contracts - verify @Input/@Output work correctly
 * ✅ Rendering - verify template renders with correct values
 * ✅ User interactions - click, type, keyboard events
 * ✅ Accessibility - aria attributes, labels, WCAG AA compliance
 * ✅ Edge cases - empty values, disabled state, errors
 *
 * @module TextInputFieldComponent.spec
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { TextInputFieldComponent } from './text-input-field.component';

describe('TextInputFieldComponent', () => {
  let component: TextInputFieldComponent;
  let fixture: ComponentFixture<TextInputFieldComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TextInputFieldComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TextInputFieldComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  afterEach(() => {
    fixture.destroy();
  });

  /**
   * BASIC SETUP TESTS
   * Verify component initializes correctly
   */
  describe('Component Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.value).toBe('');
      expect(component.type).toBe('text');
      expect(component.disabled).toBe(false);
      expect(component.required).toBe(false);
      expect(component.hasErrorMessage).toBeNull();
    });

    it('should generate fieldId on init', () => {
      fixture.detectChanges();
      expect(component.fieldId).toBeTruthy();
      expect(component.fieldId).toContain('text-input');
    });

    it('should generate ariaDescribedBy on init', () => {
      fixture.detectChanges();
      expect(component.ariaDescribedBy).toContain(component.fieldId);
    });
  });

  /**
   * INPUT PROPERTIES TESTS
   * Verify @Input properties work correctly
   */
  describe('@Input Properties', () => {
    beforeEach(() => {
      component.label = 'Search articles';
      component.placeholder = 'Enter search term';
      component.value = 'test search';
      component.type = 'search';
      component.icon = 'search';
      fixture.detectChanges();
    });

    it('should display label', () => {
      const label = debugElement.query(By.css('mat-label'));
      expect(label.nativeElement.textContent).toContain('Search articles');
    });

    it('should display placeholder', () => {
      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.placeholder).toBe('Enter search term');
    });

    it('should set input value', () => {
      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.value).toBe('test search');
    });

    it('should set input type', () => {
      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.type).toBe('search');
    });

    it('should show icon when input is empty', () => {
      component.value = '';
      fixture.detectChanges();
      const icon = debugElement.query(By.css('mat-icon[matPrefix]'));
      expect(icon).toBeTruthy();
      expect(icon.nativeElement.textContent).toContain('search');
    });

    it('should hide icon when input has value', () => {
      component.value = 'something';
      fixture.detectChanges();
      const icon = debugElement.query(By.css('mat-icon[matPrefix]'));
      expect(icon).toBeFalsy();
    });
  });

  /**
   * OUTPUT EVENTS TESTS
   * Verify @Output events are emitted correctly
   */
  describe('@Output Events', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should emit valueChanged when input value changes', (done) => {
      spyOn(component.valueChanged, 'emit');
      const input = debugElement.query(By.css('input[matInput]'));

      input.nativeElement.value = 'new value';
      input.nativeElement.dispatchEvent(new Event('input'));

      expect(component.valueChanged.emit).toHaveBeenCalledWith('new value');
      done();
    });

    it('should emit cleared when clear button clicked', (done) => {
      component.value = 'test';
      component.showClearButton = true;
      fixture.detectChanges();

      spyOn(component.cleared, 'emit');
      spyOn(component.valueChanged, 'emit');

      const clearButton = debugElement.query(By.css('.clear-button'));
      clearButton.nativeElement.click();

      expect(component.cleared.emit).toHaveBeenCalled();
      expect(component.valueChanged.emit).toHaveBeenCalledWith('');
      expect(component.value).toBe('');
      done();
    });

    it('should emit focused when field receives focus', (done) => {
      spyOn(component.focused, 'emit');
      const input = debugElement.query(By.css('input[matInput]'));

      input.nativeElement.dispatchEvent(new Event('focus'));

      expect(component.focused.emit).toHaveBeenCalled();
      expect(component.isFocused).toBe(true);
      done();
    });

    it('should emit blurred when field loses focus', (done) => {
      component.isFocused = true;
      fixture.detectChanges();

      spyOn(component.blurred, 'emit');
      const input = debugElement.query(By.css('input[matInput]'));

      input.nativeElement.dispatchEvent(new Event('blur'));

      expect(component.blurred.emit).toHaveBeenCalled();
      expect(component.isFocused).toBe(false);
      done();
    });

    it('should emit enterPressed when Enter key pressed', (done) => {
      spyOn(component.enterPressed, 'emit');
      component.value = 'search term';
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      const event = new KeyboardEvent('keydown', { key: 'Enter' });
      input.nativeElement.dispatchEvent(event);

      expect(component.enterPressed.emit).toHaveBeenCalledWith('search term');
      done();
    });

    it('should call clear on Escape key pressed', (done) => {
      component.value = 'test';
      component.showClearButton = true;
      fixture.detectChanges();

      spyOn(component, 'onClear');
      const input = debugElement.query(By.css('input[matInput]'));
      const event = new KeyboardEvent('keydown', { key: 'Escape' });

      input.nativeElement.dispatchEvent(event);

      expect(component.onClear).toHaveBeenCalled();
      done();
    });
  });

  /**
   * UI STATE TESTS
   * Verify component renders correctly based on state
   */
  describe('UI States', () => {
    it('should show clear button when value exists and showClearButton is true', () => {
      component.value = 'test';
      component.showClearButton = true;
      fixture.detectChanges();

      const clearButton = debugElement.query(By.css('.clear-button'));
      expect(clearButton).toBeTruthy();
    });

    it('should hide clear button when value is empty', () => {
      component.value = '';
      component.showClearButton = true;
      fixture.detectChanges();

      const clearButton = debugElement.query(By.css('.clear-button'));
      expect(clearButton).toBeFalsy();
    });

    it('should hide clear button when disabled', () => {
      component.value = 'test';
      component.showClearButton = true;
      component.disabled = true;
      fixture.detectChanges();

      const clearButton = debugElement.query(By.css('.clear-button'));
      expect(clearButton).toBeFalsy();
    });

    it('should disable input when disabled prop is true', () => {
      component.disabled = true;
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.disabled).toBe(true);
    });

    it('should show required indicator when required is true', () => {
      component.required = true;
      fixture.detectChanges();

      const indicator = debugElement.query(By.css('.required-indicator'));
      expect(indicator).toBeTruthy();
      expect(indicator.nativeElement.textContent).toContain('*');
    });

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
   * ACCESSIBILITY TESTS
   * Verify WCAG AA compliance
   */
  describe('Accessibility (WCAG AA)', () => {
    beforeEach(() => {
      component.label = 'Email address';
      component.type = 'email';
      component.required = true;
      fixture.detectChanges();
    });

    it('should have aria-label on input', () => {
      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.getAttribute('aria-label')).toBeTruthy();
    });

    it('should associate label with input via matLabel', () => {
      const label = debugElement.query(By.css('mat-label'));
      expect(label).toBeTruthy();
    });

    it('should set aria-invalid when error exists', () => {
      component.hasErrorMessage = 'Invalid email format';
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.getAttribute('aria-invalid')).toBe('true');
    });

    it('should set aria-invalid to false when no error', () => {
      component.hasErrorMessage = null;
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.getAttribute('aria-invalid')).toBe('false');
    });

    it('should link error message via aria-describedby', () => {
      component.hasErrorMessage = 'Error message';
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      const ariaDescribedBy = input.nativeElement.getAttribute('aria-describedby');
      expect(ariaDescribedBy).toBeTruthy();

      const error = debugElement.query(By.css('mat-error'));
      expect(error.nativeElement.id).toBe(ariaDescribedBy);
    });

    it('should have proper button accessibility for clear button', () => {
      component.value = 'test';
      component.showClearButton = true;
      fixture.detectChanges();

      const clearButton = debugElement.query(By.css('.clear-button'));
      expect(clearButton.nativeElement.getAttribute('aria-label')).toBeTruthy();
      expect(clearButton.nativeElement.getAttribute('type')).toBe('button');
    });

    it('should hide icon prefix from screen readers', () => {
      component.value = ''; // Icon shown when empty
      fixture.detectChanges();

      const icon = debugElement.query(By.css('mat-icon[matPrefix]'));
      expect(icon.nativeElement.getAttribute('aria-hidden')).toBe('true');
    });
  });

  /**
   * VALIDATION TESTS
   * Verify input validation works
   */
  describe('Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should set maxlength attribute', () => {
      component.maxLength = 50;
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.getAttribute('maxlength')).toBe('50');
    });

    it('should set minlength attribute', () => {
      component.minLength = 3;
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.getAttribute('minlength')).toBe('3');
    });

    it('should set pattern attribute', () => {
      component.pattern = '^[a-zA-Z]+$';
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.getAttribute('pattern')).toBe('^[a-zA-Z]+$');
    });

    it('should show character count hint', () => {
      component.maxLength = 100;
      component.value = 'test';
      fixture.detectChanges();

      const hint = debugElement.query(By.css('mat-hint'));
      expect(hint.nativeElement.textContent).toContain('4 / 100');
    });
  });

  /**
   * EDGE CASES TEST
   * Test unusual but valid scenarios
   */
  describe('Edge Cases', () => {
    it('should handle very long input values', () => {
      const longValue = 'a'.repeat(1000);
      component.value = longValue;
      component.maxLength = 1000;
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.value).toBe(longValue);
    });

    it('should handle special characters in value', () => {
      component.value = 'test@#$%^&*()';
      fixture.detectChanges();

      const input = debugElement.query(By.css('input[matInput]'));
      expect(input.nativeElement.value).toBe('test@#$%^&*()');
    });

    it('should handle rapid focus/blur cycles', (done) => {
      const input = debugElement.query(By.css('input[matInput]'));
      let focusCount = 0;
      let blurCount = 0;

      component.focused.subscribe(() => focusCount++);
      component.blurred.subscribe(() => blurCount++);

      input.nativeElement.dispatchEvent(new Event('focus'));
      input.nativeElement.dispatchEvent(new Event('blur'));
      input.nativeElement.dispatchEvent(new Event('focus'));
      input.nativeElement.dispatchEvent(new Event('blur'));

      fixture.detectChanges();
      expect(focusCount).toBe(2);
      expect(blurCount).toBe(2);
      done();
    });

    it('should handle clear button spam clicks', () => {
      component.value = 'test';
      component.showClearButton = true;
      fixture.detectChanges();

      const clearButton = debugElement.query(By.css('.clear-button'));

      // Multiple rapid clicks
      for (let i = 0; i < 5; i++) {
        clearButton.nativeElement.click();
      }

      // Value should still be empty (idempotent)
      expect(component.value).toBe('');
    });
  });

  /**
   * GETTER TESTS
   * Verify computed properties
   */
  describe('Getters', () => {
    it('shouldShowIcon should be true when icon exists and value is empty', () => {
      component.icon = 'search';
      component.value = '';
      expect(component.shouldShowIcon).toBe(true);
    });

    it('shouldShowIcon should be false when value is not empty', () => {
      component.icon = 'search';
      component.value = 'test';
      expect(component.shouldShowIcon).toBe(false);
    });

    it('shouldShowClearButton should be true when conditions met', () => {
      component.showClearButton = true;
      component.value = 'test';
      component.disabled = false;
      expect(component.shouldShowClearButton).toBe(true);
    });

    it('shouldShowClearButton should be false when disabled', () => {
      component.showClearButton = true;
      component.value = 'test';
      component.disabled = true;
      expect(component.shouldShowClearButton).toBe(false);
    });

    it('charCount should return correct values', () => {
      component.maxLength = 100;
      component.value = 'test';
      expect(component.charCount).toEqual({ current: 4, max: 100 });
    });

    it('isCharLimitReached should be true when at limit', () => {
      component.maxLength = 10;
      component.value = '0123456789';
      expect(component.isCharLimitReached).toBe(true);
    });

    it('isCharLimitReached should be false when below limit', () => {
      component.maxLength = 10;
      component.value = 'test';
      expect(component.isCharLimitReached).toBe(false);
    });
  });
});
