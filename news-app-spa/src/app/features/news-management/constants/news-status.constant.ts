/**
 * News Workflow Status Constants & Types
 * 
 * ⚠️ All actual status configuration data is loaded from backend API at runtime.
 * This file contains only TypeScript types and utility functions.
 * 
 * DO NOT hardcode status values here - use NewsWorkflowStatusService instead.
 * 
 * @see NewsWorkflowStatusService
 * @see NEWS_API_ENDPOINTS.news.workflowStatuses
 * 
 * @author MMVA Team
 * @version 2.0 - Dynamic loading from API
 * @since 2026-04-14
 */

/**
 * Workflow Status Type - More flexible than union type
 * Allows backend to add new statuses without frontend code changes
 */
export type WorkflowStatus = string;

/**
 * Known workflow statuses as of last documentation update
 * 
 * ⚠️ This is for REFERENCE ONLY and documentation purposes.
 * Actual values are loaded from backend API at runtime.
 * Do NOT rely on this - always use NewsWorkflowStatusService
 * 
 * @see NewsWorkflowStatusService.loadStatuses()
 */
export const KNOWN_WORKFLOW_STATUSES = [
  'DRAFT',
  'SUBMITTED',
  'REVIEWED',
  'APPROVED',
  'SCHEDULED',
  'PUBLISHED',
  'ARCHIVED',
] as const;

/**
 * Format workflow status for display
 * 
 * Converts snake_case or uppercase to Title Case
 * 
 * @param status - Raw status string
 * @returns Formatted display label (English)
 */
export function formatWorkflowStatus(status: string): string {
  return status
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
}

/**
 * Format workflow status in Spanish
 * 
 * @param status - Raw status string
 * @returns Formatted display label (Spanish)
 */
export function formatWorkflowStatusEs(status: string): string {
  // Basic formatting - full translations come from backend
  return formatWorkflowStatus(status);
}
