/**
 * Role-Based Access Control (RBAC) module for managing roles and permissions.
 *
 * <p>
 * This infrastructure module provides comprehensive RBAC functionality
 * including:
 * </p>
 * <ul>
 * <li><b>Role Management:</b> Create, update, delete, and clone roles</li>
 * <li><b>Permission Management:</b> Define and manage granular permissions</li>
 * <li><b>Role-Permission Assignment:</b> Assign/revoke permissions to
 * roles</li>
 * <li><b>Audit Logging:</b> Track all RBAC operations for compliance</li>
 * </ul>
 *
 * <h2>Package Structure</h2>
 * 
 * <pre>
 * rbac/
 * ├── dto/                 - Data Transfer Objects
 * │   ├── permission/      - Permission-related DTOs
 * │   └── role/            - Role-related DTOs
 * ├── exception/           - RBAC-specific exceptions
 * ├── mapper/              - MapStruct mappers
 * │   ├── permission/      - Permission mappers
 * │   └── role/            - Role mappers
 * ├── model/               - JPA entities
 * │   ├── permission/      - Permission entities
 * │   └── role/            - Role entities
 * ├── repository/          - Spring Data repositories
 * │   ├── permission/      - Permission repositories
 * │   └── role/            - Role repositories
 * └── service/             - Business logic services
 *     ├── permission/      - Permission services
 *     └── role/            - Role services
 * </pre>
 *
 * <h2>Key Components</h2>
 * <ul>
 * <li>{@link com.mmva.newsapp.infrastructure.rbac.RbacRole.role.Role} - Role
 * entity</li>
 * <li>{@link com.mmva.newsapp.infrastructure.rbac.RbacPermission.permission.Permission}
 * -
 * Permission entity</li>
 * <li>{@link com.mmva.newsapp.infrastructure.rbac.role.core.service.service.role.RoleService}
 * - Role
 * operations</li>
 * <li>{@link com.mmva.newsapp.infrastructure.rbac.permission.core.service.service.permission.PermissionService}
 * - Permission operations</li>
 * </ul>
 *
 * <h2>Portability</h2>
 * <p>
 * This is a <b>infrastructure module</b> designed to be portable across
 * products.
 * It has no dependencies on domain-specific modules and can be reused in other
 * applications.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 * @see com.mmva.newsapp.infrastructure.security
 */
package com.mmva.newsapp.infrastructure.rbac;
