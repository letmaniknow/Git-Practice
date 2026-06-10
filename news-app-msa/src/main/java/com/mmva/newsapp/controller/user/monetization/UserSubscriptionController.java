package com.mmva.newsapp.controller.user.monetization;

import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionCancelRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.service.UserSubscriptionService;
import com.mmva.newsapp.infrastructure.security.userdetails.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Subscription Controller.
 *
 * <p>
 * Handles subscription management operations for authenticated users.
 * Located in controller/user/monetization/ per PROJECT_PRINCIPLES.md.
 * </p>
 *
 * <h3>Authentication Required:</h3>
 * <p>
 * Requires authenticated user (any role).
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>View and manage own subscriptions</li>
 * <li>Subscribe to plans</li>
 * <li>Cancel, pause, resume subscriptions</li>
 * <li>Change subscription plans</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/me/subscriptions")
@RequiredArgsConstructor
@Tag(name = "User - Subscription Management", description = "Subscription operations for authenticated users")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class UserSubscriptionController {

    private final UserSubscriptionService subscriptionService;

    // =============================
    // View My Subscriptions
    // =============================

    @Operation(summary = "1. Get my subscriptions")
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserSubscriptionResponseDto>>> getMySubscriptions(
            @AuthenticationPrincipal AppUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        log.debug("User [{}]: Fetching subscriptions", userId);

        List<UserSubscriptionResponseDto> subscriptions = subscriptionService.getUserSubscriptions(userId);

        log.debug("User [{}]: Retrieved {} subscriptions", userId, subscriptions.size());
        return ResponseEntity.ok(ApiResponseDto.success("Subscriptions retrieved", subscriptions));
    }

    @Operation(summary = "2. Get my active subscription")
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> getMyActiveSubscription(
            @AuthenticationPrincipal AppUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        log.debug("User [{}]: Fetching active subscription", userId);

        Optional<UserSubscriptionResponseDto> activeSubscription = subscriptionService
                .getActiveSubscription(userId, "default");

        if (activeSubscription.isEmpty()) {
            log.debug("User [{}]: No active subscription found", userId);
            return ResponseEntity.ok(ApiResponseDto.success("No active subscription", null));
        }

        log.debug("User [{}]: Active subscription retrieved", userId);
        return ResponseEntity.ok(ApiResponseDto.success("Active subscription retrieved", activeSubscription.get()));
    }

    // =============================
    // Manage My Subscriptions
    // =============================

    @Operation(summary = "3. Subscribe to a plan")
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> subscribeToPlan(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @Valid @RequestBody UserSubscriptionRequestDto request) {
        UUID userId = userDetails.getUserId();
        log.debug("User [{}]: Subscribing to plan {}", userId, request.getUserSubscriptionPlanId());

        UserSubscriptionResponseDto response = subscriptionService.subscribe(userId, request);

        log.debug("User [{}]: Successfully subscribed to plan {}", userId, request.getUserSubscriptionPlanId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Successfully subscribed to plan", response));
    }

    @Operation(summary = "4. Cancel my subscription")
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> cancelMySubscription(
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody UserSubscriptionCancelRequestDto request,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        log.debug("User [{}]: Cancelling subscription {}", userId, subscriptionId);

        // Verify the subscription belongs to the user
        UserSubscriptionResponseDto subscription = subscriptionService.getById(subscriptionId);
        if (!subscription.getUserSubscriptionUserId().equals(userId)) {
            log.warn("User [{}] attempted to cancel subscription {} belonging to another user", userId, subscriptionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("Cannot cancel subscription belonging to another user"));
        }

        UserSubscriptionResponseDto response = subscriptionService.cancel(subscriptionId, request);

        log.debug("User [{}]: Subscription {} cancelled", userId, subscriptionId);
        return ResponseEntity.ok(ApiResponseDto.success("Subscription cancelled", response));
    }

    @Operation(summary = "5. Pause my subscription")
    @PostMapping("/{subscriptionId}/pause")
    public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> pauseMySubscription(
            @PathVariable UUID subscriptionId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        log.debug("User [{}]: Pausing subscription {}", userId, subscriptionId);

        // Verify the subscription belongs to the user
        UserSubscriptionResponseDto subscription = subscriptionService.getById(subscriptionId);
        if (!subscription.getUserSubscriptionUserId().equals(userId)) {
            log.warn("User [{}] attempted to pause subscription {} belonging to another user", userId, subscriptionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("Cannot pause subscription belonging to another user"));
        }

        UserSubscriptionResponseDto response = subscriptionService.pause(subscriptionId);

        log.debug("User [{}]: Subscription {} paused", userId, subscriptionId);
        return ResponseEntity.ok(ApiResponseDto.success("Subscription paused", response));
    }

    @Operation(summary = "6. Resume my subscription")
    @PostMapping("/{subscriptionId}/resume")
    public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> resumeMySubscription(
            @PathVariable UUID subscriptionId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        log.debug("User [{}]: Resuming subscription {}", userId, subscriptionId);

        // Verify the subscription belongs to the user
        UserSubscriptionResponseDto subscription = subscriptionService.getById(subscriptionId);
        if (!subscription.getUserSubscriptionUserId().equals(userId)) {
            log.warn("User [{}] attempted to resume subscription {} belonging to another user", userId, subscriptionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("Cannot resume subscription belonging to another user"));
        }

        UserSubscriptionResponseDto response = subscriptionService.resume(subscriptionId);

        log.debug("User [{}]: Subscription {} resumed", userId, subscriptionId);
        return ResponseEntity.ok(ApiResponseDto.success("Subscription resumed", response));
    }

    @Operation(summary = "7. Change my subscription plan")
    @PostMapping("/{subscriptionId}/change-plan/{newPlanId}")
    public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> changeMyPlan(
            @PathVariable UUID subscriptionId,
            @PathVariable UUID newPlanId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        log.debug("User [{}]: Changing subscription {} to plan {}", userId, subscriptionId, newPlanId);

        // Verify the subscription belongs to the user
        UserSubscriptionResponseDto subscription = subscriptionService.getById(subscriptionId);
        if (!subscription.getUserSubscriptionUserId().equals(userId)) {
            log.warn("User [{}] attempted to change plan for subscription {} belonging to another user", userId,
                    subscriptionId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDto.error("Cannot change plan for subscription belonging to another user"));
        }

        UserSubscriptionResponseDto response = subscriptionService.changePlan(subscriptionId, newPlanId);

        log.debug("User [{}]: Subscription {} plan changed to {}", userId, subscriptionId, newPlanId);
        return ResponseEntity.ok(ApiResponseDto.success("Plan changed successfully", response));
    }
}