import { Component, Input, Output, EventEmitter, ElementRef, AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

/**
 * 🚨 ERROR MESSAGE COMPONENT
 * 
 * Industry-standard error notification display with:
 * - Icon indication by error type (error, warning, info)
 * - Accessible alert roles and live regions
 * - Dismissible with close button
 * - Optional retry button
 * - Auto-dismiss for session errors
 * - Support for toast notifications (fixed position)
 * - Smooth animations (slide-in, fade-out)
 */
@Component({
  selector: 'app-error-message',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './error-message.component.html',
  styleUrl: './error-message.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErrorMessageComponent implements AfterViewInit, OnInit {
  @Input() title = 'Error';
  @Input() message = 'An error occurred';
  @Input() type: 'error' | 'warning' | 'info' = 'error';
  @Input() dismissible = true;
  @Input() showIcon = true;
  @Input() showRetry = false;
  @Input() retryText = 'Try Again';
  @Input() compact = false;
  @Input() position: 'inline' | 'toast' | undefined = 'inline';
  @Input() autoDismissMs: number | undefined = 0; // 0 = no auto-dismiss, otherwise ms

  @Output() dismiss = new EventEmitter<void>();
  @Output() retry = new EventEmitter<void>();

  private autoDismissTimer: any;

  constructor(private elRef: ElementRef, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    if (this.autoDismissMs && this.autoDismissMs > 0) {
      this.autoDismissTimer = setTimeout(() => {
        this.onDismiss();
      }, this.autoDismissMs);
    }
  }

  ngAfterViewInit() {
    // Focus the error message for accessibility
    if (this.elRef.nativeElement.tabIndex === -1) {
      this.elRef.nativeElement.focus();
    }
  }

  ngOnDestroy() {
    if (this.autoDismissTimer) {
      clearTimeout(this.autoDismissTimer);
    }
  }

  onDismiss(): void {
    this.dismiss.emit();
  }

  onRetry(): void {
    this.retry.emit();
  }

  getIconName(): string {
    switch (this.type) {
      case 'error':
        return 'error';
      case 'warning':
        return 'warning';
      case 'info':
        return 'info_outline';
      default:
        return 'error';
    }
  }

  getAriaRole(): string {
    switch (this.type) {
      case 'error':
        return 'alert';
      case 'warning':
        return 'status';
      case 'info':
        return 'status';
      default:
        return 'status';
    }
  }
}