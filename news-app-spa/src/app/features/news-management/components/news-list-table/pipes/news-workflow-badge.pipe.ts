import { Pipe, PipeTransform } from '@angular/core';
import { WorkflowStatusConfig, NewsWorkflowStatusService } from '../../../services/news-workflow-status.service';

/**
 * Workflow Status Badge Pipe
 * 
 * Transforms workflow status values into badge configuration objects.
 * Provides consistent styling information for status badges across the application.
 * 
 * Usage in template:
 * {{ article.newsWorkflowStatus | newsWorkflowBadge | json }}
 * 
 * Or access individual properties:
 * {{ (article.newsWorkflowStatus | newsWorkflowBadge).label }}
 * 
 * @author MMVA Team
 * @version 1.0
 * @since 2026-04-14
 */
@Pipe({
  name: 'newsWorkflowBadge',
  standalone: true,
  pure: true,
})
export class NewsWorkflowBadgePipe implements PipeTransform {
  constructor(private statusService: NewsWorkflowStatusService) {}

  /**
   * Transform workflow status to badge configuration
   * 
   * @param status - Workflow status value (DRAFT, PUBLISHED, etc.)
   * @returns Badge configuration object with styling and metadata
   */
  transform(status: string | null | undefined): WorkflowStatusConfig {
    return this.statusService.getStatusConfig(status ?? '');
  }
}
