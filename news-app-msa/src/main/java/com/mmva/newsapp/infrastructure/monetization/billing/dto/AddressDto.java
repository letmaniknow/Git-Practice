package com.mmva.newsapp.infrastructure.monetization.billing.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * Address DTO for billing and shipping addresses.
 */
@Data
@Builder
public class AddressDto {

    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String streetAddress;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State/Province cannot exceed 100 characters")
    private String stateProvince;

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 3, message = "Country code must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Country code must be 3 uppercase letters")
    private String countryCode;

    /**
     * Get formatted full address
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            sb.append(streetAddress.trim());
        }
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(city.trim());
        }
        if (stateProvince != null && !stateProvince.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(stateProvince.trim());
        }
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(postalCode.trim());
        }
        if (country != null && !country.trim().isEmpty()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(country.trim());
        }
        return sb.toString();
    }
}