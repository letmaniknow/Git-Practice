package com.mmva.newsapp.infrastructure.push.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Push Notification Monitoring Dashboard Controller.
 *
 * <h3>Features:</h3>
 * <ul>
 * <li>Push notification monitoring dashboard</li>
 * <li>Real-time metrics visualization</li>
 * <li>Health status monitoring</li>
 * <li>Admin-only access control</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Controller
@RequestMapping("/admin/monitoring")
@Tag(name = "Push Monitoring Dashboard", description = "Push notification monitoring and visualization")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class PushMonitoringDashboardController {

    /**
     * Display the push notification monitoring dashboard.
     *
     * @return dashboard view
     */
    @GetMapping("/push-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Push monitoring dashboard", description = "Display the push notification monitoring dashboard with real-time metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard displayed successfully"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public String pushMonitoringDashboard() {
        log.info("Accessing push notification monitoring dashboard");
        return "push/monitoring-dashboard";
    }
}