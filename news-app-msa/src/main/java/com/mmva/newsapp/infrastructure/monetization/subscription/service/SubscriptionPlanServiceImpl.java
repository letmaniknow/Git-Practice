package com.mmva.newsapp.infrastructure.monetization.subscription.service;

import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionPlanBillingCycle;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionPlanRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionPlanResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.mapper.SubscriptionPlanMapper;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionPlan;
import com.mmva.newsapp.infrastructure.monetization.subscription.repository.SubscriptionPlanRepository;
import com.mmva.newsapp.infrastructure.monetization.subscription.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link SubscriptionPlanService}.
 * 
 * <p>
 * Provides subscription plan management with full CRUD operations,
 * multi-tenant support, and business validation.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanMapper planMapper;

    // =========================
    // Create Operations
    // =========================

    @Override
    @Transactional
    public SubscriptionPlanResponseDto create(SubscriptionPlanRequestDto dto) {
        log.info("SubscriptionPlan.create - Creating plan with code: {}", dto.getSubscriptionPlanCode());

        // Validate plan code uniqueness
        String tenantId = dto.getSubscriptionPlanTenantId() != null ? dto.getSubscriptionPlanTenantId() : "default";
        if (planRepository.existsByPlanCodeAndTenantId(dto.getSubscriptionPlanCode(), tenantId)) {
            log.warn("SubscriptionPlan.create - Plan code already exists: {}", dto.getSubscriptionPlanCode());
            throw new IllegalArgumentException("Plan code already exists: " + dto.getSubscriptionPlanCode());
        }

        // Map and save
        SubscriptionPlan entity = planMapper.toEntity(dto);
        entity.setSubscriptionPlanTenantId(tenantId);

        SubscriptionPlan saved = planRepository.save(entity);
        log.info("SubscriptionPlan.create - Created plan with ID: {}", saved.getSubscriptionPlanId());

        return planMapper.toResponseDto(saved);
    }

    // =========================
    // Read Operations
    // =========================

    @Override
    public SubscriptionPlanResponseDto getById(UUID planId) {
        log.debug("SubscriptionPlan.getById - Fetching plan: {}", planId);

        SubscriptionPlan plan = findPlanOrThrow(planId);
        return planMapper.toResponseDto(plan);
    }

    @Override
    public SubscriptionPlanResponseDto getByCode(String planCode, String tenantId) {
        log.debug("SubscriptionPlan.getByCode - Fetching plan: {} for tenant: {}", planCode, tenantId);

        SubscriptionPlan plan = planRepository.findByPlanCodeAndTenantId(planCode, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "planCode", planCode));

        return planMapper.toResponseDto(plan);
    }

    @Override
    public List<SubscriptionPlanResponseDto> getActivePlans(String tenantId) {
        log.debug("SubscriptionPlan.getActivePlans - Fetching active plans for tenant: {}", tenantId);

        List<SubscriptionPlan> plans = planRepository.findActiveByTenantId(tenantId);
        return planMapper.toResponseDtoList(plans);
    }

    @Override
    public List<SubscriptionPlanResponseDto> getAllPlans(String tenantId) {
        log.debug("SubscriptionPlan.getAllPlans - Fetching all plans for tenant: {}", tenantId);

        List<SubscriptionPlan> plans = planRepository.findAllByTenantId(tenantId);
        return planMapper.toResponseDtoList(plans);
    }

    @Override
    public List<SubscriptionPlanResponseDto> getByTier(Integer tierCode, String tenantId) {
        log.debug("SubscriptionPlan.getByTier - Fetching plans for tier: {} tenant: {}", tierCode, tenantId);

        List<SubscriptionPlan> plans = planRepository.findActiveByTierCodeAndTenantId(tierCode, tenantId);
        return planMapper.toResponseDtoList(plans);
    }

    @Override
    public List<SubscriptionPlanResponseDto> getByBillingCycle(SubscriptionPlanBillingCycle billingCycle,
            String tenantId) {
        log.debug("SubscriptionPlan.getByBillingCycle - Fetching plans for cycle: {} tenant: {}",
                billingCycle, tenantId);

        List<SubscriptionPlan> plans = planRepository.findActiveByBillingCycleAndTenantId(billingCycle, tenantId);
        return planMapper.toResponseDtoList(plans);
    }

    @Override
    public SubscriptionPlanResponseDto getRecommendedPlan(String tenantId) {
        log.debug("SubscriptionPlan.getRecommendedPlan - Fetching recommended plan for tenant: {}", tenantId);

        return planRepository.findRecommendedByTenantId(tenantId)
                .map(planMapper::toResponseDto)
                .orElse(null);
    }

    // =========================
    // Update Operations
    // =========================

    @Override
    @Transactional
    public SubscriptionPlanResponseDto update(UUID planId, SubscriptionPlanRequestDto dto) {
        log.info("SubscriptionPlan.update - Updating plan: {}", planId);

        SubscriptionPlan existing = findPlanOrThrow(planId);

        // Check if plan code is being changed and validate uniqueness
        if (!existing.getSubscriptionPlanCode().equals(dto.getSubscriptionPlanCode())) {
            String tenantId = dto.getSubscriptionPlanTenantId() != null ? dto.getSubscriptionPlanTenantId()
                    : existing.getSubscriptionPlanTenantId();
            if (planRepository.existsByPlanCodeAndTenantId(dto.getSubscriptionPlanCode(), tenantId)) {
                throw new IllegalArgumentException("Plan code already exists: " + dto.getSubscriptionPlanCode());
            }
        }

        planMapper.updateEntityFromDto(dto, existing);
        SubscriptionPlan saved = planRepository.save(existing);

        log.info("SubscriptionPlan.update - Updated plan: {}", planId);
        return planMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponseDto activate(UUID planId) {
        log.info("SubscriptionPlan.activate - Activating plan: {}", planId);

        SubscriptionPlan plan = findPlanOrThrow(planId);
        plan.setSubscriptionPlanIsActive(true);

        SubscriptionPlan saved = planRepository.save(plan);
        log.info("SubscriptionPlan.activate - Activated plan: {}", planId);

        return planMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponseDto deactivate(UUID planId) {
        log.info("SubscriptionPlan.deactivate - Deactivating plan: {}", planId);

        SubscriptionPlan plan = findPlanOrThrow(planId);
        plan.setSubscriptionPlanIsActive(false);

        SubscriptionPlan saved = planRepository.save(plan);
        log.info("SubscriptionPlan.deactivate - Deactivated plan: {}", planId);

        return planMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponseDto setAsRecommended(UUID planId) {
        log.info("SubscriptionPlan.setAsRecommended - Setting plan {} as recommended", planId);

        SubscriptionPlan plan = findPlanOrThrow(planId);
        String tenantId = plan.getSubscriptionPlanTenantId();

        // Unset current recommended plan
        planRepository.findRecommendedByTenantId(tenantId).ifPresent(current -> {
            if (!current.getSubscriptionPlanId().equals(planId)) {
                current.setSubscriptionPlanIsRecommended(false);
                planRepository.save(current);
            }
        });

        // Set new recommended
        plan.setSubscriptionPlanIsRecommended(true);
        SubscriptionPlan saved = planRepository.save(plan);

        log.info("SubscriptionPlan.setAsRecommended - Plan {} is now recommended", planId);
        return planMapper.toResponseDto(saved);
    }

    // =========================
    // Delete Operations
    // =========================

    @Override
    @Transactional
    public void delete(UUID planId) {
        log.info("SubscriptionPlan.delete - Deleting plan: {}", planId);

        SubscriptionPlan plan = findPlanOrThrow(planId);

        // Check for active subscriptions
        long activeCount = subscriptionRepository.countActiveByPlanId(planId);
        if (activeCount > 0) {
            log.warn("SubscriptionPlan.delete - Cannot delete plan with {} active subscriptions", activeCount);
            throw new IllegalStateException(
                    "Cannot delete plan with " + activeCount + " active subscriptions. Deactivate instead.");
        }

        // Soft delete
        plan.setDeletedAt(Instant.now());
        plan.setSubscriptionPlanIsActive(false);
        planRepository.save(plan);

        log.info("SubscriptionPlan.delete - Soft deleted plan: {}", planId);
    }

    // =========================
    // Validation Operations
    // =========================

    @Override
    public boolean existsByCode(String planCode, String tenantId) {
        return planRepository.existsByPlanCodeAndTenantId(planCode, tenantId);
    }

    @Override
    public long countActiveSubscribers(UUID planId) {
        return subscriptionRepository.countActiveByPlanId(planId);
    }

    // =========================
    // Private Helpers
    // =========================

    private SubscriptionPlan findPlanOrThrow(UUID planId) {
        return planRepository.findById(planId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "planId", planId));
    }
}
