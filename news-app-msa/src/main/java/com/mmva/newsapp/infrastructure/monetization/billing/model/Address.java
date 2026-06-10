package com.mmva.newsapp.infrastructure.monetization.billing.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Address entity for billing and shipping addresses.
 * Reusable across invoices and clients.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "street_address", length = 255)
    private String streetAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "country_code", length = 3)
    private String countryCode; // ISO 3166-1 alpha-3

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (streetAddress != null)
            sb.append(streetAddress);
        if (city != null) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(city);
        }
        if (stateProvince != null) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(stateProvince);
        }
        if (postalCode != null) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(postalCode);
        }
        if (country != null) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }

    // Additional getter methods for compatibility
    public String getStreet() {
        return streetAddress;
    }

    public String getState() {
        return stateProvince;
    }

    public String getZipCode() {
        return postalCode;
    }
}