package com.mmva.newsapp.infrastructure.monetization.billing.dunning;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing dunning processes for overdue payments.
 *
 * <p>
 * Handles collections workflow including:
 * </p>
 * <ul>
 * <li>Automated payment reminders</li>
 * <li>Late fee assessment</li>
 * <li>Escalation procedures</li>
 * <li>Account suspension/termination</li>
 * <li>Collections agency handoff</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface DunningManagementService {

    /**
     * Initiates dunning process for an overdue invoice.
     *
     * @param invoice The overdue invoice
     * @return Dunning initiation result
     */
    DunningInitiationResult initiateDunning(Invoice invoice);

    /**
     * Processes the next dunning action for an account.
     *
     * @param accountId           The account identifier
     * @param currentDunningLevel Current dunning level
     * @return Dunning action result
     */
    DunningActionResult processDunningAction(String accountId, int currentDunningLevel);

    /**
     * Handles payment received during dunning process.
     *
     * @param invoice       The invoice that was paid
     * @param paymentAmount Amount received
     * @return Resolution result
     */
    DunningResolutionResult resolveDunning(Invoice invoice, BigDecimal paymentAmount);

    /**
     * Generates dunning report for a date range.
     *
     * @param startDate Start date for the report
     * @param endDate   End date for the report
     * @return Dunning report
     */
    DunningReport generateDunningReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Updates dunning configuration for an account type.
     *
     * @param accountType Account type (individual, business, etc.)
     * @param newConfig   New dunning configuration
     * @return Update result
     */
    DunningConfigUpdateResult updateDunningConfig(String accountType, DunningConfiguration newConfig);

    /**
     * Dunning configuration for different account types.
     */
    class DunningConfiguration {
        private final List<DunningLevel> levels;
        private final boolean autoSuspend;
        private final int suspensionDays;
        private final boolean autoTerminate;
        private final int terminationDays;
        private final BigDecimal lateFeePercentage;
        private final int gracePeriodDays;

        public DunningConfiguration(List<DunningLevel> levels, boolean autoSuspend, int suspensionDays,
                boolean autoTerminate, int terminationDays, BigDecimal lateFeePercentage,
                int gracePeriodDays) {
            this.levels = levels;
            this.autoSuspend = autoSuspend;
            this.suspensionDays = suspensionDays;
            this.autoTerminate = autoTerminate;
            this.terminationDays = terminationDays;
            this.lateFeePercentage = lateFeePercentage;
            this.gracePeriodDays = gracePeriodDays;
        }

        // Getters
        public List<DunningLevel> getLevels() {
            return levels;
        }

        public boolean isAutoSuspend() {
            return autoSuspend;
        }

        public int getSuspensionDays() {
            return suspensionDays;
        }

        public boolean isAutoTerminate() {
            return autoTerminate;
        }

        public int getTerminationDays() {
            return terminationDays;
        }

        public BigDecimal getLateFeePercentage() {
            return lateFeePercentage;
        }

        public int getGracePeriodDays() {
            return gracePeriodDays;
        }
    }

    /**
     * Individual dunning level configuration.
     */
    class DunningLevel {
        private final int level;
        private final int daysOverdue;
        private final String action;
        private final String template;
        private final BigDecimal additionalFee;
        private final boolean allowPaymentPlan;

        public DunningLevel(int level, int daysOverdue, String action, String template,
                BigDecimal additionalFee, boolean allowPaymentPlan) {
            this.level = level;
            this.daysOverdue = daysOverdue;
            this.action = action;
            this.template = template;
            this.additionalFee = additionalFee;
            this.allowPaymentPlan = allowPaymentPlan;
        }

        // Getters
        public int getLevel() {
            return level;
        }

        public int getDaysOverdue() {
            return daysOverdue;
        }

        public String getAction() {
            return action;
        }

        public String getTemplate() {
            return template;
        }

        public BigDecimal getAdditionalFee() {
            return additionalFee;
        }

        public boolean isAllowPaymentPlan() {
            return allowPaymentPlan;
        }
    }

    /**
     * Result of dunning initiation.
     */
    class DunningInitiationResult {
        private final boolean initiated;
        private final String dunningId;
        private final LocalDateTime firstActionDate;
        private final String message;

        public DunningInitiationResult(boolean initiated, String dunningId, LocalDateTime firstActionDate,
                String message) {
            this.initiated = initiated;
            this.dunningId = dunningId;
            this.firstActionDate = firstActionDate;
            this.message = message;
        }

        // Getters
        public boolean isInitiated() {
            return initiated;
        }

        public String getDunningId() {
            return dunningId;
        }

        public LocalDateTime getFirstActionDate() {
            return firstActionDate;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Result of dunning action processing.
     */
    class DunningActionResult {
        private final boolean success;
        private final String actionTaken;
        private final int newLevel;
        private final LocalDateTime nextActionDate;
        private final String message;

        public DunningActionResult(boolean success, String actionTaken, int newLevel,
                LocalDateTime nextActionDate, String message) {
            this.success = success;
            this.actionTaken = actionTaken;
            this.newLevel = newLevel;
            this.nextActionDate = nextActionDate;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getActionTaken() {
            return actionTaken;
        }

        public int getNewLevel() {
            return newLevel;
        }

        public LocalDateTime getNextActionDate() {
            return nextActionDate;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Result of dunning resolution.
     */
    class DunningResolutionResult {
        private final boolean resolved;
        private final String resolutionType;
        private final BigDecimal amountReceived;
        private final BigDecimal remainingBalance;
        private final String message;

        public DunningResolutionResult(boolean resolved, String resolutionType, BigDecimal amountReceived,
                BigDecimal remainingBalance, String message) {
            this.resolved = resolved;
            this.resolutionType = resolutionType;
            this.amountReceived = amountReceived;
            this.remainingBalance = remainingBalance;
            this.message = message;
        }

        // Getters
        public boolean isResolved() {
            return resolved;
        }

        public String getResolutionType() {
            return resolutionType;
        }

        public BigDecimal getAmountReceived() {
            return amountReceived;
        }

        public BigDecimal getRemainingBalance() {
            return remainingBalance;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Dunning report data.
     */
    class DunningReport {
        private final LocalDateTime reportPeriodStart;
        private final LocalDateTime reportPeriodEnd;
        private final int totalAccountsInDunning;
        private final int accountsResolved;
        private final int accountsEscalated;
        private final BigDecimal totalAmountCollected;
        private final BigDecimal totalAmountOutstanding;
        private final Map<Integer, Integer> accountsByLevel;
        private final List<DunningCase> topCases;

        public DunningReport(LocalDateTime reportPeriodStart, LocalDateTime reportPeriodEnd,
                int totalAccountsInDunning, int accountsResolved, int accountsEscalated,
                BigDecimal totalAmountCollected, BigDecimal totalAmountOutstanding,
                Map<Integer, Integer> accountsByLevel, List<DunningCase> topCases) {
            this.reportPeriodStart = reportPeriodStart;
            this.reportPeriodEnd = reportPeriodEnd;
            this.totalAccountsInDunning = totalAccountsInDunning;
            this.accountsResolved = accountsResolved;
            this.accountsEscalated = accountsEscalated;
            this.totalAmountCollected = totalAmountCollected;
            this.totalAmountOutstanding = totalAmountOutstanding;
            this.accountsByLevel = accountsByLevel;
            this.topCases = topCases;
        }

        // Getters
        public LocalDateTime getReportPeriodStart() {
            return reportPeriodStart;
        }

        public LocalDateTime getReportPeriodEnd() {
            return reportPeriodEnd;
        }

        public int getTotalAccountsInDunning() {
            return totalAccountsInDunning;
        }

        public int getAccountsResolved() {
            return accountsResolved;
        }

        public int getAccountsEscalated() {
            return accountsEscalated;
        }

        public BigDecimal getTotalAmountCollected() {
            return totalAmountCollected;
        }

        public BigDecimal getTotalAmountOutstanding() {
            return totalAmountOutstanding;
        }

        public Map<Integer, Integer> getAccountsByLevel() {
            return accountsByLevel;
        }

        public List<DunningCase> getTopCases() {
            return topCases;
        }
    }

    /**
     * Individual dunning case for reporting.
     */
    class DunningCase {
        private final String accountId;
        private final String invoiceNumber;
        private final BigDecimal amount;
        private final int daysOverdue;
        private int currentLevel;
        private LocalDateTime lastAction;

        public DunningCase(String accountId, String invoiceNumber, BigDecimal amount,
                int daysOverdue, int currentLevel, LocalDateTime lastAction) {
            this.accountId = accountId;
            this.invoiceNumber = invoiceNumber;
            this.amount = amount;
            this.daysOverdue = daysOverdue;
            this.currentLevel = currentLevel;
            this.lastAction = lastAction;
        }

        // Getters
        public String getAccountId() {
            return accountId;
        }

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public int getDaysOverdue() {
            return daysOverdue;
        }

        public int getCurrentLevel() {
            return currentLevel;
        }

        public LocalDateTime getLastAction() {
            return lastAction;
        }

        // Setters for mutable fields
        public void setCurrentLevel(int currentLevel) {
            this.currentLevel = currentLevel;
        }

        public void setLastAction(LocalDateTime lastAction) {
            this.lastAction = lastAction;
        }
    }

    /**
     * Result of dunning configuration update.
     */
    class DunningConfigUpdateResult {
        private final boolean success;
        private final String accountType;
        private final String message;

        public DunningConfigUpdateResult(boolean success, String accountType, String message) {
            this.success = success;
            this.accountType = accountType;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getAccountType() {
            return accountType;
        }

        public String getMessage() {
            return message;
        }
    }
}