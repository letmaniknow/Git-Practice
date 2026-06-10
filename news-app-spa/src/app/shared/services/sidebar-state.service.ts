/**
 * Sidebar State Service
 * 
 * Manages sidebar collapse/expand state and nested menu accordion behavior.
 * Persists state to localStorage for user preference.
 * 
 * Features:
 * - Collapsed/expanded state management
 * - Accordion behavior for nested items (only one open per domain)
 * - localStorage persistence
 * - Observable streams for reactive updates
 * 
 * @author Admin Portal Team
 * @since 1.0.0
 */

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SidebarStateService {
  private readonly COLLAPSED_KEY = 'sidebar_collapsed';
  private readonly EXPANDED_ITEMS_KEY = 'sidebar_expanded_items';

  // Collapsed state
  private collapsedSubject = new BehaviorSubject<boolean>(this.getInitialCollapsedState());
  public collapsed$ = this.collapsedSubject.asObservable();

  // Expanded nested items (one per parent domain)
  // Map<parentId, expandedChildId>
  private expandedItemsSubject = new BehaviorSubject<Map<string, string>>(
    this.getInitialExpandedItems()
  );
  public expandedItems$ = this.expandedItemsSubject.asObservable();

  constructor() {}

  /**
   * Toggle sidebar collapsed/expanded state
   */
  toggleCollapsed(): void {
    const newState = !this.collapsedSubject.value;
    this.collapsedSubject.next(newState);
    this.persistCollapsedState(newState);
  }

  /**
   * Set collapsed state directly
   */
  setCollapsed(collapsed: boolean): void {
    this.collapsedSubject.next(collapsed);
    this.persistCollapsedState(collapsed);
  }

  /**
   * Get current collapsed state
   */
  get isCollapsed(): boolean {
    return this.collapsedSubject.value;
  }

  /**
   * Toggle nested item expansion (accordion behavior)
   * When one item in a parent is expanded, others close
   */
  toggleNestedItem(parentId: string, childId: string): void {
    const current = this.expandedItemsSubject.value;
    const newExpanded = new Map(current);

    // If already expanded, close it
    if (newExpanded.get(parentId) === childId) {
      newExpanded.delete(parentId);
    } else {
      // Open new one, close old one
      newExpanded.set(parentId, childId);
    }

    this.expandedItemsSubject.next(newExpanded);
    this.persistExpandedItems(newExpanded);
  }

  /**
   * Check if nested item is expanded
   */
  isNestedItemExpanded(parentId: string, childId: string): boolean {
    return this.expandedItemsSubject.value.get(parentId) === childId;
  }

  /**
   * Close all nested items under a parent
   */
  closeNestedItems(parentId: string): void {
    const current = this.expandedItemsSubject.value;
    if (current.has(parentId)) {
      const newExpanded = new Map(current);
      newExpanded.delete(parentId);
      this.expandedItemsSubject.next(newExpanded);
      this.persistExpandedItems(newExpanded);
    }
  }

  /**
   * Get initial collapsed state from localStorage or default
   */
  private getInitialCollapsedState(): boolean {
    try {
      const stored = localStorage.getItem(this.COLLAPSED_KEY);
      return stored ? JSON.parse(stored) : true; // Default: collapsed
    } catch {
      return true;
    }
  }

  /**
   * Get initial expanded items from localStorage
   */
  private getInitialExpandedItems(): Map<string, string> {
    try {
      const stored = localStorage.getItem(this.EXPANDED_ITEMS_KEY);
      if (stored) {
        const data = JSON.parse(stored) as Array<[string, string]>;
        return new Map(data);
      }
    } catch {
      // Ignore parse errors
    }
    return new Map();
  }

  /**
   * Persist collapsed state to localStorage
   */
  private persistCollapsedState(collapsed: boolean): void {
    try {
      localStorage.setItem(this.COLLAPSED_KEY, JSON.stringify(collapsed));
    } catch {
      console.warn('Failed to persist sidebar collapsed state');
    }
  }

  /**
   * Persist expanded items to localStorage
   */
  private persistExpandedItems(items: Map<string, string>): void {
    try {
      const data = Array.from(items.entries());
      localStorage.setItem(this.EXPANDED_ITEMS_KEY, JSON.stringify(data));
    } catch {
      console.warn('Failed to persist sidebar expanded items');
    }
  }

  /**
   * Reset all state to defaults
   */
  reset(): void {
    this.setCollapsed(true);
    this.expandedItemsSubject.next(new Map());
    localStorage.removeItem(this.COLLAPSED_KEY);
    localStorage.removeItem(this.EXPANDED_ITEMS_KEY);
  }
}
