import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { NgIf } from '@angular/common';

@Component({
	selector: 'auth-header',
	standalone: true,
	imports: [MatIconModule, NgIf],
	template: `
		<header class="header">
			<div class="header-logo">
				<mat-icon>newspaper</mat-icon>
			</div>
			<span class="header-title">Admin Portal</span>
			<span class="header-spacer"></span>
		</header>
	`,
	styles: [
		`
		.header {
			display: flex;
			align-items: center;
			height: var(--header-height, 64px);
			background: var(--header-bg, #1976d2);
			color: var(--header-color, #fff);
			padding: var(--header-padding, 0 24px);
			font-family: var(--header-font-family, 'Roboto, Arial, sans-serif');
			font-size: var(--header-title-size, 1.25rem);
			font-weight: var(--header-title-weight, 700);
			border-radius: var(--header-border-radius, 0);
			box-shadow: var(--header-shadow, 0 2px 8px rgba(0,0,0,0.08));
			transition: var(--header-transition, background 0.3s, color 0.3s);
			z-index: var(--header-z-index, 101);
			margin-bottom: 32px;
			width: 100%;
			max-width: 480px;
			margin-left: auto;
			margin-right: auto;
		}
		.header-logo {
			display: flex;
			align-items: center;
			margin-right: var(--header-spacing, 16px);
			color: var(--header-color, #fff);
		}
		.header-title {
			font-size: var(--header-title-size, 1.25rem);
			font-weight: var(--header-title-weight, 700);
			color: var(--header-color, #fff);
			margin-left: var(--header-spacing, 16px);
		}
		.header-spacer {
			flex: 1;
			min-width: var(--header-spacer-min-width, 0);
		}
		mat-icon {
			font-size: var(--header-logo-size, 24px);
			color: var(--header-icon-color, #fff);
			transition: var(--header-icon-transition, color 0.3s);
		}
		`
	]
})
export class AuthHeaderComponent {}
