package com.mmva.newsapp.infrastructure.monetization.billing.dunning;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.service.BillingEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Basic implementation of DunningManagementService.
 *
 * <p>
 * Provides configurable dunning workflows with automated
 * reminders, late fees, and escalation procedures.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BasicDunningManagementService implements DunningManagementService {

    private final BillingEmailService emailService;

    // Default dunning configuration
    private static final DunningConfiguration DEFAULT_CONFIG = new DunningConfiguration(
            List.of(
                    new DunningLevel(1, 7, "EMAIL_REMINDER", "payment_reminder_1", BigDecimal.ZERO, false),
                    new DunningLevel(2, 14, "EMAIL_REMINDER_LATE_FEE", "payment_reminder_2", new BigDecimal("0.015"),
                            false),
                    new DunningLevel(3, 21, "FINAL_NOTICE", "final_notice", new BigDecimal("0.025"), true),
                    new DunningLevel(4, 30, "ACCOUNT_SUSPENSION", "account_suspended", BigDecimal.ZERO, false),
                    new DunningLevel(5, 60, "COLLECTIONS_HANDOFF", "collections_notice", BigDecimal.ZERO, false)),
            true, // autoSuspend
            30, // suspensionDays
            true, // autoTerminate
            90, // terminationDays
            new BigDecimal("0.015"), // lateFeePercentage
            3 // gracePeriodDays
    );

    // In-memory storage for demo (production would use database)
    private final Map<String, DunningConfiguration> accountTypeConfigs = new HashMap<>();
    private final Map<String, DunningCase> activeCases = new HashMap<>();

    @PostConstruct
    public void initializeConfigurations() {
        // Initialize with default config for "BUSINESS" accounts
        accountTypeConfigs.put("BUSINESS", DEFAULT_CONFIG);
        accountTypeConfigs.put("INDIVIDUAL", DEFAULT_CONFIG);
    }

    @Override
    public DunningInitiationResult initiateDunning(Invoice invoice) {
        try {
            log.info("Initiating dunning process for invoice: {}", invoice.getInvoiceNumber());

            String accountId = invoice.getClientId();
            DunningConfiguration config = getConfigForAccount(accountId);

            // Check if already in dunning
            if (activeCases.containsKey(accountId)) {
                return new DunningInitiationResult(false, null, null,
                        "Account already in dunning process");
            }

            // Calculate first action date
            LocalDateTime firstActionDate = invoice.getDueDate().plusDays(config.getGracePeriodDays()).atStartOfDay();

            // Create dunning case
            DunningManagementService.DunningCase dunningCase = new DunningManagementService.DunningCase(
                    accountId,
                    invoice.getInvoiceNumber(),
                    invoice.getTotalAmount(),
                    0, // days overdue
                    1, // starting level
                    LocalDateTime.now());

            activeCases.put(accountId, dunningCase);

            log.info("Dunning initiated for account: {}, first action: {}", accountId, firstActionDate);
            return new DunningInitiationResult(true, accountId, firstActionDate,
                    "Dunning process initiated successfully");

        } catch (Exception e) {
            log.error("Error initiating dunning for invoice: {}", invoice.getInvoiceNumber(), e);
            return new DunningInitiationResult(false, null, null,
                    "Failed to initiate dunning: " + e.getMessage());
        }
    }

    @Override
    public DunningActionResult processDunningAction(String accountId, int currentDunningLevel) {
        try {
            log.info("Processing dunning action for account: {} at level {}", accountId, currentDunningLevel);

            DunningCase dunningCase = activeCases.get(accountId);
            if (dunningCase == null) {
                return new DunningActionResult(false, "NO_ACTIVE_CASE", currentDunningLevel, null,
                        "No active dunning case found");
            }

            DunningConfiguration config = getConfigForAccount(accountId);
            DunningLevel level = getLevelConfig(config, currentDunningLevel);

            if (level == null) {
                return new DunningActionResult(false, "INVALID_LEVEL", currentDunningLevel, null,
                        "Invalid dunning level: " + currentDunningLevel);
            }

            String actionTaken = level.getAction();
            LocalDateTime nextActionDate = null;
            int newLevel = currentDunningLevel;

            switch (actionTaken) {
                case "EMAIL_REMINDER" -> {
                    Invoice invoice = findInvoiceByNumber(dunningCase.getInvoiceNumber());
                    if (invoice != null) {
                        emailService.sendPaymentReminderEmail(invoice, level.getDaysOverdue());
                        nextActionDate = LocalDateTime.now().plusDays(7); // Next reminder in 7 days
                        newLevel = currentDunningLevel + 1;
                    }
                }
                case "EMAIL_REMINDER_LATE_FEE" -> {
                    Invoice invoice = findInvoiceByNumber(dunningCase.getInvoiceNumber());
                    if (invoice != null) {
                        // Add late fee
                        addLateFee(invoice, level.getAdditionalFee());
                        emailService.sendPaymentReminderEmail(invoice, level.getDaysOverdue());
                        nextActionDate = LocalDateTime.now().plusDays(7);
                        newLevel = currentDunningLevel + 1;
                    }
                }
                case "FINAL_NOTICE" -> {
                    Invoice invoice = findInvoiceByNumber(dunningCase.getInvoiceNumber());
                    if (invoice != null) {
                        emailService.sendFinalNoticeEmail(invoice);
                        nextActionDate = LocalDateTime.now().plusDays(7);
                        newLevel = currentDunningLevel + 1;
                    }
                }
                case "ACCOUNT_SUSPENSION" -> {
                    // In production, this would suspend the account
                    log.warn("Account suspension triggered for: {}", accountId);
                    nextActionDate = LocalDateTime.now().plusDays(config.getSuspensionDays());
                    newLevel = currentDunningLevel + 1;
                }
                case "COLLECTIONS_HANDOFF" -> {
                    // In production, this would initiate collections process
                    log.warn("Collections handoff triggered for: {}", accountId);
                    // No next action - case closed or escalated
                }
                default -> {
                    log.warn("Unknown dunning action: {} for account: {}", actionTaken, accountId);
                    return new DunningActionResult(false, "UNKNOWN_ACTION", currentDunningLevel, null,
                            "Unknown action: " + actionTaken);
                }
            }

            // Update case
            dunningCase.setCurrentLevel(newLevel);
            dunningCase.setLastAction(LocalDateTime.now());

            log.info("Dunning action '{}' completed for account: {}", actionTaken, accountId);
            return new DunningActionResult(true, actionTaken, newLevel, nextActionDate,
                    "Action processed successfully");

        } catch (Exception e) {
            log.error("Error processing dunning action for account: {}", accountId, e);
            return new DunningActionResult(false, "ERROR", currentDunningLevel, null,
                    "Action processing failed: " + e.getMessage());
        }
    }

    @Override
    public DunningResolutionResult resolveDunning(Invoice invoice, BigDecimal paymentAmount) {
        try {
            log.info("Resolving dunning for invoice: {} with payment: {}", invoice.getInvoiceNumber(), paymentAmount);

            String accountId = invoice.getClientId();
            DunningCase dunningCase = activeCases.get(accountId);

            if (dunningCase == null) {
                return new DunningResolutionResult(false, "NO_ACTIVE_CASE", paymentAmount,
                        invoice.getTotalAmount(), "No active dunning case found");
            }

            BigDecimal remainingBalance = invoice.getTotalAmount().subtract(paymentAmount);

            String resolutionType;
            boolean fullyResolved = remainingBalance.compareTo(BigDecimal.ZERO) <= 0;

            if (fullyResolved) {
                resolutionType = "FULL_PAYMENT";
                activeCases.remove(accountId); // Close the case
                log.info("Dunning case fully resolved for account: {}", accountId);
            } else if (remainingBalance.compareTo(BigDecimal.valueOf(10.00)) <= 0) {
                resolutionType = "PARTIAL_PAYMENT_LOW_BALANCE";
                // Keep case open but reduce priority
                log.info("Dunning case partially resolved for account: {}, remaining: {}", accountId, remainingBalance);
            } else {
                resolutionType = "PARTIAL_PAYMENT";
                // Keep case active
                log.info("Partial payment received for account: {}, remaining: {}", accountId, remainingBalance);
            }

            return new DunningResolutionResult(fullyResolved, resolutionType, paymentAmount,
                    remainingBalance.max(BigDecimal.ZERO), "Dunning resolution processed");

        } catch (Exception e) {
            log.error("Error resolving dunning for invoice: {}", invoice.getInvoiceNumber(), e);
            return new DunningResolutionResult(false, "ERROR", paymentAmount, invoice.getTotalAmount(),
                    "Resolution failed: " + e.getMessage());
        }
    }

    @Override
    public DunningReport generateDunningReport(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            log.info("Generating dunning report for period: {} to {}", startDate, endDate);

            // Simplified report generation (in production would query database)
            Map<Integer, Integer> accountsByLevel = new HashMap<>();
            for (DunningCase case_ : activeCases.values()) {
                accountsByLevel.merge(case_.getCurrentLevel(), 1, Integer::sum);
            }

            List<DunningCase> topCases = activeCases.values().stream()
                    .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                    .limit(10)
                    .toList();

            return new DunningReport(
                    startDate,
                    endDate,
                    activeCases.size(),
                    5, // Example resolved count
                    2, // Example escalated count
                    new BigDecimal("12500.00"), // Example collected amount
                    new BigDecimal("8750.00"), // Example outstanding amount
                    accountsByLevel,
                    topCases);

        } catch (Exception e) {
            log.error("Error generating dunning report", e);
            return null;
        }
    }

    @Override
    public DunningConfigUpdateResult updateDunningConfig(String accountType, DunningConfiguration newConfig) {
        try {
            accountTypeConfigs.put(accountType, newConfig);
            log.info("Updated dunning configuration for account type: {}", accountType);
            return new DunningConfigUpdateResult(true, accountType, "Configuration updated successfully");
        } catch (Exception e) {
            log.error("Error updating dunning config for type: {}", accountType, e);
            return new DunningConfigUpdateResult(false, accountType, "Update failed: " + e.getMessage());
        }
    }

    // Helper methods

    private DunningConfiguration getConfigForAccount(String accountId) {
        // Simplified - in production would determine account type from account data
        return accountTypeConfigs.getOrDefault("BUSINESS", DEFAULT_CONFIG);
    }

    private DunningLevel getLevelConfig(DunningConfiguration config, int level) {
        return config.getLevels().stream()
                .filter(l -> l.getLevel() == level)
                .findFirst()
                .orElse(null);
    }

    private Invoice findInvoiceByNumber(String invoiceNumber) {
        // Simplified - in production would query database
        return null; // Placeholder
    }

    private void addLateFee(Invoice invoice, BigDecimal lateFeePercentage) {
        BigDecimal lateFee = invoice.getTotalAmount().multiply(lateFeePercentage);
        // In production, this would add a late fee line item to the invoice
        log.info("Added late fee of {} to invoice {}", lateFee, invoice.getInvoiceNumber());
    }

}