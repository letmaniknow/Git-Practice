/**
 * RBAC-specific exceptions for role and permission operations.
 *
 * <p>
 * This package contains all exceptions specific to the RBAC module:
 * </p>
 * <ul>
 * <li>{@link com.mmva.newsapp.infrastructure.rbac.exception.RoleNotFoundException} -
 * Thrown when a role cannot be found</li>
 * <li>{@link com.mmva.newsapp.infrastructure.rbac.exception.PermissionNotFoundException}
 * - Thrown when a permission cannot be found</li>
 * <li>{@link com.mmva.newsapp.infrastructure.rbac.exception.RbacUnauthorizedOperationException}
 * - Thrown when an unauthorized RBAC operation is attempted</li>
 * </ul>
 *
 * <h2>Exception Handling</h2>
 * <p>
 * These exceptions are handled by the global exception handler and return
 * appropriate
 * HTTP status codes:
 * </p>
 * <ul>
 * <li>NotFoundException variants → 404 Not Found</li>
 * <li>UnauthorizedOperationException → 403 Forbidden</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.rbac.exception;
