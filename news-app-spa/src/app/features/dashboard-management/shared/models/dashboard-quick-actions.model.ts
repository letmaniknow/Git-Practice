/**
 * Quick Action Model
 * Used across all dashboard features for navigation and quick actions
 * 
 * The disabled flag is set by role-based permission checks.
 * If disabled=true, the action button will be grayed out but still visible.
 */
export interface QuickAction {
  /** Unique identifier for the action */
  id: string;
  
  /** Display label for the button */
  label: string;
  
  /** Material Icon name to display */
  icon: string;
  
  /** Optional: button color (CSS color or var name) */
  color?: string;
  
  /** Optional: whether action is disabled due to role restrictions */
  disabled?: boolean;
}
