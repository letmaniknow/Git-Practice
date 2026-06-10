import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  template: `
    <mat-card>
      <h2>User Management</h2>
      <p>Manage app users here.</p>
    </mat-card>
  `,
  styles: [
    `mat-card { margin: 24px; padding: 24px; }`
  ]
})
export class UsersComponent {}
