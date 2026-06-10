package com.mmva.newsapp.infrastructure.common.geographic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object containing geographic metadata for coordinates.
 *
 * <p>
 * Encapsulates detailed geographic information derived from latitude and
 * longitude
 * coordinates, including country, state, city, timezone, and other location
 * data.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeographicMetadata {

    /**
     * Country name.
     */
    private String country;

    /**
     * Country ISO code (2-letter).
     */
    private String countryCode;

    /**
     * State/province name.
     */
    private String state;

    /**
     * State/province code.
     */
    private String stateCode;

    /**
     * City name.
     */
    private String city;

    /**
     * Postal/ZIP code.
     */
    private String postalCode;

    /**
     * Timezone identifier (e.g., "America/New_York").
     */
    private String timezone;

    /**
     * UTC offset in hours.
     */
    private double utcOffset;

    /**
     * Continent name.
     */
    private String continent;

    /**
     * Geographic region.
     */
    private String region;

    /**
     * Whether the location is in a major metropolitan area.
     */
    private boolean isMetropolitanArea;

    /**
     * Population density category (Rural, Suburban, Urban, Metropolitan).
     */
    private String populationDensity;

    /**
     * Validation confidence score (0-100).
     */
    private double confidenceScore;
}