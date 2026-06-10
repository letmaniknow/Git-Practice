package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Error analytics and patterns.
 */
public record ErrorAnalytics(
                long totalErrors,
                Double errorRate,
                List<ErrorByEndpoint> errorsByEndpoint,
                List<ErrorStatusDistribution> statusDistributions,
                List<ExceptionPattern> exceptionPatterns) {

        public record ErrorByEndpoint(
                        String endpoint,
                        String method,
                        int statusCode,
                        long count) {
        }

        public record ErrorStatusDistribution(
                        int statusCode,
                        long count,
                        double percentage) {
        }

        public record ExceptionPattern(
                        String exceptionType,
                        long count) {
        }
}