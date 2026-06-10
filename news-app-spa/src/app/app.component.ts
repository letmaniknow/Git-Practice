
import { Component } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ErrorAlertComponent } from './shared/components/error-alert/error-alert.component';
import { ErrorService, AppError } from './core/services/error.service';
import { ThemeService } from './core/services/theme.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, RouterModule, ErrorAlertComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  title = 'The US News';
  error: AppError | null = null;

  get errorTitle(): string {
    if (!this.error) return 'Error';
    if (typeof this.error.title === 'string' && this.error.title.trim())
      return this.error.title;
    return 'Error';
  }

  get errorIcon(): string {
    if (!this.error) return 'error';
    switch (this.error.type) {
      case 'warning':
        return 'warning';
      case 'info':
        return 'info';
      default:
        return 'error';
    }
  }

  constructor(private errorService: ErrorService, private themeService: ThemeService) {
    this.errorService.error$.subscribe((err) => {
      this.error = err;
    });
  }

  onDismissError() {
    this.errorService.clear();
  }
}
