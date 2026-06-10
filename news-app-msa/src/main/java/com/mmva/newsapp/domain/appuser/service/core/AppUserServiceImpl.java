package com.mmva.newsapp.domain.appuser.service.core;

// ========================================
// Configuration Imports
// ========================================
import com.mmva.newsapp.infrastructure.requestanalytics.exception.RateLimitExceededException;
import com.mmva.newsapp.domain.appuser.config.core.AppUserCacheConstants;
import com.mmva.newsapp.infrastructure.security.config.SecurityAlertNotificationProperties;

// ========================================
// DTO Imports
// ========================================
import com.mmva.newsapp.infrastructure.common.api.dto.BulkOperationResultDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserChangePasswordDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserLoginRequestDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserOAuthLoginDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetCompleteDto;
import com.mmva.newsapp.domain.appuser.dto.auth.AppUserPasswordResetRequestDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserReactivateAccountDto;
import com.mmva.newsapp.domain.appuser.dto.operations.AppUserDataExportDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileRequestDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileResponseDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserProfileUpdateDto;
import com.mmva.newsapp.domain.appuser.dto.core.AppUserSessionDto;
import com.mmva.newsapp.infrastructure.security.dto.SecurityAlertDto;

// ========================================
// Exception Imports
// ========================================
import com.mmva.newsapp.infrastructure.common.exception.DuplicateResourceException;
import com.mmva.newsapp.infrastructure.security.exception.InvalidCredentialsException;
import com.mmva.newsapp.infrastructure.common.exception.InvalidRequestException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.domain.appuser.exception.core.AppUserNotFoundException;
import com.mmva.newsapp.domain.appuser.mapper.core.AppUserMapper;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserAuditAction;
import com.mmva.newsapp.domain.appuser.enums.core.AppUserStatus;
import com.mmva.newsapp.domain.appuser.model.core.AppUsers;
import com.mmva.newsapp.domain.appuser.model.audit.AppUserAuditLog;
import com.mmva.newsapp.domain.appuser.model.core.AppUserSession;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.model.UserDevice;
import com.mmva.newsapp.domain.appuser.repository.core.AppUserRepository;
import com.mmva.newsapp.domain.appuser.repository.core.AppUserSessionRepository;
// ========================================
// Soft-Delete Imports
// ========================================
import com.mmva.newsapp.infrastructure.common.audit.repository.SoftDeleteSpec;
import com.mmva.newsapp.infrastructure.common.ratelimit.service.RateLimiterService;
// ========================================
// Service Imports
// ========================================
import com.mmva.newsapp.infrastructure.clientcontext.core.dto.ClientContextDto;
import com.mmva.newsapp.infrastructure.clientcontext.core.service.ClientContextService;
import com.mmva.newsapp.infrastructure.clientcontext.userdevice.core.service.UserDeviceService;
import com.mmva.newsapp.infrastructure.email.service.EmailService;
import com.mmva.newsapp.domain.appuser.service.audit.AppUserAuditLogService;

// ========================================
// Utility Imports
// ========================================
import com.mmva.newsapp.infrastructure.common.util.InputSanitizer;

// ========================================
// Lombok Imports
// ========================================
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ========================================
// Spring Framework Imports
// ========================================
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

// ========================================
// Java Core Imports
// ========================================
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for Public User Profile Management.
 *
 * <p>
 * Implements all operations for managing public/customer user profiles
 * including:
 * </p>
 * <ul>
 * <li>Authentication and login</li>
 * <li>Account creation, update, and deletion</li>
 * <li>Account lookup and search</li>
 * <li>Security operations (password, verification)</li>
 * <li>Profile and preferences management</li>
 * <li>Session management</li>
 * <li>OAuth/Social login integration</li>
 * <li>Bulk operations for admindashboard use</li>
 * <li>Audit logging</li>
 * </ul>
 *
 * @author MMVA News Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppUserServiceImpl implements AppUserService {

    // ========================================
    // Dependencies
    // ========================================
    private final AppUserRepository userRepository;
    private final AppUserAuditLogService auditLogService;
    private final AppUserSessionRepository userSessionRepository;
    private final AppUserMapper userProfileMapper;
    private final ClientContextService clientContextService;
    private final UserDeviceService userDeviceService;
    private final EmailService emailService;
    private final SecurityAlertNotificationProperties securityAlertNotificationProperties;
    private final InputSanitizer inputSanitizer;
    private final RateLimiterService rateLimiterService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ========================================
    // Configuration
    // ========================================

    @Value("${media.entities.users.avatars:${media.root-path}/processed/users/avatars}")
    private String avatarFolder;

    // ========================================
    // Constants - Rate Limiting
    // ========================================
    private static final String RATE_LIMIT_EMAIL_VERIFICATION = "email-verification";
    private static final String RATE_LIMIT_PHONE_VERIFICATION = "phone-verification";
    private static final String RATE_LIMIT_PASSWORD_RESET = "password-reset";

    // ========================================
    // Constants - Account Lockout
    // ========================================
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    // ========================================
    // Constants - Password History
    // ========================================
    private static final int PASSWORD_HISTORY_SIZE = 5;

    // ========================================
    // Authentication Methods
    // ========================================

    @Override
    public AppUserProfileResponseDto login(AppUserLoginRequestDto loginRequest) {
        String emailOrPhone = loginRequest.getAppUsersEmailOrPhone();
        String password = loginRequest.getAppUsersPassword();
        Optional<AppUsers> userOpt = Optional.empty();

        log.debug("UserProfileService: Processing login for: {}", emailOrPhone);

        if (emailOrPhone == null || emailOrPhone.isBlank()) {
            log.warn("UserProfileService: Login attempt with empty email or phone number");
            throw new InvalidRequestException("credentials", "Email or phone number is required");
        }
        if (emailOrPhone.contains("@")) {
            userOpt = userRepository.findByAppUsersEmail(emailOrPhone);
        } else {
            userOpt = userRepository.findByAppUsersPhoneNumber(emailOrPhone);
        }
        if (userOpt.isEmpty()) {
            log.warn("UserProfileService: Login failed - user not found: {}", emailOrPhone);
            throw new InvalidCredentialsException();
        }
        AppUsers user = userOpt.get();

        // Check if account is locked
        if (Boolean.TRUE.equals(user.getAppUsersAccountLocked())) {
            if (user.getAppUsersAccountLockExpiresAt() != null) {
                Instant lockExpiry = user.getAppUsersAccountLockExpiresAt();
                if (Instant.now().isBefore(lockExpiry)) {
                    log.warn("UserProfileService: Login failed - account locked for user: {} until {}",
                            user.getAppUsersId(), lockExpiry);
                    throw new InvalidRequestException("account", "Account is locked. Try again later.");
                } else {
                    // Lock has expired, unlock the account
                    user.setAppUsersAccountLocked(false);
                    user.setAppUsersAccountLockedAt(null);
                    user.setAppUsersAccountLockExpiresAt(null);
                    user.setAppUsersFailedLoginAttempts(0);
                }
            }
        }

        if (AppUserStatus.ACTIVE != user.getAppUsersStatus()) {
            log.warn("UserProfileService: Login failed - account deactivated for user: {}", user.getAppUsersId());
            throw new InvalidRequestException("account", "Account is deactivated");
        }

        // Use BCryptPasswordEncoder.matches() for secure password validation
        if (!passwordEncoder.matches(password, user.getAppUsersPasswordHash())) {
            // Increment failed login attempts
            int failedAttempts = (user.getAppUsersFailedLoginAttempts() != null ? user.getAppUsersFailedLoginAttempts()
                    : 0) + 1;
            user.setAppUsersFailedLoginAttempts(failedAttempts);

            // Track failed login on device for security
            try {
                ClientContextDto ctx = clientContextService.getCurrentContext();
                if (ctx.deviceFingerprint() != null) {
                    userDeviceService.recordFailedLogin(user.getAppUsersId(), ctx.deviceFingerprint());
                }
            } catch (Exception e) {
                log.trace("Could not record device failed login: {}", e.getMessage());
            }

            if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                // Lock the account
                user.setAppUsersAccountLocked(true);
                user.setAppUsersAccountLockedAt(Instant.now());
                user.setAppUsersAccountLockExpiresAt(Instant.now().plusSeconds(LOCKOUT_DURATION_MINUTES * 60));
                userRepository.save(user);
                log.warn("UserProfileService: Account locked for user: {} after {} failed attempts",
                        user.getAppUsersId(), failedAttempts);
                throw new InvalidRequestException("account",
                        "Account locked due to too many failed login attempts. Try again in " + LOCKOUT_DURATION_MINUTES
                                + " minutes.");
            }

            userRepository.save(user);
            log.warn("UserProfileService: Login failed - incorrect password for user: {} (attempt {}/{})",
                    user.getAppUsersId(), failedAttempts, MAX_FAILED_LOGIN_ATTEMPTS);
            throw new InvalidCredentialsException();
        }

        // Reset failed attempts on successful login
        user.setAppUsersFailedLoginAttempts(0);
        user.setAppUsersLastLogin(Instant.now());

        // Capture login context for security tracking
        ClientContextDto context = clientContextService.getCurrentContext();
        populateLoginContext(user, context);

        userRepository.save(user);

        // Register/update device and check for anomalies
        try {
            var deviceResult = userDeviceService.registerOrUpdateDevice(user.getAppUsersId(), context);
            if (deviceResult.isNewDevice()) {
                log.info("UserProfileService: New device detected for user: {}", user.getAppUsersId());
                sendNewDeviceLoginEmail(user, context);
            }
            if (deviceResult.locationChanged()) {
                log.warn("UserProfileService: Location change detected for user: {}", user.getAppUsersId());
                sendNewLocationLoginEmail(user, context, deviceResult.device());
            }
        } catch (Exception e) {
            log.warn("Failed to register device on login: {}", e.getMessage());
        }

        log.info("UserProfileService: Login successful for user: {}", user.getAppUsersId());
        return userProfileMapper.toResponseDto(user);
    }

    /**
     * Populates login context fields on the user profile.
     */
    private void populateLoginContext(AppUsers userProfile, ClientContextDto context) {
        userProfile.setAppUsersLastLoginIpAddress(context.ipAddress());
        userProfile.setAppUsersLastLoginUserAgent(truncate(context.userAgent(), 512));
        userProfile.setAppUsersLastLoginCountryCode(context.countryCode());
        userProfile.setAppUsersLastLoginDeviceType(context.deviceType() != null ? context.deviceType().name() : null);
        userProfile.setAppUsersLastLoginDeviceFingerprint(context.deviceFingerprint());
    }

    // ========================================
    // Security Alert Email Methods
    // ========================================

    /**
     * Sends new device login alert email to the user.
     */
    private void sendNewDeviceLoginEmail(AppUsers user, ClientContextDto context) {
        if (!securityAlertNotificationProperties.isEnabled() ||
                !securityAlertNotificationProperties.getNewDeviceAlert().isEnabled()) {
            return;
        }

        try {
            SecurityAlertDto alert = SecurityAlertDto.forNewDevice(
                    user.getAppUsersFirstName() != null ? user.getAppUsersFirstName() : user.getAppUsersUsername(),
                    user.getAppUsersEmail(),
                    context.deviceType() != null ? context.deviceType().name() : null,
                    context.browserName(),
                    context.browserVersion(),
                    context.osName(),
                    context.osVersion(),
                    context.city(),
                    context.countryName(),
                    context.countryCode(),
                    context.ipAddress(),
                    Instant.now());

            emailService.sendNewDeviceLoginAlert(alert);
            log.debug("New device login alert queued for user: {}", user.getAppUsersId());
        } catch (Exception e) {
            log.warn("Failed to send new device login alert: {}", e.getMessage());
            // Don't fail the login - this is a notification, not critical
        }
    }

    /**
     * Sends new location login alert email to the user.
     */
    private void sendNewLocationLoginEmail(AppUsers user, ClientContextDto context,
            UserDevice device) {
        if (!securityAlertNotificationProperties.isEnabled() ||
                !securityAlertNotificationProperties.getNewLocationAlert().isEnabled()) {
            return;
        }

        try {
            // Get previous location from device if available
            String previousCity = device != null ? device.getLastCity() : null;
            String previousCountry = device != null ? device.getLastCountryCode() : null;

            SecurityAlertDto alert = SecurityAlertDto.forNewLocation(
                    user.getAppUsersFirstName() != null ? user.getAppUsersFirstName() : user.getAppUsersUsername(),
                    user.getAppUsersEmail(),
                    context.deviceType() != null ? context.deviceType().name() : null,
                    context.browserName(),
                    context.osName(),
                    context.city(),
                    context.countryName(),
                    context.countryCode(),
                    previousCity,
                    previousCountry,
                    context.ipAddress(),
                    Instant.now());

            emailService.sendNewLocationLoginAlert(alert);
            log.debug("New location login alert queued for user: {}", user.getAppUsersId());
        } catch (Exception e) {
            log.warn("Failed to send new location login alert: {}", e.getMessage());
            // Don't fail the login - this is a notification, not critical
        }
    }

    // ========================================
    // Account Validation Methods
    // ========================================

    @Override
    public boolean emailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        boolean exists = userRepository.existsByAppUsersEmail(email);
        log.debug("UserProfileService: Email exists check for '{}': {}", email, exists);
        return exists;
    }

    @Override
    public boolean phoneExists(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        boolean exists = userRepository.existsByAppUsersPhoneNumber(phoneNumber);
        log.debug("UserProfileService: Phone exists check for '{}': {}", phoneNumber, exists);
        return exists;
    }

    // ========================================
    // Account Creation & Update Methods
    // ========================================

    @Override
    @Transactional
    public AppUserProfileResponseDto createAppUser(AppUserProfileRequestDto dto) {
        log.info("UserProfileService: Creating app user with email: {}", dto.getAppUsersEmail());

        // Sanitize input fields to prevent XSS
        sanitizeUserRequestDto(dto);

        // Validate email is required
        if (dto.getAppUsersEmail() == null || dto.getAppUsersEmail().isBlank()) {
            log.warn("UserProfileService: User creation failed - email is required");
            throw new InvalidRequestException("registration", "Email is required for registration");
        }

        // Check for duplicate email
        if (userRepository.existsByAppUsersEmail(dto.getAppUsersEmail())) {
            log.warn("UserProfileService: User creation failed - email already exists: {}", dto.getAppUsersEmail());
            throw new DuplicateResourceException("User", "email", dto.getAppUsersEmail());
        }

        // Check for duplicate phone if provided
        boolean hasPhone = dto.getAppUsersPhoneNumber() != null && !dto.getAppUsersPhoneNumber().isBlank();
        if (hasPhone && userRepository.existsByAppUsersPhoneNumber(dto.getAppUsersPhoneNumber())) {
            log.warn("UserProfileService: User creation failed - phone already exists: {}",
                    dto.getAppUsersPhoneNumber());
            throw new DuplicateResourceException("User", "phone", dto.getAppUsersPhoneNumber());
        }

        // Auto-generate fullName from firstName + lastName if not provided
        if (dto.getAppUsersFullName() == null || dto.getAppUsersFullName().isBlank()) {
            String generatedFullName = (dto.getAppUsersFirstName() + " " + dto.getAppUsersLastName()).trim();
            dto.setAppUsersFullName(generatedFullName);
            log.debug("UserProfileService: Auto-generated fullName: {}", generatedFullName);
        }

        // Auto-generate userName from email if not provided
        if (dto.getAppUsersUsername() == null || dto.getAppUsersUsername().isBlank()) {
            String emailPrefix = dto.getAppUsersEmail().split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
            String generatedUserName = emailPrefix + "_"
                    + java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999);
            dto.setAppUsersUsername(generatedUserName);
            log.debug("UserProfileService: Auto-generated userName: {}", generatedUserName);
        }

        try {
            AppUsers userProfile = userProfileMapper.toEntity(dto);

            // Hash password (in production, use BCrypt or similar)
            userProfile.setAppUsersPasswordHash(hashPassword(dto.getAppUsersPassword()));
            userProfile.setAppUsersPasswordUpdatedAt(Instant.now());

            // Capture comprehensive registration context
            ClientContextDto context = clientContextService.getCurrentContext();
            populateRegistrationContext(userProfile, context);
            log.debug("UserProfileService: Captured registration from IP: {}, Device: {}",
                    context.ipAddress(), context.deviceType());

            AppUsers savedUser = userRepository.save(userProfile);

            // Register the device for multi-device tracking
            try {
                userDeviceService.registerOrUpdateDevice(savedUser.getAppUsersId(), context);
            } catch (Exception e) {
                log.warn("Failed to register device for new user: {}", e.getMessage());
            }

            log.info("UserProfileService: User created successfully with ID: {}", savedUser.getAppUsersId());
            return userProfileMapper.toResponseDto(savedUser);
        } catch (Exception e) {
            log.error("UserProfileService: Failed to create user with email: {}. Error: {}",
                    dto.getAppUsersEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Populates registration context fields on the user profile.
     */
    private void populateRegistrationContext(AppUsers userProfile, ClientContextDto context) {
        // Network
        userProfile.setAppUsersRegisteredIpAddress(context.ipAddress());
        userProfile.setAppUsersRegisteredUserAgent(truncate(context.userAgent(), 512));

        // Location
        userProfile.setAppUsersRegisteredCountryCode(context.countryCode());
        userProfile.setAppUsersRegisteredCity(context.city());

        // Device
        userProfile.setAppUsersRegisteredPlatform(context.osName());
        userProfile.setAppUsersRegisteredDeviceType(context.deviceType() != null ? context.deviceType().name() : null);
        userProfile.setAppUsersRegisteredBrowser(context.browserName());
        userProfile.setAppUsersRegisteredDeviceFingerprint(context.deviceFingerprint());

        // Channel & Attribution
        userProfile.setAppUsersRegisteredChannel(context.channel() != null ? context.channel().name() : null);
        userProfile.setAppUsersRegisteredReferer(truncate(context.referer(), 2048));

        // Language
        userProfile.setAppUsersDetectedLanguage(context.primaryLanguage());

        // Set preferred language from detected if not provided
        if (userProfile.getAppUsersPreferredLanguage() == null && context.primaryLanguage() != null) {
            userProfile.setAppUsersPreferredLanguage(context.primaryLanguage());
        }

        // Set timezone from GeoIP if available
        if (userProfile.getAppUsersTimezone() == null && context.timezone() != null) {
            userProfile.setAppUsersTimezone(context.timezone());
        }

        // Set country code from GeoIP if available
        if (userProfile.getAppUsersCountryCode() == null && context.countryCode() != null) {
            userProfile.setAppUsersCountryCode(context.countryCode());
        }
    }

    /**
     * Sanitizes user request DTO fields to prevent XSS attacks.
     */
    private void sanitizeUserRequestDto(AppUserProfileRequestDto dto) {
        if (dto.getAppUsersFirstName() != null) {
            dto.setAppUsersFirstName(inputSanitizer.sanitizeName(dto.getAppUsersFirstName()));
        }
        if (dto.getAppUsersLastName() != null) {
            dto.setAppUsersLastName(inputSanitizer.sanitizeName(dto.getAppUsersLastName()));
        }
        if (dto.getAppUsersFullName() != null) {
            dto.setAppUsersFullName(inputSanitizer.sanitize(dto.getAppUsersFullName()));
        }
        if (dto.getAppUsersPhoneNumber() != null) {
            dto.setAppUsersPhoneNumber(inputSanitizer.sanitizePhoneNumber(dto.getAppUsersPhoneNumber()));
        }
        if (dto.getAppUsersAvatarUrl() != null) {
            dto.setAppUsersAvatarUrl(inputSanitizer.sanitizeUrl(dto.getAppUsersAvatarUrl()));
        }
    }

    /**
     * Truncates a string to fit database column.
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppUserCacheConstants.USER_CACHE, key = "#id"),
            @CacheEvict(value = AppUserCacheConstants.USER_LIST_CACHE, allEntries = true),
            @CacheEvict(value = AppUserCacheConstants.USER_BY_EMAIL_CACHE, allEntries = true),
            @CacheEvict(value = AppUserCacheConstants.USER_BY_PHONE_CACHE, allEntries = true)
    })
    public AppUserProfileResponseDto updateUser(UUID id, AppUserProfileUpdateDto dto) {
        log.info("UserProfileService: Updating user: {}", id);

        // Sanitize input fields to prevent XSS
        sanitizeUserUpdateDto(dto);

        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: User update failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        try {
            userProfileMapper.updateEntityFromDto(dto, userProfile);
            AppUsers updatedUser = userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.UPDATE.name(), AppUserAuditAction.UPDATE.getDescription(), id);

            log.info("UserProfileService: User updated successfully: {}", id);
            return userProfileMapper.toResponseDto(updatedUser);
        } catch (Exception e) {
            log.error("UserProfileService: Failed to update user: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Sanitizes user update DTO fields to prevent XSS attacks.
     */
    private void sanitizeUserUpdateDto(AppUserProfileUpdateDto dto) {
        if (dto.getAppUsersFirstName() != null) {
            dto.setAppUsersFirstName(inputSanitizer.sanitizeName(dto.getAppUsersFirstName()));
        }
        if (dto.getAppUsersLastName() != null) {
            dto.setAppUsersLastName(inputSanitizer.sanitizeName(dto.getAppUsersLastName()));
        }
        if (dto.getAppUsersFullName() != null) {
            dto.setAppUsersFullName(inputSanitizer.sanitize(dto.getAppUsersFullName()));
        }
        if (dto.getAppUsersAvatarUrl() != null) {
            dto.setAppUsersAvatarUrl(inputSanitizer.sanitizeUrl(dto.getAppUsersAvatarUrl()));
        }
    }

    // ========================================
    // Account Lookup & Search Methods
    // ========================================

    @Override
    @Cacheable(value = AppUserCacheConstants.USER_CACHE, key = "#id")
    public AppUserProfileResponseDto getUserById(UUID id) {
        log.debug("UserProfileService: Fetching user by ID: {}", id);

        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: User not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        log.debug("UserProfileService: User fetched successfully: {}", id);
        return userProfileMapper.toResponseDto(userProfile);
    }

    @Override
    @Cacheable(value = AppUserCacheConstants.USER_BY_EMAIL_CACHE, key = "#userEmail")
    public AppUserProfileResponseDto getUserByEmail(String userEmail) {
        log.debug("UserProfileService: Fetching user by email: {}", userEmail);

        AppUsers userProfile = userRepository.findByAppUsersEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: User not found with email: {}", userEmail);
                    return new AppUserNotFoundException("email", userEmail);
                });

        log.debug("UserProfileService: User fetched by email: {}", userEmail);
        return userProfileMapper.toResponseDto(userProfile);
    }

    @Override
    @Cacheable(value = AppUserCacheConstants.USER_BY_PHONE_CACHE, key = "#userPhoneNumber")
    public AppUserProfileResponseDto getUserByPhoneNumber(String userPhoneNumber) {
        log.debug("UserProfileService: Fetching user by phone: {}", userPhoneNumber);

        AppUsers userProfile = userRepository.findByAppUsersPhoneNumber(userPhoneNumber)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: User not found with phone: {}", userPhoneNumber);
                    return new AppUserNotFoundException("phone", userPhoneNumber);
                });

        log.debug("UserProfileService: User fetched by phone: {}", userPhoneNumber);
        return userProfileMapper.toResponseDto(userProfile);
    }

    @Override
    public Page<AppUserProfileResponseDto> getAllUsers(Pageable pageable) {
        log.debug("UserProfileService: Fetching users - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Admin API: Use SoftDeleteSpec.includeDeleted() to see all records including
        // soft-deleted
        Page<AppUsers> page = userRepository.findAll(SoftDeleteSpec.includeDeleted(), pageable);

        log.debug("UserProfileService: Fetched {} users for page: {}",
                page.getNumberOfElements(), pageable.getPageNumber());
        return page.map(userProfileMapper::toResponseDto);
    }

    @Override
    public Page<AppUserProfileResponseDto> searchUsersByName(String searchTerm, Pageable pageable) {
        log.debug("UserProfileService: Searching users by name: '{}', page: {}",
                searchTerm, pageable.getPageNumber());

        String sanitizedSearch = inputSanitizer.sanitizeForQuery(searchTerm);
        Page<AppUsers> page = userRepository.searchByName(sanitizedSearch, pageable);

        log.debug("UserProfileService: Found {} users matching: '{}'", page.getTotalElements(), searchTerm);
        return page.map(userProfileMapper::toResponseDto);
    }

    @Override
    public Page<AppUserProfileResponseDto> filterUsersByStatus(AppUserStatus status, Pageable pageable) {
        log.debug("UserProfileService: Filtering users by status: '{}', page: {}",
                status, pageable.getPageNumber());

        Page<AppUsers> page = userRepository.findByAppUsersStatus(status, pageable);

        log.debug("UserProfileService: Found {} users with status: '{}'", page.getTotalElements(), status);
        return page.map(userProfileMapper::toResponseDto);
    }

    @Override
    public Page<AppUserProfileResponseDto> searchUsersWithFilters(String searchTerm, AppUserStatus status,
            Pageable pageable) {
        log.debug("UserProfileService: Searching users - term: '{}', status: '{}', page: {}",
                searchTerm, status, pageable.getPageNumber());

        Page<AppUsers> page;

        boolean hasSearch = searchTerm != null && !searchTerm.isBlank();
        boolean hasStatus = status != null;

        String sanitizedSearch = hasSearch ? inputSanitizer.sanitizeForQuery(searchTerm) : null;

        if (hasSearch && hasStatus) {
            page = userRepository.searchByNameAndStatus(sanitizedSearch, status, pageable);
        } else if (hasSearch) {
            page = userRepository.searchByName(sanitizedSearch, pageable);
        } else if (hasStatus) {
            page = userRepository.findByAppUsersStatus(status, pageable);
        } else {
            // Admin API: Use SoftDeleteSpec.includeDeleted() for unfiltered listing
            page = userRepository.findAll(SoftDeleteSpec.includeDeleted(), pageable);
        }

        log.debug("UserProfileService: Found {} users with applied filters", page.getTotalElements());
        return page.map(userProfileMapper::toResponseDto);
    }

    // ========================================
    // Account Security & Verification Methods
    // ========================================

    @Override
    @Transactional
    public void changePassword(UUID id, AppUserChangePasswordDto dto) {
        log.info("UserProfileService: Changing password for user: {}", id);

        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: Password change failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        // Validate current password using BCrypt
        if (!passwordEncoder.matches(dto.getAppUsersCurrentPassword(), userProfile.getAppUsersPasswordHash())) {
            log.warn("UserProfileService: Password change failed - incorrect current password for user: {}", id);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Validate new password confirmation
        if (!dto.getAppUsersNewPassword().equals(dto.getAppUsersConfirmPassword())) {
            log.warn("UserProfileService: Password change failed - passwords do not match for user: {}", id);
            throw new InvalidRequestException("password", "New password and confirm password do not match");
        }

        // Check password history (prevent reuse of recent passwords)
        String newPasswordHash = hashPassword(dto.getAppUsersNewPassword());
        if (isPasswordInHistory(userProfile, newPasswordHash)) {
            log.warn("UserProfileService: Password change failed - password reuse detected for user: {}", id);
            throw new InvalidRequestException("password",
                    "Cannot reuse any of your last " + PASSWORD_HISTORY_SIZE + " passwords");
        }

        try {
            // Add current password to history before changing
            addPasswordToHistory(userProfile, userProfile.getAppUsersPasswordHash());

            userProfile.setAppUsersPasswordHash(newPasswordHash);
            userProfile.setAppUsersPasswordUpdatedAt(Instant.now());
            userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.CHANGE_PASSWORD.name(),
                    AppUserAuditAction.CHANGE_PASSWORD.getDescription(), id);

            log.info("UserProfileService: Password changed successfully for user: {}", id);
        } catch (Exception e) {
            log.error("UserProfileService: Failed to change password for user: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a password hash exists in the user's password history.
     */
    private boolean isPasswordInHistory(AppUsers userProfile, String passwordHash) {
        if (userProfile.getAppUsersPasswordHistory() == null || userProfile.getAppUsersPasswordHistory().isBlank()) {
            return false;
        }
        List<String> history = new ArrayList<>(Arrays.asList(userProfile.getAppUsersPasswordHistory().split(",")));
        return history.contains(passwordHash);
    }

    /**
     * Adds a password hash to the user's password history (keeping last N
     * passwords).
     */
    private void addPasswordToHistory(AppUsers userProfile, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            return;
        }
        List<String> history = new ArrayList<>();
        if (userProfile.getAppUsersPasswordHistory() != null && !userProfile.getAppUsersPasswordHistory().isBlank()) {
            history = new ArrayList<>(Arrays.asList(userProfile.getAppUsersPasswordHistory().split(",")));
        }
        history.add(0, passwordHash); // Add to beginning
        // Keep only last N passwords
        while (history.size() > PASSWORD_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
        userProfile.setAppUsersPasswordHistory(String.join(",", history));
    }

    @Override
    @Transactional
    public void requestPasswordReset(AppUserPasswordResetRequestDto dto) {
        String emailOrPhoneNumber = dto.getAppUsersEmailOrPhone();
        log.info("UserProfileService: Requesting password reset for: {}", emailOrPhoneNumber);

        if (emailOrPhoneNumber == null || emailOrPhoneNumber.isBlank()) {
            throw new InvalidRequestException("credentials", "Email or phone number is required");
        }

        // Check rate limit using email/phone as key
        String rateLimitKey = RATE_LIMIT_PASSWORD_RESET + ":" + emailOrPhoneNumber;
        if (!rateLimiterService.isAllowed(rateLimitKey)) {
            long retryAfter = rateLimiterService.getRemainingCooldownSeconds(rateLimitKey);
            throw new RateLimitExceededException(
                    "Too many password reset requests. Please wait before requesting again.", retryAfter);
        }

        // Record the request for rate limiting
        rateLimiterService.recordRequest(rateLimitKey);

        // Find user by email or phone
        Optional<AppUsers> userOpt;
        boolean isEmail = emailOrPhoneNumber.contains("@");

        if (isEmail) {
            userOpt = userRepository.findByAppUsersEmail(emailOrPhoneNumber);
        } else {
            userOpt = userRepository.findByAppUsersPhoneNumber(emailOrPhoneNumber);
        }

        if (userOpt.isEmpty()) {
            // For security, don't reveal if user exists
            log.warn("UserProfileService: Password reset requested for non-existent user: {}", emailOrPhoneNumber);
            return; // Silent return for security
        }

        AppUsers user = userOpt.get();

        // Generate 6-digit reset code
        String resetCode = String.format("%06d", new java.security.SecureRandom().nextInt(999999));

        // Set expiration (30 minutes for password reset)
        Instant expiresAt = Instant.now().plusSeconds(30 * 60);

        user.setAppUsersPasswordResetCode(resetCode);
        user.setAppUsersPasswordResetExpiresAt(expiresAt);
        userRepository.save(user);

        // Send code via email
        if (isEmail) {
            emailService.sendPasswordResetEmail(user.getAppUsersEmail(), resetCode);
        }
        // TODO: Add SMS service for phone-based reset

        logActionInternal(user.getAppUsersId(), AppUserAuditAction.PASSWORD_RESET_REQUESTED.name(),
                AppUserAuditAction.PASSWORD_RESET_REQUESTED.getDescription(), user.getAppUsersId());

        log.info("UserProfileService: Password reset code sent for user: {} (expires: {})",
                user.getAppUsersId(), expiresAt);
    }

    @Override
    @Transactional
    public void completePasswordReset(AppUserPasswordResetCompleteDto dto) {
        log.info("UserProfileService: Completing password reset for: {}", dto.getAppUsersEmailOrPhone());

        // Validate passwords match
        if (!dto.getAppUsersNewPassword().equals(dto.getAppUsersConfirmPassword())) {
            throw new InvalidRequestException("password", "New password and confirm password do not match");
        }

        // Find user
        Optional<AppUsers> userOpt;
        boolean isEmail = dto.getAppUsersEmailOrPhone().contains("@");

        if (isEmail) {
            userOpt = userRepository.findByAppUsersEmail(dto.getAppUsersEmailOrPhone());
        } else {
            userOpt = userRepository.findByAppUsersPhoneNumber(dto.getAppUsersEmailOrPhone());
        }

        AppUsers user = userOpt
                .orElseThrow(() -> new AppUserNotFoundException("account", dto.getAppUsersEmailOrPhone()));

        // Validate reset code exists
        if (user.getAppUsersPasswordResetCode() == null) {
            throw new InvalidRequestException("reset", "No password reset was requested");
        }

        // Validate code matches
        if (!dto.getAppUsersVerificationCode().equals(user.getAppUsersPasswordResetCode())) {
            log.warn("UserProfileService: Invalid password reset code for user: {}", user.getAppUsersId());
            throw new InvalidRequestException("verification", "Invalid verification code");
        }

        // Check if code expired
        if (user.getAppUsersPasswordResetExpiresAt() != null) {
            Instant expiresAt = user.getAppUsersPasswordResetExpiresAt();
            if (Instant.now().isAfter(expiresAt)) {
                throw new InvalidRequestException("verification", "Verification code has expired");
            }
        }

        // Check password history (prevent reuse of recent passwords)
        String newPasswordHash = hashPassword(dto.getAppUsersNewPassword());
        if (isPasswordInHistory(user, newPasswordHash)) {
            log.warn("UserProfileService: Password reset failed - password reuse detected for user: {}",
                    user.getAppUsersId());
            throw new InvalidRequestException("password",
                    "Cannot reuse any of your last " + PASSWORD_HISTORY_SIZE + " passwords");
        }

        // Add current password to history before resetting
        addPasswordToHistory(user, user.getAppUsersPasswordHash());

        // Reset password
        user.setAppUsersPasswordHash(newPasswordHash);
        user.setAppUsersPasswordUpdatedAt(Instant.now());
        user.setAppUsersLastPasswordResetAt(Instant.now());
        user.setAppUsersPasswordResetCode(null);
        user.setAppUsersPasswordResetExpiresAt(null);
        // Also unlock account if it was locked
        user.setAppUsersAccountLocked(false);
        user.setAppUsersAccountLockedAt(null);
        user.setAppUsersAccountLockExpiresAt(null);
        user.setAppUsersFailedLoginAttempts(0);
        userRepository.save(user);

        logActionInternal(user.getAppUsersId(), AppUserAuditAction.PASSWORD_RESET_COMPLETED.name(),
                AppUserAuditAction.PASSWORD_RESET_COMPLETED.getDescription(), user.getAppUsersId());

        log.info("UserProfileService: Password reset completed for user: {}", user.getAppUsersId());
    }

    @Override
    @Transactional
    public String generateEmailVerificationCode(UUID id) {
        log.info("UserProfileService: Generating email verification code for user: {}", id);

        // Check rate limit
        if (!rateLimiterService.isAllowed(id, RATE_LIMIT_EMAIL_VERIFICATION)) {
            long retryAfter = rateLimiterService.getRemainingCooldownSeconds(id, RATE_LIMIT_EMAIL_VERIFICATION);
            throw new RateLimitExceededException(
                    "Too many verification code requests. Please wait before requesting again.", retryAfter);
        }

        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: Email verification failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        // Check if already verified
        if (userProfile.getAppUsersEmailVerified() != null && userProfile.getAppUsersEmailVerified()) {
            throw new InvalidRequestException("email", "Email is already verified");
        }

        // Record the request for rate limiting
        rateLimiterService.recordRequest(id, RATE_LIMIT_EMAIL_VERIFICATION);

        // Generate 6-digit code using SecureRandom
        String verificationCode = String.format("%06d", new java.security.SecureRandom().nextInt(999999));

        // Set expiration time (15 minutes)
        Instant expiresAt = Instant.now().plusSeconds(15 * 60);

        userProfile.setAppUsersEmailVerificationCode(verificationCode);
        userProfile.setAppUsersEmailVerificationExpiresAt(expiresAt);
        userRepository.save(userProfile);

        // Send verification code via email
        emailService.sendVerificationCode(userProfile.getAppUsersEmail(), verificationCode);

        logActionInternal(id, AppUserAuditAction.EMAIL_VERIFICATION_SENT.name(),
                AppUserAuditAction.EMAIL_VERIFICATION_SENT.getDescription(), id);
        log.info("Email verification code generated and sent for user: {} (expires at: {})", id, expiresAt);

        return "Verification code sent to " + maskEmail(userProfile.getAppUsersEmail());
    }

    @Override
    @Transactional
    public String generatePhoneVerificationCode(UUID id) {
        log.info("Generating phone verification code for user: {}", id);

        // Check rate limit
        if (!rateLimiterService.isAllowed(id, RATE_LIMIT_PHONE_VERIFICATION)) {
            long retryAfter = rateLimiterService.getRemainingCooldownSeconds(id, RATE_LIMIT_PHONE_VERIFICATION);
            throw new RateLimitExceededException(
                    "Too many verification code requests. Please wait before requesting again.", retryAfter);
        }

        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Generate phone verification code failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        // Check if already verified
        if (userProfile.getAppUsersPhoneVerified() != null && userProfile.getAppUsersPhoneVerified()) {
            throw new InvalidRequestException("phone", "Phone is already verified");
        }

        // Record the request for rate limiting
        rateLimiterService.recordRequest(id, RATE_LIMIT_PHONE_VERIFICATION);

        // Generate 6-digit code using SecureRandom
        String verificationCode = String.format("%06d", new java.security.SecureRandom().nextInt(999999));

        // Set expiration time (15 minutes)
        Instant expiresAt = Instant.now().plusSeconds(15 * 60);

        userProfile.setAppUsersPhoneVerificationCode(verificationCode);
        userProfile.setAppUsersPhoneVerificationExpiresAt(expiresAt);
        userRepository.save(userProfile);

        // TODO: Integrate SMS service when available
        // smsService.sendVerificationCode(userProfile.getUserPhoneNumber(),
        // verificationCode);

        logActionInternal(id, AppUserAuditAction.PHONE_VERIFICATION_SENT.name(),
                AppUserAuditAction.PHONE_VERIFICATION_SENT.getDescription(), id);
        log.info("Phone verification code generated for user: {} (expires at: {})", id, expiresAt);

        return "Verification code sent to " + maskPhone(userProfile.getAppUsersPhoneNumber());
    }

    @Override
    @Transactional
    @CacheEvict(value = AppUserCacheConstants.USER_CACHE, key = "#id")
    public void verifyEmail(UUID id, String verificationCode) {
        log.info("Verifying email for user: {}", id);
        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Email verification failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        // Validate verification code exists
        if (userProfile.getAppUsersEmailVerificationCode() == null) {
            throw new InvalidRequestException("verification", "No verification code was generated");
        }

        // Validate code matches
        if (!verificationCode.equals(userProfile.getAppUsersEmailVerificationCode())) {
            logActionInternal(id, AppUserAuditAction.EMAIL_VERIFICATION_FAILED.name(),
                    AppUserAuditAction.EMAIL_VERIFICATION_FAILED.getDescription(), id);
            throw new InvalidRequestException("verification", "Invalid verification code");
        }

        // Check if code expired
        if (userProfile.getAppUsersEmailVerificationExpiresAt() != null) {
            Instant expiresAt = userProfile.getAppUsersEmailVerificationExpiresAt();
            if (Instant.now().isAfter(expiresAt)) {
                throw new InvalidRequestException("verification", "Verification code has expired");
            }
        }

        try {
            userProfile.setAppUsersEmailVerified(true);
            userProfile.setAppUsersEmailVerificationCode(null);
            userProfile.setAppUsersEmailVerificationExpiresAt(null);
            userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.VERIFY_EMAIL.name(),
                    AppUserAuditAction.VERIFY_EMAIL.getDescription(), id);
            log.info("Email verified successfully for user: {}", id);
        } catch (Exception e) {
            log.error("Failed to verify email for user with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = AppUserCacheConstants.USER_CACHE, key = "#id")
    public void verifyPhone(UUID id, String verificationCode) {
        log.info("Verifying phone for user: {}", id);
        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Phone verification failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        // Validate verification code exists
        if (userProfile.getAppUsersPhoneVerificationCode() == null) {
            throw new InvalidRequestException("verification", "No verification code was generated");
        }

        // Validate code matches
        if (!verificationCode.equals(userProfile.getAppUsersPhoneVerificationCode())) {
            logActionInternal(id, AppUserAuditAction.PHONE_VERIFICATION_FAILED.name(),
                    AppUserAuditAction.PHONE_VERIFICATION_FAILED.getDescription(), id);
            throw new InvalidRequestException("verification", "Invalid verification code");
        }

        // Check if code expired
        if (userProfile.getAppUsersPhoneVerificationExpiresAt() != null) {
            Instant expiresAt = userProfile.getAppUsersPhoneVerificationExpiresAt();
            if (Instant.now().isAfter(expiresAt)) {
                throw new InvalidRequestException("verification", "Verification code has expired");
            }
        }

        try {
            userProfile.setAppUsersPhoneVerified(true);
            userProfile.setAppUsersPhoneVerificationCode(null);
            userProfile.setAppUsersPhoneVerificationExpiresAt(null);
            userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.VERIFY_PHONE.name(),
                    AppUserAuditAction.VERIFY_PHONE.getDescription(), id);
            log.info("Phone verified successfully for user: {}", id);
        } catch (Exception e) {
            log.error("UserProfileService: Failed to verify phone for user: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Profile & Preferences Methods
    // ========================================

    @Override
    @Transactional
    public void updateProfilePicture(UUID id, String avatarUrl) {
        log.info("UserProfileService: Updating profile picture for user: {}", id);

        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: Profile picture update failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        try {
            userProfile.setAppUsersAvatarUrl(avatarUrl);
            userProfile.setAppUsersAvatarUpdatedAt(Instant.now());
            userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.UPDATE_PROFILE_PICTURE.name(),
                    AppUserAuditAction.UPDATE_PROFILE_PICTURE.getDescription(), id);

            log.info("UserProfileService: Profile picture updated for user: {}", id);
        } catch (Exception e) {
            log.error("Failed to update profile picture for user with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public String updateProfilePicture(UUID id, MultipartFile avatarFile) {
        log.info("Uploading new avatar for user ID: {}", id);
        if (id == null) {
            throw new InvalidRequestException("id", "User ID cannot be null");
        }

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new InvalidRequestException("avatarFile", "Avatar file is required");
        }

        AppUsers user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Avatar upload failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        String newFileName = UUID.randomUUID() + "_" + avatarFile.getOriginalFilename();
        String filePath = resolveAvatarFilePath(newFileName);

        ensureAvatarFolderExists();

        java.io.File target = new java.io.File(filePath);
        try {
            avatarFile.transferTo(target);
            user.setAppUsersAvatarUrl(filePath);
            user.setAppUsersAvatarUpdatedAt(Instant.now());
            userRepository.save(user);

            logActionInternal(id, AppUserAuditAction.UPDATE_PROFILE_PICTURE.name(),
                    AppUserAuditAction.UPDATE_PROFILE_PICTURE.getDescription(), id);

            log.info("Avatar uploaded and profile updated for user ID: {}", id);
            return filePath;
        } catch (Exception e) {
            if (target.exists()) {
                target.delete();
            }
            log.error("Failed to upload avatar for user ID: {}. Error: {}", id, e.getMessage());
            throw new InvalidRequestException("avatarFile", "Failed to upload avatar: " + e.getMessage());
        }
    }

    @Override
    public org.springframework.core.io.Resource getAvatarFile(UUID id) {
        log.debug("Fetching avatar file for user ID: {}", id);
        if (id == null) {
            throw new InvalidRequestException("id", "User ID cannot be null");
        }

        AppUsers user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Avatar file fetch failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        String avatarPath = user.getAppUsersAvatarUrl();
        if (avatarPath == null || avatarPath.isBlank()) {
            log.warn("No avatar set for user ID: {}", id);
            throw new InvalidRequestException("avatar", "No avatar set for this user");
        }

        java.io.File file = new java.io.File(avatarPath);
        if (!file.exists()) {
            log.warn("Avatar file not found: {}", avatarPath);
            throw new ResourceNotFoundException("Avatar file", "path", avatarPath);
        }

        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toURI());
            log.debug("Avatar file fetched successfully: {}", avatarPath);
            return resource;
        } catch (Exception e) {
            log.error("Cannot read avatar file: {}. Error: {}", avatarPath, e.getMessage());
            throw new InvalidRequestException("avatar", "Cannot read avatar file: " + avatarPath);
        }
    }

    @Override
    @Transactional
    public void updatePreferences(UUID id, Boolean marketingOptIn, String privacySettings) {
        log.info("Updating preferences for user: {}", id);
        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Preferences update failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });
        try {
            if (marketingOptIn != null)
                userProfile.setAppUsersMarketingOptIn(marketingOptIn);
            if (privacySettings != null)
                userProfile.setAppUsersPrivacySettings(privacySettings);
            userRepository.save(userProfile);
            log.info("Preferences updated successfully for user: {}", id);
        } catch (Exception e) {
            log.error("Failed to update preferences for user with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AppUserDataExportDto exportUserData(UUID id) {
        log.info("Exporting user data for GDPR compliance: {}", id);
        AppUsers user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Export user data failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        // Get audit trail for this user
        List<AppUserAuditLog> auditLogs = auditLogService.findByUserId(id);
        List<AppUserDataExportDto.AuditEntry> auditEntries = auditLogs.stream()
                .map(auditLog -> AppUserDataExportDto.AuditEntry.builder()
                        .appUsersAction(auditLog.getAppUsersAuditLogAction())
                        .appUsersDetails(auditLog.getAppUsersAuditLogDetails())
                        .appUsersCreatedAt(auditLog.getAppUsersAuditLogCreatedAt() != null
                                ? auditLog.getAppUsersAuditLogCreatedAt().toString()
                                : null)
                        .build())
                .toList();

        // Build the export DTO
        AppUserDataExportDto exportDto = AppUserDataExportDto.builder()
                .exportDate(Instant.now().toString())
                .exportVersion("1.0")
                .appUsersId(user.getAppUsersId())
                .personalInfo(AppUserDataExportDto.PersonalInfo.builder()
                        .appUsersFirstName(user.getAppUsersFirstName())
                        .appUsersLastName(user.getAppUsersLastName())
                        .appUsersFullName(user.getAppUsersFullName())
                        .appUsersDateOfBirth(user.getAppUsersDateOfBirth())
                        .appUsersGender(user.getAppUsersGender())
                        .appUsersAvatarUrl(user.getAppUsersAvatarUrl())
                        .build())
                .accountInfo(AppUserDataExportDto.AccountInfo.builder()
                        .appUsersUsername(user.getAppUsersUsername())
                        .appUsersAccountType(user.getAppUsersAccountType())
                        .appUsersStatus(user.getAppUsersStatus())
                        .appUsersSegment(user.getAppUsersSegment())
                        .appUsersIsAnonymous(user.getAppUsersIsAnonymous())
                        .appUsersProfileCompleted(user.getAppUsersProfileCompleted())
                        .appUsersCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                        .appUsersUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
                        .appUsersLastLogin(
                                user.getAppUsersLastLogin() != null ? user.getAppUsersLastLogin().toString() : null)
                        .appUsersLastActivityAt(
                                user.getAppUsersLastActivityAt() != null ? user.getAppUsersLastActivityAt().toString()
                                        : null)
                        .appUsersReferralCode(user.getAppUsersReferralCode())
                        .appUsersExternalId(user.getAppUsersExternalId())
                        .build())
                .contactInfo(AppUserDataExportDto.ContactInfo.builder()
                        .appUsersEmail(user.getAppUsersEmail())
                        .appUsersEmailVerified(user.getAppUsersEmailVerified())
                        .appUsersPhoneNumber(user.getAppUsersPhoneNumber())
                        .appUsersPhoneVerified(user.getAppUsersPhoneVerified())
                        .build())
                .preferences(AppUserDataExportDto.Preferences.builder()
                        .appUsersPreferredLanguage(user.getAppUsersPreferredLanguage())
                        .appUsersLanguagePreferences(user.getAppUsersLanguagePreferences())
                        .appUsersTimezone(user.getAppUsersTimezone())
                        .appUsersPreferredCurrency(user.getAppUsersPreferredCurrency())
                        .appUsersTheme(user.getAppUsersTheme())
                        .appUsersNotificationEnabled(user.getAppUsersNotificationEnabled())
                        .appUsersMarketingOptIn(user.getAppUsersMarketingOptIn())
                        .appUsersPrivacySettings(user.getAppUsersPrivacySettings())
                        .appUsersSocialLinks(user.getAppUsersSocialLinks())
                        .appUsersCustomAttributes(user.getAppUsersCustomAttributes())
                        .build())
                .securityInfo(AppUserDataExportDto.SecurityInfo.builder()
                        .appUsersAuthProvider(user.getAppUsersAuthProvider())
                        .appUsersMultiFactorEnabled(user.getAppUsersMfaEnabled())
                        .appUsersPasswordUpdatedAt(
                                user.getAppUsersPasswordUpdatedAt() != null
                                        ? user.getAppUsersPasswordUpdatedAt().toString()
                                        : null)
                        .appUsersLastPasswordResetAt(
                                user.getAppUsersLastPasswordResetAt() != null
                                        ? user.getAppUsersLastPasswordResetAt().toString()
                                        : null)
                        .appUsersRiskScore(user.getAppUsersRiskScore())
                        // Note: passwordHash is NOT included for security
                        .build())
                .locationDeviceInfo(AppUserDataExportDto.LocationDeviceInfo.builder()
                        .appUsersCountryCode(user.getAppUsersCountryCode())
                        .appUsersInstallCountryCode(user.getAppUsersInstallCountryCode())
                        .appUsersLastLoginCountryCode(user.getAppUsersLastLoginCountryCode())
                        .appUsersDeviceId(user.getAppUsersDeviceId())
                        .appUsersRegisteredIpAddress(user.getAppUsersRegisteredIpAddress())
                        .appUsersRegisteredUserAgent(user.getAppUsersRegisteredUserAgent())
                        .appUsersLastLoginIpAddress(user.getAppUsersLastLoginIpAddress())
                        .appUsersLastLoginUserAgent(user.getAppUsersLastLoginUserAgent())
                        .appUsersInstalledIpAddress(user.getAppUsersInstalledIpAddress())
                        .appUsersInstalledUserAgent(user.getAppUsersInstalledUserAgent())
                        .build())
                .gdprInfo(AppUserDataExportDto.GdprInfo.builder()
                        .appUsersGdprConsentGiven(user.getAppUsersGdprConsentGiven())
                        .appUsersGdprConsentDate(
                                user.getAppUsersGdprConsentDate() != null ? user.getAppUsersGdprConsentDate().toString()
                                        : null)
                        .appUsersGdprConsentVersion(user.getAppUsersGdprConsentVersion())
                        .appUsersConsentVersion(user.getAppUsersConsentVersion())
                        .appUsersGdprDataExportRequestedAt(user.getAppUsersGdprDataExportRequestedAt() != null
                                ? user.getAppUsersGdprDataExportRequestedAt().toString()
                                : null)
                        .appUsersGdprDataDeleteRequestedAt(user.getAppUsersGdprDataDeleteRequestedAt() != null
                                ? user.getAppUsersGdprDataDeleteRequestedAt().toString()
                                : null)
                        .appUsersDeletedAt(user.getDeletedAt() != null ? user.getDeletedAt().toString() : null)
                        .build())
                .auditTrail(auditEntries)
                .build();

        // Update GDPR export requested timestamp
        user.setAppUsersGdprDataExportRequestedAt(Instant.now());
        userRepository.save(user);

        logActionInternal(id, AppUserAuditAction.EXPORT_DATA.name(),
                AppUserAuditAction.EXPORT_DATA.getDescription(), id);

        log.info("UserProfileService: User data exported for user: {}", id);
        return exportDto;
    }

    // ========================================
    // Account Lifecycle Methods
    // ========================================

    @Override
    @Transactional
    public void requestAccountDeletion(UUID id, UUID deletedBy) {
        log.info("UserProfileService: Requesting account deletion for user: {} by: {}", id, deletedBy);

        // Note: Admin validation is done at controller level for admindashboard paths.
        // Self-deletion (id == deletedBy) is allowed for GDPR compliance.
        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: Account deletion failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        try {
            userProfile.setDeletedAt(Instant.now());
            userProfile.setAppUsersStatus(AppUserStatus.DELETED);
            userProfile.setDeletedBy(deletedBy);
            userRepository.save(userProfile);
            log.info("Account deletion processed for user: {} by {}", id, deletedBy);
        } catch (Exception e) {
            log.error("Failed to request account deletion for user with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = AppUserCacheConstants.USER_CACHE, key = "#id"),
            @CacheEvict(value = AppUserCacheConstants.USER_LIST_CACHE, allEntries = true),
            @CacheEvict(value = AppUserCacheConstants.USER_BY_EMAIL_CACHE, allEntries = true),
            @CacheEvict(value = AppUserCacheConstants.USER_BY_PHONE_CACHE, allEntries = true)
    })
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Delete user failed: user not found with ID: {}", id);
            throw new AppUserNotFoundException("id", id.toString());
        }
        try {
            userRepository.deleteById(id);
            logActionInternal(id, AppUserAuditAction.DELETE.name(), AppUserAuditAction.DELETE.getDescription(), id);
            log.info("User deleted successfully: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete user with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = AppUserCacheConstants.USER_CACHE, key = "#id")
    public void deactivateUser(UUID id) {
        log.info("Deactivating user with ID: {}", id);
        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Deactivate user failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });
        try {
            userProfile.setAppUsersStatus(AppUserStatus.INACTIVE);
            userProfile.setUpdatedAt(Instant.now());
            userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.DEACTIVATE.name(),
                    AppUserAuditAction.DEACTIVATE.getDescription(), id);
            log.info("User deactivated successfully: {}", id);
        } catch (Exception e) {
            log.error("Failed to deactivate user with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = AppUserCacheConstants.USER_CACHE, key = "#id")
    public void activateUser(UUID id) {
        log.info("Activating user with ID: {}", id);
        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Activate user failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });
        try {
            userProfile.setAppUsersStatus(AppUserStatus.ACTIVE);
            userProfile.setUpdatedAt(Instant.now());
            userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.ACTIVATE.name(), AppUserAuditAction.ACTIVATE.getDescription(),
                    id);
            log.info("User activated successfully: {}", id);
        } catch (Exception e) {
            log.error("Failed to activate user with ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AppUserProfileResponseDto reactivateAccount(AppUserReactivateAccountDto dto) {
        String emailOrPhone = dto.getAppUsersEmailOrPhone();
        log.info("Self-service reactivation request for: {}", emailOrPhone);

        // Find user by email or phone
        Optional<AppUsers> userOpt;
        boolean isEmail = emailOrPhone.contains("@");

        if (isEmail) {
            userOpt = userRepository.findByAppUsersEmail(emailOrPhone);
        } else {
            userOpt = userRepository.findByAppUsersPhoneNumber(emailOrPhone);
        }

        AppUsers user = userOpt.orElseThrow(() -> {
            log.warn("Reactivation failed: user not found with credentials: {}", emailOrPhone);
            return new InvalidCredentialsException("Invalid email/phone or password");
        });

        // Validate password using BCrypt
        if (!passwordEncoder.matches(dto.getAppUsersPassword(), user.getAppUsersPasswordHash())) {
            log.warn("Reactivation failed: invalid password for user: {}", user.getAppUsersId());
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        // Check if account is already active
        if (AppUserStatus.ACTIVE.equals(user.getAppUsersStatus())) {
            log.info("Account is already active for user: {}", user.getAppUsersId());
            return userProfileMapper.toResponseDto(user);
        }

        // Reactivate the account
        user.setAppUsersStatus(AppUserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now());
        user.setAppUsersLastLogin(Instant.now());
        user.setAppUsersFailedLoginAttempts(0);

        AppUsers reactivatedUser = userRepository.save(user);

        logActionInternal(user.getAppUsersId(), AppUserAuditAction.ACTIVATE.name(),
                "Self-service account reactivation", user.getAppUsersId());

        log.info("Account reactivated successfully via self-service for user: {}", user.getAppUsersId());

        return userProfileMapper.toResponseDto(reactivatedUser);
    }

    @Override
    @Transactional
    public AppUserProfileResponseDto recordLogin(UUID id) {
        log.info("Recording login for user: {}", id);
        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Record login failed: user not found with ID: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        try {
            userProfile.setAppUsersLastLogin(Instant.now());
            userProfile.setAppUsersFailedLoginAttempts(0);
            AppUsers updatedUser = userRepository.save(userProfile);
            logActionInternal(id, AppUserAuditAction.LOGIN.name(), AppUserAuditAction.LOGIN.getDescription(), id);

            log.info("UserProfileService: Login recorded for user: {}", id);
            return userProfileMapper.toResponseDto(updatedUser);
        } catch (Exception e) {
            log.error("UserProfileService: Failed to record login for user: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    // ========================================
    // Audit & Activity Methods
    // ========================================

    @Override
    public String getUserActivityLog(UUID id) {
        log.debug("UserProfileService: Fetching activity log for user: {}", id);

        AppUsers userProfile = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UserProfileService: Activity log fetch failed - user not found: {}", id);
                    return new AppUserNotFoundException("id", id.toString());
                });

        // TODO: Return formatted activity log (lastActivityAt, logins, etc.)
        log.debug("UserProfileService: Activity log fetched for user: {}", id);
        return "Last activity: " + userProfile.getAppUsersLastActivityAt();
    }

    // Helper method - Uses BCryptPasswordEncoder for secure password hashing
    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    // Helper method to mask email for privacy (e.g., "j***@example.com")
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    // Helper method to mask phone for privacy (e.g., "***1234")
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return "***" + phone.substring(phone.length() - 4);
    }

    // ========================================
    // Session Management Methods
    // ========================================

    @Override
    @Transactional
    public AppUserSessionDto createSession(UUID userId, String deviceInfo, String ipAddress, String userAgent,
            String location) {
        log.info("UserProfileService: Creating session for user: {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new AppUserNotFoundException("id", userId.toString());
        }

        AppUserSession session = AppUserSession.builder()
                .appUsersSessionsUserId(userId)
                .appUsersSessionsDeviceInfo(deviceInfo)
                .appUsersSessionsIpAddress(ipAddress)
                .appUsersSessionsUserAgent(userAgent)
                .appUsersSessionsLocation(location)
                .appUsersSessionsCreatedAt(Instant.now())
                .appUsersSessionsLastActivityAt(Instant.now())
                .appUsersSessionsExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                .appUsersSessionsIsActive(true)
                .appUsersSessionsToken(UUID.randomUUID().toString())
                .build();

        AppUserSession savedSession = userSessionRepository.save(session);
        log.info("Session created for user: {} with sessionId: {}", userId, savedSession.getAppUsersSessionsId());

        return mapSessionToDto(savedSession, true);
    }

    @Override
    public List<AppUserSessionDto> getActiveSessions(UUID userId) {
        log.info("Getting active sessions for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new AppUserNotFoundException("id", userId.toString());
        }

        List<AppUserSession> sessions = userSessionRepository
                .findByAppUsersSessionsUserIdAndAppUsersSessionsIsActiveTrue(userId);
        log.info("Found {} active sessions for user: {}", sessions.size(), userId);

        return sessions.stream()
                .map(s -> mapSessionToDto(s, false))
                .toList();
    }

    @Override
    @Transactional
    public void invalidateSession(UUID userId, UUID sessionId) {
        log.info("Invalidating session: {} for user: {}", sessionId, userId);

        if (!userRepository.existsById(userId)) {
            throw new AppUserNotFoundException("id", userId.toString());
        }

        int updated = userSessionRepository.invalidateSession(sessionId);
        if (updated == 0) {
            log.warn("Session not found or already invalidated: {}", sessionId);
            throw new InvalidRequestException("session", "Session not found");
        }

        log.info("Session invalidated: {} for user: {}", sessionId, userId);
    }

    @Override
    @Transactional
    public void invalidateAllSessions(UUID userId) {
        log.info("Invalidating all sessions for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new AppUserNotFoundException("id", userId.toString());
        }

        int count = userSessionRepository.invalidateAllUserSessions(userId);
        log.info("Invalidated {} sessions for user: {}", count, userId);
    }

    private AppUserSessionDto mapSessionToDto(AppUserSession session, boolean isCurrent) {
        return AppUserSessionDto.builder()
                .appUsersSessionId(session.getAppUsersSessionsId())
                .appUsersDeviceInfo(session.getAppUsersSessionsDeviceInfo())
                .appUsersIpAddress(session.getAppUsersSessionsIpAddress())
                .appUsersLocation(session.getAppUsersSessionsLocation())
                .appUsersSessionCreatedAt(session.getAppUsersSessionsCreatedAt() != null
                        ? session.getAppUsersSessionsCreatedAt().toString()
                        : null)
                .appUsersLastActivityAt(
                        session.getAppUsersSessionsLastActivityAt() != null
                                ? session.getAppUsersSessionsLastActivityAt().toString()
                                : null)
                .appUsersIsCurrent(isCurrent)
                .build();
    }

    // ========================================
    // OAuth/Social Login Methods
    // ========================================

    @Override
    @Transactional
    public AppUserProfileResponseDto oauthLogin(AppUserOAuthLoginDto dto) {
        log.info("UserProfileService: OAuth login - provider: {}, providerId: {}", dto.getAppUsersProvider(),
                dto.getAppUsersProviderId());

        // Check if user exists with this OAuth provider
        Optional<AppUsers> existingUser = userRepository
                .findByAppUsersOauthProviderAndAppUsersOauthProviderId(
                        dto.getAppUsersProvider(), dto.getAppUsersProviderId());

        if (existingUser.isPresent()) {
            // Update OAuth tokens and login
            AppUsers user = existingUser.get();
            user.setAppUsersOauthAccessToken(dto.getAppUsersAccessToken());
            user.setAppUsersLastLogin(Instant.now());
            user.setAppUsersFailedLoginAttempts(0);
            userRepository.save(user);

            log.info("UserProfileService: OAuth login successful for existing user: {}", user.getAppUsersId());
            return userProfileMapper.toResponseDto(user);
        }

        // Check if user exists with this email
        if (dto.getAppUsersEmail() != null && !dto.getAppUsersEmail().isBlank()) {
            Optional<AppUsers> userByEmail = userRepository.findByAppUsersEmail(dto.getAppUsersEmail());
            if (userByEmail.isPresent()) {
                // Link OAuth to existing account
                AppUsers user = userByEmail.get();
                user.setAppUsersOauthProvider(dto.getAppUsersProvider());
                user.setAppUsersOauthProviderId(dto.getAppUsersProviderId());
                user.setAppUsersOauthAccessToken(dto.getAppUsersAccessToken());
                user.setAppUsersLastLogin(Instant.now());
                if (dto.getAppUsersAvatarUrl() != null && user.getAppUsersAvatarUrl() == null) {
                    user.setAppUsersAvatarUrl(dto.getAppUsersAvatarUrl());
                }
                userRepository.save(user);
                log.info("OAuth linked to existing email account for user: {}", user.getAppUsersId());
                return userProfileMapper.toResponseDto(user);
            }
        }

        // Create new user with OAuth
        AppUsers newUser = new AppUsers();
        newUser.setAppUsersEmail(dto.getAppUsersEmail());
        newUser.setAppUsersPhoneNumber(""); // OAuth users may not have phone
        newUser.setAppUsersFullName(dto.getAppUsersName());
        newUser.setAppUsersAvatarUrl(dto.getAppUsersAvatarUrl());
        newUser.setAppUsersOauthProvider(dto.getAppUsersProvider());
        newUser.setAppUsersOauthProviderId(dto.getAppUsersProviderId());
        newUser.setAppUsersOauthAccessToken(dto.getAppUsersAccessToken());
        newUser.setAppUsersAuthProvider(dto.getAppUsersProvider());
        newUser.setAppUsersEmailVerified(true); // OAuth emails are usually verified by provider
        newUser.setAppUsersStatus(AppUserStatus.ACTIVE);
        newUser.setAppUsersLastLogin(Instant.now());

        AppUsers savedUser = userRepository.save(newUser);

        log.info("UserProfileService: New user created via OAuth: {} (provider: {})",
                savedUser.getAppUsersId(), dto.getAppUsersProvider());
        return userProfileMapper.toResponseDto(savedUser);
    }

    // ========================================
    // Account Lockout Methods
    // ========================================

    @Override
    @Transactional
    @CacheEvict(value = AppUserCacheConstants.USER_CACHE, key = "#userId")
    public void unlockAccount(UUID userId) {
        log.info("UserProfileService: Unlocking account for user: {}", userId);

        AppUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new AppUserNotFoundException("id", userId.toString()));

        user.setAppUsersAccountLocked(false);
        user.setAppUsersAccountLockedAt(null);
        user.setAppUsersAccountLockExpiresAt(null);
        user.setAppUsersFailedLoginAttempts(0);
        userRepository.save(user);

        logActionInternal(userId, "ACCOUNT_UNLOCKED", "Account manually unlocked", userId);

        log.info("UserProfileService: Account unlocked for user: {}", userId);
    }

    @Override
    public boolean isAccountLocked(UUID userId) {
        log.debug("UserProfileService: Checking lock status for user: {}", userId);

        AppUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new AppUserNotFoundException("id", userId.toString()));

        if (!Boolean.TRUE.equals(user.getAppUsersAccountLocked())) {
            return false;
        }

        // Check if lock has expired
        if (user.getAppUsersAccountLockExpiresAt() != null) {
            Instant lockExpiry = user.getAppUsersAccountLockExpiresAt();
            return Instant.now().isBefore(lockExpiry);
        }

        return true;
    }

    // ========================================
    // Bulk Operations Methods
    // ========================================

    @Override
    @Transactional
    public BulkOperationResultDto bulkActivateUsers(List<UUID> userIds) {
        log.info("UserProfileService: Bulk activating {} users", userIds.size());
        return performBulkOperation(userIds, "activate", user -> {
            user.setAppUsersStatus(AppUserStatus.ACTIVE);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public BulkOperationResultDto bulkDeactivateUsers(List<UUID> userIds) {
        log.info("UserProfileService: Bulk deactivating {} users", userIds.size());
        return performBulkOperation(userIds, "deactivate", user -> {
            user.setAppUsersStatus(AppUserStatus.INACTIVE);
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public BulkOperationResultDto bulkDeleteUsers(List<UUID> userIds) {
        log.info("UserProfileService: Bulk deleting {} users", userIds.size());

        List<UUID> successfulIds = new ArrayList<>();
        List<UUID> failedIds = new ArrayList<>();

        for (UUID userId : userIds) {
            try {
                if (userRepository.existsById(userId)) {
                    userRepository.deleteById(userId);
                    successfulIds.add(userId);
                } else {
                    failedIds.add(userId);
                }
            } catch (Exception e) {
                log.error("UserProfileService: Failed to delete user: {}", userId, e);
                failedIds.add(userId);
            }
        }

        return BulkOperationResultDto.builder()
                .successCount(successfulIds.size())
                .failureCount(failedIds.size())
                .successfulIds(successfulIds)
                .failedIds(failedIds)
                .message("Bulk delete completed. Success: " + successfulIds.size() + ", Failed: " + failedIds.size())
                .build();
    }

    @Override
    @Transactional
    public BulkOperationResultDto bulkUnlockAccounts(List<UUID> userIds) {
        log.info("Bulk unlocking {} accounts", userIds.size());
        return performBulkOperation(userIds, "unlock", user -> {
            user.setAppUsersAccountLocked(false);
            user.setAppUsersAccountLockedAt(null);
            user.setAppUsersAccountLockExpiresAt(null);
            user.setAppUsersFailedLoginAttempts(0);
            userRepository.save(user);
        });
    }

    private BulkOperationResultDto performBulkOperation(List<UUID> userIds, String operation,
            java.util.function.Consumer<AppUsers> action) {
        List<UUID> successfulIds = new ArrayList<>();
        List<UUID> failedIds = new ArrayList<>();

        for (UUID userId : userIds) {
            try {
                Optional<AppUsers> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    action.accept(userOpt.get());
                    successfulIds.add(userId);
                } else {
                    failedIds.add(userId);
                }
            } catch (Exception e) {
                log.error("Failed to {} user: {}", operation, userId, e);
                failedIds.add(userId);
            }
        }

        return BulkOperationResultDto.builder()
                .successCount(successfulIds.size())
                .failureCount(failedIds.size())
                .successfulIds(successfulIds)
                .failedIds(failedIds)
                .message("Bulk " + operation + " completed. Success: " + successfulIds.size() + ", Failed: "
                        + failedIds.size())
                .build();
    }

    // ========== CONSOLIDATED AUDIT LOG METHODS ==========

    /**
     * Private helper method for internal audit logging.
     */
    private void logActionInternal(UUID userProfileId, String action, String details, UUID actorId) {
        try {
            auditLogService.logAction(userProfileId, action, details, actorId);
            log.debug("Audit log created: action={}, userProfileId={}, actor={}", action, userProfileId, actorId);
        } catch (Exception e) {
            log.warn("Failed to create audit log for user {}: {}", userProfileId, e.getMessage());
        }
    }

    @Override
    public void logAction(UUID userProfileId, String action, String details, UUID actorId) {
        log.info("Creating user profile audit log for userProfileId: {} action: {}", userProfileId, action);
        try {
            auditLogService.logAction(userProfileId, action, details, actorId);
            log.info("User profile audit log created successfully for userProfileId: {}", userProfileId);
        } catch (Exception e) {
            log.error("Failed to create user profile audit log for userProfileId: {}. Error: {}", userProfileId,
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Ensures the avatar folder exists.
     */
    private void ensureAvatarFolderExists() {
        java.io.File dir = new java.io.File(avatarFolder);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new InvalidRequestException("avatarFolder", "Avatar directory cannot be created: " + avatarFolder);
        }
    }

    /**
     * Resolves the full path for an avatar file.
     */
    private String resolveAvatarFilePath(String filename) {
        return java.nio.file.Path.of(avatarFolder, filename).toString();
    }
}
