package com.mmva.newsapp.controller.admin.subscriptions;

import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.*;
import com.mmva.newsapp.infrastructure.monetization.subscription.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.mmva.newsapp.infrastructure.common.api.dto.ApiResponseDto;

/**
 * Admin controller for subscription plan management.
 *
 * <p>
 * Located in controller/admin/monetization/ per PROJECT_PRINCIPLES.md.
 * </p>
 *
 * <h3>Authentication Required:</h3>
 * <p>
 * Requires admin authentication and SUBSCRIPTION_MANAGE permission.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>Manage subscription plans</li>
 * <li>Subscription analytics</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Admin - Subscription Plan Management", description = "Admin operations for subscription plan management")
@SecurityRequirement(name = "bearerAuth")
public class AdminSubscriptionPlanController {

    private final SubscriptionPlanService planService;

    @Operation(summary = "1. Create subscription plan", description = """
            Creates a new subscription plan with specified pricing, features, and billing terms.

            **Tier Codes:**
            - `0`: Free
            - `1`: Basic
            - `2`: Pro
            - `3`: Enterprise
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or plan code exists"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<SubscriptionPlanResponseDto>> createPlan(
            @Valid @RequestBody SubscriptionPlanRequestDto request) {
        log.info("AdminSubscriptionPlan.createPlan - Creating plan: {}", request.getSubscriptionPlanCode());
        SubscriptionPlanResponseDto response = planService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success("Plan created", response));
    }

    @Operation(summary = "2. Get subscription plan by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan found"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    @GetMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<SubscriptionPlanResponseDto>> getPlanById(
            @Parameter(description = "Plan ID") @PathVariable UUID planId) {
        log.debug("AdminSubscriptionPlan.getPlanById - Fetching plan: {}", planId);
        SubscriptionPlanResponseDto response = planService.getById(planId);
        return ResponseEntity.ok(ApiResponseDto.success("Plan retrieved", response));
    }

    @Operation(summary = "3. Get all subscription plans", description = """
            Retrieves all subscription plans for a tenant.
            Use `activeOnly=true` to filter to only active plans.
            """)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<SubscriptionPlanResponseDto>>> getAllPlans(
            @Parameter(description = "Tenant ID") @RequestParam(defaultValue = "default") String tenantId,
            @Parameter(description = "Only return active plans") @RequestParam(defaultValue = "false") boolean activeOnly) {
        log.debug("AdminSubscriptionPlan.getAllPlans - Tenant: {} ActiveOnly: {}", tenantId, activeOnly);
        List<SubscriptionPlanResponseDto> response = activeOnly
                ? planService.getActivePlans(tenantId)
                : planService.getAllPlans(tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Plans retrieved", response));
    }

    @Operation(summary = "4. Get plans by tier")
    @GetMapping("/by-tier/{tierCode}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<SubscriptionPlanResponseDto>>> getPlansByTier(
            @Parameter(description = "Tier code (0-3)") @PathVariable Integer tierCode,
            @RequestParam(defaultValue = "default") String tenantId) {
        log.debug("AdminSubscriptionPlan.getPlansByTier - Tier: {} Tenant: {}", tierCode, tenantId);
        List<SubscriptionPlanResponseDto> response = planService.getByTier(tierCode, tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Plans retrieved", response));
    }

    @Operation(summary = "5. Get plans by billing cycle")
    @GetMapping("/by-cycle/{billingCycle}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<List<SubscriptionPlanResponseDto>>> getPlansByBillingCycle(
            @Parameter(description = "Billing cycle") @PathVariable SubscriptionPlanBillingCycle billingCycle,
            @RequestParam(defaultValue = "default") String tenantId) {
        log.debug("AdminSubscriptionPlan.getPlansByBillingCycle - Cycle: {} Tenant: {}", billingCycle, tenantId);
        List<SubscriptionPlanResponseDto> response = planService.getByBillingCycle(billingCycle, tenantId);
        return ResponseEntity.ok(ApiResponseDto.success("Plans retrieved", response));
    }

    @Operation(summary = "6. Update subscription plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan updated"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<SubscriptionPlanResponseDto>> updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody SubscriptionPlanRequestDto request) {
        log.info("AdminSubscriptionPlan.updatePlan - Updating plan: {}", planId);
        SubscriptionPlanResponseDto response = planService.update(planId, request);
        return ResponseEntity.ok(ApiResponseDto.success("Plan updated", response));
    }

    @Operation(summary = "7. Activate subscription plan")
    @PostMapping("/{planId}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<SubscriptionPlanResponseDto>> activatePlan(@PathVariable UUID planId) {
        log.info("AdminSubscriptionPlan.activatePlan - Activating plan: {}", planId);
        SubscriptionPlanResponseDto response = planService.activate(planId);
        return ResponseEntity.ok(ApiResponseDto.success("Plan activated", response));
    }

    @Operation(summary = "8. Deactivate subscription plan")
    @PostMapping("/{planId}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<SubscriptionPlanResponseDto>> deactivatePlan(@PathVariable UUID planId) {
        log.info("AdminSubscriptionPlan.deactivatePlan - Deactivating plan: {}", planId);
        SubscriptionPlanResponseDto response = planService.deactivate(planId);
        return ResponseEntity.ok(ApiResponseDto.success("Plan deactivated", response));
    }

    @Operation(summary = "9. Set plan as recommended")
    @PostMapping("/{planId}/recommend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<SubscriptionPlanResponseDto>> setAsRecommended(@PathVariable UUID planId) {
        log.info("AdminSubscriptionPlan.setAsRecommended - Setting plan {} as recommended", planId);
        SubscriptionPlanResponseDto response = planService.setAsRecommended(planId);
        return ResponseEntity.ok(ApiResponseDto.success("Plan set as recommended", response));
    }

    @Operation(summary = "10. Delete subscription plan", description = """
            Soft deletes a subscription plan.
            Cannot delete plans with active subscriptions.
            """)
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deletePlan(@PathVariable UUID planId) {
        log.info("AdminSubscriptionPlan.deletePlan - Deleting plan: {}", planId);
        planService.delete(planId);
        return ResponseEntity.ok(ApiResponseDto.success("Plan deleted", null));
    }

    @Operation(summary = "11. Get subscription statistics")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getStats(
            @RequestParam(defaultValue = "default") String tenantId) {
        log.debug("AdminSubscriptionPlan.getStats - Tenant: {}", tenantId);
        SubscriptionPlanResponseDto recommended = planService.getRecommendedPlan(tenantId);
        Map<String, Object> stats = Map.of(
                "activePlansCount", planService.getActivePlans(tenantId).size(),
                "totalPlansCount", planService.getAllPlans(tenantId).size(),
                "recommendedPlan", recommended != null ? recommended.getSubscriptionPlanCode() : "none");
        return ResponseEntity.ok(ApiResponseDto.success("Subscription stats retrieved", stats));
    }
}
