package com.mmva.newsapp.infrastructure.clientcontext.core.enums;

/**
 * Accuracy level of geolocation data.
 * 
 * <p>
 * Indicates how precise the location data is:
 * </p>
 * <ul>
 * <li>GPS - GPS-level accuracy (~10 meters)</li>
 * <li>CITY - City-level accuracy (~10-50 kilometers)</li>
 * <li>COUNTRY - Country-level only</li>
 * <li>UNKNOWN - Unknown accuracy level</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum AccuracyLevel {
    /** GPS-level accuracy (~10m) */
    GPS,

    /** City-level accuracy (~10-50km) */
    CITY,

    /** Country-level only */
    COUNTRY,

    /** Unknown accuracy */
    UNKNOWN
}
