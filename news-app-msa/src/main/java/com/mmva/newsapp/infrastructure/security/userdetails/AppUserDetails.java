package com.mmva.newsapp.infrastructure.security.userdetails;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Custom UserDetails implementation for App users (customers/readers).
 * 
 * <p>
 * Holds app user-specific information.
 * Used by Spring Security for authentication and authorization.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Getter
public class AppUserDetails implements UserDetails {

    private final UUID userId;
    @Getter(lombok.AccessLevel.NONE) // Avoid conflict with UserDetails.getUsername()
    private final String userName;
    private final String userEmail;
    private final String phoneNumber;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserDetails(
            UUID userId,
            String userName,
            String userEmail,
            String phoneNumber,
            String password,
            boolean enabled) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.enabled = enabled;

        // App users have USER role by default
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the display name (userName) for the user.
     * This is separate from getUsername() which returns the email for
     * authentication.
     */
    public String getDisplayName() {
        return userName;
    }

    /**
     * Returns the userEmail as the login identifier for Spring Security.
     */
    @Override
    public String getUsername() {
        return userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
