package com.mmva.newsapp.infrastructure.monetization.billing.tax;

import com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice;
import com.mmva.newsapp.infrastructure.monetization.billing.model.InvoiceLineItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating taxes on invoices and line items.
 *
 * <p>
 * Supports various tax calculation methods:
 * </p>
 * <ul>
 * <li>Location-based tax rates (state, country, city)</li>
 * <li>Product-specific tax categories</li>
 * <li>Tax exemptions and overrides</li>
 * <li>Compound tax calculations</li>
 * <li>Tax-inclusive vs tax-exclusive pricing</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface TaxCalculationService {

    /**
     * Calculates tax for an entire invoice.
     *
     * @param invoice The invoice to calculate tax for
     * @return Tax calculation result with breakdown
     */
    TaxCalculationResult calculateInvoiceTax(Invoice invoice);

    /**
     * Calculates tax for individual line items.
     *
     * @param lineItems  The line items to calculate tax for
     * @param taxContext The tax context (location, customer type, etc.)
     * @return Tax calculation result for line items
     */
    TaxCalculationResult calculateLineItemTax(List<InvoiceLineItem> lineItems, TaxContext taxContext);

    /**
     * Gets applicable tax rates for a given location and product category.
     *
     * @param country     Country code (ISO 3166-1 alpha-2)
     * @param state       State/province code
     * @param city        City name (optional)
     * @param postalCode  Postal code (optional)
     * @param taxCategory Product/service tax category
     * @return Applicable tax rates
     */
    List<TaxRate> getApplicableTaxRates(String country, String state, String city, String postalCode,
            String taxCategory);

    /**
     * Validates tax exemption status for a customer.
     *
     * @param customerId           The customer ID
     * @param exemptionCertificate Certificate number or ID
     * @param taxCategory          The tax category being exempted
     * @return Validation result
     */
    TaxExemptionValidation validateTaxExemption(String customerId, String exemptionCertificate, String taxCategory);

    /**
     * Context information for tax calculations.
     */
    class TaxContext {
        private final String country;
        private final String state;
        private final String city;
        private final String postalCode;
        private final String customerType;
        private final boolean isTaxExempt;
        private final String exemptionCertificate;
        private final Map<String, Object> additionalData;

        public TaxContext(String country, String state, String city, String postalCode,
                String customerType, boolean isTaxExempt, String exemptionCertificate,
                Map<String, Object> additionalData) {
            this.country = country;
            this.state = state;
            this.city = city;
            this.postalCode = postalCode;
            this.customerType = customerType;
            this.isTaxExempt = isTaxExempt;
            this.exemptionCertificate = exemptionCertificate;
            this.additionalData = additionalData;
        }

        // Getters
        public String getCountry() {
            return country;
        }

        public String getState() {
            return state;
        }

        public String getCity() {
            return city;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getCustomerType() {
            return customerType;
        }

        public boolean isTaxExempt() {
            return isTaxExempt;
        }

        public String getExemptionCertificate() {
            return exemptionCertificate;
        }

        public Map<String, Object> getAdditionalData() {
            return additionalData;
        }
    }

    /**
     * Result of a tax calculation.
     */
    class TaxCalculationResult {
        private final BigDecimal subtotal;
        private final BigDecimal totalTax;
        private final BigDecimal totalAmount;
        private final List<TaxBreakdown> taxBreakdown;
        private final boolean taxExempt;
        private final String exemptionReason;

        public TaxCalculationResult(BigDecimal subtotal, BigDecimal totalTax, BigDecimal totalAmount,
                List<TaxBreakdown> taxBreakdown, boolean taxExempt, String exemptionReason) {
            this.subtotal = subtotal;
            this.totalTax = totalTax;
            this.totalAmount = totalAmount;
            this.taxBreakdown = taxBreakdown;
            this.taxExempt = taxExempt;
            this.exemptionReason = exemptionReason;
        }

        // Getters
        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public BigDecimal getTotalTax() {
            return totalTax;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public List<TaxBreakdown> getTaxBreakdown() {
            return taxBreakdown;
        }

        public boolean isTaxExempt() {
            return taxExempt;
        }

        public String getExemptionReason() {
            return exemptionReason;
        }
    }

    /**
     * Breakdown of tax amounts by type/rate.
     */
    class TaxBreakdown {
        private final String taxType;
        private final String description;
        private final BigDecimal rate;
        private final BigDecimal taxableAmount;
        private final BigDecimal taxAmount;
        private final String jurisdiction;

        public TaxBreakdown(String taxType, String description, BigDecimal rate,
                BigDecimal taxableAmount, BigDecimal taxAmount, String jurisdiction) {
            this.taxType = taxType;
            this.description = description;
            this.rate = rate;
            this.taxableAmount = taxableAmount;
            this.taxAmount = taxAmount;
            this.jurisdiction = jurisdiction;
        }

        // Getters
        public String getTaxType() {
            return taxType;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public BigDecimal getTaxableAmount() {
            return taxableAmount;
        }

        public BigDecimal getTaxAmount() {
            return taxAmount;
        }

        public String getJurisdiction() {
            return jurisdiction;
        }
    }

    /**
     * Tax rate information.
     */
    class TaxRate {
        private final String taxType;
        private final String description;
        private final BigDecimal rate;
        private final String jurisdiction;
        private final boolean isCompound;
        private final String effectiveDate;
        private final String expiryDate;

        public TaxRate(String taxType, String description, BigDecimal rate, String jurisdiction,
                boolean isCompound, String effectiveDate, String expiryDate) {
            this.taxType = taxType;
            this.description = description;
            this.rate = rate;
            this.jurisdiction = jurisdiction;
            this.isCompound = isCompound;
            this.effectiveDate = effectiveDate;
            this.expiryDate = expiryDate;
        }

        // Getters
        public String getTaxType() {
            return taxType;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getRate() {
            return rate;
        }

        public String getJurisdiction() {
            return jurisdiction;
        }

        public boolean isCompound() {
            return isCompound;
        }

        public String getEffectiveDate() {
            return effectiveDate;
        }

        public String getExpiryDate() {
            return expiryDate;
        }
    }

    /**
     * Tax exemption validation result.
     */
    class TaxExemptionValidation {
        private final boolean valid;
        private final String message;
        private final String exemptionType;
        private final String validUntil;
        private final Map<String, Object> details;

        public TaxExemptionValidation(boolean valid, String message, String exemptionType,
                String validUntil, Map<String, Object> details) {
            this.valid = valid;
            this.message = message;
            this.exemptionType = exemptionType;
            this.validUntil = validUntil;
            this.details = details;
        }

        // Getters
        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public String getExemptionType() {
            return exemptionType;
        }

        public String getValidUntil() {
            return validUntil;
        }

        public Map<String, Object> getDetails() {
            return details;
        }
    }
}