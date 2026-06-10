/**
 * Navigation Configuration Models
 * 
 * Defines the menu structure with support for hierarchical navigation,
 * nested items, and role-based access control.
 * 
 * @author Admin Portal Team
 * @since 1.0.0
 */

/**
 * Represents a single navigation menu item
 * 
 * Supports both:
 * - Simple links (route navigation)
 * - Expandable groups (nested menu items)
 */
export interface NavigationItem {
  /**
   * Unique identifier for the menu item
   * Used for tracking active state and analytics
   */
  id: string;

  /**
   * Display label for the menu item
   * Shown in sidebar when expanded
   */
  label: string;

  /**
   * Material Icon name
   * Reference: https://fonts.google.com/icons
   */
  icon: string;

  /**
   * Route path for navigation
   * Can be full path: '/news'
   * Or relative to parent: 'list'
   * Optional if children are provided (for group items)
   */
  route?: string;

  /**
   * Nested/child menu items
   * Transforms this item into an expandable group
   * When provided, route is optional
   */
  children?: NavigationItem[];

  /**
   * Whether this item should be displayed
   * Useful for feature flags and role-based access
   */
  visible?: boolean;

  /**
   * Whether this item can be expanded/collapsed
   * Defaults to true if children exist
   */
  expandable?: boolean;

  /**
   * Tooltip text shown on hover
   * Defaults to label if not provided
   */
  tooltip?: string;

  /**
   * Badge count displayed next to label
   * Useful for notifications, pending items, etc.
   */
  badge?: number;

  /**
   * Badge color/severity
   * 'primary' | 'accent' | 'warn' (Material Design theme palettes)
   */
  badgeColor?: 'primary' | 'accent' | 'warn';

  /**
   * Whether this item is currently expanded
   * Used for state management in sidebar
   */
  expanded?: boolean;

  /**
   * CSS class for custom styling
   * Useful for highlighting special sections
   */
  cssClass?: string;

  /**
   * Roles that can access this menu item
   * Empty array or undefined = accessible to all
   */
  requiredRoles?: string[];

  /**
   * Divider line after this item
   * Useful for grouping sections visually
   */
  showDivider?: boolean;
}

/**
 * Complete navigation configuration
 * Contains main menu items and special items (like settings)
 */
export interface NavigationConfig {
  /**
   * Primary navigation menu items
   */
  mainMenu: NavigationItem[];

  /**
   * Secondary menu items shown at bottom of sidebar
   * Typically settings, profile, help, etc.
   */
  bottomMenu: NavigationItem[];
}

/**
 * Route matching strategy for nested menu items
 */
export interface RouteMatch {
  /**
   * The matched navigation item
   */
  item: NavigationItem;

  /**
   * Whether this is an exact match (exact route)
   */
  exact: boolean;

  /**
   * Whether this is a partial match (parent route)
   */
  partial: boolean;

  /**
   * Breadcrumb path for this route
   * Example: [News Management, Scheduler]
   */
  breadcrumb: NavigationItem[];
}
