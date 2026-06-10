// ...existing imports and code...
import { MatDialog } from '@angular/material/dialog';
import { SuccessErrorDialogComponent } from 'src/app/shared/components/success-error-dialog/success-error-dialog.component';
import { UnifiedDatetimePickerDialogComponent } from 'src/app/shared/components/unified-datetime-picker/unified-datetime-picker-dialog.component';
import { NewsChangeStatusDialogComponent } from '../news-change-status-dialog.component';
import { NewsFormService } from '../../services/news-form.service';
import { NewsListService } from '../../services/news-list.service';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { RouterModule, Router } from '@angular/router';
import { NewsWorkflowBadgePipe } from './pipes/news-workflow-badge.pipe';


export interface GridSortEvent {
  columnId: string;
  direction: 'asc' | 'desc';
}

@Component({
  selector: 'app-news-list-table-grid',
  templateUrl: './news-list-table-grid.component.html',
  styleUrls: ['./news-list-table-grid.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatTooltipModule,
    MatMenuModule,
    MatDividerModule,
    MatChipsModule,
    RouterModule,
    NewsWorkflowBadgePipe
  ]
})
export class NewsListTableGridComponent {
  /**
   * Emitted after any backend-altering action (soft delete, restore, status change, etc.)
   * to request a table refresh in the parent. This is the industry-standard, future-proof pattern.
   */
  @Output() refreshRequested = new EventEmitter<void>();
  isScheduling = false;
  constructor(
    private router: Router,
    private dialog: MatDialog,
    private newsListService: NewsListService,
    private newsFormService: NewsFormService
  ) {}


  onChangeStatus(newsItem: any) {
    // Fetch workflow statuses from backend
    this.newsFormService.getWorkflowStatuses().subscribe({
      next: (statuses: string[]) => {
        const dialogRef = this.dialog.open(NewsChangeStatusDialogComponent, {
          width: '500px', // Consistent modal size
          data: {
            statuses,
            currentStatus: newsItem.newsWorkflowStatus
          }
        });
        dialogRef.afterClosed().subscribe((selectedStatus: string) => {
          if (!selectedStatus || selectedStatus === newsItem.newsWorkflowStatus) {
            return;
          }
          if (selectedStatus === 'SCHEDULED') {
            // Redirect to schedule/reschedule dialog
            this.onScheduleNews(newsItem);
            return;
          }
          // Call update workflow API
          this.newsListService
            .updateWorkflowStatus(newsItem.newsNewsId, selectedStatus)
            .subscribe({
              next: () => {
                this.dialog.open(SuccessErrorDialogComponent, {
                  data: {
                    type: 'success',
                    title: 'Status Changed',
                    message: `Status for <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b> changed to <b>${selectedStatus}</b> successfully.`
                  }
                });
                // Refresh the table by emitting sortChange (parent should reload data)
                this.sortChange.emit({ columnId: this.sortColumn || 'newsNewsId', direction: this.sortDirection || 'desc' });
                // Request parent to refresh table (future-proof, not tied to sort)
                this.refreshRequested.emit();
              },
              error: (err: any) => {
                this.dialog.open(SuccessErrorDialogComponent, {
                  data: {
                    type: 'error',
                    title: 'Status Change Failed',
                    message: `Failed to change status for <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b>.<br>${err?.userMessage || err?.message || 'Unknown error.'}`
                  }
                });
              }
            });
        });
      },
      error: (err) => {
        this.dialog.open(SuccessErrorDialogComponent, {
          data: {
            type: 'error',
            title: 'Load Statuses Failed',
            message: `Could not load workflow statuses.<br>${err?.userMessage || err?.message || 'Unknown error.'}`
          }
        });
      }
    });
  }

  onDeletePermanent(newsItem: any) {
    // Step 1: Open confirmation dialog (modern, professional, irreversible warning)
    // First confirmation dialog
    const dialogRef = this.dialog.open(SuccessErrorDialogComponent, {
      width: '420px',
      data: {
        type: 'warning',
        title: 'Permanently Delete News',
        message: `Are you sure you want to <b>permanently delete</b> <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'this news item'}</b>?<br><br><span style='color:red;font-weight:bold;'>This action cannot be undone.</span>`,
        okText: 'Delete Permanently',
        cancelText: 'Cancel',
        showCancel: true
      }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        // Second confirmation dialog
        const dialogRef2 = this.dialog.open(SuccessErrorDialogComponent, {
          width: '420px',
          data: {
            type: 'warning',
            title: 'Are you absolutely sure?',
            message: `This action <b>cannot be undone</b>. Are you absolutely sure you want to permanently delete <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'this news item'}</b>?`,
            okText: 'Yes, Delete Permanently',
            cancelText: 'Cancel',
            showCancel: true
          }
        });
        dialogRef2.afterClosed().subscribe((confirmed2: boolean) => {
          if (confirmed2) {
            // Step 2: Call backend to permanently delete
            this.newsListService.permanentDeleteNews(newsItem.newsNewsId).subscribe({
              next: () => {
                this.dialog.open(SuccessErrorDialogComponent, {
                  data: {
                    type: 'success',
                    title: 'News Permanently Deleted',
                    message: `News <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b> was permanently deleted and cannot be restored.`
                  }
                });
                // Request parent to refresh table (future-proof, not tied to sort)
                this.refreshRequested.emit();
              },
              error: (err: any) => {
                this.dialog.open(SuccessErrorDialogComponent, {
                  data: {
                    type: 'error',
                    title: 'Permanent Delete Failed',
                    message: `Failed to permanently delete <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b>.<br>${err?.userMessage || err?.message || 'Unknown error.'}`
                  }
                });
              }
            });
          }
        });
      }
    });
  }
    // --- Action menu handlers (stubs, to be implemented) ---
    onUnpublishNews(newsItem: any) {
      this.unpublishNews?.emit(newsItem);
    }
    @Output() unpublishNews = new EventEmitter<any>();

    @Output() scheduleNews = new EventEmitter<any>();
    onScheduleNews(newsItem: any) {
      this.scheduleNews.emit(newsItem);
    }

    onPreviewNews(newsItem: any) {
      // TODO: Implement preview logic
      // Open preview dialog or navigate as needed
    }

    onViewAuditTrail(newsItem: any): void {
      this.router.navigate(['/news', newsItem.newsNewsId, 'audit-trail']);
    }


    onSoftDeleteNews(newsItem: any) {
    // Step 1: Open confirmation dialog using SuccessErrorDialogComponent
    // First confirmation dialog
    const dialogRef = this.dialog.open(SuccessErrorDialogComponent, {
      width: '420px',
      data: {
        type: 'warning',
        title: 'Soft Delete News',
        message: `Are you sure you want to <b>soft delete</b> <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'this news item'}</b>?<br><br>This can be restored later from the trash.`,
        okText: 'Delete',
        cancelText: 'Cancel',
        showCancel: true
      }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        // Second confirmation dialog
        const dialogRef2 = this.dialog.open(SuccessErrorDialogComponent, {
          width: '420px',
          data: {
            type: 'warning',
            title: 'Are you absolutely sure?',
            message: `This can be restored later from the trash. Are you absolutely sure you want to <b>soft delete</b> <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'this news item'}</b>?`,
            okText: 'Yes, Soft Delete',
            cancelText: 'Cancel',
            showCancel: true
          }
        });
        dialogRef2.afterClosed().subscribe((confirmed2: boolean) => {
          if (confirmed2) {
            // Step 2: Call backend to soft delete
            this.newsListService.softDeleteNews(newsItem.newsNewsId).subscribe({
              next: () => {
                const dialogRef = this.dialog.open(SuccessErrorDialogComponent, {
                  data: {
                    type: 'success',
                    title: 'News Soft Deleted',
                    message: `News <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b> was soft deleted successfully.`,
                    okText: 'OK',
                    cancelText: 'Restore',
                    showCancel: true
                  }
                });
                dialogRef.afterClosed().subscribe((result: string) => {
                  if (result === 'cancel') {
                    // User clicked Restore
                    this.onRestoreNews(newsItem);
                  } else {
                    // Refresh the table by emitting sortChange (parent should reload data)
                    this.sortChange.emit({ columnId: this.sortColumn || 'newsNewsId', direction: this.sortDirection || 'desc' });
                    // Request parent to refresh table (future-proof, not tied to sort)
                    this.refreshRequested.emit();
                  }
                });
              },
              error: (err: any) => {
                this.dialog.open(SuccessErrorDialogComponent, {
                  data: {
                    type: 'error',
                    title: 'Soft Delete Failed',
                    message: `Failed to soft delete <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b>.<br>${err?.userMessage || err?.message || 'Unknown error.'}`
                  }
                });
              }
            });
          }
        });
      }
    });
    }

    onRestoreNews(newsItem: any) {
      // Step 1: Call backend to restore
      this.newsListService.restoreNews(newsItem.newsNewsId).subscribe({
        next: () => {
          this.dialog.open(SuccessErrorDialogComponent, {
            data: {
              type: 'success',
              title: 'News Restored',
              message: `News <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b> was restored successfully.`
            }
          });
          // Request parent to refresh table (future-proof, not tied to sort)
          this.refreshRequested.emit();
        },
        error: (err: any) => {
          this.dialog.open(SuccessErrorDialogComponent, {
            data: {
              type: 'error',
              title: 'Restore Failed',
              message: `Failed to restore <b>${newsItem.newsTitleEn || newsItem.newsTitleEs || 'News'}</b>.<br>${err?.userMessage || err?.message || 'Unknown error.'}`
            }
          });
        }
      });
    }
  @Output() cloneNews = new EventEmitter<any>();
  @Output() sortChange = new EventEmitter<GridSortEvent>();
  @Output() toggleRowSelection = new EventEmitter<string>();
  @Output() selectAllRows = new EventEmitter<void>();
  @Output() clearSelection = new EventEmitter<void>();

  @Input() sortColumn: string | null = null;
  @Input() sortDirection: 'asc' | 'desc' | null = null;
  @Input() isRowSelected: (id: string) => boolean = () => false;
  @Input() news: any[] = [];
  @Input() columnDefinitions: any[] = [];
  @Input() visibleColumnIds: string[] = [];
  @Input() getThumbnailUrl!: (item: any) => string;
  @Input() getCategoryNameById!: (id: any) => string;
  /**
   * GENERIC METHOD: Resolves admin user name by ID
   * Single unified method for all admin user name resolution (author, publisher, updater, scheduler, deleter)
   * Industry Standard: DRY principle - Don't Repeat Yourself
   */
  @Input() getAdminUserNameById!: (id: string | null) => string;
  @Input() trackByNewsId!: (index: number, item: any) => any;
  @Input() getTitle: (item: any) => string = () => '';
  @Input() getStatusConfig: (status: string) => any = () => undefined;
  @Input() onThumbnailError: (event: Event) => void = () => {};
  @Input() isAllSelected: boolean = false;

  // ...existing code...
  onPublishNews(newsItem: any) {
    // Emit event to parent for actual publish logic
    this.publishNews?.emit(newsItem);
  }
  @Output() publishNews = new EventEmitter<any>();

  navigateToView(id: string) {
    this.router.navigate(['/news', id]);
  }

  navigateToEdit(id: string) {
    this.router.navigate(['/news', id, 'edit']);
  }

  onCloneNews(newsItem: any) {
    this.cloneNews.emit(newsItem);
  }

  onHeaderClick(col: any) {
    if (!col || col.id === 'checkbox' || col.id === 'actions') return;
    let direction: 'asc' | 'desc' = 'asc';
    if (this.sortColumn === col.propertyPath) {
      direction = this.sortDirection === 'asc' ? 'desc' : 'asc';
    }
    this.sortChange.emit({ columnId: col.propertyPath, direction });
  }

  onSelectAllChange(event: Event) {
    const checked = (event.target && (event.target as HTMLInputElement).checked) || false;
    if (checked) {
      this.selectAllRows.emit();
    } else {
      this.clearSelection.emit();
    }
  }

  onRowCheckboxChange(event: Event, newsItem: any) {
    this.toggleRowSelection.emit(newsItem.newsNewsId);
  }

  get visibleColumns() {
    const forcedFirst = ['checkbox', 'thumbnail'];
    const forcedLast = ['actions'];
    const userOrder = this.visibleColumnIds.filter(id => !forcedFirst.includes(id) && !forcedLast.includes(id));
    const finalOrder = [...forcedFirst, ...userOrder, ...forcedLast];
    return finalOrder
      .map(id => this.columnDefinitions.find(col => col.id === id))
      .filter(Boolean);
  }

  get gridTemplateColumns(): string {
    return this.visibleColumns
      .map(col => col.id === 'actions' ? '180px' : (col.width || '1fr'))
      .join(' ');
  }
}
