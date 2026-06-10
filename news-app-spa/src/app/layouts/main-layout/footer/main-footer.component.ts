import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  template: `<footer class="footer">
    <span>&copy; 2026 News Admin Portal</span>
  </footer>`,
  styles: [`.footer { padding: 16px; text-align: center; background: #f5f5f5; color: #666; }`]
})
export class FooterComponent {}
