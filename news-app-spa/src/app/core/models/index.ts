/**
 * Core Models - Barrel Export
 * Single import point for all core models/interfaces
 * 
 * USAGE: instead of multiple imports
 * ✅ import { AppRole, IApiError, IErrorAlert } from '@app/core/models';
 * ❌ import { AppRole } from '@app/core/models/app-role.enum';
 * ❌ import { IApiError } from '@app/core/models/error.models';
 */

export * from './app-role.enum';
export * from './error.models';
