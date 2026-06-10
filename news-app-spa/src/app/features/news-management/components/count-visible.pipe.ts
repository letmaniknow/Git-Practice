import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe to count visible columns in the customization dialog
 */
@Pipe({
  name: 'countVisible',
  standalone: true,
  pure: true,
})
export class CountVisiblePipe implements PipeTransform {
  transform(items: any[] | null | undefined): number {
    if (!items) return 0;
    return items.filter(item => item.isVisible).length;
  }
}
