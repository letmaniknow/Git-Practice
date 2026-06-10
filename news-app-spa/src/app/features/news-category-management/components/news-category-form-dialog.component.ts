/**
 * News Category Form Dialog Component - Create/Edit Form
 * Purpose: Dialog for creating and editing news categories
 * Features: Form validation, bilingual support (English/Spanish), auto-generated slug
 * Pattern: Follows admin-user-form-dialog.component.ts structure
 */

import { Component, OnInit, Inject, Optional, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AsyncValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Observable } from 'rxjs';
import { map, debounceTime } from 'rxjs/operators';

import { NewsCategoryViewModel, DialogResult } from '../models/news-category.model';
import { NEWS_CATEGORY_FORM_VALIDATION, NEWS_CATEGORY_OPERATION_MESSAGES } from '../constants/news-category-api.constant';
import { NewsCategoryService } from '../services/news-category.service';

/**
 * NewsCategoryFormDialogComponent - Form dialog for CRUD operations
 * Used for: Create new category, Edit existing category
 * Injected data: category (optional - if provided, opens in edit mode)
 */
@Component({
  selector: 'app-news-category-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
  ],
  templateUrl: './news-category-form-dialog.component.html',
  styleUrls: ['./news-category-form-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewsCategoryFormDialogComponent implements OnInit {
  /**
   * Form instance for category data
   */
  categoryForm!: FormGroup;

  /**
   * Current mode: 'create' or 'edit'
   */
  mode: 'create' | 'edit' = 'create';

  /**
   * Loading state during submission
   */
  isLoading = false;

  /**
   * Form validation configuration
   */
  validationRules = NEWS_CATEGORY_FORM_VALIDATION;
  operationMessages = NEWS_CATEGORY_OPERATION_MESSAGES;

  /**
   * Category being edited (if any)
   */
  private editingCategory?: NewsCategoryViewModel;

  /**
   * Backend error message for duplicate fields
   */
  backendErrorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<NewsCategoryFormDialogComponent>,
    private categoryService: NewsCategoryService,
    @Optional() @Inject(MAT_DIALOG_DATA) private data: NewsCategoryViewModel | null
  ) {
    // If data is provided, we're in edit mode
    if (this.data) {
      this.mode = 'edit';
      this.editingCategory = this.data;
    }
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  /**
   * Initialize form with validation rules
   * If editing, populate form with existing category data
   * Called in ngOnInit
   */
  private initializeForm(): void {
    this.categoryForm = this.fb.group({
      nameEn: [
        this.editingCategory?.nameEn || '',
        [
          Validators.required,
          Validators.minLength(this.validationRules.CATEGORY_NAME_EN.MIN_LENGTH),
          Validators.maxLength(this.validationRules.CATEGORY_NAME_EN.MAX_LENGTH),
          Validators.pattern(this.validationRules.CATEGORY_NAME_EN.PATTERN),
        ],
        [this.duplicateNameEnValidator()],
      ],
      nameEs: [
        this.editingCategory?.nameEs || '',
        [
          Validators.required,
          Validators.minLength(this.validationRules.CATEGORY_NAME_ES.MIN_LENGTH),
          Validators.maxLength(this.validationRules.CATEGORY_NAME_ES.MAX_LENGTH),
          Validators.pattern(this.validationRules.CATEGORY_NAME_ES.PATTERN),
        ],
        [this.duplicateNameEsValidator()],
      ],
      slug: [
        this.editingCategory?.slug || '',
        [
          Validators.pattern(this.validationRules.SLUG.PATTERN),
        ],
      ],
      description: [
        this.editingCategory?.description || '',
        [
          Validators.maxLength(this.validationRules.DESCRIPTION.MAX_LENGTH),
        ],
      ],
    });
  }

  /**
   * Handle form submission
   * Validates form, then emits result and closes dialog
   */
  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.markFormGroupTouched(this.categoryForm);
      return;
    }

    this.isLoading = true;
    this.backendErrorMessage = null;

    // Prepare result
    const result: DialogResult = {
      mode: this.mode,
      success: true,
      data: this.categoryForm.value as NewsCategoryViewModel,
    };

    // Small delay to simulate network latency
    setTimeout(() => {
      this.dialogRef.close(result);
    }, 300);
  }

  /**
   * Handle cancel button click
   * Closes dialog without saving
   */
  onCancel(): void {
    this.dialogRef.close({ mode: this.mode, success: false });
  }

  /**
   * Mark all form fields as touched to show validation errors
   * @param formGroup - Form group to mark
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach((key) => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  /**
   * Generate slug from English name
   * Converts "Technology News" → "technology-news"
   */
  generateSlug(): void {
    const nameEn = this.categoryForm.get('nameEn')?.value || '';
    const slug = nameEn
      .toLowerCase()
      .replace(/\s+/g, '-')
      .replace(/[^a-z0-9\-]/g, '');

    this.categoryForm.patchValue({ slug }, { emitEvent: false });
  }

  /**
   * Get error message for form field
   * @param fieldName - Field name to get error for
   */
  getErrorMessage(fieldName: string): string {
    const control = this.categoryForm.get(fieldName);
    if (!control || !control.errors) return '';

    const errors = control.errors;
    const validation = this.validationRules[fieldName as keyof typeof this.validationRules] as any;

    // Backend error takes priority
    if (errors['backendError']) {
      return fieldName === 'nameEn'
        ? 'This English name already exists. Please try another name.'
        : fieldName === 'nameEs'
        ? 'This Spanish name already exists. Please try another name.'
        : 'This value already exists. Please try another.';
    }

    if (errors['required']) {
      return validation?.REQUIRED_ERROR || 'This field is required';
    }
    if (errors['minlength']) {
      return validation?.MIN_ERROR || `Minimum length is ${errors['minlength'].requiredLength}`;
    }
    if (errors['maxlength']) {
      return validation?.MAX_ERROR || `Maximum length is ${errors['maxlength'].requiredLength}`;
    }
    if (errors['pattern']) {
      return validation?.PATTERN_ERROR || 'Invalid format';
    }
    if (errors['duplicateNameEn'] || errors['duplicateNameEs']) {
      return 'This name already exists. Please use a different name.';
    }

    return 'Invalid input';
  }

  /**
   * Check if field has error and is touched
   * @param fieldName - Field name to check
   */
  hasError(fieldName: string): boolean {
    const control = this.categoryForm.get(fieldName);
    return control?.invalid && (control?.dirty || control?.touched) ? true : false;
  }

  /**
   * Get title based on mode
   */
  get dialogTitle(): string {
    return this.mode === 'create'
      ? this.operationMessages.CREATE.TITLE
      : this.operationMessages.EDIT.TITLE;
  }

  /**
   * Get button label based on mode
   */
  get submitButtonLabel(): string {
    return this.mode === 'create'
      ? this.operationMessages.CREATE.CONFIRM_BTN
      : this.operationMessages.EDIT.CONFIRM_BTN;
  }

  /**
   * Character count for description field
   */
  get descriptionCharCount(): number {
    return this.categoryForm.get('description')?.value?.length || 0;
  }

  /**
   * Set backend error on specific field
   * Called from parent component when backend returns field-specific errors
   * @param fieldName - Field name (nameEn, nameEs, slug)
   * @param errorMessage - Error message to display
   */
  setFieldBackendError(fieldName: string, errorMessage: string): void {
    const control = this.categoryForm.get(fieldName);
    if (control) {
      control.setErrors({ 'backendError': true });
      control.markAsTouched();
    }
    this.backendErrorMessage = errorMessage;
    this.isLoading = false;
  }

  /**
   * Clear all backend errors
   */
  clearBackendErrors(): void {
    this.backendErrorMessage = null;
    Object.keys(this.categoryForm.controls).forEach((key) => {
      const control = this.categoryForm.get(key);
      if (control?.errors?.['backendError']) {
        control.setErrors(null);
      }
    });
  }

  /**
   * Async validator for duplicate English name
   * Checks if name already exists in loaded categories
   * Excludes current category when in edit mode
   */
  private duplicateNameEnValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value) {
        return new Observable((observer) => {
          observer.next(null);
          observer.complete();
        });
      }

      return this.categoryService
        .checkDuplicateNameEn(control.value, this.editingCategory?.id)
        .pipe(
          debounceTime(300),
          map((isDuplicate: boolean) =>
            isDuplicate ? { 'duplicateNameEn': true } : null
          )
        );
    };
  }

  /**
   * Async validator for duplicate Spanish name
   * Checks if name already exists in loaded categories
   * Excludes current category when in edit mode
   */
  private duplicateNameEsValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      if (!control.value) {
        return new Observable((observer) => {
          observer.next(null);
          observer.complete();
        });
      }

      return this.categoryService
        .checkDuplicateNameEs(control.value, this.editingCategory?.id)
        .pipe(
          debounceTime(300),
          map((isDuplicate: boolean) =>
            isDuplicate ? { 'duplicateNameEs': true } : null
          )
        );
    };
  }
}
