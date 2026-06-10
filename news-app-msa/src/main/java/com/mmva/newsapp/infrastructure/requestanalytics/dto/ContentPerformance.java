package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Content and API performance.
 */
public record ContentPerformance(
                List<EndpointStats> mostAccessedEndpoints,
                List<EndpointStats> leastAccessedEndpoints,
                List<RefererStats> topReferrers,
                List<MethodDistribution> methodDistribution) {

        public record EndpointStats(
                        String uri,
                        String method,
                        long count,
                        Double averageResponseTime) {
        }

        public record RefererStats(
                        String referer,
                        long count) {
        }

        public record MethodDistribution(
                        String method,
                        long count,
                        Double percentage) {
        }
}