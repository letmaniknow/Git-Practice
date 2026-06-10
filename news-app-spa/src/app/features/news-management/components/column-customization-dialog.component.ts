import { Component, Inject, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { CdkDrag, CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import {
  ColumnDefinition,
  ColumnPreferences,
  DEFAULT_COLUMN_DEFINITIONS
} from '../models/column-config.model';
import { ColumnPreferencesService } from '../services/column-preferences.service';
import { CountVisiblePipe } from './count-visible.pipe';

interface ColumnToggle {
  column: ColumnDefinition;
  isVisible: boolean;
}

/**
 * Column Customization Dialog
 * 
 * Allows users to:
 * - Show/hide columns
 * - Reorder columns (drag & drop)
 * - Reset to defaults
 */
@Component({
  selector: 'app-column-customization-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrls: ['./column-customization-dialog.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatListModule,
    MatTooltipModule,
    MatDividerModule,
    MatFormFieldModule,
    MatInputModule,
    MatExpansionModule,
    MatSlideToggleModule,
    CountVisiblePipe,
  ],
  template: `
    <div class="column-customization-dialog">
      <!-- Header -->
      <div class="dialog-header">
        <h2 mat-dialog-title>
          <mat-icon class="header-icon">view_week</mat-icon>
          Customize Columns
        </h2>
        <button 
          mat-icon-button 
          (click)="onClose()"
          class="close-button"
          aria-label="Close dialog"
        >
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <!-- Content -->
      <mat-dialog-content class="dialog-content">
        <!-- Search Section -->
        <div class="search-section">
          <div class="search-container">
            <mat-form-field appearance="outline">
              <mat-label>Search columns...</mat-label>
              <input
                matInput
                [(ngModel)]="searchQuery"
                placeholder="Filter by name, tooltip, or ID"
                autocomplete="off"
              >
              <button
                mat-icon-button
                matSuffix
                (click)="clearSearch()"
                *ngIf="searchQuery"
                class="clear-search"
              >
                <mat-icon>clear</mat-icon>
              </button>
              <mat-icon matSuffix *ngIf="!searchQuery">search</mat-icon>
            </mat-form-field>
          </div>

          <div class="global-controls">
            <button
              mat-stroked-button
              (click)="selectAllVisible()"
              matTooltip="Select all visible columns"
            >
              <mat-icon>check_box</mat-icon>
              Select All
            </button>
            <button
              mat-stroked-button
              (click)="deselectAll()"
              matTooltip="Deselect all columns (except critical)"
            >
              <mat-icon>check_box_outline_blank</mat-icon>
              Deselect All
            </button>
            <button
              mat-stroked-button
              (click)="toggleAllGroups()"
              matTooltip="Expand/collapse all groups"
            >
              <mat-icon>{{ allExpanded ? 'unfold_less' : 'unfold_more' }}</mat-icon>
              {{ allExpanded ? 'Collapse' : 'Expand' }} All
            </button>
          </div>
        </div>

        <!-- Presets Section -->
        <div class="presets-section">
          <div class="preset-buttons">
            <button
              mat-stroked-button
              (click)="applyPreset('minimal')"
              matTooltip="Show only essential columns"
            >
              <mat-icon>minimize</mat-icon>
              Minimal
            </button>
            <button
              mat-stroked-button
              (click)="applyPreset('standard')"
              matTooltip="Show commonly used columns"
            >
              <mat-icon>list</mat-icon>
              Standard
            </button>
            <button
              mat-stroked-button
              (click)="applyPreset('detailed')"
              matTooltip="Show detailed view with metrics"
            >
              <mat-icon>analytics</mat-icon>
              Detailed
            </button>
            <button
              mat-stroked-button
              (click)="applyPreset('all')"
              matTooltip="Show all available columns"
            >
              <mat-icon>select_all</mat-icon>
              All
            </button>
          </div>
        </div>

        <!-- Info Section -->
        <div class="info-section">
          <p class="info-text">
            <mat-icon class="info-icon">info</mat-icon>
            Drag to reorder columns. Uncheck to hide. Critical columns cannot be hidden.
          </p>
        </div>

        <!-- Column Groups by Category -->
        <div class="columns-section">
          <mat-accordion [multi]="true" class="column-accordion">
            <mat-expansion-panel
              [expanded]="expandedGroups['identity']"
              (opened)="onGroupExpanded('identity', true)"
              (closed)="onGroupExpanded('identity', false)"
            >
              <mat-expansion-panel-header>
                <mat-panel-title>
                  <mat-icon class="group-icon">folder</mat-icon>
                  Identity & Discovery
                  <span class="group-stats">({{ getGroupVisibleCount('identity') }}/{{ getGroupTotalCount('identity') }})</span>
                </mat-panel-title>
                <mat-panel-description>
                  <div class="group-actions">
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('identity', true); $event.stopPropagation()"
                      [matTooltip]="'Select all in this group'"
                    >
                      <mat-icon>check_box</mat-icon>
                    </button>
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('identity', false); $event.stopPropagation()"
                      [matTooltip]="'Deselect all in this group'"
                    >
                      <mat-icon>check_box_outline_blank</mat-icon>
                    </button>
                  </div>
                </mat-panel-description>
              </mat-expansion-panel-header>

              <div class="group-content">
                <div
                  *ngFor="let item of getFilteredColumnsByGroup('identity')"
                  class="column-item"
                  [class.critical]="item.column.importance === 'critical'"
                  [class.important]="item.column.importance === 'important'"
                  [class.hidden-by-search]="!isColumnVisibleInSearch(item)"
                >
                  <div class="checkbox-container">
                    <mat-checkbox
                      [(ngModel)]="item.isVisible"
                      (change)="onColumnToggle(item.column.id)"
                      [disabled]="item.column.importance === 'critical'"
                      class="column-checkbox"
                    ></mat-checkbox>
                  </div>
                  <div class="column-info">
                    <div class="column-label">
                      <mat-icon *ngIf="item.column.icon" class="column-icon">{{ item.column.icon }}</mat-icon>
                      <span class="label-text">{{ item.column.label }}</span>
                      <span *ngIf="item.column.importance === 'critical'" class="badge badge-critical">Critical</span>
                      <span *ngIf="item.column.importance === 'important'" class="badge badge-important">Important</span>
                    </div>
                    <p *ngIf="item.column.tooltip" class="column-tooltip">{{ item.column.tooltip }}</p>
                  </div>
                  <div class="visibility-status">
                    <mat-icon [class.visible]="item.isVisible" [class.hidden]="!item.isVisible">
                      {{ item.isVisible ? 'visibility' : 'visibility_off' }}
                    </mat-icon>
                  </div>
                </div>
              </div>
            </mat-expansion-panel>

            <!-- GROUP 2: Publishing & Monetization -->
            <mat-expansion-panel
              [expanded]="expandedGroups['timeline']"
              (opened)="onGroupExpanded('timeline', true)"
              (closed)="onGroupExpanded('timeline', false)"
            >
              <mat-expansion-panel-header>
                <mat-panel-title>
                  <mat-icon class="group-icon">schedule</mat-icon>
                  Publishing & Monetization
                  <span class="group-stats">({{ getGroupVisibleCount('timeline') }}/{{ getGroupTotalCount('timeline') }})</span>
                </mat-panel-title>
                <mat-panel-description>
                  <div class="group-actions">
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('timeline', true); $event.stopPropagation()"
                      [matTooltip]="'Select all in this group'"
                    >
                      <mat-icon>check_box</mat-icon>
                    </button>
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('timeline', false); $event.stopPropagation()"
                      [matTooltip]="'Deselect all in this group'"
                    >
                      <mat-icon>check_box_outline_blank</mat-icon>
                    </button>
                  </div>
                </mat-panel-description>
              </mat-expansion-panel-header>

              <div class="group-content">
                <div
                  *ngFor="let item of getFilteredColumnsByGroup('timeline')"
                  class="column-item"
                  [class.critical]="item.column.importance === 'critical'"
                  [class.important]="item.column.importance === 'important'"
                  [class.hidden-by-search]="!isColumnVisibleInSearch(item)"
                >
                  <div class="checkbox-container">
                    <mat-checkbox
                      [(ngModel)]="item.isVisible"
                      (change)="onColumnToggle(item.column.id)"
                      [disabled]="item.column.importance === 'critical'"
                      class="column-checkbox"
                    ></mat-checkbox>
                  </div>
                  <div class="column-info">
                    <div class="column-label">
                      <mat-icon *ngIf="item.column.icon" class="column-icon">{{ item.column.icon }}</mat-icon>
                      <span class="label-text">{{ item.column.label }}</span>
                      <span *ngIf="item.column.importance === 'critical'" class="badge badge-critical">Critical</span>
                      <span *ngIf="item.column.importance === 'important'" class="badge badge-important">Important</span>
                    </div>
                    <p *ngIf="item.column.tooltip" class="column-tooltip">{{ item.column.tooltip }}</p>
                  </div>
                  <div class="visibility-status">
                    <mat-icon [class.visible]="item.isVisible" [class.hidden]="!item.isVisible">
                      {{ item.isVisible ? 'visibility' : 'visibility_off' }}
                    </mat-icon>
                  </div>
                </div>
              </div>
            </mat-expansion-panel>

            <!-- GROUP 3: Engagement & Status -->
            <mat-expansion-panel
              [expanded]="expandedGroups['engagement']"
              (opened)="onGroupExpanded('engagement', true)"
              (closed)="onGroupExpanded('engagement', false)"
            >
              <mat-expansion-panel-header>
                <mat-panel-title>
                  <mat-icon class="group-icon">trending_up</mat-icon>
                  Engagement & Status
                  <span class="group-stats">({{ getGroupVisibleCount('engagement') }}/{{ getGroupTotalCount('engagement') }})</span>
                </mat-panel-title>
                <mat-panel-description>
                  <div class="group-actions">
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('engagement', true); $event.stopPropagation()"
                      [matTooltip]="'Select all in this group'"
                    >
                      <mat-icon>check_box</mat-icon>
                    </button>
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('engagement', false); $event.stopPropagation()"
                      [matTooltip]="'Deselect all in this group'"
                    >
                      <mat-icon>check_box_outline_blank</mat-icon>
                    </button>
                  </div>
                </mat-panel-description>
              </mat-expansion-panel-header>

              <div class="group-content">
                <div
                  *ngFor="let item of getFilteredColumnsByGroup('engagement')"
                  class="column-item"
                  [class.critical]="item.column.importance === 'critical'"
                  [class.important]="item.column.importance === 'important'"
                  [class.hidden-by-search]="!isColumnVisibleInSearch(item)"
                >
                  <div class="checkbox-container">
                    <mat-checkbox
                      [(ngModel)]="item.isVisible"
                      (change)="onColumnToggle(item.column.id)"
                      [disabled]="item.column.importance === 'critical'"
                      class="column-checkbox"
                    ></mat-checkbox>
                  </div>
                  <div class="column-info">
                    <div class="column-label">
                      <mat-icon *ngIf="item.column.icon" class="column-icon">{{ item.column.icon }}</mat-icon>
                      <span class="label-text">{{ item.column.label }}</span>
                      <span *ngIf="item.column.importance === 'critical'" class="badge badge-critical">Critical</span>
                      <span *ngIf="item.column.importance === 'important'" class="badge badge-important">Important</span>
                    </div>
                    <p *ngIf="item.column.tooltip" class="column-tooltip">{{ item.column.tooltip }}</p>
                  </div>
                  <div class="visibility-status">
                    <mat-icon [class.visible]="item.isVisible" [class.hidden]="!item.isVisible">
                      {{ item.isVisible ? 'visibility' : 'visibility_off' }}
                    </mat-icon>
                  </div>
                </div>
              </div>
            </mat-expansion-panel>

            <!-- GROUP 4: Content Quality & Editorial -->
            <mat-expansion-panel
              [expanded]="expandedGroups['editorial']"
              (opened)="onGroupExpanded('editorial', true)"
              (closed)="onGroupExpanded('editorial', false)"
            >
              <mat-expansion-panel-header>
                <mat-panel-title>
                  <mat-icon class="group-icon">edit</mat-icon>
                  Content Quality & Editorial
                  <span class="group-stats">({{ getGroupVisibleCount('editorial') }}/{{ getGroupTotalCount('editorial') }})</span>
                </mat-panel-title>
                <mat-panel-description>
                  <div class="group-actions">
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('editorial', true); $event.stopPropagation()"
                      [matTooltip]="'Select all in this group'"
                    >
                      <mat-icon>check_box</mat-icon>
                    </button>
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('editorial', false); $event.stopPropagation()"
                      [matTooltip]="'Deselect all in this group'"
                    >
                      <mat-icon>check_box_outline_blank</mat-icon>
                    </button>
                  </div>
                </mat-panel-description>
              </mat-expansion-panel-header>

              <div class="group-content">
                <div
                  *ngFor="let item of getFilteredColumnsByGroup('editorial')"
                  class="column-item"
                  [class.critical]="item.column.importance === 'critical'"
                  [class.important]="item.column.importance === 'important'"
                  [class.hidden-by-search]="!isColumnVisibleInSearch(item)"
                >
                  <div class="checkbox-container">
                    <mat-checkbox
                      [(ngModel)]="item.isVisible"
                      (change)="onColumnToggle(item.column.id)"
                      [disabled]="item.column.importance === 'critical'"
                      class="column-checkbox"
                    ></mat-checkbox>
                  </div>
                  <div class="column-info">
                    <div class="column-label">
                      <mat-icon *ngIf="item.column.icon" class="column-icon">{{ item.column.icon }}</mat-icon>
                      <span class="label-text">{{ item.column.label }}</span>
                      <span *ngIf="item.column.importance === 'critical'" class="badge badge-critical">Critical</span>
                      <span *ngIf="item.column.importance === 'important'" class="badge badge-important">Important</span>
                    </div>
                    <p *ngIf="item.column.tooltip" class="column-tooltip">{{ item.column.tooltip }}</p>
                  </div>
                  <div class="visibility-status">
                    <mat-icon [class.visible]="item.isVisible" [class.hidden]="!item.isVisible">
                      {{ item.isVisible ? 'visibility' : 'visibility_off' }}
                    </mat-icon>
                  </div>
                </div>
              </div>
            </mat-expansion-panel>

            <!-- GROUP 5: Advanced & Geographic -->
            <mat-expansion-panel
              [expanded]="expandedGroups['advanced']"
              (opened)="onGroupExpanded('advanced', true)"
              (closed)="onGroupExpanded('advanced', false)"
            >
              <mat-expansion-panel-header>
                <mat-panel-title>
                  <mat-icon class="group-icon">admin_panel_settings</mat-icon>
                  Advanced & Geographic
                  <span class="group-stats">({{ getGroupVisibleCount('advanced') }}/{{ getGroupTotalCount('advanced') }})</span>
                </mat-panel-title>
                <mat-panel-description>
                  <div class="group-actions">
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('advanced', true); $event.stopPropagation()"
                      [matTooltip]="'Select all in this group'"
                    >
                      <mat-icon>check_box</mat-icon>
                    </button>
                    <button
                      mat-icon-button
                      (click)="selectGroupColumns('advanced', false); $event.stopPropagation()"
                      [matTooltip]="'Deselect all in this group'"
                    >
                      <mat-icon>check_box_outline_blank</mat-icon>
                    </button>
                  </div>
                </mat-panel-description>
              </mat-expansion-panel-header>

              <div class="group-content">
                <div
                  *ngFor="let item of getFilteredColumnsByGroup('advanced')"
                  class="column-item"
                  [class.critical]="item.column.importance === 'critical'"
                  [class.important]="item.column.importance === 'important'"
                  [class.hidden-by-search]="!isColumnVisibleInSearch(item)"
                >
                  <div class="checkbox-container">
                    <mat-checkbox
                      [(ngModel)]="item.isVisible"
                      (change)="onColumnToggle(item.column.id)"
                      [disabled]="item.column.importance === 'critical'"
                      class="column-checkbox"
                    ></mat-checkbox>
                  </div>
                  <div class="column-info">
                    <div class="column-label">
                      <mat-icon *ngIf="item.column.icon" class="column-icon">{{ item.column.icon }}</mat-icon>
                      <span class="label-text">{{ item.column.label }}</span>
                      <span *ngIf="item.column.importance === 'critical'" class="badge badge-critical">Critical</span>
                      <span *ngIf="item.column.importance === 'important'" class="badge badge-important">Important</span>
                    </div>
                    <p *ngIf="item.column.tooltip" class="column-tooltip">{{ item.column.tooltip }}</p>
                  </div>
                  <div class="visibility-status">
                    <mat-icon [class.visible]="item.isVisible" [class.hidden]="!item.isVisible">
                      {{ item.isVisible ? 'visibility' : 'visibility_off' }}
                    </mat-icon>
                  </div>
                </div>
              </div>
            </mat-expansion-panel>
          </mat-accordion>
        </div>

        <!-- Stats Section -->
        <div class="stats-section">
          <p class="stats-text">
            Showing <strong>{{ (columnToggles | countVisible) }} of {{ columnToggles.length }}</strong> columns
          </p>
        </div>
      </mat-dialog-content>

      <!-- Actions -->
      <mat-dialog-actions align="end" class="dialog-actions">
        <button 
          mat-stroked-button
          (click)="onResetToDefaults()"
          class="reset-button"
        >
          <mat-icon>refresh</mat-icon>
          Reset to Defaults
        </button>
        <button 
          mat-stroked-button 
          (click)="onClose()"
        >
          Cancel
        </button>
        <button 
          mat-raised-button 
          color="primary"
          (click)="onSave()"
        >
          <mat-icon>check</mat-icon>
          Apply Changes
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .column-customization-dialog {
      display: flex;
      flex-direction: column;
      height: 100%;
      max-height: 85vh;
      min-width: 500px;
      max-width: 700px;
      width: 90vw;
      border-radius: 12px;
      overflow: hidden;
      background: white;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    .dialog-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 24px;
      border-bottom: 1px solid #e0e0e0;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      position: relative;

      h2 {
        display: flex;
        align-items: center;
        gap: 12px;
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #1a1a1a;
      }

      .header-icon {
        color: #1976d2;
        font-size: 24px;
        width: 24px;
        height: 24px;
      }

      .close-button {
        color: #6c757d;
        transition: all 0.2s ease;
        border-radius: 8px;

        &:hover {
          background-color: rgba(108, 117, 125, 0.1);
          color: #495057;
          transform: scale(1.05);
        }
      }
    }

    .dialog-content {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
      padding: 20px;
      max-height: calc(85vh - 140px); /* Account for header and actions */
    }

    .info-section {
      margin-bottom: 16px;
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
      border-left: 4px solid #1976d2;

      .info-text {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0;
        font-size: 13px;
        color: #424242;

        .info-icon {
          font-size: 18px;
          width: 18px;
          height: 18px;
          color: #1976d2;
        }
      }
    }

    .columns-section {
      margin-bottom: 16px;
      display: flex;
      flex-direction: column;
      gap: 16px;

      .column-group {
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        background-color: #fafafa;
        overflow: hidden;

        .group-header {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 12px 16px;
          background-color: #f5f5f5;
          border-bottom: 2px solid #e0e0e0;

          .group-icon {
            font-size: 20px;
            width: 20px;
            height: 20px;
            color: #1976d2;
          }

          h3 {
            margin: 0;
            font-size: 14px;
            font-weight: 600;
            color: #212121;
          }
        }

        .group-content {
          display: flex;
          flex-direction: column;
          width: 100%;
          padding: 8px 0;

          .column-item {
            /* Basic flexbox layout without CDK interference */
            display: flex;
            flex-direction: row;
            align-items: center;
            padding: 12px 16px;
            border-bottom: 1px solid #f0f0f0;
            background-color: white;
            transition: background-color 0.2s ease-in-out;
            user-select: none;
            min-height: 56px;

            &:hover {
              background-color: #f9f9f9;
            }

            &:last-child {
              border-bottom: none;
            }

            &.critical {
              background-color: #f3e5f5;
              border-left: 3px solid #9c27b0;
            }

            &.important {
              background-color: #e3f2fd;
              border-left: 3px solid #1976d2;
            }

            &:hover {
              background-color: #f9f9f9 !important;
            }

            &:last-child {
              border-bottom: none !important;
            }

            &.drag-disabled {
              opacity: 0.7 !important;
              cursor: not-allowed !important;

              &:hover {
                background-color: white !important;
              }
            }

            /* Basic flexbox layout without CDK interference */

            .checkbox-container {
              flex: 0 0 32px;
              display: flex;
              align-items: center;
              justify-content: center;
              width: 32px;
              height: 32px;
            }

            .column-info {
              flex: 1;
              min-width: 0;
              margin: 0 12px;
              overflow: hidden;
              display: flex;
              flex-direction: column;

              .column-label {
                display: flex;
                align-items: center;
                font-weight: 500;
                color: #212121;
                margin-bottom: 2px;
                width: 100%;

                .column-icon {
                  font-size: 18px;
                  width: 18px;
                  height: 18px;
                  color: #1976d2;
                  flex-shrink: 0;
                  margin-right: 8px;
                }

                .label-text {
                  flex: 1;
                  min-width: 0;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  margin-right: 8px;
                }

                .badge {
                  display: inline-flex;
                  align-items: center;
                  justify-content: center;
                  padding: 2px 8px;
                  border-radius: 12px;
                  font-size: 11px;
                  font-weight: 600;
                  white-space: nowrap;
                  flex-shrink: 0;

                  &.badge-critical {
                    background-color: #f3e5f5;
                    color: #7b1fa2;
                  }

                  &.badge-important {
                    background-color: #e3f2fd;
                    color: #1565c0;
                  }
                }
              }

              .column-tooltip {
                margin: 4px 0 0 26px;
                font-size: 12px;
                color: #757575;
                line-height: 1.3;
              }
            }

            .visibility-status {
              flex: 0 0 32px;
              display: flex;
              align-items: center;
              justify-content: center;
              width: 32px;
              height: 32px;

              mat-icon {
                font-size: 18px;
                width: 18px;
                height: 18px;

                &.visible {
                  color: #4caf50;
                }

                &.hidden {
                  color: #bdbdbd;
                }
              }
            }
          }
        }
      }
    }

    .stats-section {
      padding: 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
      text-align: center;

      .stats-text {
        margin: 0;
        font-size: 13px;
        color: #424242;

        strong {
          color: #1976d2;
          font-weight: 600;
        }
      }
    }

    .dialog-actions {
      padding: 20px 24px;
      border-top: 1px solid #e0e0e0;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      position: relative;

      button {
        border-radius: 8px;
        font-weight: 500;
        transition: all 0.2s ease;
        min-height: 40px;

        mat-icon {
          margin-right: 6px;
        }

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        }
      }

      .reset-button {
        margin-right: auto;
        background-color: #dc3545;
        color: white;

        &:hover {
          background-color: #c82333;
        }
      }
    }

    @media (max-width: 768px) {
      .column-customization-dialog {
        min-width: 95vw;
        max-width: 95vw;
        width: 95vw;
        margin: 16px;
        max-height: 90vh;
      }

      .dialog-content {
        max-height: calc(90vh - 160px);
        padding: 16px;
      }

      .dialog-header {
        padding: 16px;

        h2 {
          font-size: 16px;
        }
      }

      .dialog-actions {
        padding: 16px;
        flex-wrap: wrap;
        gap: 8px;

        button {
          flex: 1;
          min-width: 120px;
        }
      }
    }

    @media (max-width: 480px) {
      .column-customization-dialog {
        min-width: 98vw;
        max-width: 98vw;
        width: 98vw;
        margin: 8px;
        max-height: 95vh;
      }

      .dialog-content {
        max-height: calc(95vh - 180px);
        padding: 12px;
      }

      .dialog-header {
        padding: 12px;

        h2 {
          font-size: 15px;
        }
      }

      .dialog-actions {
        padding: 12px;

        button {
          font-size: 13px;
          padding: 8px 16px;
        }
      }
    }

    @media (max-width: 480px) {
      .column-customization-dialog {
        min-width: 400px;
        max-width: 98vw;
        margin: 8px;
      }

      .dialog-content {
        max-height: 50vh;
      }

      .columns-section .column-accordion .column-item {
        flex-wrap: wrap;
        gap: 8px;
        align-items: flex-start;
        padding: 12px;
      }

      .column-item .column-info {
        flex-basis: 100% !important;
        order: 1 !important;
        margin-right: 0 !important;
        margin-bottom: 8px !important;
      }

      .column-item .visibility-status {
        order: 2 !important;
        margin-left: auto !important;
        width: 32px !important;
      }

      .column-item .checkbox-container {
        width: 32px !important;
      }
    }

    /* New styles for enhanced UX */
    .search-section {
      padding: 16px;
      border-bottom: 1px solid #e0e0e0;
      background-color: #fafafa;

      .search-container {
        position: relative;
        max-width: 400px;

        mat-form-field {
          width: 100%;
        }

        .clear-search {
          position: absolute;
          right: 8px;
          top: 50%;
          transform: translateY(-50%);
          color: #757575;
        }
      }

      .global-controls {
        display: flex;
        gap: 8px;
        margin-top: 12px;
        flex-wrap: wrap;

        button {
          font-size: 12px;
          padding: 4px 12px;
          min-height: 32px;

          mat-icon {
            font-size: 16px;
            width: 16px;
            height: 16px;
            margin-right: 4px;
          }
        }
      }
    }

    .presets-section {
      padding: 12px 16px;
      border-bottom: 1px solid #e0e0e0;
      background-color: #f8f9fa;

      .preset-buttons {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;

        button {
          font-size: 12px;
          padding: 6px 12px;
          min-height: 36px;
          border: 1px solid #e0e0e0;

          mat-icon {
            font-size: 16px;
            width: 16px;
            height: 16px;
            margin-right: 4px;
          }
        }
      }
    }

    .columns-section {
      .column-accordion {
        ::ng-deep {
          mat-accordion {
            .mat-expansion-panel {
              margin-bottom: 8px;
              border-radius: 8px;
              overflow: hidden;

              &.mat-expanded {
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
              }

              .mat-expansion-panel-header {
                padding: 12px 16px;
                background-color: #f8f9fa;
                border-bottom: 1px solid #e0e0e0;

                .mat-expansion-panel-header-title {
                  display: flex;
                  align-items: center;
                  gap: 8px;

                  .group-icon {
                    font-size: 20px;
                    width: 20px;
                    height: 20px;
                    color: #1976d2;
                  }

                  .group-stats {
                    font-size: 12px;
                    color: #757575;
                    font-weight: 500;
                  }
                }

                .mat-expansion-panel-header-description {
                  .group-actions {
                    display: flex;
                    gap: 4px;

                    button {
                      width: 32px;
                      height: 32px;
                      line-height: 32px;

                      mat-icon {
                        font-size: 18px;
                        width: 18px;
                        height: 18px;
                      }
                    }
                  }
                }
              }

              .mat-expansion-panel-body {
                padding: 0 !important;
                display: flex !important;
                flex-direction: column !important;
                
                .mat-expansion-panel-content {
                  display: flex !important;
                  flex-direction: column !important;
                  padding: 0 !important;
                }
              }
            }
          }
        }
      }
    }
  `],
})
export class ColumnCustomizationDialogComponent implements OnInit, OnDestroy {
  columnToggles: ColumnToggle[] = [];
  private destroy$ = new Subject<void>();

  // New properties for enhanced UX
  searchQuery = '';
  expandedGroups: { [key: string]: boolean } = {
    identity: true,
    timeline: true,
    engagement: true,
    editorial: false,
    advanced: false,
  };
  allExpanded = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { preferences: ColumnPreferences },
    private dialogRef: MatDialogRef<ColumnCustomizationDialogComponent>,
    private columnsService: ColumnPreferencesService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.initializeColumnToggles();
    console.log('✅ Column customization dialog opened');
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialize column toggles from current preferences
   */
  private initializeColumnToggles(): void {
    this.columnToggles = DEFAULT_COLUMN_DEFINITIONS.map(col => ({
      column: col,
      isVisible: this.columnsService.isColumnVisible(col.id),
    }));
  }

  /**
   * Handle column visibility toggle
   */
  onColumnToggle(columnId: string): void {
    const toggle = this.columnToggles.find(t => t.column.id === columnId);
    if (toggle) {
      console.log(
        `🔄 Toggled column '${columnId}' → ${toggle.isVisible ? 'visible' : 'hidden'}`
      );
    }
  }

  /**
   * Handle drag-drop reordering of columns
   */
  onColumnDrop(event: CdkDragDrop<any[]>): void {
    console.log('🔄 Drop event fired', {
      previousIndex: event.previousIndex,
      currentIndex: event.currentIndex,
      previousContainer: event.previousContainer === event.container,
      item: event.item.data?.column?.label
    });

    if (event.previousIndex === event.currentIndex) {
      console.log('ℹ️ Column was not moved (same position)');
      return;
    }

    // Reorder the columnToggles array
    moveItemInArray(this.columnToggles, event.previousIndex, event.currentIndex);

    console.log(
      `✅ Column moved from position ${event.previousIndex} to ${event.currentIndex}`
    );
    console.log(
      '📋 New order:',
      this.columnToggles.map(t => t.column.label).join(' → ')
    );

    // Force change detection for OnPush strategy
    this.cdr.markForCheck();
  }

  /**
   * Debug: Log when drag enters the drop list
   */
  onDropListEntered(event: any): void {
    console.log('📍 Drag entered drop list');
  }

  /**
   * Debug: Log when drag exits the drop list
   */
  onDropListExited(): void {
    console.log('📍 Drag exited drop list');
  }

  /**
   * Save changes and close dialog
   */
  onSave(): void {
    // Get new visible columns
    const newVisibleIds = this.columnToggles
      .filter(t => t.isVisible)
      .map(t => t.column.id);

    // Update order
    const newOrder = this.columnToggles.map(t => t.column.id);

    // Apply changes
    this.columnsService.setVisibleColumns(newVisibleIds);
    this.columnsService.reorderColumns(newOrder);

    console.log('✅ Column customization saved');
    this.dialogRef.close({ saved: true });
  }

  /**
   * Reset to defaults
   */
  onResetToDefaults(): void {
    if (
      confirm(
        'Are you sure you want to reset columns to default configuration?'
      )
    ) {
      this.columnsService.resetToDefaults();
      this.initializeColumnToggles();
      console.log('🔄 Columns reset to defaults');
    }
  }

  /**
   * Get columns filtered by group
   * Categories: identity, timeline, engagement, editorial, advanced
   */
  getColumnsByGroup(group: string): ColumnToggle[] {
    const groupMap: { [key: string]: string[] } = {
      identity: ['title', 'category', 'status', 'featured', 'thumbnail'],
      timeline: ['publishedAt', 'scheduledPublishAt', 'createdBy', 'active', 'sponsored', 'premium'],
      engagement: ['views', 'likes', 'comments', 'shares', 'bookmarks', 'replies', 'breaking'],
      editorial: ['wordCount', 'readTime', 'characterCount', 'readabilityScore', 'urgency', 'priority', 'sourceAgency', 'seriesId', 'seriesOrder', 'version'],
      advanced: ['date', 'updatedAt', 'country', 'region', 'city', 'badges', 'actions'],
    };

    const columnIds = groupMap[group] || [];
    return this.columnToggles.filter(t => columnIds.includes(t.column.id));
  }

  /**
   * Get filtered columns by group (with search)
   */
  getFilteredColumnsByGroup(group: string): ColumnToggle[] {
    const groupColumns = this.getColumnsByGroup(group);
    if (!this.searchQuery.trim()) {
      return groupColumns;
    }

    const query = this.searchQuery.toLowerCase();
    return groupColumns.filter(item =>
      item.column.label.toLowerCase().includes(query) ||
      item.column.tooltip?.toLowerCase().includes(query) ||
      item.column.id.toLowerCase().includes(query)
    );
  }

  /**
   * Check if column is visible in search results
   */
  isColumnVisibleInSearch(item: ColumnToggle): boolean {
    if (!this.searchQuery.trim()) {
      return true;
    }

    const query = this.searchQuery.toLowerCase();
    return item.column.label.toLowerCase().includes(query) ||
      item.column.tooltip?.toLowerCase().includes(query) ||
      item.column.id.toLowerCase().includes(query);
  }

  /**
   * Get visible count for a group
   */
  getGroupVisibleCount(group: string): number {
    return this.getColumnsByGroup(group).filter(t => t.isVisible).length;
  }

  /**
   * Get total count for a group
   */
  getGroupTotalCount(group: string): number {
    return this.getColumnsByGroup(group).length;
  }

  /**
   * Handle group expansion state change
   */
  onGroupExpanded(group: string, expanded: boolean): void {
    this.expandedGroups[group] = expanded;
  }

  /**
   * Select/deselect all columns in a group
   */
  selectGroupColumns(group: string, select: boolean): void {
    const groupColumns = this.getColumnsByGroup(group);
    groupColumns.forEach(item => {
      if (item.column.importance !== 'critical') {
        item.isVisible = select;
        this.onColumnToggle(item.column.id);
      }
    });
    this.cdr.markForCheck();
  }

  /**
   * Apply preset configurations
   */
  applyPreset(preset: 'minimal' | 'standard' | 'detailed' | 'all'): void {
    const presets: { [key: string]: string[] } = {
      minimal: ['title', 'category', 'status', 'publishedAt', 'createdBy', 'actions'],
      standard: ['title', 'category', 'status', 'publishedAt', 'createdBy', 'views', 'likes', 'comments', 'active', 'actions'],
      detailed: ['title', 'category', 'status', 'publishedAt', 'createdBy', 'views', 'likes', 'comments', 'shares', 'bookmarks', 'wordCount', 'readTime', 'active', 'featured', 'sponsored', 'premium', 'actions'],
      all: this.columnToggles.map(t => t.column.id),
    };

    const visibleIds = presets[preset] || [];
    this.columnToggles.forEach(toggle => {
      toggle.isVisible = visibleIds.includes(toggle.column.id);
      this.onColumnToggle(toggle.column.id);
    });

    console.log(`✅ Applied '${preset}' preset`);
    this.cdr.markForCheck();
  }

  /**
   * Toggle all groups expansion
   */
  toggleAllGroups(): void {
    this.allExpanded = !this.allExpanded;
    Object.keys(this.expandedGroups).forEach(group => {
      this.expandedGroups[group] = this.allExpanded;
    });
  }

  /**
   * Select all visible columns
   */
  selectAllVisible(): void {
    this.columnToggles.forEach(toggle => {
      if (toggle.column.importance !== 'critical') {
        toggle.isVisible = true;
        this.onColumnToggle(toggle.column.id);
      }
    });
    this.cdr.markForCheck();
  }

  /**
   * Deselect all columns (except critical)
   */
  deselectAll(): void {
    this.columnToggles.forEach(toggle => {
      if (toggle.column.importance !== 'critical') {
        toggle.isVisible = false;
        this.onColumnToggle(toggle.column.id);
      }
    });
    this.cdr.markForCheck();
  }

  /**
   * Clear search query
   */
  clearSearch(): void {
    this.searchQuery = '';
    this.cdr.markForCheck();
  }

  /**
   * Close without saving
   */
  onClose(): void {
    this.dialogRef.close({ saved: false });
  }
}
