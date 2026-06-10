import { Component } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [MatToolbarModule, MatButtonModule],
  template: `<mat-toolbar color="accent">
    <span>Quick Actions</span>
    <button mat-button>Refresh</button>
    <button mat-button>Settings</button>
  </mat-toolbar>`,
  styles: [``]
})
export class ToolbarComponent {}
