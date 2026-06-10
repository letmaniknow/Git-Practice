import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  template: `
    <mat-card>
      <h2>Settings</h2>
      <p>Configure portal settings here.</p>
    </mat-card>
  `,
  styles: [
    `mat-card { margin: 24px; padding: 24px; }`
  ]
})
export class SettingsComponent {}
