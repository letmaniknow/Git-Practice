import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import {
  ColumnDefinition,
  ColumnPreferences,
  DEFAULT_COLUMN_DEFINITIONS,
  getDefaultColumnPreferences,
} from '../models/column-config.model';

/**
 * Service to manage column customization preferences
 * 
 * Responsibilities:
 * 1. Load/save column preferences from localStorage
 * 2. Broadcast column changes to all subscribers
 * 3. Reset columns to defaults
 * 4. Validate and migrate preferences
 */
@Injectable({
  providedIn: 'root',
})
export class ColumnPreferencesService {
  private readonly STORAGE_KEY = 'news_management_column_preferences';
  private readonly COLUMN_DEFINITIONS_MAP = new Map(
    DEFAULT_COLUMN_DEFINITIONS.map(col => [col.id, col])
  );

  // BehaviorSubject to broadcast preference changes
  private preferencesSubject = new BehaviorSubject<ColumnPreferences>(
    this.loadPreferences()
  );
  public preferences$ = this.preferencesSubject.asObservable();

  // BehaviorSubject for column definitions
  private columnDefinitionsSubject = new BehaviorSubject<ColumnDefinition[]>(
    DEFAULT_COLUMN_DEFINITIONS
  );
  public columnDefinitions$ = this.columnDefinitionsSubject.asObservable();

  // Observable for visible columns only
  public visibleColumns$: Observable<ColumnDefinition[]> = new Observable(
    (observer) => {
      this.preferencesSubject.subscribe((prefs) => {
        const visibleCols = DEFAULT_COLUMN_DEFINITIONS.filter(col =>
          prefs.visibleColumnIds.includes(col.id)
        ).sort((a, b) => {
          const indexA = prefs.columnOrder.indexOf(a.id);
          const indexB = prefs.columnOrder.indexOf(b.id);
          return indexA - indexB;
        });
        observer.next(visibleCols);
      });
    }
  );

  constructor() {
    console.log('✅ ColumnPreferencesService initialized');
    console.log(
      '📊 Current preferences:',
      this.preferencesSubject.getValue()
    );
  }

  /**
   * Load preferences from localStorage or return defaults
   */
  private loadPreferences(): ColumnPreferences {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (!stored) {
        console.log('📂 No stored preferences found, using defaults');
        return getDefaultColumnPreferences();
      }

      const parsed = JSON.parse(stored) as ColumnPreferences;

      // Validate and migrate if needed
      if (!this.isValidPreferences(parsed)) {
        console.warn('⚠️ Invalid preferences, resetting to defaults');
        return getDefaultColumnPreferences();
      }

      // MIGRATION: Remove legacy/duplicate columns
      const legacyIds = ['publishedAt', 'scheduledPublishAt', 'author'];
      parsed.visibleColumnIds = parsed.visibleColumnIds.filter(id => !legacyIds.includes(id));
      parsed.columnOrder = parsed.columnOrder.filter(id => !legacyIds.includes(id));

      // Get fresh defaults to compare against
      const defaults = getDefaultColumnPreferences();
      // 1. ADD new columns that should be visible by default but aren't in preferences
      const newVisibleColumns = defaults.visibleColumnIds.filter(
        id => !parsed.visibleColumnIds.includes(id)
      );
      if (newVisibleColumns.length > 0) {
        console.log('✨ Adding new default visible columns:', newVisibleColumns);
        parsed.visibleColumnIds.push(...newVisibleColumns);
      }

      // 2. REMOVE columns that are no longer visible by default (e.g., Featured -> optional)
      const columnsToRemove = parsed.visibleColumnIds.filter(id => {
        const columnDef = this.COLUMN_DEFINITIONS_MAP.get(id);
        // Remove if: column exists AND defaultVisible is now false AND not critical
        return columnDef && !columnDef.defaultVisible && columnDef.importance !== 'critical';
      });
      if (columnsToRemove.length > 0) {
        console.log('🗑️ Removing columns that are no longer visible by default:', columnsToRemove);
        parsed.visibleColumnIds = parsed.visibleColumnIds.filter(
          id => !columnsToRemove.includes(id)
        );
      }

      // Ensure columnOrder includes all columns from defaults
      const newColumnIds = defaults.columnOrder.filter(
        id => !parsed.columnOrder.includes(id)
      );
      if (newColumnIds.length > 0) {
        console.log('✨ Adding new columns to order:', newColumnIds);
        parsed.columnOrder.push(...newColumnIds);
      }

      console.log('📂 Loaded preferences from localStorage');
      return parsed;
    } catch (error) {
      console.error('❌ Error loading preferences:', error);
      return getDefaultColumnPreferences();
    }
  }

  /**
   * Save preferences to localStorage
   */
  private savePreferences(prefs: ColumnPreferences): void {
    try {
      prefs.lastModified = new Date();
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(prefs));
      this.preferencesSubject.next(prefs);
      console.log('💾 Column preferences saved to localStorage');
    } catch (error) {
      console.error('❌ Error saving preferences:', error);
    }
  }

  /**
   * Validate preferences structure
   */
  private isValidPreferences(prefs: any): boolean {
    return (
      prefs &&
      Array.isArray(prefs.visibleColumnIds) &&
      Array.isArray(prefs.columnOrder) &&
      prefs.version === 1
    );
  }

  /**
   * Toggle column visibility
   */
  toggleColumnVisibility(columnId: string): void {
    const prefs = this.preferencesSubject.getValue();
    const index = prefs.visibleColumnIds.indexOf(columnId);

    if (index === -1) {
      prefs.visibleColumnIds.push(columnId);
      console.log(`✅ Column '${columnId}' is now visible`);
    } else {
      // Prevent hiding critical columns
      const column = this.COLUMN_DEFINITIONS_MAP.get(columnId);
      if (column?.importance === 'critical') {
        console.warn(`⚠️ Cannot hide critical column '${columnId}'`);
        return;
      }
      prefs.visibleColumnIds.splice(index, 1);
      console.log(`✅ Column '${columnId}' is now hidden`);
    }

    this.savePreferences(prefs);
  }

  /**
   * Get current visible columns
   */
  getVisibleColumns(): ColumnDefinition[] {
    const prefs = this.preferencesSubject.getValue();
    return DEFAULT_COLUMN_DEFINITIONS.filter(col =>
      prefs.visibleColumnIds.includes(col.id)
    );
  }

  /**
   * Check if column is currently visible
   */
  isColumnVisible(columnId: string): boolean {
    return this.preferencesSubject
      .getValue()
      .visibleColumnIds.includes(columnId);
  }

  /**
   * Reorder columns
   */
  reorderColumns(newOrder: string[]): void {
    const prefs = this.preferencesSubject.getValue();
    prefs.columnOrder = newOrder;
    this.savePreferences(prefs);
    console.log('✅ Column order updated:', newOrder);
  }

  /**
   * Reset to default preferences
   */
  resetToDefaults(): void {
    const defaults = getDefaultColumnPreferences();
    this.savePreferences(defaults);
    console.log('🔄 Column preferences reset to defaults');
  }

  /**
   * Export current preferences as JSON (for backup/sharing)
   */
  exportPreferences(): string {
    return JSON.stringify(this.preferencesSubject.getValue(), null, 2);
  }

  /**
   * Import preferences from JSON
   */
  importPreferences(jsonString: string): boolean {
    try {
      const imported = JSON.parse(jsonString) as ColumnPreferences;
      if (this.isValidPreferences(imported)) {
        this.savePreferences(imported);
        console.log('✅ Preferences imported successfully');
        return true;
      }
      console.warn('⚠️ Invalid import data');
      return false;
    } catch (error) {
      console.error('❌ Error importing preferences:', error);
      return false;
    }
  }

  /**
   * Get column definition by ID
   */
  getColumnDefinition(columnId: string): ColumnDefinition | undefined {
    return this.COLUMN_DEFINITIONS_MAP.get(columnId);
  }

  /**
   * Get all available columns
   */
  getAllColumnDefinitions(): ColumnDefinition[] {
    return DEFAULT_COLUMN_DEFINITIONS;
  }

  /**
   * Get only hideable columns
   */
  getHideableColumns(): ColumnDefinition[] {
    return DEFAULT_COLUMN_DEFINITIONS.filter(col => col.hideable);
  }

  /**
   * Get current preferences
   */
  getCurrentPreferences(): ColumnPreferences {
    return this.preferencesSubject.getValue();
  }

  /**
   * Bulk update column visibility
   */
  setVisibleColumns(columnIds: string[]): void {
    const prefs = this.preferencesSubject.getValue();

    // Ensure critical columns are always visible
    const criticalIds = DEFAULT_COLUMN_DEFINITIONS
      .filter(col => col.importance === 'critical')
      .map(col => col.id);

    const newVisibleIds = Array.from(new Set([...criticalIds, ...columnIds]));
    prefs.visibleColumnIds = newVisibleIds;

    this.savePreferences(prefs);
    console.log('✅ Visible columns updated:', newVisibleIds);
  }
}
