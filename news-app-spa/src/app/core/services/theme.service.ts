/**
 * 🎨 THEME SERVICE - Runtime Theme Management
 *
 * Responsible for:
 * 1. Applying theme values to CSS variables at runtime
 * 2. Managing theme switching without page reload
 * 3. Persisting theme preferences to localStorage
 * 4. Broadcasting theme changes to all components
 *
 * Usage in components:
 * ```typescript
 * constructor(private themeService: ThemeService) {
 *   this.themeService.theme$.subscribe(theme => {
 *     // React to theme changes
 *   });
 * }
 * ```
 *
 * @location src/app/core/services/theme.service.ts
 * @version 1.0.0
 * @created 2026-04-03
 */

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { THEME_CONFIG, THEME_CSS_VARIABLES, ThemeConfig } from '../config/theme.config';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private currentTheme = new BehaviorSubject<ThemeConfig>(THEME_CONFIG);

  /**
   * Observable for components to subscribe to theme changes
   *
   * Example:
   * ```typescript
   * this.themeService.theme$.subscribe(theme => {
   *   console.log('Theme updated:', theme.colors.primary[500]);
   * });
   * ```
   */
  public theme$: Observable<ThemeConfig> = this.currentTheme.asObservable();

  /**
   * STORAGE KEY - for persisting theme to localStorage
   */
  private readonly STORAGE_KEY = 'app-theme-config';

  /**
   * PRIVATE: Load saved theme from localStorage
   *
   * Called on service initialization. If a saved theme exists,
   * it will be applied instead of the default.
   */
  private loadSavedTheme(): void {
    try {
      const saved = localStorage.getItem(this.STORAGE_KEY);
      if (saved) {
        const savedTheme = JSON.parse(saved);
        this.updateTheme(savedTheme);
        console.log('✅ Saved theme loaded from localStorage');
      }
    } catch (error) {
      console.warn(
        '⚠️ Failed to load saved theme, using default',
        error
      );
      this.resetTheme();
    }
  }

  constructor() {
    // Apply initial theme on service initialization
    this.applyThemeToCSSVariables(THEME_CONFIG);

    // Load saved theme if it exists
    this.loadSavedTheme();

    console.log('✅ ThemeService initialized with current theme');
  }

  /**
   * CRITICAL: Apply theme values to CSS variables
   *
   * This makes all theme tokens available throughout the app via CSS:
   * - var(--color-primary)
   * - var(--spacing-md)
   * - var(--border-radius-lg)
   * - etc.
   *
   * @param theme The theme configuration to apply
   */
  private applyThemeToCSSVariables(theme: ThemeConfig): void {
    const root = document.documentElement;

    // Apply all predefined CSS variables
    Object.entries(THEME_CSS_VARIABLES).forEach(([varName, varValue]) => {
      if (varValue !== undefined && varValue !== null) {
        root.style.setProperty(varName, varValue);
      }
    });

    // Apply all color values for Material components
    Object.entries(theme.colors.primary).forEach(([key, value]) => {
      root.style.setProperty(`--color-primary-${key}`, value);
    });

    Object.entries(theme.colors.error).forEach(([key, value]) => {
      root.style.setProperty(`--color-error-${key}`, value);
    });

    Object.entries(theme.colors.success).forEach(([key, value]) => {
      root.style.setProperty(`--color-success-${key}`, value);
    });

    Object.entries(theme.colors.gray).forEach(([key, value]) => {
      root.style.setProperty(`--color-gray-${key}`, value);
    });

    // Apply all spacing values
    Object.entries(theme.spacing).forEach(([key, value]) => {
      root.style.setProperty(`--spacing-${key}`, value);
    });

    // Apply all border radius values
    Object.entries(theme.borderRadius).forEach(([key, value]) => {
      root.style.setProperty(`--border-radius-${key}`, value);
    });

    // Apply all shadow values
    Object.entries(theme.shadows).forEach(([key, value]) => {
      root.style.setProperty(`--shadow-${key}`, value);
    });

    // Apply typography scale
    Object.entries(theme.typography.scale).forEach(([key, value]) => {
      root.style.setProperty(`--font-size-${key}`, value);
    });

    // Apply typography metadata
    root.style.setProperty('--font-family', theme.typography.fontFamily);

    // Apply icon sizing values
    Object.entries(theme.icons).forEach(([key, value]) => {
      root.style.setProperty(`--icon-${key}`, value);
    });

    // Apply overlay colors if theme includes them
    if (theme.overlays) {
      Object.entries(theme.overlays).forEach(([key, value]) => {
        root.style.setProperty(`--overlay-${key}`, value);
      });
    }

    console.log('✅ CSS variables applied from theme.config.ts');
  }

  /**
   * UPDATE THEME - Apply new theme and persist to localStorage
   *
   * Changes made here automatically update all components using
   * CSS variables, without requiring a page reload.
   *
   * Example - Change primary color:
   * ```typescript
   * const customTheme = { ...THEME_CONFIG };
   * customTheme.colors.primary[500] = '#ff0000'; // Red
   * this.themeService.updateTheme(customTheme);
   * ```
   *
   * @param newTheme Partial or complete theme configuration
   */
  public updateTheme(newTheme: Partial<ThemeConfig> | ThemeConfig): void {
    const mergedTheme = this.deepMerge(this.currentTheme.value, newTheme);
    this.currentTheme.next(mergedTheme);
    this.applyThemeToCSSVariables(mergedTheme);

    // Persist to localStorage
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(newTheme));

    console.log('✅ Theme updated and persisted to localStorage', mergedTheme);
  }

  /**
   * GET CURRENT THEME - Retrieve the active theme
   *
   * @returns The currently active theme configuration
   */
  public getCurrentTheme(): ThemeConfig {
    return this.currentTheme.value;
  }

  /**
   * GET COLOR - Get a specific color from current theme
   *
   * Example:
   * ```typescript
   * const primaryColor = this.themeService.getColor('primary', 500);
   * // Returns: '#3b82f6'
   * ```
   *
   * @param colorName The color palette name
   * @param shade The color shade (50, 100, 500, 700, 900)
   * @returns The hex color value
   */
  public getColor(
    colorName: keyof typeof THEME_CONFIG.colors,
    shade: number | string = 500
  ): string {
    const colors = this.currentTheme.value.colors;
    if (colorName === 'gray') {
      return (colors.gray as any)[shade] || colors.gray[900];
    }
    return (colors[colorName] as any)[shade] || colors.gray[900];
  }

  /**
   * GET SPACING - Get spacing value from current theme
   *
   * Example:
   * ```typescript
   * const padding = this.themeService.getSpacing('md');
   * // Returns: '16px'
   * ```
   *
   * @param spacing The spacing key (xs, sm, md, lg, xl)
   * @returns The spacing value (e.g., '16px')
   */
  public getSpacing(spacing: keyof typeof THEME_CONFIG.spacing): string {
    return this.currentTheme.value.spacing[spacing];
  }

  /**
   * GET COMPONENT VALUE - Get a specific component configuration value
   *
   * Example:
   * ```typescript
   * const rowHeight = this.themeService.getComponentValue('table-row-height');
   * // Returns: '60px'
   * ```
   *
   * @param componentKey The component configuration key
   * @returns The component value as string
   */
  public getComponentValue(componentKey: keyof typeof THEME_CONFIG.components): string {
    return this.currentTheme.value.components[componentKey];
  }
  /**
   * RESET THEME - Restore to production default theme
   *
   * Clears localStorage and reapplies THEME_CONFIG
   */
  public resetTheme(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.currentTheme.next(THEME_CONFIG);
    this.applyThemeToCSSVariables(THEME_CONFIG);

    console.log('✅ Theme reset to production defaults');
  }

  /**
   * PRIVATE: Deep merge objects for theme merging
   *
   * Recursively merges newConfig into baseConfig without
   * losing unspecified properties.
   *
   * @param baseConfig The base theme
   * @param newConfig The new values to merge
   * @returns Merged configuration
   */
  private deepMerge(baseConfig: any, newConfig: any): any {
    const result = { ...baseConfig };

    for (const key in newConfig) {
      if (newConfig[key] && typeof newConfig[key] === 'object' && !Array.isArray(newConfig[key])) {
        result[key] = this.deepMerge(result[key] || {}, newConfig[key]);
      } else {
        result[key] = newConfig[key];
      }
    }

    return result;
  }
}
