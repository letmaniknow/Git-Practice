package com.mmva.newsapp.infrastructure.common.geographic.service;

import com.mmva.newsapp.infrastructure.common.geographic.dto.GeographicMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of GeographicValidationService for geographic data validation.
 *
 * <p>
 * Provides basic geographic validation and metadata generation using
 * built-in data and simple coordinate-based calculations.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class GeographicValidationServiceImpl implements GeographicValidationService {

    // Basic country data for validation
    private static final Map<String, String> COUNTRY_CODES = new HashMap<>();
    private static final Map<String, String> COUNTRY_NAMES = new HashMap<>();

    static {
        // Initialize with some common countries
        COUNTRY_CODES.put("US", "United States");
        COUNTRY_CODES.put("CA", "Canada");
        COUNTRY_CODES.put("GB", "United Kingdom");
        COUNTRY_CODES.put("DE", "Germany");
        COUNTRY_CODES.put("FR", "France");
        COUNTRY_CODES.put("IT", "Italy");
        COUNTRY_CODES.put("ES", "Spain");
        COUNTRY_CODES.put("AU", "Australia");
        COUNTRY_CODES.put("JP", "Japan");
        COUNTRY_CODES.put("CN", "China");
        COUNTRY_CODES.put("IN", "India");
        COUNTRY_CODES.put("BR", "Brazil");
        COUNTRY_CODES.put("MX", "Mexico");
        COUNTRY_CODES.put("RU", "Russia");

        // Reverse mapping
        COUNTRY_CODES.forEach((code, name) -> COUNTRY_NAMES.put(name.toLowerCase(), code));
    }

    @Override
    public boolean validateCoordinates(double latitude, double longitude) {
        boolean isValid = latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0;

        log.debug("Coordinate validation: lat={}, lon={}, valid={}", latitude, longitude, isValid);
        return isValid;
    }

    @Override
    public String validateLocationName(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            return null;
        }

        String normalized = locationName.trim();

        // Basic validation - check for minimum length and reasonable characters
        if (normalized.length() < 2 || normalized.length() > 100) {
            log.debug("Location name validation failed: invalid length for '{}'", locationName);
            return null;
        }

        // Check for valid characters (letters, spaces, hyphens, apostrophes)
        if (!normalized.matches("^[a-zA-Z\\s\\-']+$")) {
            log.debug("Location name validation failed: invalid characters in '{}'", locationName);
            return null;
        }

        // Normalize capitalization (title case)
        String[] words = normalized.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        String finalResult = result.toString().trim();
        log.debug("Location name validated: '{}' -> '{}'", locationName, finalResult);
        return finalResult;
    }

    @Override
    public String validateCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            return null;
        }

        String normalized = country.trim().toUpperCase();

        // Check if it's a valid country code
        if (COUNTRY_CODES.containsKey(normalized)) {
            String countryName = COUNTRY_CODES.get(normalized);
            log.debug("Country validated by code: '{}' -> '{}'", country, countryName);
            return countryName;
        }

        // Check if it's a valid country name
        String countryCode = COUNTRY_NAMES.get(country.toLowerCase());
        if (countryCode != null) {
            String countryName = COUNTRY_CODES.get(countryCode);
            log.debug("Country validated by name: '{}' -> '{}'", country, countryName);
            return countryName;
        }

        log.debug("Country validation failed for: '{}'", country);
        return null;
    }

    @Override
    public String validateState(String state, String country) {
        if (state == null || state.trim().isEmpty()) {
            return null;
        }

        String normalizedState = state.trim();
        String validatedCountry = validateCountry(country);

        if (validatedCountry == null) {
            log.debug("State validation failed: invalid country '{}'", country);
            return null;
        }

        // Basic state validation - for now, just normalize the name
        // In a real implementation, this would check against actual state/province
        // lists
        String normalized = normalizedState.substring(0, 1).toUpperCase() +
                normalizedState.substring(1).toLowerCase();

        // Length check
        if (normalized.length() < 2 || normalized.length() > 50) {
            log.debug("State validation failed: invalid length for '{}'", state);
            return null;
        }

        log.debug("State validated: '{}' in '{}' -> '{}'", state, country, normalized);
        return normalized;
    }

    @Override
    public String getTimezoneFromCoordinates(double latitude, double longitude) {
        if (!validateCoordinates(latitude, longitude)) {
            return null;
        }

        // Simple timezone determination based on longitude
        // This is a very basic approximation - real implementation would use timezone
        // database
        int hourOffset = (int) Math.round(longitude / 15.0);

        // Clamp to valid timezone offsets
        hourOffset = Math.max(-12, Math.min(12, hourOffset));

        String timezoneId;
        try {
            // Try to find a valid timezone ID
            if (hourOffset == 0) {
                timezoneId = "UTC";
            } else {
                String sign = hourOffset > 0 ? "+" : "";
                timezoneId = "UTC" + sign + hourOffset;
            }

            // Verify it's a valid timezone
            ZoneId.of(timezoneId);
        } catch (Exception e) {
            // Fallback to UTC
            timezoneId = "UTC";
        }

        log.debug("Timezone determined for coordinates ({}, {}): {}", latitude, longitude, timezoneId);
        return timezoneId;
    }

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (!validateCoordinates(lat1, lon1) || !validateCoordinates(lat2, lon2)) {
            return 0.0;
        }

        // Haversine formula for distance calculation
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;

        log.debug("Distance calculated between ({}, {}) and ({}, {}): {} km",
                lat1, lon1, lat2, lon2, distance);
        return distance;
    }

    @Override
    public boolean isWithinCountry(double latitude, double longitude, String country) {
        if (!validateCoordinates(latitude, longitude)) {
            return false;
        }

        String validatedCountry = validateCountry(country);
        if (validatedCountry == null) {
            return false;
        }

        // Very basic country boundary check using rough bounding boxes
        // In a real implementation, this would use proper GIS/geospatial data
        boolean isWithin = false;

        switch (validatedCountry.toLowerCase()) {
            case "united states":
                isWithin = latitude >= 24.0 && latitude <= 49.0 &&
                        longitude >= -125.0 && longitude <= -67.0;
                break;
            case "canada":
                isWithin = latitude >= 41.0 && latitude <= 83.0 &&
                        longitude >= -141.0 && longitude <= -52.0;
                break;
            case "united kingdom":
                isWithin = latitude >= 49.0 && latitude <= 59.0 &&
                        longitude >= -8.0 && longitude <= 2.0;
                break;
            default:
                // For other countries, just check if coordinates are reasonable
                isWithin = latitude >= -90.0 && latitude <= 90.0 &&
                        longitude >= -180.0 && longitude <= 180.0;
                break;
        }

        log.debug("Country boundary check: ({}, {}) in '{}' -> {}", latitude, longitude, country, isWithin);
        return isWithin;
    }

    @Override
    public GeographicMetadata getGeographicMetadata(double latitude, double longitude) {
        if (!validateCoordinates(latitude, longitude)) {
            return GeographicMetadata.builder()
                    .confidenceScore(0.0)
                    .build();
        }

        // Basic metadata generation - in a real implementation, this would use
        // geocoding services
        String timezone = getTimezoneFromCoordinates(latitude, longitude);

        // Determine continent based on coordinates (rough approximation)
        String continent = determineContinent(latitude, longitude);
        String region = determineRegion(latitude, longitude, continent);

        // Determine if metropolitan area (rough check for major cities)
        boolean isMetropolitan = isMajorCity(latitude, longitude);

        // Population density estimation
        String populationDensity = estimatePopulationDensity(latitude, longitude);

        double confidenceScore = 30.0; // Low confidence for basic implementation

        return GeographicMetadata.builder()
                .timezone(timezone)
                .continent(continent)
                .region(region)
                .isMetropolitanArea(isMetropolitan)
                .populationDensity(populationDensity)
                .confidenceScore(confidenceScore)
                .build();
    }

    private String determineContinent(double latitude, double longitude) {
        // Rough continent determination
        if (latitude >= -60 && latitude <= 80) {
            if (longitude >= -168 && longitude <= -35) {
                return "North America";
            } else if (longitude >= -35 && longitude <= 70) {
                return "Europe";
            } else if (longitude >= 70 && longitude <= 180) {
                return "Asia";
            }
        }

        if (latitude >= -50 && latitude <= 40 && longitude >= -20 && longitude <= 55) {
            return "Africa";
        }

        if (latitude >= -60 && latitude <= 20 && longitude >= 110 && longitude <= 180) {
            return "Oceania";
        }

        if (latitude >= -60 && latitude <= 15 && longitude >= -90 && longitude <= -30) {
            return "South America";
        }

        return "Unknown";
    }

    private String determineRegion(double latitude, double longitude, String continent) {
        // Basic region determination within continents
        switch (continent) {
            case "North America":
                return longitude < -100 ? "Western US/Canada" : "Eastern US/Canada";
            case "Europe":
                return latitude > 50 ? "Northern Europe" : "Southern Europe";
            case "Asia":
                return longitude > 100 ? "Eastern Asia" : "Western Asia";
            default:
                return "General";
        }
    }

    private boolean isMajorCity(double latitude, double longitude) {
        // Rough check for some major cities
        // New York: ~40.7, -74.0
        if (Math.abs(latitude - 40.7) < 0.5 && Math.abs(longitude - (-74.0)) < 0.5)
            return true;
        // London: ~51.5, -0.1
        if (Math.abs(latitude - 51.5) < 0.5 && Math.abs(longitude - (-0.1)) < 0.5)
            return true;
        // Tokyo: ~35.7, 139.7
        if (Math.abs(latitude - 35.7) < 0.5 && Math.abs(longitude - 139.7) < 0.5)
            return true;

        return false; // Not a major city in our simple check
    }

    private String estimatePopulationDensity(double latitude, double longitude) {
        // Rough population density estimation
        // Check if near major metropolitan areas
        if (isMajorCity(latitude, longitude)) {
            return "Metropolitan";
        }

        // Check latitude bands (temperate zones tend to be more populated)
        if (latitude >= 20 && latitude <= 60) {
            return "Urban";
        } else if (latitude >= -20 && latitude <= 20) {
            return "Suburban";
        } else {
            return "Rural";
        }
    }
}