/**
 * NewsBulkActionsToolbarComponent - Bulk Selection & Actions
 * 
 * Responsibility: Manages bulk selection controls and action buttons
 * - Select All / Clear Selection checkbox
 * - Display count of selected items
 * - Bulk action buttons (Delete, Publish, Unpublish)
 * - Hidden when no items are selected
 * 
 * Communication: Pure Input/Output pattern (no Store injection)
 * - Receives: selectedCount, totalCount, isLoading
 * - Emits: selectAllRequested, clearSelectionRequested, bulkSoftDeleteRequested, etc.
 * 
 * Design: Appears between filters and list when user starts selecting items
 * Phase 4.2 Ready: All action buttons pre-built and functional
 * 
 * Future Phase 4.2 Integration:
 * - Wire bulkSoftDeleteRequested → Show confirmation dialog → Call soft delete API
 * - Wire bulkPublishRequested → Call publish API
 * - Wire bulkUnpublishRequested → Call unpublish API
 * - Wire bulkChangeStatusRequested → Show status selector → Call API
 */

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';

/**
 * Interface: Bulk action event payload
 * Clear which action was triggered and how many items affected
 */
export interface IBulkActionRequestedEvent {
  actionType: 'softDelete' | 'publish' | 'unpublish' | 'changeStatus';
  selectedCount: number;
  timestamp: Date;
}

@Component({
  selector: 'app-news-bulk-actions-toolbar',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
  ],
  templateUrl: './news-bulk-actions-toolbar.component.html',
})
export class NewsBulkActionsToolbarComponent {
  // ============================================================================
  // INPUT: Receive selection state from parent
  // ============================================================================

  /** Number of currently selected items */
  @Input() selectedNewsIdsCount: number = 0;

  /** Total number of items in list */
  @Input() totalNewsItemsCount: number = 0;

  /** Is any async action currently in progress? */
  @Input() isActionInProgress: boolean = false;

  // ============================================================================
  // OUTPUT: Emit when user triggers actions
  // ============================================================================

  /** User clicked "Select All" or toggled checkbox to select all */
  @Output() selectAllRequested = new EventEmitter<void>();

  /** User clicked "Clear Selection" or unchecked select-all checkbox */
  @Output() clearSelectionRequested = new EventEmitter<void>();

  /** User clicked "Delete Selected" button */
  @Output() bulkSoftDeleteRequested = new EventEmitter<IBulkActionRequestedEvent>();

  /** User clicked "Publish Selected" button */
  @Output() bulkPublishRequested = new EventEmitter<IBulkActionRequestedEvent>();

  /** User clicked "Unpublish Selected" button */
  @Output() bulkUnpublishRequested = new EventEmitter<IBulkActionRequestedEvent>();

  /** User clicked "Change Status" and selected new status */
  @Output() bulkChangeStatusRequested = new EventEmitter<{ newStatus: string; selectedCount: number }>();

  // ============================================================================
  // Component State
  // ============================================================================

  /** Show toolbar only if items are selected, hide otherwise */
  get shouldShowToolbar(): boolean {
    return this.selectedNewsIdsCount > 0;
  }

  /** Calculate if "Select All" checkbox should be checked */
  get isSelectAllChecked(): boolean {
    return this.selectedNewsIdsCount === this.totalNewsItemsCount && this.totalNewsItemsCount > 0;
  }

  /** Calculate if "Select All" checkbox should be indeterminate (some selected, not all) */
  get isSelectAllIndeterminate(): boolean {
    return this.selectedNewsIdsCount > 0 && this.selectedNewsIdsCount < this.totalNewsItemsCount;
  }

  /** Get percentage of items selected for visual indicator */
  get selectionPercentage(): number {
    if (this.totalNewsItemsCount === 0) return 0;
    return Math.round((this.selectedNewsIdsCount / this.totalNewsItemsCount) * 100);
  }

  // ============================================================================
  // Selection Handler Methods
  // ============================================================================

  /**
   * User toggled "Select All" checkbox
   * Determine if they want to select or deselect based on current state
   */
  onSelectAllToggled(isChecked: boolean): void {
    if (isChecked || (this.isSelectAllIndeterminate && !isChecked)) {
      // Select all items
      this.selectAllRequested.emit();
    } else {
      // Deselect all items
      this.clearSelectionRequested.emit();
    }
  }

  /**
   * User clicked "Clear Selection" button explicitly
   */
  onClearSelectionButtonClicked(): void {
    this.clearSelectionRequested.emit();
  }

  // ============================================================================
  // Bulk Action Handler Methods
  // ============================================================================

  /**
   * User clicked "Soft Delete Selected" button
   * Emits event with full details for parent to handle confirmation
   * This is a soft delete (recoverable) operation
   */
  onBulkSoftDeleteNewsButtonClicked(): void {
    if (this.selectedNewsIdsCount === 0 || this.isActionInProgress) {
      return; // Guard: no items selected or action in progress
    }

    const event: IBulkActionRequestedEvent = {
      actionType: 'softDelete',
      selectedCount: this.selectedNewsIdsCount,
      timestamp: new Date(),
    };
    this.bulkSoftDeleteRequested.emit(event);
  }

  /**
   * User clicked "Publish Selected" button
   * Parent will handle logic to publish without confirmation (publish is safe)
   */
  onBulkPublishButtonClicked(): void {
    if (this.selectedNewsIdsCount === 0 || this.isActionInProgress) {
      return;
    }

    const event: IBulkActionRequestedEvent = {
      actionType: 'publish',
      selectedCount: this.selectedNewsIdsCount,
      timestamp: new Date(),
    };
    this.bulkPublishRequested.emit(event);
  }

  /**
   * User clicked "Unpublish Selected" button
   * Parent will handle logic to unpublish
   */
  onBulkUnpublishButtonClicked(): void {
    if (this.selectedNewsIdsCount === 0 || this.isActionInProgress) {
      return;
    }

    const event: IBulkActionRequestedEvent = {
      actionType: 'unpublish',
      selectedCount: this.selectedNewsIdsCount,
      timestamp: new Date(),
    };
    this.bulkUnpublishRequested.emit(event);
  }

  /**
   * User clicked "Change Status" menu item
   * Parent will show status selector dialog
   */
  onBulkChangeStatusRequested(): void {
    if (this.selectedNewsIdsCount === 0 || this.isActionInProgress) {
      return;
    }

    // Emit with placeholder, parent will handle status selection
    this.bulkChangeStatusRequested.emit({
      newStatus: '', // Parent shows dialog to select status
      selectedCount: this.selectedNewsIdsCount,
    });
  }

  // ============================================================================
  // Utility Methods
  // ============================================================================

  /**
   * Get human-readable description of current selection
   * Used in template for clarity
   */
  getSelectionDescription(): string {
    if (this.selectedNewsIdsCount === this.totalNewsItemsCount) {
      return `All ${this.totalNewsItemsCount} items selected`;
    }
    return `${this.selectedNewsIdsCount} of ${this.totalNewsItemsCount} items selected`;
  }

  /**
   * Get tooltip text for "Delete Selected" button
   * Provides user confirmation of what will happen
   */
  getDeleteButtonTooltip(): string {
    if (this.selectedNewsIdsCount === 0) {
      return 'No items selected';
    }
    return `Permanently delete ${this.selectedNewsIdsCount} news`;
  }

  /**
   * Get tooltip text for "Publish Selected" button
   */
  getPublishButtonTooltip(): string {
    if (this.selectedNewsIdsCount === 0) {
      return 'No items selected';
    }
    return `Publish ${this.selectedNewsIdsCount} news`;
  }

  /**
   * Get tooltip text for "Unpublish Selected" button
   */
  getUnpublishButtonTooltip(): string {
    if (this.selectedNewsIdsCount === 0) {
      return 'No items selected';
    }
    return `Unpublish ${this.selectedNewsIdsCount} news`;
  }
}
