import { Injectable } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

/**
 * 🔐 AUTH GUARD
 * 
 * Protects routes that require authentication
 * - Checks if user has valid token
 * - Redirects to login if not authenticated
 * - Allows route activation only when authenticated
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  const hasToken = authService.getToken();
  
  if (!hasToken) {
    // No token found - redirect to login
    console.warn('⚠️ Auth Guard: No token found, redirecting to login');
    router.navigate(['/auth/login'], { 
      queryParams: { returnUrl: state.url } 
    });
    return false;
  }
  
  // Token exists - allow access
  return true;
};
