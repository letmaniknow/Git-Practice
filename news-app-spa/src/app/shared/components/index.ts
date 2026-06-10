/**
 * Shared Components - Barrel Export
 * Single import point for all shared reusable components
 * 
 * USAGE: Clean imports for all components
 * ✅ import { ErrorAlertComponent, LoadingComponent } from '@app/shared/components';
 * ❌ import { ErrorAlertComponent } from '@app/shared/components/error-alert/error-alert.component';
 */

export { ErrorAlertComponent } from './error-alert/error-alert.component';
export { ErrorMessageComponent } from './error-message/error-message.component';
export { LoadingComponent } from './loading/loading.component';
