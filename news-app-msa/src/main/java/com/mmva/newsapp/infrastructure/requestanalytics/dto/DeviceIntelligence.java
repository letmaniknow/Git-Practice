package com.mmva.newsapp.infrastructure.requestanalytics.dto;

import java.util.List;

/**
 * Device and browser intelligence.
 */
public record DeviceIntelligence(
                List<DeviceStats> deviceStats,
                List<BrowserShare> browserShares,
                List<OsShare> osShares) {

        public record DeviceStats(
                        String deviceType,
                        long count,
                        Double percentage) {
        }

        public record BrowserShare(
                        String browser,
                        String version,
                        long count,
                        Double percentage) {
        }

        public record OsShare(
                        String os,
                        String version,
                        long count,
                        Double percentage) {
        }
}