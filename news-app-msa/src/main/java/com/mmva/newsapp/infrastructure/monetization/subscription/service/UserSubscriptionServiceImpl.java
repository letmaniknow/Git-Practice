package com.mmva.newsapp.infrastructure.monetization.subscription.service;

import com.mmva.newsapp.infrastructure.common.exception.ActiveSubscriptionExistsException;
import com.mmva.newsapp.infrastructure.common.exception.ResourceNotFoundException;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.UserSubscriptionStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionCancelRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.UserSubscriptionResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.mapper.UserSubscriptionMapper;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionPlan;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.UserSubscription;
import com.mmva.newsapp.infrastructure.monetization.subscription.repository.SubscriptionPlanRepository;
import com.mmva.newsapp.infrastructure.monetization.subscription.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link UserSubscriptionService}.
 * 
 * <p>
 * Provides subscription lifecycle management with multi-tenant support.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionMapper subscriptionMapper;

    // =========================
    // Subscribe Operations
    // =========================

    @Override
    @Transactional
    public UserSubscriptionResponseDto subscribe(UUID userId, UserSubscriptionRequestDto dto) {
        log.info("UserSubscription.subscribe - User {} subscribing to plan {}", userId,
                dto.getUserSubscriptionPlanId());

        String tenantId = dto.getUserSubscriptionTenantId() != null ? dto.getUserSubscriptionTenantId() : "default";

        // Check for existing active subscription
        if (subscriptionRepository.hasActiveSubscription(userId, tenantId)) {
            log.warn("UserSubscription.subscribe - User {} already has active subscription", userId);

            // Get existing subscription details for graceful error handling
            UserSubscriptionResponseDto existingSubscription = getActiveSubscription(userId, tenantId)
                    .orElseThrow(() -> new IllegalStateException(
                            "User has active subscription but cannot retrieve details"));

            // Get all available plans for upgrade suggestions
            List<SubscriptionPlan> availablePlans = planRepository.findAllActive().stream()
                    .filter(plan -> !plan.getSubscriptionPlanId()
                            .equals(existingSubscription.getUserSubscriptionPlanId()))
                    .toList();

            // Determine available actions based on current plan
            List<String> availableActions = new ArrayList<>();
            availableActions.add("cancel_current_subscription");
            availableActions.add("view_current_subscription");

            // Add upgrade/downgrade options
            for (SubscriptionPlan plan : availablePlans) {
                if (plan.getSubscriptionPlanPrice()
                        .compareTo(existingSubscription.getUserSubscriptionCurrentPrice()) > 0) {
                    availableActions.add("upgrade_to_" + plan.getSubscriptionPlanCode());
                } else if (plan.getSubscriptionPlanPrice()
                        .compareTo(existingSubscription.getUserSubscriptionCurrentPrice()) < 0) {
                    availableActions.add("downgrade_to_" + plan.getSubscriptionPlanCode());
                } else {
                    availableActions.add("switch_to_" + plan.getSubscriptionPlanCode());
                }
            }

            String suggestion = "You already have an active "
                    + existingSubscription.getUserSubscriptionPlanName() +
                    " subscription. Would you like to upgrade, downgrade, or manage your current subscription?";

            throw new ActiveSubscriptionExistsException(suggestion, existingSubscription, availableActions);
        }

        // Get the plan
        SubscriptionPlan plan = planRepository.findById(dto.getUserSubscriptionPlanId())
                .filter(p -> p.getSubscriptionPlanIsActive() && p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "planId",
                        dto.getUserSubscriptionPlanId()));

        // Create subscription
        Instant now = Instant.now();
        UserSubscription subscription = UserSubscription.builder()
                .userSubscriptionUserId(userId)
                .userSubscriptionPlan(plan)
                .userSubscriptionTenantId(tenantId)
                .userSubscriptionStartedAt(now)
                .userSubscriptionCurrentPrice(plan.getSubscriptionPlanPrice())
                .userSubscriptionCurrency(plan.getSubscriptionPlanCurrency())
                .userSubscriptionPromoCode(dto.getUserSubscriptionPromoCode())
                .userSubscriptionMetadataJson(dto.getUserSubscriptionMetadataJson())
                .build();

        // Set initial status and dates based on trial availability
        boolean startWithTrial = Boolean.TRUE.equals(dto.getUserSubscriptionStartWithTrial()) && plan.hasTrialPeriod();

        if (startWithTrial) {
            subscription.setUserSubscriptionStatus(UserSubscriptionStatus.TRIAL);
            subscription.setUserSubscriptionTrialEnd(now.plus(plan.getSubscriptionPlanTrialDays(), ChronoUnit.DAYS));
            subscription.setUserSubscriptionCurrentPeriodStart(now);
            subscription.setUserSubscriptionCurrentPeriodEnd(subscription.getUserSubscriptionTrialEnd());
            log.info("UserSubscription.subscribe - Starting trial for {} days", plan.getSubscriptionPlanTrialDays());
        } else {
            subscription.setUserSubscriptionStatus(UserSubscriptionStatus.ACTIVE);
            subscription.setUserSubscriptionCurrentPeriodStart(now);
            subscription.setUserSubscriptionCurrentPeriodEnd(
                    now.plus(plan.getSubscriptionPlanBillingCycle().getDays(), ChronoUnit.DAYS));
            subscription.setUserSubscriptionNextPaymentAt(subscription.getUserSubscriptionCurrentPeriodEnd());
        }

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("UserSubscription.subscribe - Created subscription {} for user {}",
                saved.getUserSubscriptionId(), userId);

        return subscriptionMapper.toResponseDto(saved);
    }

    // =========================
    // Read Operations
    // =========================

    @Override
    public UserSubscriptionResponseDto getById(UUID subscriptionId) {
        log.debug("UserSubscription.getById - Fetching subscription: {}", subscriptionId);

        UserSubscription subscription = findSubscriptionOrThrow(subscriptionId);
        return subscriptionMapper.toResponseDto(subscription);
    }

    @Override
    public Optional<UserSubscriptionResponseDto> getActiveSubscription(UUID userId, String tenantId) {
        log.debug("UserSubscription.getActiveSubscription - User: {} Tenant: {}", userId, tenantId);

        return subscriptionRepository.findActiveByUserIdAndTenantId(userId, tenantId)
                .map(subscriptionMapper::toResponseDto);
    }

    @Override
    public List<UserSubscriptionResponseDto> getUserSubscriptions(UUID userId) {
        log.debug("UserSubscription.getUserSubscriptions - User: {}", userId);

        List<UserSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptionMapper.toResponseDtoList(subscriptions);
    }

    @Override
    public boolean hasActiveSubscription(UUID userId, String tenantId) {
        return subscriptionRepository.hasActiveSubscription(userId, tenantId);
    }

    @Override
    public int getUserTierCode(UUID userId, String tenantId) {
        return subscriptionRepository.findActiveByUserIdAndTenantId(userId, tenantId)
                .map(UserSubscription::getTierCode)
                .orElse(0);
    }

    @Override
    public boolean canAccessTier(UUID userId, int requiredTier, String tenantId) {
        int userTier = getUserTierCode(userId, tenantId);
        return userTier >= requiredTier;
    }

    // =========================
    // Admin Query Operations
    // =========================

    @Override
    public Page<UserSubscriptionResponseDto> getByStatus(
            UserSubscriptionStatus status,
            String tenantId,
            Pageable pageable) {
        log.debug("UserSubscription.getByStatus - Status: {} Tenant: {}", status, tenantId);

        return subscriptionRepository.findByStatusAndTenantId(status, tenantId, pageable)
                .map(subscriptionMapper::toResponseDto);
    }

    @Override
    public Page<UserSubscriptionResponseDto> getByPlan(UUID planId, Pageable pageable) {
        log.debug("UserSubscription.getByPlan - Plan: {}", planId);

        return subscriptionRepository.findByPlanId(planId, pageable)
                .map(subscriptionMapper::toResponseDto);
    }

    // =========================
    // Lifecycle Operations
    // =========================

    @Override
    @Transactional
    public UserSubscriptionResponseDto cancel(UUID subscriptionId, UserSubscriptionCancelRequestDto dto) {
        log.info("UserSubscription.cancel - Cancelling subscription: {}", subscriptionId);

        UserSubscription subscription = findSubscriptionOrThrow(subscriptionId);
        Instant now = Instant.now();

        subscription.setUserSubscriptionCancelledAt(now);
        subscription.setUserSubscriptionCancellationReason(dto.getUserSubscriptionCancelReason());

        if (Boolean.TRUE.equals(dto.getUserSubscriptionCancelImmediately())) {
            subscription.setUserSubscriptionStatus(UserSubscriptionStatus.CANCELLED);
            subscription.setUserSubscriptionCurrentPeriodEnd(now);
        } else {
            subscription.setUserSubscriptionCancelAtPeriodEnd(true);
        }

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("UserSubscription.cancel - Cancelled subscription: {} (immediate: {})",
                subscriptionId, dto.getUserSubscriptionCancelImmediately());

        return subscriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public UserSubscriptionResponseDto pause(UUID subscriptionId) {
        log.info("UserSubscription.pause - Pausing subscription: {}", subscriptionId);

        UserSubscription subscription = findSubscriptionOrThrow(subscriptionId);

        if (subscription.getUserSubscriptionStatus() != UserSubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Can only pause active subscriptions");
        }

        subscription.setUserSubscriptionStatus(UserSubscriptionStatus.PAUSED);
        subscription.setUserSubscriptionPausedAt(Instant.now());

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("UserSubscription.pause - Paused subscription: {}", subscriptionId);

        return subscriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public UserSubscriptionResponseDto resume(UUID subscriptionId) {
        log.info("UserSubscription.resume - Resuming subscription: {}", subscriptionId);

        UserSubscription subscription = findSubscriptionOrThrow(subscriptionId);

        if (subscription.getUserSubscriptionStatus() != UserSubscriptionStatus.PAUSED) {
            throw new IllegalStateException("Can only resume paused subscriptions");
        }

        subscription.setUserSubscriptionStatus(UserSubscriptionStatus.ACTIVE);
        subscription.setUserSubscriptionPausedAt(null);
        subscription.setUserSubscriptionResumeAt(null);

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("UserSubscription.resume - Resumed subscription: {}", subscriptionId);

        return subscriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public UserSubscriptionResponseDto changePlan(UUID subscriptionId, UUID newPlanId) {
        log.info("UserSubscription.changePlan - Changing subscription {} to plan {}",
                subscriptionId, newPlanId);

        UserSubscription subscription = findSubscriptionOrThrow(subscriptionId);

        SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                .filter(p -> p.getSubscriptionPlanIsActive() && p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "planId", newPlanId));

        subscription.setUserSubscriptionPlan(newPlan);
        subscription.setUserSubscriptionCurrentPrice(newPlan.getSubscriptionPlanPrice());

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("UserSubscription.changePlan - Changed subscription {} to plan {}",
                subscriptionId, newPlanId);

        return subscriptionMapper.toResponseDto(saved);
    }

    // =========================
    // Payment Integration
    // =========================

    @Override
    @Transactional
    public UserSubscriptionResponseDto recordPayment(UUID subscriptionId, String externalTransactionId) {
        log.info("UserSubscription.recordPayment - Subscription: {} Transaction: {}",
                subscriptionId, externalTransactionId);

        UserSubscription subscription = findSubscriptionOrThrow(subscriptionId);
        Instant now = Instant.now();

        // Update subscription
        subscription.setUserSubscriptionStatus(UserSubscriptionStatus.ACTIVE);
        subscription.setUserSubscriptionLastPaymentAt(now);
        subscription.setUserSubscriptionFailedPaymentAttempts(0);
        subscription.setUserSubscriptionBillingCyclesCompleted(
                subscription.getUserSubscriptionBillingCyclesCompleted() + 1);

        // Extend billing period
        if (subscription.getUserSubscriptionPlan() != null
                && subscription.getUserSubscriptionPlan().getSubscriptionPlanBillingCycle() != null) {
            int days = subscription.getUserSubscriptionPlan().getSubscriptionPlanBillingCycle().getDays();
            subscription.setUserSubscriptionCurrentPeriodStart(now);
            subscription.setUserSubscriptionCurrentPeriodEnd(now.plus(days, ChronoUnit.DAYS));
            subscription.setUserSubscriptionNextPaymentAt(subscription.getUserSubscriptionCurrentPeriodEnd());
        }

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.info("UserSubscription.recordPayment - Payment recorded for subscription: {}", subscriptionId);

        return subscriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public UserSubscriptionResponseDto recordPaymentFailure(UUID subscriptionId, String failureReason) {
        log.warn("UserSubscription.recordPaymentFailure - Subscription: {} Reason: {}",
                subscriptionId, failureReason);

        UserSubscription subscription = findSubscriptionOrThrow(subscriptionId);

        subscription
                .setUserSubscriptionFailedPaymentAttempts(subscription.getUserSubscriptionFailedPaymentAttempts() + 1);

        // Move to past due after first failure
        if (subscription.getUserSubscriptionStatus() == UserSubscriptionStatus.ACTIVE) {
            subscription.setUserSubscriptionStatus(UserSubscriptionStatus.PAST_DUE);
        }

        // Cancel after too many failures
        if (subscription.getUserSubscriptionFailedPaymentAttempts() >= 4) {
            subscription.setUserSubscriptionStatus(UserSubscriptionStatus.CANCELLED);
            subscription.setUserSubscriptionCancelledAt(Instant.now());
            subscription.setUserSubscriptionCancellationReason("Payment failed repeatedly");
        }

        UserSubscription saved = subscriptionRepository.save(subscription);
        log.warn("UserSubscription.recordPaymentFailure - Failed payment recorded, attempts: {}",
                saved.getUserSubscriptionFailedPaymentAttempts());

        return subscriptionMapper.toResponseDto(saved);
    }

    // =========================
    // Background Operations
    // =========================

    @Override
    @Transactional
    public int processExpiredTrials(String tenantId) {
        log.info("UserSubscription.processExpiredTrials - Processing for tenant: {}", tenantId);

        int count = subscriptionRepository.expireTrialSubscriptions(Instant.now(), tenantId);
        log.info("UserSubscription.processExpiredTrials - Processed {} expired trials", count);

        return count;
    }

    @Override
    @Transactional
    public int processExpiredSubscriptions(String tenantId) {
        log.info("UserSubscription.processExpiredSubscriptions - Processing for tenant: {}", tenantId);

        int count = subscriptionRepository.expireCancelledSubscriptions(Instant.now(), tenantId);
        log.info("UserSubscription.processExpiredSubscriptions - Processed {} expired subscriptions", count);

        return count;
    }

    // =========================
    // Private Helpers
    // =========================

    private UserSubscription findSubscriptionOrThrow(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserSubscription", "subscriptionId", subscriptionId));
    }
}
