/**
 * Monetization module - Self-contained, portable monetization feature.
 *
 * <h2>Package Structure:</h2>
 *
 * <pre>
 * com.mmva.newsapp.infrastructure.monetization/
 * ├── billing/        # ⭐ NEW: Unified billing engine
 * │   ├── model/      # Invoice, Payment, Receipt entities
 * │   ├── repository/
 * │   ├── dto/
 * │   ├── mapper/
 * │   ├── enums/      # InvoiceStatus, PaymentMethod, Currency
 * │   ├── service/    # BillingService, InvoiceService, PaymentProcessor
 * │   └── pdf/        # Receipt/PDF generation
 * ├── subscription/   # User subscriptions (with billing integration)
 * ├── campaign/       # Client campaigns (with billing integration)
 * ├── ads/local/            # Ad placements and tracking
 * ├── ads/external/    # Google, Facebook, AdMob integrations
 * └── revenue/        # ⭐ NEW: Revenue aggregation and analytics
 *     ├── model/      # RevenueStream, RevenueReport
 *     ├── service/    # RevenueAggregationService, AnalyticsService
 *     └── dashboard/  # Revenue dashboard data
 * </pre>
 *
 * <h2>Revenue Streams Architecture:</h2>
 *
 * <h3>Stream 1: Direct Monetization (Your Effort)</h3>
 * <ul>
 * <li><b>User Subscriptions:</b> Monthly/annual plans → BillingService →
 * Invoice generation</li>
 * <li><b>Client Campaigns:</b> Sponsored content/ads → CampaignService →
 * BillingService</li>
 * <li><b>Local Business Ads:</b> Direct ad placements → AdsService →
 * BillingService</li>
 * </ul>
 *
 * <h3>Stream 2: Ad Provider Monetization</h3>
 * <ul>
 * <li><b>Google AdSense:</b> AdProviderService → Revenue sync → No direct
 * billing (Google pays you)</li>
 * <li><b>Facebook Audience Network:</b> Same as above</li>
 * <li><b>AdMob:</b> Same as above</li>
 * </ul>
 *
 * <h2>Unified Billing Flow:</h2>
 * 
 * <pre>
 * Client/User Action → Service → BillingService.createInvoice()
 *                     ↓
 *              Invoice Generated → Payment Processed → Receipt Created
 *                     ↓
 *              Revenue Recorded → Analytics Updated → Dashboard Refreshed
 * </pre>
 *
 * <h2>Business Priorities:</h2>
 * <ol>
 * <li><b>Revenue First:</b> All features drive toward monetization</li>
 * <li><b>Compliance:</b> Proper invoices, receipts, tax handling</li>
 * <li><b>Scalability:</b> Handle growth from 100 to 100K+ users</li>
 * <li><b>Automation:</b> Recurring billing, payment retries, dunning</li>
 * </ol>
 *
 * <h2>Design Principles:</h2>
 * <ul>
 * <li>Feature ownership: Each module owns ALL its files (enums, DTOs,
 * etc.)</li>
 * <li>No shared "core" packages - enums belong to their owning feature</li>
 * <li>Controllers in controller/admindashboard/, not in infrastructure
 * modules</li>
 * <li>Flat service structure (no impl/ subfolder)</li>
 * </ul>
 *
 * <h2>Portability Rules:</h2>
 * <ul>
 * <li>NO imports from com.mmva.newsapp.model.newsapp.*</li>
 * <li>NO imports from com.mmva.newsapp.model.appuser.*</li>
 * <li>Uses UUID userId, NOT AppUser entity</li>
 * <li>Uses UUID contentId, NOT NewsMasterEntity</li>
 * <li>Uses String tenantId for multi-app support</li>
 * </ul>
 *
 * <h2>Monetization Models Supported:</h2>
 * <table border="1">
 * <tr>
 * <th>Model</th>
 * <th>Module</th>
 * <th>Revenue Type</th>
 * <th>Billing Required</th>
 * </tr>
 * <tr>
 * <td>User Subscriptions</td>
 * <td>subscription</td>
 * <td>Recurring</td>
 * <td>✅ Yes</td>
 * </tr>
 * <tr>
 * <td>Client Campaigns</td>
 * <td>campaign</td>
 * <td>One-time/Project</td>
 * <td>✅ Yes</td>
 * </tr>
 * <tr>
 * <td>Local Business Ads</td>
 * <td>ads/local</td>
 * <td>CPM/CPC</td>
 * <td>✅ Yes</td>
 * </tr>
 * <tr>
 * <td>Ad Provider Networks</td>
 * <td>ads/external</td>
 * <td>CPM/CPC</td>
 * <td>❌ No (they pay you)</td>
 * </tr>
 * </table>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.monetization;
