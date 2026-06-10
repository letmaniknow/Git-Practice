package com.mmva.newsapp.infrastructure.security.userdetails;

import com.mmva.newsapp.domain.appuser.model.core.AppUsers;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;
import com.mmva.newsapp.domain.appuser.repository.core.AppUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * UserDetailsService implementation for App users (customers/readers).
 * 
 * <p>
 * Loads app user details from database for authentication.
 * Supports loading by email or phone number.
 * </p>
 * 
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Service("appUserDetailsService")
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
        log.debug("Loading app user by email/phone: {}", emailOrPhone);

        // Try to find by email first, then by phone
        AppUsers userProfile = userRepository.findByAppUsersEmail(emailOrPhone)
                .or(() -> userRepository.findByAppUsersPhoneNumber(emailOrPhone))
                .orElseThrow(() -> {
                    log.warn("App user not found: {}", emailOrPhone);
                    return new UsernameNotFoundException("User not found: " + emailOrPhone);
                });

        return buildUserDetails(userProfile);
    }

    /**
     * Loads app user details by user ID.
     *
     * @param userId User UUID
     * @return AppUserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public AppUserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
        log.debug("Loading app user by ID: {}", userId);

        AppUsers userProfile = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("App user not found by ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });

        return buildUserDetails(userProfile);
    }

    /**
     * Builds AppUserDetails from UserProfile entity.
     */
    private AppUserDetails buildUserDetails(AppUsers userProfile) {
        // Determine if account is active based on status
        boolean isEnabled = AppUserStatus.ACTIVE.equals(userProfile.getAppUsersStatus()) &&
                userProfile.getDeletedAt() == null;

        log.debug("Built AppUserDetails for user: {}", userProfile.getAppUsersId());

        return new AppUserDetails(
                userProfile.getAppUsersId(),
                userProfile.getAppUsersUsername(),
                userProfile.getAppUsersEmail(),
                userProfile.getAppUsersPhoneNumber(),
                userProfile.getAppUsersPasswordHash(),
                isEnabled);
    }
}
