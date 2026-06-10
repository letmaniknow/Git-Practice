package com.mmva.newsapp.infrastructure.monetization.subscription.service;

import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionTransactionRequestDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.dto.SubscriptionTransactionResponseDto;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionPaymentStatus;
import com.mmva.newsapp.infrastructure.monetization.subscription.enums.SubscriptionTransactionType;
import com.mmva.newsapp.infrastructure.monetization.subscription.exception.SubscriptionTransactionNotFoundException;
import com.mmva.newsapp.infrastructure.monetization.subscription.mapper.SubscriptionTransactionMapper;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.SubscriptionTransaction;
import com.mmva.newsapp.infrastructure.monetization.subscription.model.UserSubscription;
import com.mmva.newsapp.infrastructure.monetization.subscription.repository.SubscriptionTransactionRepository;
import com.mmva.newsapp.infrastructure.monetization.subscription.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link SubscriptionTransactionService}.
 * 
 * <p>
 * Provides subscription transaction management with support for
 * payment recording, status updates, refunds, and revenue reporting.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionTransactionServiceImpl implements SubscriptionTransactionService {

    private final SubscriptionTransactionRepository transactionRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionTransactionMapper transactionMapper;

    // =========================
    // Create Operations
    // =========================

    @Override
    @Transactional
    public SubscriptionTransactionResponseDto create(SubscriptionTransactionRequestDto dto) {
        log.info("SubscriptionTransaction.create - Recording transaction for user: {}, type: {}",
                dto.getSubscriptionTransactionUserId(), dto.getSubscriptionTransactionType());

        // Map request to entity
        SubscriptionTransaction entity = transactionMapper.toEntity(dto);

        // Link to subscription if provided
        if (dto.getSubscriptionTransactionSubscriptionId() != null) {
            UserSubscription subscription = subscriptionRepository
                    .findById(dto.getSubscriptionTransactionSubscriptionId())
                    .orElse(null);
            if (subscription != null) {
                entity.setSubscriptionTransactionSubscription(subscription);
            }
        }

        // Save transaction
        SubscriptionTransaction saved = transactionRepository.save(entity);
        log.info("SubscriptionTransaction.create - Recorded transaction ID: {}", saved.getSubscriptionTransactionId());

        return transactionMapper.toResponseDto(saved);
    }

    // =========================
    // Read Operations
    // =========================

    @Override
    public SubscriptionTransactionResponseDto getById(UUID transactionId) {
        log.debug("SubscriptionTransaction.getById - Fetching transaction: {}", transactionId);

        SubscriptionTransaction transaction = findTransactionOrThrow(transactionId);
        return transactionMapper.toResponseDto(transaction);
    }

    @Override
    public SubscriptionTransactionResponseDto getByExternalId(String externalTransactionId, String tenantId) {
        log.debug("SubscriptionTransaction.getByExternalId - Fetching by external ID: {}", externalTransactionId);

        SubscriptionTransaction transaction = transactionRepository
                .findBySubscriptionTransactionExternalTransactionId(externalTransactionId)
                .orElseThrow(() -> new SubscriptionTransactionNotFoundException(externalTransactionId));

        return transactionMapper.toResponseDto(transaction);
    }

    @Override
    public List<SubscriptionTransactionResponseDto> getByUserId(UUID userId, String tenantId) {
        log.debug("SubscriptionTransaction.getByUserId - Fetching transactions for user: {}", userId);

        List<SubscriptionTransaction> transactions = transactionRepository
                .findSuccessfulByUserIdAndTenantId(userId, tenantId);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    public List<SubscriptionTransactionResponseDto> getBySubscriptionId(UUID subscriptionId) {
        log.debug("SubscriptionTransaction.getBySubscriptionId - Fetching transactions for subscription: {}",
                subscriptionId);

        List<SubscriptionTransaction> transactions = transactionRepository
                .findBySubscriptionId(subscriptionId);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    public List<SubscriptionTransactionResponseDto> getByDateRange(String tenantId, Instant startDate,
            Instant endDate) {
        log.debug("SubscriptionTransaction.getByDateRange - Fetching transactions from {} to {}", startDate, endDate);

        List<SubscriptionTransaction> transactions = transactionRepository
                .findByTenantIdAndDateRangeOrderByDateDesc(tenantId, startDate, endDate);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    public List<SubscriptionTransactionResponseDto> getByStatus(String tenantId,
            SubscriptionTransactionPaymentStatus status) {
        log.debug("SubscriptionTransaction.getByStatus - Fetching transactions with status: {}", status);

        List<SubscriptionTransaction> transactions = transactionRepository
                .findByTenantIdAndStatus(tenantId, status);
        return transactionMapper.toResponseDtoList(transactions);
    }

    @Override
    public List<SubscriptionTransactionResponseDto> getByType(String tenantId, SubscriptionTransactionType type) {
        log.debug("SubscriptionTransaction.getByType - Fetching transactions with type: {}", type);

        List<SubscriptionTransaction> transactions = transactionRepository
                .findByTenantIdAndType(tenantId, type);
        return transactionMapper.toResponseDtoList(transactions);
    }

    // =========================
    // Update Operations
    // =========================

    @Override
    @Transactional
    public SubscriptionTransactionResponseDto updateStatus(UUID transactionId,
            SubscriptionTransactionPaymentStatus newStatus,
            String failureCode,
            String failureMessage) {
        log.info("SubscriptionTransaction.updateStatus - Updating transaction: {} to status: {}",
                transactionId, newStatus);

        SubscriptionTransaction transaction = findTransactionOrThrow(transactionId);

        transaction.setSubscriptionTransactionStatus(newStatus);

        if (newStatus == SubscriptionTransactionPaymentStatus.FAILED) {
            transaction.setSubscriptionTransactionFailureCode(failureCode);
            transaction.setSubscriptionTransactionFailureMessage(failureMessage);
        }

        SubscriptionTransaction saved = transactionRepository.save(transaction);
        log.info("SubscriptionTransaction.updateStatus - Updated transaction ID: {}",
                saved.getSubscriptionTransactionId());

        return transactionMapper.toResponseDto(saved);
    }

    // =========================
    // Refund Operations
    // =========================

    @Override
    @Transactional
    public SubscriptionTransactionResponseDto createRefund(UUID originalTransactionId,
            String refundReason,
            boolean isPartial,
            BigDecimal partialAmount) {
        log.info("SubscriptionTransaction.createRefund - Creating refund for transaction: {}", originalTransactionId);

        SubscriptionTransaction original = findTransactionOrThrow(originalTransactionId);

        // Validate refund is possible
        if (!original.canBeRefunded()) {
            log.warn("SubscriptionTransaction.createRefund - Cannot refund transaction: {}", originalTransactionId);
            throw new IllegalArgumentException("Transaction cannot be refunded: " + originalTransactionId);
        }

        // Determine refund amount
        BigDecimal refundAmount = isPartial && partialAmount != null
                ? partialAmount.negate()
                : original.getSubscriptionTransactionAmount().negate();

        // Create refund transaction
        SubscriptionTransaction refund = new SubscriptionTransaction();
        refund.setSubscriptionTransactionUserId(original.getSubscriptionTransactionUserId());
        refund.setSubscriptionTransactionSubscription(original.getSubscriptionTransactionSubscription());
        refund.setSubscriptionTransactionPlanId(original.getSubscriptionTransactionPlanId());
        refund.setSubscriptionTransactionPlanCode(original.getSubscriptionTransactionPlanCode());
        refund.setSubscriptionTransactionType(
                isPartial ? SubscriptionTransactionType.PARTIAL_REFUND : SubscriptionTransactionType.REFUND);
        refund.setSubscriptionTransactionAmount(refundAmount);
        refund.setSubscriptionTransactionCurrency(original.getSubscriptionTransactionCurrency());
        refund.setSubscriptionTransactionStatus(SubscriptionTransactionPaymentStatus.SUCCEEDED);
        refund.setSubscriptionTransactionDate(Instant.now());
        refund.setSubscriptionTransactionPaymentMethod(original.getSubscriptionTransactionPaymentMethod());
        refund.setSubscriptionTransactionOriginalTransactionId(originalTransactionId);
        refund.setSubscriptionTransactionRefundReason(refundReason);
        refund.setSubscriptionTransactionTenantId(original.getSubscriptionTransactionTenantId());

        SubscriptionTransaction saved = transactionRepository.save(refund);
        log.info("SubscriptionTransaction.createRefund - Created refund transaction ID: {}",
                saved.getSubscriptionTransactionId());

        // Update original transaction status if full refund
        if (!isPartial) {
            original.setSubscriptionTransactionStatus(SubscriptionTransactionPaymentStatus.REFUNDED);
            transactionRepository.save(original);
        }

        return transactionMapper.toResponseDto(saved);
    }

    // =========================
    // Query Operations
    // =========================

    @Override
    public boolean hasSuccessfulTransactions(UUID userId, String tenantId) {
        return transactionRepository.existsSuccessfulByUserIdAndTenantId(userId, tenantId);
    }

    @Override
    public long countByStatus(String tenantId, SubscriptionTransactionPaymentStatus status) {
        return transactionRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public BigDecimal calculateRevenue(String tenantId, Instant startDate, Instant endDate) {
        log.debug("SubscriptionTransaction.calculateRevenue - Calculating revenue from {} to {}", startDate, endDate);

        BigDecimal revenue = transactionRepository.calculateRevenueByTenantIdAndDateRange(tenantId, startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // =========================
    // Private Helper Methods
    // =========================

    private SubscriptionTransaction findTransactionOrThrow(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new SubscriptionTransactionNotFoundException(transactionId));
    }
}
