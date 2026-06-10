import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './confirmation-dialog.component.html',
  styleUrl: './confirmation-dialog.component.css'
})
export class ConfirmationDialogComponent {
  @Input() isOpen = false;
  @Input() title = 'Confirm Action';
  @Input() message = 'Are you sure you want to proceed?';
  @Input() confirmText = 'Confirm';
  @Input() cancelText = 'Cancel';
  @Input() type: 'danger' | 'warning' | 'info' = 'warning';
  @Input() isLoading = false;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    if (!this.isLoading) {
      this.confirm.emit();
    }
  }

  onCancel(): void {
    if (!this.isLoading) {
      this.cancel.emit();
    }
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget && !this.isLoading) {
      this.onCancel();
    }
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && !this.isLoading) {
      this.onCancel();
    } else if (event.key === 'Enter') {
      this.onConfirm();
    }
  }

  getIconForType(): string {
    switch (this.type) {
      case 'danger':
        return 'error';
      case 'warning':
        return 'warning';
      case 'info':
        return 'info';
      default:
        return 'warning';
    }
  }

  getStatusClass(): string {
    return `dialog-${this.type}`;
  }
}