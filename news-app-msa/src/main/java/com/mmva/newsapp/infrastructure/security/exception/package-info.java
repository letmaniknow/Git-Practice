/**
 * Security-specific exceptions for authentication and authorization failures.
 *
 * <p>
 * This package contains exceptions thrown during security operations:
 * </p>
 * <ul>
 * <li>{@link com.mmva.newsapp.infrastructure.security.exception.InvalidCredentialsException}
 * -
 * Thrown when login credentials are invalid</li>
 * </ul>
 *
 * <h2>Exception Handling</h2>
 * <p>
 * These exceptions are typically handled by:
 * </p>
 * <ul>
 * <li>GlobalExceptionHandler for API error responses</li>
 * <li>JwtAuthenticationEntryPoint for 401 responses</li>
 * <li>SecurityAccessDeniedHandler for 403 responses</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.security.exception;
