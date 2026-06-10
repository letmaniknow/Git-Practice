package com.mmva.newsapp.infrastructure.common.geographic.service;

import com.mmva.newsapp.infrastructure.common.geographic.dto.GeographicMetadata;

/**
 * Service interface for geographic data validation and processing.
 *
 * <p>
 * Provides comprehensive geographic validation including coordinate validation,
 * location name verification, country/state lookups, and geographic data
 * normalization for news content with location-based information.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Geographic coordinate validation</li>
 * <li>Location name verification and normalization</li>
 * <li>Country and state/province validation</li>
 * <li>Timezone determination from coordinates</li>
 * <li>Distance calculations between locations</li>
 * <li>Geographic boundary checks</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public interface GeographicValidationService {

    /**
     * Validates geographic coordinates (latitude and longitude).
     *
     * @param latitude  the latitude (-90 to 90)
     * @param longitude the longitude (-180 to 180)
     * @return true if coordinates are valid
     */
    boolean validateCoordinates(double latitude, double longitude);

    /**
     * Validates and normalizes a location name.
     *
     * @param locationName the location name to validate
     * @return normalized location name or null if invalid
     */
    String validateLocationName(String locationName);

    /**
     * Validates country name or code.
     *
     * @param country the country name or ISO code
     * @return normalized country name or null if invalid
     */
    String validateCountry(String country);

    /**
     * Validates state/province name within a country.
     *
     * @param state   the state/province name
     * @param country the country name or code
     * @return normalized state name or null if invalid
     */
    String validateState(String state, String country);

    /**
     * Determines timezone from geographic coordinates.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @return timezone ID (e.g., "America/New_York") or null if not found
     */
    String getTimezoneFromCoordinates(double latitude, double longitude);

    /**
     * Calculates distance between two geographic points.
     *
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    /**
     * Validates if coordinates are within country boundaries.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @param country   the country name or code
     * @return true if coordinates are within the country
     */
    boolean isWithinCountry(double latitude, double longitude, String country);

    /**
     * Gets geographic metadata for coordinates.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @return GeographicMetadata object with location details
     */
    GeographicMetadata getGeographicMetadata(double latitude, double longitude);
}