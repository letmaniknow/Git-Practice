import { Pipe, PipeTransform } from '@angular/core';

/**
 * Transforms enum values from UPPERCASE format to Title Case for display
 * Example: DRAFT → Draft, PUBLISHED → Published
 * 
 * Usage in templates: {{ status | enumDisplay }}
 */
@Pipe({
  name: 'enumDisplay',
  standalone: true
})
export class EnumDisplayPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    
    // Convert UPPERCASE_WITH_UNDERSCORES to Title Case
    return value
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }
}
