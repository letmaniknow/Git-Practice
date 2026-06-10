package com.mmva.newsapp.infrastructure.monetization.ads.external.common.constant;

/**
 * Constants for provider metadata keys
 * 
 * Metadata stored in AdProviderMetricsDto.adProviderMetadata Map
 * allows providers to store custom fields without modifying core DTO
 * 
 * Naming Convention:
 * - Keys follow pattern: AD_PROVIDER_METADATA_{FIELD_NAME}
 * - Example: AD_PROVIDER_METADATA_NETWORK_TYPE,
 * AD_PROVIDER_METADATA_DEVICE_TYPE
 * 
 * Usage:
 * AdProviderMetricsDto metrics = ...;
 * metrics.addProviderMetadata(
 * ProviderMetadataKeys.AD_PROVIDER_METADATA_NETWORK_TYPE,
 * "display"
 * );
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public final class ProviderMetadataKeys {

    private ProviderMetadataKeys() {
        // Utility class - no instantiation
    }

    // Google AdSense specific metadata
    public static final String AD_PROVIDER_METADATA_NETWORK_TYPE = "networkType";
    public static final String AD_PROVIDER_METADATA_AD_UNIT_ID = "adUnitId";
    public static final String AD_PROVIDER_METADATA_AD_UNIT_NAME = "adUnitName";
    public static final String AD_PROVIDER_METADATA_AD_FORMAT = "adFormat";
    public static final String AD_PROVIDER_METADATA_REVENUE_BY_AD_UNIT = "revenueByAdUnit";

    // Google AdMob specific metadata
    public static final String AD_PROVIDER_METADATA_APP_ID = "appId";
    public static final String AD_PROVIDER_METADATA_PLATFORM = "platform";
    public static final String AD_PROVIDER_METADATA_AD_MEDIATION_NETWORK = "mediationNetwork";
    public static final String AD_PROVIDER_METADATA_AD_NETWORK_NAME = "adNetworkName";
    public static final String AD_PROVIDER_METADATA_ESTIMATED_EARNINGS_BY_NETWORK = "estimatedEarningsByNetwork";

    // Device/Traffic metadata (common to all)
    public static final String AD_PROVIDER_METADATA_DEVICE_TYPE = "deviceType";
    public static final String AD_PROVIDER_METADATA_COUNTRY = "country";
    public static final String AD_PROVIDER_METADATA_OPERATING_SYSTEM = "operatingSystem";
    public static final String AD_PROVIDER_METADATA_TRAFFIC_SOURCE = "trafficSource";

    // Performance metrics metadata (common to all)
    public static final String AD_PROVIDER_METADATA_BLOCKED_IMPRESSIONS = "blockedImpressions";
    public static final String AD_PROVIDER_METADATA_MATCHED_IMPRESSIONS = "matchedImpressions";
    public static final String AD_PROVIDER_METADATA_COVERAGE = "coverage";
    public static final String AD_PROVIDER_METADATA_FILTER_REASON = "filterReason";

    // Sync metadata
    public static final String AD_PROVIDER_METADATA_SYNC_REQUEST_ID = "syncRequestId";
    public static final String AD_PROVIDER_METADATA_SYNC_BATCH_ID = "syncBatchId";
    public static final String AD_PROVIDER_METADATA_LAST_MODIFIED = "lastModified";

    // Error/Warning metadata
    public static final String AD_PROVIDER_METADATA_DATA_INCOMPLETENESS = "dataIncompleteness";
    public static final String AD_PROVIDER_METADATA_WARNING_MESSAGE = "warningMessage";
    public static final String AD_PROVIDER_METADATA_ESTIMATED_INDICATOR = "estimatedIndicator";
}
