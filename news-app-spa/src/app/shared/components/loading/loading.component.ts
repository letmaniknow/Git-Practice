import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './loading.component.html',
  styleUrl: './loading.component.css'
})
export class LoadingComponent {
  @Input() message = 'Loading...';
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() type: 'spinner' | 'dots' | 'pulse' = 'spinner';
  @Input() overlay = false;
  @Input() fullScreen = false;
}