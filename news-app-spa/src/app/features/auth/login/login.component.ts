import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService, LoginRequest } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

/**
 * 🔐 LOGIN COMPONENT
 * 
 * Handles user authentication for the admin portal.
 * Rendered inside LoginLayoutComponent which provides the fixed header.
 * 
 * Features:
 * - Email and password validation
 * - Loading state management with proper change detection
 * - Error handling and display
 * - Responsive form with Material Design 3 theme
 * 
 * Note: Uses OnPush change detection strategy for performance.
 * Manual change detection triggered via ChangeDetectorRef.markForCheck()
 * when internal state (loading, error) changes.
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  loginForm: FormGroup;
  error: string | null = null;
  loading = false;
  showPassword = false; // Industry standard: password visibility toggle

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.loginForm = this.fb.group({
      adminUsersUsernameOrEmail: ['', [Validators.required, Validators.email]],
      adminUsersPassword: ['', Validators.required]
    });
  }

  /**
   * Toggle password visibility (industry standard feature)
   * Improves UX by allowing users to preview their password before submitting
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Handle form submission
   * - Validates form data
   * - Calls authentication service
   * - Handles success/error responses
   * - Navigates to admin dashboard on success
   * - Properly manages loading and error states with change detection
   */
  onSubmit(): void {
    if (this.loginForm.invalid) return;
    
    this.loading = true;
    this.error = null;
    this.cdr.markForCheck();
    
    const credentials: LoginRequest = this.loginForm.value;
    
    this.authService.login(credentials).subscribe({
      next: () => {
        this.loading = false;
        this.cdr.markForCheck();
        this.router.navigate(['/dashboard']);
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err.error?.message || 'Login failed. Please try again.';
        this.cdr.markForCheck();
      }
    });
  }
}
