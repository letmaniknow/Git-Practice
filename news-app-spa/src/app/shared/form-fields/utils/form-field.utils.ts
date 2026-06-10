/**
 * Form Fields Utility Functions & Helpers
 * 
 * Purpose: Reusable utility functions for form field components
 * Promotes: DRY principle, consistency, testability
 * 
 * @module FormFieldUtils
 */

import { ISelectOption, IValidationResult } from '../models/form-field.models';

/**
 * SORT & FILTER UTILITIES
 */

/**
 * Sort select options by label (alphabetically)
 * @param options - Array of select options
 * @returns Sorted array
 * 
 * @example
 * const sorted = sortOptionsByLabel(categories);
 */
export function sortOptionsByLabel<T = any>(
  options: ISelectOption<T>[]
): ISelectOption<T>[] {
  return [...options].sort((a, b) => 
    a.label.localeCompare(b.label)
  );
}

/**
 * Filter select options by text search
 * Searches in label and description
 * @param options - Array of options
 * @param searchText - Text to search for
 * @returns Filtered array
 * 
 * @example
 * const filtered = filterOptions(categories, 'tech');
 */
export function filterOptions<T = any>(
  options: ISelectOption<T>[],
  searchText: string
): ISelectOption<T>[] {
  const lowerSearch = searchText.toLowerCase();
  
  return options.filter(option => 
    option.label.toLowerCase().includes(lowerSearch) ||
    (option.description?.toLowerCase().includes(lowerSearch) || false)
  );
}

/**
 * Group options by group property
 * @param options - Array of options
 * @returns Grouped object: { groupName: [options...] }
 * 
 * @example
 * const grouped = groupOptionsByGroup(allOptions);
 * // Result: { 'Technology': [...], 'Sports': [...] }
 */
export function groupOptionsByGroup<T = any>(
  options: ISelectOption<T>[]
): Record<string, ISelectOption<T>[]> {
  return options.reduce((acc, option) => {
    const group = option.group || 'Other';
    if (!acc[group]) acc[group] = [];
    acc[group].push(option);
    return acc;
  }, {} as Record<string, ISelectOption<T>[]>);
}

/**
 * VALIDATION UTILITIES
 */

/**
 * Validate email format
 * @param email - Email string
 * @returns Validation result
 * 
 * @example
 * const result = validateEmail('user@example.com');
 */
export function validateEmail(email: string): IValidationResult {
  const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const isValid = pattern.test(email);
  
  return {
    isValid,
    errorMessage: isValid ? undefined : 'Invalid email format',
    errorCode: isValid ? undefined : 'EMAIL_INVALID',
  };
}

/**
 * Validate URL format
 * @param url - URL string
 * @returns Validation result
 */
export function validateUrl(url: string): IValidationResult {
  try {
    new URL(url);
    return { isValid: true };
  } catch {
    return {
      isValid: false,
      errorMessage: 'Invalid URL format',
      errorCode: 'URL_INVALID',
    };
  }
}

/**
 * Validate minimum length
 * @param value - String value
 * @param minLength - Minimum length
 * @returns Validation result
 */
export function validateMinLength(
  value: string,
  minLength: number
): IValidationResult {
  const isValid = value.length >= minLength;
  
  return {
    isValid,
    errorMessage: isValid 
      ? undefined 
      : `Minimum ${minLength} characters required`,
    errorCode: isValid ? undefined : 'MIN_LENGTH',
    metadata: { minLength },
  };
}

/**
 * Validate maximum length
 * @param value - String value
 * @param maxLength - Maximum length
 * @returns Validation result
 */
export function validateMaxLength(
  value: string,
  maxLength: number
): IValidationResult {
  const isValid = value.length <= maxLength;
  
  return {
    isValid,
    errorMessage: isValid 
      ? undefined 
      : `Maximum ${maxLength} characters allowed`,
    errorCode: isValid ? undefined : 'MAX_LENGTH',
    metadata: { maxLength },
  };
}

/**
 * Validate date range (from <= to)
 * @param fromDate - Start date
 * @param toDate - End date
 * @returns Validation result
 */
export function validateDateRange(
  fromDate: Date | null,
  toDate: Date | null
): IValidationResult {
  if (!fromDate || !toDate) {
    return {
      isValid: true, // Allow partial dates
    };
  }
  
  const isValid = fromDate <= toDate;
  
  return {
    isValid,
    errorMessage: isValid 
      ? undefined 
      : 'From date must be before or equal to To date',
    errorCode: isValid ? undefined : 'DATE_RANGE_INVALID',
  };
}

/**
 * Validate required field
 * @param value - Field value
 * @returns Validation result
 */
export function validateRequired(value: any): IValidationResult {
  const isValid = value !== null && value !== undefined && value !== '';
  
  return {
    isValid,
    errorMessage: isValid ? undefined : 'This field is required',
    errorCode: isValid ? undefined : 'REQUIRED',
  };
}

/**
 * DISPLAY & FORMATTING UTILITIES
 */

/**
 * Format date for display
 * Default format: "MMM d, y" (Mar 15, 2024)
 * @param date - Date to format
 * @param format - Optional format string
 * @returns Formatted date string
 * 
 * @example
 * const formatted = formatDate(new Date(), 'MM/dd/yyyy');
 */
export function formatDate(
  date: Date | null | undefined,
  format: string = 'MMM d, y'
): string {
  if (!date) return '';
  
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
}

/**
 * Get display label for option
 * Handles nested properties via dot notation (e.g., "user.name")
 * @param item - Item to get label from
 * @param property - Property path
 * @returns Display label
 * 
 * @example
 * const label = getDisplayLabel(user, 'profile.fullName');
 */
export function getDisplayLabel(item: any, property: string): string {
  if (!item || !property) return '';
  
  const value = property.split('.').reduce((obj, prop) => {
    return obj?.[prop];
  }, item);
  
  return String(value || '');
}

/**
 * Truncate text with ellipsis
 * @param text - Text to truncate
 * @param maxLength - Max characters before truncation
 * @param suffix - Suffix to append (default: "...")
 * @returns Truncated text
 * 
 * @example
 * const short = truncateText('Very long description', 20);
 * // Result: "Very long description..."
 */
export function truncateText(
  text: string,
  maxLength: number,
  suffix: string = '...'
): string {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + suffix;
}

/**
 * ARRAY UTILITIES
 */

/**
 * Check if two arrays contain same items (order-independent)
 * @param array1 - First array
 * @param array2 - Second array
 * @returns True if same items
 * 
 * @example
 * const same = arraysEqual([1, 2, 3], [3, 2, 1]); // true
 */
export function arraysEqual<T>(array1: T[], array2: T[]): boolean {
  if (array1.length !== array2.length) return false;
  
  const set1 = new Set(array1);
  const set2 = new Set(array2);
  
  return Array.from(set1).every(item => set2.has(item));
}

/**
 * Remove duplicate items from array
 * @param array - Array with possible duplicates
 * @param property - Optional property to check for duplicates
 * @returns Unique array
 * 
 * @example
 * const unique = removeDuplicates([1, 2, 2, 3]); // [1, 2, 3]
 * const uniqueByName = removeDuplicates(users, 'id');
 */
export function removeDuplicates<T>(
  array: T[],
  property?: keyof T
): T[] {
  if (!property) {
    return [...new Set(array)];
  }
  
  const seen = new Set<any>();
  return array.filter(item => {
    const value = item[property];
    if (seen.has(value)) return false;
    seen.add(value);
    return true;
  });
}

/**
 * FORM STATE UTILITIES
 */

/**
 * Combine validation results
 * @param results - Array of validation results
 * @returns Combined validation result (fails if any fails)
 * 
 * @example
 * const combined = combineValidations([
 *   validateRequired(value),
 *   validateMaxLength(value, 50),
 *   validateEmail(value)
 * ]);
 */
export function combineValidations(
  results: IValidationResult[]
): IValidationResult {
  const isValid = results.every(r => r.isValid);
  const errors = results.filter(r => !r.isValid);
  
  return {
    isValid,
    errorMessage: errors[0]?.errorMessage,
    errorCode: errors[0]?.errorCode,
    metadata: {
      customValidations: errors.map(e => e.errorCode || e.errorMessage).filter((v): v is string => !!v),
    },
  };
}

/**
 * Check if field should display error
 * Shows error only if field was touched AND has error
 * @param isTouched - Field touched state
 * @param isDirty - Field dirty state
 * @param isValid - Validation state
 * @returns True if should show error
 */
export function shouldShowError(
  isTouched: boolean,
  isDirty: boolean,
  isValid: boolean
): boolean {
  return (isTouched || isDirty) && !isValid;
}

/**
 * ACCESSIBILITY UTILITIES
 */

/**
 * Generate unique ID for form field + label association
 * @param fieldName - Field name
 * @returns Unique ID string
 * 
 * @example
 * const id = generateFieldId('search-input');
 * // Result: "search-input-abc123xyz789"
 */
export function generateFieldId(fieldName: string): string {
  const timestamp = Date.now().toString(36);
  const random = Math.random().toString(36).substring(2, 9);
  return `${fieldName}-${timestamp}${random}`;
}

/**
 * Generate aria-describedby for error messages
 * @param fieldId - Field ID
 * @returns aria-describedby value
 */
export function generateAriaDescribedBy(fieldId: string): string {
  return `${fieldId}-error`;
}
