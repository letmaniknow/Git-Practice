package com.mmva.newsapp.controller.admin.subscriptions;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.UserSubscriptionStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.*;
import com.mmva.newsapp.infrastructure.monetization.subscription.service.UserSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

/**
 * Admin controller for user subscription management.
 *
 * <p>
 * Located in controller/admin/monetization/ per PROJECT_PRINCIPLES.md.
 * </p>
 *
 * <h3>Authentication Required:</h3>
 * <p>
 * Requires ADMIN or SUPER_ADMIN role.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>View and manage user subscriptions</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/user-subscriptions")
@RequiredArgsConstructor
@Tag(name = "Admin - User Subscription Management", description = "Admin operations for user subscription management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserSubscriptionController {

        // =============================
        // Fetch User Subscriptions
        // =============================

        private final UserSubscriptionService subscriptionService;

        @Operation(summary = "1. Get subscription by ID")
        @GetMapping("/{subscriptionId}")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> getSubscriptionById(
                        @PathVariable UUID subscriptionId) {
                return ResponseEntity
                                .ok(ApiResponseDto.success("Subscription retrieved",
                                                subscriptionService.getById(subscriptionId)));
        }

        @Operation(summary = "2. Get subscriptions by status")
        @GetMapping("/by-status/{status}")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<Page<UserSubscriptionResponseDto>>> getSubscriptionsByStatus(
                        @PathVariable UserSubscriptionStatus status,
                        @RequestParam(defaultValue = "default") String tenantId,
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
                return ResponseEntity.ok(ApiResponseDto.success("Subscriptions retrieved",
                                subscriptionService.getByStatus(status, tenantId, pageable)));
        }

        @Operation(summary = "3. Get subscriptions for a plan")
        @GetMapping("/by-plan/{planId}")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<Page<UserSubscriptionResponseDto>>> getSubscriptionsByPlan(
                        @PathVariable UUID planId,
                        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
                return ResponseEntity
                                .ok(ApiResponseDto.success("Subscriptions retrieved",
                                                subscriptionService.getByPlan(planId, pageable)));
        }

        @Operation(summary = "4. Get user's subscriptions")

        // =============================
        // Manage User Subscriptions
        // =============================
        @GetMapping("/user/{userId}")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<List<UserSubscriptionResponseDto>>> getUserSubscriptions(
                        @PathVariable UUID userId) {
                return ResponseEntity.ok(ApiResponseDto.success("User subscriptions retrieved",
                                subscriptionService.getUserSubscriptions(userId)));
        }

        @Operation(summary = "5. Create subscription for user (admindashboard)")
        @PostMapping("/user/{userId}/subscribe")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> subscribeUser(
                        @PathVariable UUID userId,
                        @Valid @RequestBody UserSubscriptionRequestDto request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.success("User subscribed",
                                                subscriptionService.subscribe(userId, request)));
        }

        @Operation(summary = "6. Cancel subscription (admindashboard)")
        @PostMapping("/{subscriptionId}/cancel")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> cancelSubscription(
                        @PathVariable UUID subscriptionId,
                        @Valid @RequestBody UserSubscriptionCancelRequestDto request) {
                return ResponseEntity.ok(
                                ApiResponseDto.success("Subscription cancelled",
                                                subscriptionService.cancel(subscriptionId, request)));
        }

        @Operation(summary = "7. Pause subscription (admindashboard)")
        @PostMapping("/{subscriptionId}/pause")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> pauseSubscription(
                        @PathVariable UUID subscriptionId) {
                return ResponseEntity
                                .ok(ApiResponseDto.success("Subscription paused",
                                                subscriptionService.pause(subscriptionId)));
        }

        @Operation(summary = "8. Resume subscription (admindashboard)")
        @PostMapping("/{subscriptionId}/resume")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> resumeSubscription(
                        @PathVariable UUID subscriptionId) {
                return ResponseEntity
                                .ok(ApiResponseDto.success("Subscription resumed",
                                                subscriptionService.resume(subscriptionId)));
        }

        @Operation(summary = "9. Change subscription plan (admindashboard)")
        @PostMapping("/{subscriptionId}/change-plan/{newPlanId}")
        @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponseDto<UserSubscriptionResponseDto>> changePlan(
                        @PathVariable UUID subscriptionId,
                        @PathVariable UUID newPlanId) {
                return ResponseEntity
                                .ok(ApiResponseDto.success("Plan changed",
                                                subscriptionService.changePlan(subscriptionId, newPlanId)));
        }
}
