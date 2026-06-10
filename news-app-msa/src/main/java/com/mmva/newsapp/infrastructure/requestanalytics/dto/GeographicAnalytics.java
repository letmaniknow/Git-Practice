package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Geographic distribution analytics.
 */
public record GeographicAnalytics(
                List<CountryStats> countries,
                List<CityStats> cities,
                List<IpStats> ips,
                List<TimezoneStats> timezones,
                List<IspStats> isps) {

        public record CountryStats(
                        String country,
                        long requestCount) {
        }

        public record CityStats(
                        String city,
                        String country,
                        long requestCount) {
        }

        public record IpStats(
                        String ipAddress,
                        String country,
                        String city,
                        long requestCount) {
        }

        public record TimezoneStats(
                        String timezone,
                        long requestCount) {
        }

        public record IspStats(
                        String isp,
                        String country,
                        long requestCount) {
        }
}