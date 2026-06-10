import { Injectable } from '@angular/core';

/**
 * 🔐 JWT HELPER SERVICE
 * 
 * Utility service for JWT token operations:
 * - Decode JWT tokens without external dependencies
 * - Check token expiration
 * - Extract token payload
 */
@Injectable({ providedIn: 'root' })
export class JwtHelperService {
  /**
   * Decode JWT token and extract payload
   * @param token JWT token string
   * @returns Decoded payload object
   */
  decodeToken(token: string | null): any {
    if (!token) return null;

    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        console.warn('⚠️ Invalid JWT format (should have 3 parts)');
        return null;
      }

      // Decode the payload (second part)
      const payload = parts[1];
      const decoded = JSON.parse(this.decode(payload));
      return decoded;
    } catch (error) {
      console.error('❌ Failed to decode JWT:', error);
      return null;
    }
  }

  /**
   * Check if token is expired
   * @param token JWT token string
   * @param offsetSeconds Buffer time in seconds (default 60s = check expiry 60s before actual expiry)
   * @returns true if token is expired or will expire within offset
   */
  isTokenExpired(token: string | null, offsetSeconds: number = 60): boolean {
    if (!token) return true;

    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) {
      console.warn('⚠️ Token has no expiration (exp claim missing)');
      return true;
    }

    const now = Math.floor(Date.now() / 1000); // Current time in seconds
    const expiryTime = decoded.exp - offsetSeconds; // Subtract buffer

    const isExpired = now >= expiryTime;
    
    if (isExpired) {
      const secondsUntilActualExpiry = decoded.exp - now;
      console.warn(
        `⚠️ Token expired or expiring soon (${Math.max(0, secondsUntilActualExpiry)}s remaining)`
      );
    }

    return isExpired;
  }

  /**
   * Get remaining time in seconds until token expires
   * @param token JWT token string
   * @returns Seconds remaining (negative if already expired)
   */
  getTokenExpirationTime(token: string | null): number {
    if (!token) return -1;

    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) return -1;

    const now = Math.floor(Date.now() / 1000);
    return decoded.exp - now;
  }

  /**
   * Base64 URL decode
   * @param str Base64 URL encoded string
   * @returns Decoded string
   */
  private decode(str: string): string {
    let output = str.replace(/-/g, '+').replace(/_/g, '/');
    switch (output.length % 4) {
      case 0:
        break;
      case 2:
        output += '==';
        break;
      case 3:
        output += '=';
        break;
      default:
        throw new Error('Invalid base64url string');
    }
    return decodeURIComponent(atob(output).split('').map((c) => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
  }
}
