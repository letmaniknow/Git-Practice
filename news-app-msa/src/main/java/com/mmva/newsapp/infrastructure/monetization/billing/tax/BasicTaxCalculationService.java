package com.mmva.newsapp.infrastructure.monetization.billing.tax;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Basic implementation of TaxCalculationService.
 *
 * <p>
 * Includes predefined tax rates for common US jurisdictions and
 * basic tax exemption handling. In production, this would integrate
 * with a tax rate database or external tax service.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class BasicTaxCalculationService implements TaxCalculationService {

    // Predefined tax rates for common US states (simplified - in production use a
    // proper tax database)
    private static final Map<String, List<TaxRate>> TAX_RATES = Map.of(
            "CA", List.of(
                    new TaxRate("STATE_SALES", "California State Sales Tax", new BigDecimal("0.0625"), "CA", false,
                            "2024-01-01", null),
                    new TaxRate("LOCAL_SALES", "Local Sales Tax (varies by county)", new BigDecimal("0.01"), "CA-LOCAL",
                            false, "2024-01-01", null)),
            "NY", List.of(
                    new TaxRate("STATE_SALES", "New York State Sales Tax", new BigDecimal("0.04"), "NY", false,
                            "2024-01-01", null)),
            "TX", List.of(
                    new TaxRate("STATE_SALES", "Texas State Sales Tax", new BigDecimal("0.0625"), "TX", false,
                            "2024-01-01", null)),
            "FL", List.of(
                    new TaxRate("STATE_SALES", "Florida State Sales Tax", new BigDecimal("0.06"), "FL", false,
                            "2024-01-01", null)));

    // Tax-exempt customer registry (in production, this would be a database)
    private static final Set<String> TAX_EXEMPT_CUSTOMERS = Set.of(
            "GOV001", "NONPROFIT001", "EDU001");

    @Override
    public TaxCalculationResult calculateInvoiceTax(
            com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice invoice) {
        // Extract tax context from invoice
        TaxContext context = buildTaxContextFromInvoice(invoice);

        // Check for tax exemption
        if (context.isTaxExempt()) {
            TaxExemptionValidation exemption = validateTaxExemption(
                    invoice.getClientId(),
                    context.getExemptionCertificate(),
                    "GENERAL" // Default category
            );

            if (exemption.isValid()) {
                return new TaxCalculationResult(
                        invoice.getSubtotal(),
                        BigDecimal.ZERO,
                        invoice.getTotalAmount(),
                        List.of(),
                        true,
                        exemption.getMessage());
            }
        }

        // Calculate tax for line items
        return calculateLineItemTax(invoice.getLineItems(), context);
    }

    @Override
    public TaxCalculationResult calculateLineItemTax(
            List<com.mmva.newsapp.infrastructure.monetization.billing.model.InvoiceLineItem> lineItems,
            TaxContext context) {
        if (context.isTaxExempt()) {
            BigDecimal subtotal = lineItems.stream()
                    .map(item -> item.getTotalAmount())
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            return new TaxCalculationResult(subtotal, BigDecimal.ZERO, subtotal, List.of(), true, "Tax exempt");
        }

        BigDecimal totalTax = BigDecimal.ZERO;
        List<TaxBreakdown> breakdowns = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // Get applicable tax rates
        List<TaxRate> applicableRates = getApplicableTaxRates(
                context.getCountry(),
                context.getState(),
                context.getCity(),
                context.getPostalCode(),
                "GENERAL" // Default category
        );

        // Calculate tax for each line item
        for (com.mmva.newsapp.infrastructure.monetization.billing.model.InvoiceLineItem item : lineItems) {
            subtotal = subtotal.add(item.getTotalAmount());

            for (TaxRate rate : applicableRates) {
                BigDecimal itemTax = item.getTotalAmount()
                        .multiply(rate.getRate())
                        .setScale(2, RoundingMode.HALF_UP);

                totalTax = totalTax.add(itemTax);

                breakdowns.add(new TaxBreakdown(
                        rate.getTaxType(),
                        rate.getDescription(),
                        rate.getRate(),
                        item.getTotalAmount(),
                        itemTax,
                        rate.getJurisdiction()));
            }
        }

        BigDecimal totalAmount = subtotal.add(totalTax);

        return new TaxCalculationResult(subtotal, totalTax, totalAmount, breakdowns, false, null);
    }

    @Override
    public List<TaxRate> getApplicableTaxRates(String country, String state, String city, String postalCode,
            String taxCategory) {
        if (!"US".equalsIgnoreCase(country) || state == null) {
            return List.of(); // No tax rates for non-US or unspecified state
        }

        List<TaxRate> rates = TAX_RATES.getOrDefault(state.toUpperCase(), List.of());

        // Add federal tax if applicable (simplified - most digital services are not
        // subject to federal sales tax)
        if ("DIGITAL_SERVICES".equals(taxCategory)) {
            // No federal sales tax for digital services
            return rates;
        }

        return rates;
    }

    @Override
    public TaxExemptionValidation validateTaxExemption(String customerId, String exemptionCertificate,
            String taxCategory) {
        if (TAX_EXEMPT_CUSTOMERS.contains(customerId)) {
            return new TaxExemptionValidation(
                    true,
                    "Customer is registered as tax-exempt",
                    "REGISTERED_EXEMPT",
                    "2025-12-31", // Simplified expiry
                    Map.of("certificate", exemptionCertificate, "category", taxCategory));
        }

        // Check certificate format (simplified validation)
        if (exemptionCertificate != null && exemptionCertificate.startsWith("EXEMPT-")) {
            return new TaxExemptionValidation(
                    true,
                    "Valid exemption certificate provided",
                    "CERTIFICATE_EXEMPT",
                    "2025-12-31",
                    Map.of("certificate", exemptionCertificate, "category", taxCategory));
        }

        return new TaxExemptionValidation(
                false,
                "No valid tax exemption found",
                null,
                null,
                Map.of("reason", "Customer not in exempt list and no valid certificate"));
    }

    /**
     * Builds tax context from invoice data.
     * In production, this would extract location data from customer profile or
     * billing address.
     */
    private TaxContext buildTaxContextFromInvoice(
            com.mmva.newsapp.infrastructure.monetization.billing.model.Invoice invoice) {
        // Extract location from billing address (simplified)
        String country = "US"; // Default
        String state = "CA"; // Default - in production extract from address
        String city = null;
        String postalCode = null;

        if (invoice.getBillingAddress() != null) {
            country = invoice.getBillingAddress().getCountry() != null ? invoice.getBillingAddress().getCountry()
                    : "US";
            state = invoice.getBillingAddress().getState();
            city = invoice.getBillingAddress().getCity();
            postalCode = invoice.getBillingAddress().getZipCode();
        }

        // Check for tax exemption (simplified - in production check customer profile)
        boolean isTaxExempt = TAX_EXEMPT_CUSTOMERS.contains(invoice.getClientId());
        String exemptionCertificate = isTaxExempt ? "AUTO-EXEMPT-" + invoice.getClientId() : null;

        return new TaxContext(
                country,
                state,
                city,
                postalCode,
                "BUSINESS", // Default customer type
                isTaxExempt,
                exemptionCertificate,
                Map.of("invoice_number", invoice.getInvoiceNumber()));
    }
}