/**
 * Form Fields Module - Barrel Export
 * 
 * All public APIs exported from this entry point
 * Allows clean imports:
 *   import { FormFieldsModule } from '@shared/form-fields';
 *   import { ISelectOption } from '@shared/form-fields';
 *   import { TextInputFieldComponent } from '@shared/form-fields';
 */

// Module export
export { FormFieldsModule } from './form-fields.module';

// Component exports
export { TextInputFieldComponent } from './components/text-input-field/text-input-field.component';
export { SelectFieldComponent } from './components/select-field/select-field.component';
export { MultiSelectFieldComponent } from './components/multi-select-field/multi-select-field.component';
export { DateRangeFieldComponent } from './components/date-range-field/date-range-field.component';
export { AutocompleteFieldComponent } from './components/autocomplete-field/autocomplete-field.component';

// Model exports
export type {
  ISelectOption,
  ITextInputFieldProps,
  ISelectFieldProps,
  IMultiSelectFieldProps,
  IDateRangeFieldProps,
  IAutocompleteFieldProps,
  IValidationResult,
} from './models/form-field.models';

// Utility exports
export {
  sortOptionsByLabel,
  filterOptions,
  groupOptionsByGroup,
  validateEmail,
  validateUrl,
  validateMinLength,
  validateMaxLength,
  validateDateRange,
  validateRequired,
} from './utils/form-field.utils';
