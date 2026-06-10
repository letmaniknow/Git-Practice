import { CanDeactivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * 💾 UNSAVED CHANGES GUARD
 * 
 * Interface for components that need unsaved changes protection
 * Components must implement canDeactivate() method
 */
export interface CanComponentDeactivate {
  canDeactivate: () => boolean | Observable<boolean>;
}

/**
 * Guard that prevents navigation away from a page with unsaved changes
 * Shows browser confirmation dialog when user tries to leave
 * 
 * Usage:
 * 1. Component implements CanComponentDeactivate interface
 * 2. Component's canDeactivate() returns false if form is dirty
 * 3. Add guard to route: canDeactivate: [unsavedChangesGuard]
 */
export const unsavedChangesGuard: CanDeactivateFn<CanComponentDeactivate> = (
  component
) => {
  // If component has unsaved changes, ask for confirmation
  if (component.canDeactivate && !component.canDeactivate()) {
    return confirm(
      '⚠️ You have unsaved changes!\n\n' +
      'Are you sure you want to leave? All changes will be lost.\n\n' +
      'Click "OK" to discard changes or "Cancel" to stay on the page.'
    );
  }
  
  // No unsaved changes or component doesn't implement guard - allow navigation
  return true;
};
