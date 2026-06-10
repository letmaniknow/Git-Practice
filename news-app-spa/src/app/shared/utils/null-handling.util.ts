/**
 * Null Handling Utility Service
 *
 * Provides standardized, consistent patterns for null/undefined checking
 * across the entire application. Eliminates inconsistent optional chaining,
 * loose equality checks, and array/object fallback patterns.
 *
 * USAGE:
 * ```typescript
 * import { NullHandling } from '@shared/utils/null-handling.util';
 *
 * // Safe value extraction with fallback
 * const title = NullHandling.getOrDefault(article?.title, 'Untitled');
 *
 * // Safe array extraction
 * const items = NullHandling.getArrayOrEmpty(response.data?.items);
 *
 * // Safe object extraction
 * const config = NullHandling.getObjectOrEmpty(response.config);
 *
 * // Nullability checks
 * if (NullHandling.isNullOrEmpty(searchTerm)) { ... }
 * if (NullHandling.hasValue(data)) { ... }
 * ```
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-04-26
 */
export class NullHandling {
    /**
     * Get value or return default fallback
     * Handles: null, undefined, empty string, NaN
     *
     * @param value - Value to check
     * @param defaultValue - Default fallback value
     * @returns Value if not null/undefined/empty, otherwise defaultValue
     *
     * @example
     * NullHandling.getOrDefault(user?.name, 'Unknown') // 'Unknown' if name is null
     * NullHandling.getOrDefault(0, 10) // 0 (zero is not "empty")
     * NullHandling.getOrDefault('', 'default') // 'default'
     */
    static getOrDefault<T>(value: T | null | undefined, defaultValue: T): T {
        // Special handling for different types
        if (value === null || value === undefined) {
            return defaultValue;
        }
        if (typeof value === 'string' && value.trim() === '') {
            return defaultValue;
        }
        return value;
    }

    /**
     * Get array or return empty array
     * Ensures safe iteration without null checks in templates
     *
     * @param value - Potential array value
     * @returns Array if valid, otherwise []
     *
     * @example
     * NullHandling.getArrayOrEmpty(response.items) // [] if items is null/undefined
     * *ngFor="let item of NullHandling.getArrayOrEmpty(articles)"
     */
    static getArrayOrEmpty<T>(value: T[] | null | undefined): T[] {
        return Array.isArray(value) && value.length > 0 ? value : [];
    }

    /**
     * Get object or return empty object
     * Safe property access without nested optional chaining
     *
     * @param value - Potential object value
     * @returns Object if valid, otherwise {}
     *
     * @example
     * NullHandling.getObjectOrEmpty(response.config)
     */
    static getObjectOrEmpty<T extends object>(value: T | null | undefined): T {
        return value && typeof value === 'object' && !Array.isArray(value) ? value : ({} as T);
    }

    /**
     * Check if value is null, undefined, or empty
     * Unified null-check pattern
     *
     * @param value - Value to check
     * @returns true if null/undefined/empty
     *
     * @example
     * if (NullHandling.isNullOrEmpty(searchTerm)) { showPlaceholder(); }
     */
    static isNullOrEmpty(value: any): boolean {
        if (value === null || value === undefined) {
            return true;
        }
        if (typeof value === 'string') {
            return value.trim() === '';
        }
        if (Array.isArray(value)) {
            return value.length === 0;
        }
        if (typeof value === 'object') {
            return Object.keys(value).length === 0;
        }
        return false;
    }

    /**
     * Check if value has content (opposite of isNullOrEmpty)
     *
     * @param value - Value to check
     * @returns true if value is not null/undefined/empty
     *
     * @example
     * if (NullHandling.hasValue(data)) { processData(data); }
     */
    static hasValue(value: any): boolean {
        return !this.isNullOrEmpty(value);
    }

    /**
     * Safe number extraction with fallback
     * Returns 0 for null/undefined, preserves actual zero values
     *
     * @param value - Potential numeric value
     * @param defaultValue - Default if null/undefined (default: 0)
     * @returns Number value or default
     *
     * @example
     * NullHandling.getNumberOrDefault(response?.total) // 0 if null
     * NullHandling.getNumberOrDefault(response?.count, 100) // 100 if null
     */
    static getNumberOrDefault(value: number | null | undefined, defaultValue: number = 0): number {
        return value !== null && value !== undefined && !isNaN(value) ? value : defaultValue;
    }

    /**
     * Safe boolean extraction with fallback
     *
     * @param value - Potential boolean value
     * @param defaultValue - Default if null/undefined (default: false)
     * @returns Boolean value or default
     *
     * @example
     * NullHandling.getBooleanOrDefault(user?.isActive) // false if null
     */
    static getBooleanOrDefault(value: boolean | null | undefined, defaultValue: boolean = false): boolean {
        return value !== null && value !== undefined ? value : defaultValue;
    }

    /**
     * Safe string extraction and trim
     *
     * @param value - Potential string value
     * @param defaultValue - Default if null/undefined/empty (default: '')
     * @returns Trimmed string or default
     *
     * @example
     * NullHandling.getStringOrDefault(user?.name, 'Unknown') // 'Unknown' if null
     */
    static getStringOrDefault(value: string | null | undefined, defaultValue: string = ''): string {
        if (typeof value === 'string') {
            const trimmed = value.trim();
            return trimmed.length > 0 ? trimmed : defaultValue;
        }
        return defaultValue;
    }

    /**
     * Safe deep property access
     * Navigate nested properties safely without optional chaining cascade
     *
     * @param obj - Object to traverse
     * @param path - Property path separated by dots (e.g., 'user.profile.name')
     * @param defaultValue - Default if path doesn't exist
     * @returns Value at path or defaultValue
     *
     * @example
     * NullHandling.safeGet(response, 'data.user.profile.name', 'Unknown')
     */
    static safeGet(obj: any, path: string, defaultValue: any = null): any {
        try {
            const value = path.split('.').reduce((acc, part) => acc?.[part], obj);
            return value !== undefined ? value : defaultValue;
        } catch {
            return defaultValue;
        }
    }

    /**
     * Coalesce multiple values - return first non-null value
     *
     * @param values - Values to check
     * @returns First non-null/non-empty value, or null
     *
     * @example
     * NullHandling.coalesce(user?.nickname, user?.firstName, 'Anonymous')
     */
    static coalesce<T>(...values: (T | null | undefined)[]): T | null {
        for (const value of values) {
            if (this.hasValue(value)) {
                return value as T;
            }
        }
        return null;
    }
}
