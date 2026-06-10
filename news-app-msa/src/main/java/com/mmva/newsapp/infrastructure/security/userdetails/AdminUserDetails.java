package com.mmva.newsapp.infrastructure.security.userdetails;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom UserDetails implementation for Admin users.
 * 
 * <p>
 * Holds admindashboard-specific information including roles and permissions.
 * Used by Spring Security for authentication and authorization.
 * </p>
 * 
 * <h2>Architectural Note:</h2>
 * <p>
 * This class lives in infrastructure/security/ (not in adminuser/) because it is a
 * Spring Security adapter - it implements Spring's UserDetails interface.
 * Security adapters are cross-cutting infrastructure concerns that bridge
 * framework requirements to domain models.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 * @see AdminUserDetailsService
 */
@Getter
public class AdminUserDetails implements UserDetails {

    private final UUID adminId;
    private final String username;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final boolean accountLocked;
    private final List<String> roles;
    private final List<String> permissions;
    private final Collection<? extends GrantedAuthority> authorities;

    public AdminUserDetails(
            UUID adminId,
            String username,
            String email,
            String password,
            boolean enabled,
            boolean accountLocked,
            List<String> roles,
            List<String> permissions) {
        this.adminId = adminId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.accountLocked = accountLocked;
        this.roles = roles;
        this.permissions = permissions;

        // Convert roles to ROLE_* authorities and add permissions as authorities
        this.authorities = Stream.concat(
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())),
                permissions.stream().map(SimpleGrantedAuthority::new)).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if admindashboard has a specific permission.
     *
     * @param permission Permission name to check
     * @return true if admindashboard has the permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Checks if admindashboard has a specific role.
     *
     * @param role Role name to check (without ROLE_ prefix)
     * @return true if admindashboard has the role
     */
    public boolean hasRole(String role) {
        return roles.stream()
                .anyMatch(r -> r.equalsIgnoreCase(role));
    }
}
