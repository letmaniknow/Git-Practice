import { Validators, ReactiveFormsModule, FormGroup, AbstractControl } from '@angular/forms';
import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectorRef, OnChanges, SimpleChanges, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { createNewsForm, NewsFormModel } from '../../models/news-form.model';
import { NewsCategory } from '../../models/news-form.model';
import { NewsFormService } from '../../services/news-form.service';
import { AuthService } from '../../../../core/services/auth.service'; // Adjust path as needed
import { NewsItem } from '../../models/news-item.model';
import { ErrorResponse } from '../../models/error-response.model';
import { ErrorMessageComponent } from '../../../../shared/components/error-message/error-message.component';
import { ErrorAlertComponent } from '../../../../shared/components/error-alert/error-alert.component';
import { LoadingComponent } from '../../../../shared/components/loading/loading.component';
import { EnumDisplayPipe } from '../../../../shared/pipes/enum-display.pipe';
import { UnifiedDatetimePickerComponent } from '../../../../shared/components/unified-datetime-picker/unified-datetime-picker.component';
import { ValidationErrorDialogComponent } from '../../../../shared/components/validation-error-dialog/validation-error-dialog.component';

@Component({
  selector: 'app-news-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatExpansionModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatTooltipModule,
    MatDialogModule,
    ErrorMessageComponent,
    LoadingComponent,
    EnumDisplayPipe,
    UnifiedDatetimePickerComponent,
    ErrorAlertComponent
  ],
  templateUrl: './news-form.component.html',
  styleUrls: ['./news-form.component.scss']
})
export class NewsFormComponent implements OnInit, OnChanges, OnDestroy {
  private destroy$ = new Subject<void>();
  
  // Error handling for categories
  categoriesError: string | null = null;
  isLoadingCategories = false;
  
  mediaPreviewUrl: string | null = null;
  @Input() isEditMode = false;
  @Input() isSubmitting = false;
  @Input() errorMessage: string | null = null;
  @Input() fieldErrors: { [key: string]: string } | null = null;
  
  private pendingFieldErrors: { [key: string]: string } | null = null;
  @Input() set newsItem(value: NewsItem | null) {
    this._newsItem = value;
    if (value && this.newsForm) {
      console.log('[newsItem setter] Patching form with news item:', value);
      this.patchFormWithNewsItem(value);
      // After patching, re-apply any field errors (prevents patching from clearing errors)
      if (this.fieldErrors) {
        console.log('[newsItem setter] Re-applying field errors after patch:', this.fieldErrors);
        this.applyFieldErrors(this.fieldErrors);
      }
      // Log form value after patch
      console.log('[newsItem setter] Form value after patch:', this.newsForm.getRawValue());
    }
    // Set media preview URL if editing and media file exists
    if (value && value.newsMediaFileName) {
      this.mediaPreviewUrl = this.newsFormService.getMediaFileUrl(value.newsMediaFileName);
    } else {
      this.mediaPreviewUrl = null;
    }
  }
  get newsItem(): NewsItem | null {
    return this._newsItem;
  }
  private _newsItem: NewsItem | null = null;
  @Output() save = new EventEmitter<FormData>();
  @Output() cancel = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  
  @Input() showDelete = false;
  newsForm!: FormGroup<NewsFormModel>;
  categories: NewsCategory[] = [];
  agencies: any[] = [];
  countries: any[] = [];
  seriesList: any[] = [];
  mainMediaFile: File | null = null;
  thumbnailFile: File | null = null;
  showAdvanced = false;
  expandedSections: Record<string, boolean> = {
    breaking: false,
    publishing: false,
    location: false,
    attribution: false,
    content: false,
    media: false,
    monetization: false,
    seo: false,
    series: false,
    internal: false
  };

  // Add workflowStatuses for template dropdown (loaded dynamically from backend)
  workflowStatuses: string[] = [];

  // Add canPublish property for template
  get canPublish(): boolean {
    // Use AuthService to check if user has publish permission/role
    return this.authService.hasPermission ? this.authService.hasPermission('publish') : false;
  }

  // Getters for datetime form controls
  get scheduledPublishAtControl(): any {
    return this.newsForm.get('newsScheduledPublishAt');
  }

  get breakingExpiresAtControl(): any {
    return this.newsForm.get('newsBreakingExpiresAt');
  }

  get embargoUntilControl(): any {
    return this.newsForm.get('newsEmbargoUntil');
  }

  get expiresAtControl(): any {
    return this.newsForm.get('newsExpiresAt');
  }

  constructor(
    private newsFormService: NewsFormService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog
  ) {}

  /**
   * Handle input property changes, especially fieldErrors from parent component
   * Applies field-level validation errors to form controls when they change
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fieldErrors'] && this.fieldErrors) {
      if (this.newsForm) {
        // Form is ready - apply errors immediately
        this.applyFieldErrors(this.fieldErrors);
      } else {
        // Form not ready yet - store for application in ngOnInit
        this.pendingFieldErrors = this.fieldErrors;
      }
      // Trigger change detection to ensure template updates
      this.cdr.markForCheck();
    }
  }

  

  ngOnInit(): void {
    // Initialize form with validators
    this.newsForm = createNewsForm();
    this.newsForm.get('newsWorkflowStatus')?.setValue('DRAFT');

    // BUSINESS RULE: Media file and Source URL are MANDATORY in BOTH create and edit modes
    // This ensures content quality and proper attribution for all news items
    // No special handling needed - validators remain active in all modes

    this.loadCategories();
    this.loadWorkflowStatuses();

    // If editing, patch form with NewsItem first, then apply errors
    if (this._newsItem) {
      this.patchFormWithNewsItem(this._newsItem);
    }

    // Apply any fieldErrors that arrived before form initialization (after patching)
    if (this.pendingFieldErrors) {
      this.applyFieldErrors(this.pendingFieldErrors);
      this.pendingFieldErrors = null;
    } else if (this.fieldErrors) {
      this.applyFieldErrors(this.fieldErrors);
    }

    // Conditional validation for sponsored content
    this.newsForm.get('newsIsSponsored')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(isSponsored => {
        this.updateSponsorValidation(isSponsored);
      });

    // Initialize sponsor validation based on current value
    const currentSponsorValue = this.newsForm.get('newsIsSponsored')?.value;
    if (currentSponsorValue !== undefined) {
      this.updateSponsorValidation(currentSponsorValue);
    }

    // Conditional validation for premium tier
    this.newsForm.get('newsIsPremium')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(isPremium => {
        this.updatePremiumValidation(isPremium);
      });

    // Initialize premium validation based on current value
    const currentPremiumValue = this.newsForm.get('newsIsPremium')?.value;
    if (currentPremiumValue !== undefined) {
      this.updatePremiumValidation(currentPremiumValue);
    }
  }

  /**
   * Update sponsor field validators based on sponsorship status
   * @param isSponsored Whether the news is sponsored
   */
  private updateSponsorValidation(isSponsored: boolean): void {
    const sponsorName = this.newsForm.get('newsSponsorName');
    const sponsorLogo = this.newsForm.get('newsSponsorLogoUrl');
    const sponsorWebsite = this.newsForm.get('newsSponsorWebsiteUrl');
    
    if (sponsorName && sponsorLogo && sponsorWebsite) {
      if (isSponsored) {
        sponsorName.setValidators([Validators.required, Validators.maxLength(255)]);
        sponsorLogo.setValidators([Validators.required]);
        sponsorWebsite.setValidators([Validators.required, Validators.pattern('https?://.+')]);
      } else {
        sponsorName.clearValidators();
        sponsorLogo.clearValidators();
        sponsorWebsite.clearValidators();
      }
      sponsorName.updateValueAndValidity();
      sponsorLogo.updateValueAndValidity();
      sponsorWebsite.updateValueAndValidity();
    }
  }

  /**
   * Update premium tier validators based on premium status
   * @param isPremium Whether the news is premium
   */
  private updatePremiumValidation(isPremium: boolean): void {
    const premiumTier = this.newsForm.get('newsPremiumTier');
    if (premiumTier) {
      if (isPremium) {
        premiumTier.setValidators([Validators.required]);
      } else {
        premiumTier.clearValidators();
      }
      premiumTier.updateValueAndValidity();
    }
  }

  /**
   * Cleanup subscriptions when component is destroyed
   */
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Check if the form has unsaved changes
   * Used by CanDeactivate guard to prevent accidental navigation
   * 
   * @returns true if form has been modified and not submitted
   */
  public hasUnsavedChanges(): boolean {
    return this.newsForm ? this.newsForm.dirty : false;
  }

  /**
   * Patch the form with values from a NewsItem (API response)
   * Maps ALL available fields from backend to form controls
   */
  private patchFormWithNewsItem(news: import('../../models/news-item.model').NewsItem): void {
    if (!this.newsForm) return;
    
    // Map ALL fields from NewsItem to form controls
    const patch: any = {
      // Core content
      newsTitleEn: news.newsTitleEn,
      newsTitleEs: news.newsTitleEs,
      newsContentEn: news.newsContentEn,
      newsContentEs: news.newsContentEs,
      newsExcerptEn: news.newsExcerptEn || '',
      newsExcerptEs: news.newsExcerptEs || '',
      
      // Category & metadata
      newsNewsCategoryId: news.newsNewsCategoryId,
      newsContentFormat: news.newsContentFormat || 'PLAIN_TEXT',
      newsTags: news.newsTags || '',
      
      // Publishing & workflow
      newsWorkflowStatus: news.newsWorkflowStatus,
      newsScheduledPublishAt: news.newsScheduledPublishAt || '',
      newsIsFeatured: news.newsIsFeatured,
      newsSlug: news.newsSlug || '',
      
      // Source information
      newsSourceUrl: news.newsSourceUrl,
      newsSourceAuthorName: news.newsSourceAuthorName || '',
      newsSourceAgencyId: news.newsSourceAgencyId || '',
      newsContentOrigin: news.newsContentOrigin || 'ORIGINAL',
      
      // Breaking news
      newsIsBreaking: news.newsIsBreaking,
      newsBreakingExpiresAt: news.newsBreakingExpiresAt || '',
      
      // Advanced publishing controls
      newsEmbargoUntil: news.newsEmbargoUntil || '',
      newsExpiresAt: news.newsExpiresAt || '',
      newsUrgencyLevel: news.newsUrgencyLevel || 'NORMAL',
      newsTargetAudience: news.newsTargetAudience || '',
      newsReadTimeMinutes: news.newsReadTimeMinutes || null,
      
      // Geographic data
      newsCountryCode: news.newsCountryCode || '',
      newsRegion: news.newsRegion || '',
      newsCity: news.newsCity || '',
      newsLatitude: news.newsLatitude || null,
      newsLongitude: news.newsLongitude || null,
      
      // Sponsored content
      newsIsSponsored: news.newsIsSponsored,
      newsSponsorName: news.newsSponsorName || '',
      newsSponsorLogoUrl: news.newsSponsorLogoUrl || '',
      newsSponsorWebsiteUrl: news.newsSponsorWebsiteUrl || '',
      
      // Premium content
      newsIsPremium: news.newsIsPremium,
      newsPremiumTier: news.newsPremiumTier || 'FREE',
      
      // SEO metadata
      newsMetaTitle: news.newsMetaTitle || '',
      newsMetaDescription: news.newsMetaDescription || '',
      newsKeywords: news.newsKeywords || '',
      newsCanonicalUrl: news.newsCanonicalUrl || '',
      
      // Series support
      newsSeriesId: news.newsSeriesId || '',
      newsSeriesOrder: news.newsSeriesOrder || null,
      
      // Editor notes
      newsEditorNotes: news.newsEditorNotes || ''
    };
    
    console.log('[patchFormWithNewsItem] Patching form with complete data:', patch);
    this.newsForm.patchValue(patch);
    console.log('[patchFormWithNewsItem] Form value after patch:', this.newsForm.getRawValue());
    
    // Trigger conditional validators after patching form data
    this.updateSponsorValidation(news.newsIsSponsored || false);
    this.updatePremiumValidation(news.newsIsPremium || false);
  }

  /**
   * Load active categories for news creation/editing
   * 
   * Uses getActiveCategoriesForNewsCreation() because:
   * - Forms should only allow selecting ACTIVE categories
   * - Prevents users from assigning soft-deleted categories to new content
   * - Inline error alert shows right above category dropdown for better UX
   */
  loadCategories(): void {
    this.isLoadingCategories = true;
    this.categoriesError = null;
    
    this.newsFormService.getActiveCategoriesForNewsCreation()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (cats: any[]) => {
          this.categories = cats;
          this.categoriesError = null;
          this.isLoadingCategories = false;
          console.debug('[NewsFormComponent] Categories loaded:', cats.length);
        },
        error: (error) => {
          console.error('[NewsFormComponent] Categories load FAILED:', error);
          this.categories = [];
          this.categoriesError = 'Failed to load categories. Please try again.';
          this.isLoadingCategories = false;
        }
      });
  }

  /**
   * Load available workflow statuses from backend
   * Provides fallback to default statuses if API fails
   */
  loadWorkflowStatuses(): void {
    this.newsFormService.getWorkflowStatuses()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statuses: string[]) => {
          this.workflowStatuses = statuses;
        },
        error: () => {
          // Fallback to defaults if API fails (silent - non-critical)
          this.workflowStatuses = ['DRAFT', 'SUBMITTED', 'REVIEWED', 'APPROVED', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED'];
        }
      });
  }
  onMainMediaChange(event: Event): void {
    const input = event?.target as HTMLInputElement | null;
    if (input && input.files && input.files.length > 0) {
      this.mainMediaFile = input.files[0];
      const ctrl = this.newsForm.get('imageVideoFile');
      if (ctrl) {
        ctrl.setValue(this.mainMediaFile);
        ctrl.markAsTouched();
        ctrl.markAsDirty();
      }
    }
  }

  onThumbnailChange(event: Event): void {
    const input = event?.target as HTMLInputElement | null;
    if (input && input.files && input.files.length > 0) {
      this.thumbnailFile = input.files[0];
      const ctrl = this.newsForm.get('thumbnailFile');
      if (ctrl) ctrl.setValue(this.thumbnailFile);
    }
  }

  onSubmit(): void {
    if (!this.newsForm?.valid) {
      console.error('❌ Form is INVALID - Validation Errors:', {
        formErrors: this.newsForm?.errors,
        controlErrors: Object.entries(this.newsForm?.controls || {}).reduce((acc, [key, control]) => {
          if (control.errors) {
            acc[key] = control.errors;
          }
          return acc;
        }, {} as any)
      });
      this.markAllAsTouched();
      this.showValidationErrorDialog();
      return;
    }
    
    console.log('✅ Form is VALID - Proceeding with submission in', this.isEditMode ? 'EDIT' : 'CREATE', 'mode');
    
    // BUSINESS RULE: Media file is MANDATORY in BOTH create and edit modes
    // Users must provide media file when creating OR updating news
    if (!this.mainMediaFile) {
      console.error('❌ Media file is required in both CREATE and EDIT modes');
      this.markAllAsTouched();
      this.showMediaRequiredDialog();
      return;
    }
    
    const formData = this.buildFormData();
    console.log('📤 Emitting save event with FormData');
    this.save.emit(formData);
  }

  /**
   * Keyboard shortcut handler for Ctrl+S / Cmd+S
   * Prevents default browser save dialog and triggers form submission
   * 
   * @param event Keyboard event
   */
  @HostListener('document:keydown', ['$event'])
  handleKeyboardShortcut(event: KeyboardEvent): void {
    // Check for Ctrl+S (Windows/Linux) or Cmd+S (Mac)
    const isSaveShortcut = (event.ctrlKey || event.metaKey) && event.key === 's';
    
    if (isSaveShortcut) {
      // Prevent default browser save dialog
      event.preventDefault();
      event.stopPropagation();
      
      // Only trigger save if form is not already submitting
      if (!this.isSubmitting) {
        console.log('⌨️ Keyboard shortcut triggered: Ctrl+S / Cmd+S');
        this.onSubmit();
      }
    }
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onDelete(): void {
    this.delete.emit();
  }

  private buildFormData(): FormData {
    const formData = new FormData();
    const value = this.newsForm?.value || {};
    // Mandatory fields (append only if defined)
    if (value.newsTitleEn) formData.append('newsTitleEn', value.newsTitleEn);
    if (value.newsTitleEs) formData.append('newsTitleEs', value.newsTitleEs);
    if (value.newsContentEn) formData.append('newsContentEn', value.newsContentEn);
    if (value.newsContentEs) formData.append('newsContentEs', value.newsContentEs);
    if (this.mainMediaFile) formData.append('imageVideoFile', this.mainMediaFile);
    if (value.newsNewsCategoryId) formData.append('newsNewsCategoryId', value.newsNewsCategoryId.toString());
    if (value.newsSourceUrl) formData.append('newsSourceUrl', value.newsSourceUrl);
    if (value.newsWorkflowStatus) formData.append('newsWorkflowStatus', value.newsWorkflowStatus);
    
    // Handle Scheduled Publish DateTime (now stored as ISO string)
    if (value.newsWorkflowStatus === 'SCHEDULED' && value.newsScheduledPublishAt) {
      formData.append('newsScheduledPublishAt', value.newsScheduledPublishAt);
    }
    formData.append('newsIsFeatured', value.newsIsFeatured ? 'true' : 'false');
    
    // Handle Breaking Expires DateTime (now stored as ISO string)
    if (value.newsIsBreaking && value.newsBreakingExpiresAt) {
      formData.append('newsBreakingExpiresAt', value.newsBreakingExpiresAt);
    }
    
    // Handle Embargo Until DateTime (now stored as ISO string)
    if (value.newsEmbargoUntil) {
      formData.append('newsEmbargoUntil', value.newsEmbargoUntil);
    }
    
    // Handle Expires At DateTime (now stored as ISO string)
    if (value.newsExpiresAt) {
      formData.append('newsExpiresAt', value.newsExpiresAt);
    }
    
    // Advanced fields (add if not empty)
    [
      'newsExcerptEn', 'newsExcerptEs', 'newsContentFormat', 'newsTags',
      'newsSourceAuthorName', 'newsSourceAgencyId', 'newsContentOrigin',
      'newsIsBreaking', 'newsUrgencyLevel', 'newsTargetAudience', 'newsReadTimeMinutes',
      'newsCountryCode', 'newsRegion', 'newsCity', 'newsLatitude', 'newsLongitude',
      'newsIsSponsored', 'newsSponsorName', 'newsSponsorLogoUrl', 'newsSponsorWebsiteUrl',
      'newsIsPremium', 'newsPremiumTier', 'newsMetaTitle', 'newsMetaDescription',
      'newsKeywords', 'newsCanonicalUrl', 'newsSeriesId', 'newsSeriesOrder', 'newsEditorNotes'
    ].forEach(field => {
      const v = value[field as keyof typeof value];
      if (v !== null && v !== undefined && v !== '') {
        formData.append(field, typeof v === 'boolean' ? (v ? 'true' : 'false') : v.toString());
      }
    });
    if (this.thumbnailFile) {
      formData.append('thumbnailFile', this.thumbnailFile);
    }
    return formData;
  }

  /**
   * Mark all form controls as touched to trigger validation display
   * This ensures error borders and messages appear for all invalid fields
   */
  private markAllAsTouched(): void {
    if (!this.newsForm) return;
    
    console.log('🔍 Marking all fields as touched...');
    
    // Mark entire form as touched
    this.newsForm.markAllAsTouched();
    
    // Also mark each control individually for consistency
    Object.keys(this.newsForm.controls).forEach((key: string) => {
      const control = this.newsForm.get(key);
      if (control) {
        control.markAsTouched();
        control.markAsDirty();
        control.updateValueAndValidity();
        
        // Log invalid fields
        if (control.invalid) {
          console.log(`  ❌ ${key}: invalid -`, control.errors);
        }
      }
    });
    
    console.log('✅ All fields marked. Form touched:', this.newsForm.touched, 'Form invalid:', this.newsForm.invalid);
  }

  /**
   * Converts snake_case field names from backend to camelCase form control names
   * Example: 'news_title_en' -> 'newsTitleEn', 'news_title_es' -> 'newsTitleEs'
   * 
   * @param snakeCaseFieldName Backend field name in snake_case
   * @returns Form control name in camelCase
   */
  private snakeToCamelCase(snakeCaseFieldName: string): string {
    // Standard conversion: snake_case → camelCase
    // Examples:
    // - news_title_en → newsTitleEn
    // - news_title_es → newsTitleEs
    // - news_content_en → newsContentEn
    return snakeCaseFieldName.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
  }

  /**
   * Apply field-level validation errors from backend API to form controls
   * Enables inline error display for each field with server-side validation feedback
   * Handles conversion of snake_case backend field names to camelCase form control names
   * Special handling: news_slug errors are mapped to newsTitleEn (since slug is auto-generated)
   * 
   * Industry-standard implementation:
   * - Immutable error object handling (no mutations)
   * - Preserves validation errors while adding backend errors
   * - Clears backend errors on user edit
   * - Proper change detection
   * 
   * @param errors Map of field names to error messages from backend
   */
  /**
   * Apply backend field-level errors to form controls
   * Called when fieldErrors are received from backend validation
   * 
   * INDUSTRY STANDARD ERROR DISPLAY:
   * ✅ Shows EVERY validation error inline under its field
   * ✅ Clears automatically when user edits the field
   * ✅ Preserves frontend validation errors (required, pattern, etc.)
   * 
   * Example flow:
   * Backend returns: { news_title_en: "...", news_content_en: "...", news_title_es: "..." }
   *      ↓
   * For EACH error, find the form control
   *      ↓
   * Add 'backend' error to control (preserves other errors)
   *      ↓
   * Mark as touched/dirty (triggers Material mat-error display)
   *      ↓
   * On user edit: Auto-clear 'backend' error
   */
  private applyFieldErrors(errors: { [key: string]: string } | null): void {
    if (!errors || !this.newsForm) {
      console.log('🟢 No errors to apply or form not ready');
      return;
    }

    console.log('[applyFieldErrors] Called with errors:', errors);
    console.log('[applyFieldErrors] Current form value:', this.newsForm.getRawValue());
    console.log('[applyFieldErrors] Form control names:', Object.keys(this.newsForm.controls));

    // STEP 1: Clear previous backend errors from ALL form controls
    Object.keys(this.newsForm.controls).forEach(fieldName => {
      const control = this.newsForm.get(fieldName);
      if (control?.errors && 'backend' in control.errors) {
        const { backend, ...remainingErrors } = control.errors;
        control.setErrors(Object.keys(remainingErrors).length > 0 ? remainingErrors : null);
      }
    });

    // STEP 2: Apply new backend errors to matching form controls
    const appliedErrors: string[] = [];
    const missingControls: string[] = [];

    Object.entries(errors).forEach(([fieldName, errorMessage]) => {
      const possibleNames = [fieldName, this.snakeToCamelCase(fieldName)];
      let found = false;
      for (const formControlName of possibleNames) {
        const control = this.newsForm.get(formControlName);
        if (control) {
          found = true;
          control.markAsTouched();
          control.markAsDirty();
          const newErrors = { ...(control.errors || {}), backend: errorMessage };
          control.setErrors(newErrors);
          appliedErrors.push(formControlName);
          console.log(`[applyFieldErrors] Set backend error on '${formControlName}':`, errorMessage);
          control.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((val) => {
              console.log(`[valueChanges] '${formControlName}' changed to:`, val);
              if (control.errors?.['backend']) {
                const { backend, ...remainingErrors } = control.errors;
                control.setErrors(Object.keys(remainingErrors).length > 0 ? remainingErrors : null);
                this.cdr.markForCheck();
                console.log(`[valueChanges] Cleared backend error for '${formControlName}' after edit.`);
              }
            });
          break;
        }
      }
      if (!found) {
        console.warn(`[applyFieldErrors] Form control for '${fieldName}' not found - error will be skipped`);
        missingControls.push(fieldName);
      }
    });

    this.cdr.markForCheck();
    console.log('[applyFieldErrors] Applied errors:', appliedErrors);
    console.log('[applyFieldErrors] Missing controls:', missingControls);
    console.log('[applyFieldErrors] Form value after applying errors:', this.newsForm.getRawValue());
  }

  /**
   * Show validation error dialog to user
   * Lists all fields with errors in a professional modal
   */
  private showValidationErrorDialog(): void {
    const errorFields: string[] = [];
    
    // Collect all fields with errors
    Object.entries(this.newsForm.controls).forEach(([fieldName, control]) => {
      if (control.errors) {
        // Convert camelCase to readable text
        const readableFieldName = this.camelCaseToReadable(fieldName);
        errorFields.push(readableFieldName);
      }
    });

    if (errorFields.length === 0) {
      return;
    }

    // Open dialog with error list
    const dialogRef = this.dialog.open(ValidationErrorDialogComponent, {
      width: '500px',
      maxWidth: '90vw',
      maxHeight: '80vh',
      data: { errors: errorFields },
      disableClose: false,
      autoFocus: true,
      panelClass: 'validation-error-dialog-container'
    });

    // When dialog closes, scroll to first error field
    dialogRef.afterClosed().subscribe(() => {
      this.scrollToFirstError();
    });
  }

  /**
   * Show media required error dialog
   */
  private showMediaRequiredDialog(): void {
    const dialogRef = this.dialog.open(ValidationErrorDialogComponent, {
      width: '500px',
      maxWidth: '90vw',
      data: { errors: ['Media file (image or video) is required'] },
      disableClose: false,
      autoFocus: true,
      panelClass: 'validation-error-dialog-container'
    });

    // When dialog closes, scroll to media upload section
    dialogRef.afterClosed().subscribe(() => {
      const mediaInput = document.querySelector('.file-button.upload-button');
      if (mediaInput) {
        mediaInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  }

  /**
   * Convert camelCase field names to readable text
   * Example: newsTitleEn -> Title (English)
   */
  private camelCaseToReadable(fieldName: string): string {
    // Remove 'news' prefix if present
    let readable = fieldName.replace(/^news/, '');
    
    // Handle language suffixes
    readable = readable.replace(/En$/, ' (English)');
    readable = readable.replace(/Es$/, ' (Spanish)');
    
    // Handle common abbreviations
    readable = readable.replace(/Url$/, ' URL');
    readable = readable.replace(/Id$/, ' ID');
    readable = readable.replace(/Seo$/, ' SEO');
    
    // Add spaces before capital letters
    readable = readable.replace(/([A-Z])/g, ' $1').trim();
    
    // Capitalize first letter
    readable = readable.charAt(0).toUpperCase() + readable.slice(1);
    
    return readable;
  }

  /**
   * Scroll to first error field for better UX
   * Industry standard: Help user see validation errors immediately
   */
  private scrollToFirstError(): void {
    // Find first form control with errors
    const firstControlWithError = Object.keys(this.newsForm.controls).find(
      fieldName => this.newsForm.get(fieldName)?.errors
    );

    if (firstControlWithError) {
      // Find the mat-form-field element for this control
      const element = document.querySelector(
        `[formControlName="${firstControlWithError}"]`
      );

      if (element) {
        // Scroll to element with smooth behavior
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        console.log(`📍 Scrolled to first error field: ${firstControlWithError}`);
        
        // Focus on the element
        (element as HTMLElement).focus();
      }
    }
  }
}
