import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ads',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  template: `
    <mat-card>
      <h2>Ads & Monetization</h2>
      <p>Manage ads and monetization settings here.</p>
    </mat-card>
  `,
  styles: [
    `mat-card { margin: 24px; padding: 24px; }`
  ]
})
export class AdsComponent {}
