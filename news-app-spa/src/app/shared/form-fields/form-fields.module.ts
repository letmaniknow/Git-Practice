/**
 * Form Fields Component Library Module
 * 
 * ARCHITECT DECISIONS:
 * ✅ Barrel Export Pattern: One import for all form field components
 * ✅ Lazy Loading Compatible: Can be loaded with feature module
 * ✅ Feature Standalone: Works with standalone components or modules
 * ✅ Future Proof: Built for Angular 17+ with latest Material
 * 
 * USAGE:
 * Most feature modules will just add FormFieldsModule to imports:
 * 
 * @example
 * import { FormFieldsModule } from '@shared/form-fields';
 * 
 * @NgModule({
 *   imports: [CommonModule, FormFieldsModule],
 *   declarations: [NewsSearchFiltersComponent],
 * })
 * export class NewsModule {}
 * 
 * Standalone approach (future):
 * @Component({
 *   imports: [FormFieldsModule],
 * })
 * export class SearchComponent {}
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

/**
 * Material Modules
 * Grouped by functionality for easier maintenance
 * 
 * Future Optimization: Can lazy-load material modules per need
 */
// Form & Input
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

// Selection
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

// Date/Time
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

// Buttons & Icons
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

// Display
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Form Field Components
 * Import as we build them (Phase 2)
 * 
 * Currently building: ✅ TextInputFieldComponent, ✅ SelectFieldComponent
 */
import { TextInputFieldComponent } from './components/text-input-field/text-input-field.component';
import { SelectFieldComponent } from './components/select-field/select-field.component';
import { MultiSelectFieldComponent } from './components/multi-select-field/multi-select-field.component';
import { DateRangeFieldComponent } from './components/date-range-field/date-range-field.component';
import { AutocompleteFieldComponent } from './components/autocomplete-field/autocomplete-field.component';

/**
 * FormFieldsModule
 * 
 * ✅ No Providers: Components are stateless/presentational
 * ✅ Forward-thinking: Designed for lazy loading and tree-shaking
 * ✅ Industry Standard: Follows Angular module best practices
 */
@NgModule({
  /**
   * IMPORTS: All dependencies for form field components
   * Including standalone components and Material modules
   */
  imports: [
    // Core Angular
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    
    // Material Modules (Form & Input)
    MatFormFieldModule,
    MatInputModule,
    
    // Material Modules (Selection)
    MatSelectModule,
    MatAutocompleteModule,
    MatCheckboxModule,
    MatRadioModule,
    MatSlideToggleModule,
    
    // Material Modules (Date/Time)
    MatDatepickerModule,
    MatNativeDateModule,
    
    // Material Modules (Buttons & Icons)
    MatButtonModule,
    MatIconModule,
    
    // Material Modules (Display & State)
    MatChipsModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatTooltipModule,
    
    // Standalone Form Field Components (import, not declare)
    TextInputFieldComponent,
    SelectFieldComponent,
    MultiSelectFieldComponent,
    DateRangeFieldComponent,
    AutocompleteFieldComponent,
  ],
  
  /**
   * EXPORTS: Public API - just export the components
   * Standalone components are available via imports list
   */
  exports: [
    // Tier-1 Components
    TextInputFieldComponent,
    SelectFieldComponent,
    MultiSelectFieldComponent,
    DateRangeFieldComponent,
    AutocompleteFieldComponent,
  ],
})
export class FormFieldsModule {}

/**
 * BARREL EXPORTS for easy imports throughout the app
 * 
 * USAGE - Models:
 * import { ITextInputFieldProps, ISelectOption } from '@shared/form-fields';
 * 
 * USAGE - Utils:
 * import { sortOptionsByLabel, validateEmail } from '@shared/form-fields';
 * 
 * USAGE - Components (when built):
 * import { TextInputFieldComponent } from '@shared/form-fields';
 */
export * from './models/form-field.models';
export * from './utils/form-field.utils';
