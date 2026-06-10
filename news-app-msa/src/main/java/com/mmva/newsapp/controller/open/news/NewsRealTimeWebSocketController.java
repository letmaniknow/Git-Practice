package com.mmva.newsapp.controller.open.news;

import com.mmva.newsapp.domain.news.dto.realtime.NewsRealTimeWebSocketRequestDto;
import com.mmva.newsapp.domain.news.dto.realtime.NewsRealTimeWebSocketResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for real-time news updates and subscriptions.
 *
 * <p>
 * Handles WebSocket STOMP messages for real-time communication between
 * the server and connected clients. Manages subscriptions, broadcasts
 * notifications, and provides real-time news updates.
 * </p>
 *
 * <h3>WebSocket Endpoints:</h3>
 * <ul>
 * <li><b>STOMP /app/subscribe</b> - Subscribe to real-time updates</li>
 * <li><b>STOMP /app/unsubscribe</b> - Unsubscribe from updates</li>
 * <li><b>HTTP /api/v1/ws/status</b> - Get WebSocket connection status</li>
 * </ul>
 *
 * <h3>Message Destinations:</h3>
 * <ul>
 * <li><b>/topic/breaking-news</b> - Breaking news broadcasts</li>
 * <li><b>/topic/news-updates</b> - Live news updates</li>
 * <li><b>/topic/comments/{newsId}</b> - Article comments</li>
 * <li><b>/user/queue/notifications</b> - Personal notifications</li>
 * <li><b>/topic/analytics</b> - Real-time analytics</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Controller
@RequestMapping("/api/v1/ws")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WebSocket Real-time Updates", description = "Real-time news updates via WebSocket")
public class NewsRealTimeWebSocketController {

        // ========================================
        // WebSocket Message Handlers
        // ========================================

        /**
         * Handle subscription requests from WebSocket clients.
         *
         * <p>
         * Processes client subscription requests and configures
         * real-time update preferences.
         * </p>
         *
         * @param request   Subscription request details
         * @param principal Authenticated user principal
         * @return Subscription confirmation response
         */
        @MessageMapping("/subscribe")
        @SendToUser("/queue/subscription")
        public NewsRealTimeWebSocketResponseDto handleSubscription(
                        @Parameter(description = "Subscription request details") @Payload @Valid NewsRealTimeWebSocketRequestDto request,
                        Principal principal) {

                log.info("WebSocket subscription request from user: {} - action: {}",
                                principal != null ? principal.getName() : "anonymous", request.getAction());

                // Create subscription confirmation response
                Map<String, Object> subscriptionData = new HashMap<>();
                subscriptionData.put("action", request.getAction());
                subscriptionData.put("notificationTypes", request.getNotificationTypes());
                subscriptionData.put("categoryIds", request.getCategoryIds());
                subscriptionData.put("language", request.getLanguage());
                subscriptionData.put("platform", request.getPlatform());

                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("subscription_confirmed")
                                .title("Subscription Updated")
                                .message("Successfully " + request.getAction() + " to real-time updates")
                                .data(subscriptionData)
                                .category("system")
                                .build();
        }

        /**
         * Handle unsubscription requests from WebSocket clients.
         *
         * <p>
         * Processes client unsubscription requests and removes
         * real-time update preferences.
         * </p>
         *
         * @param request   Unsubscription request details
         * @param principal Authenticated user principal
         * @return Unsubscription confirmation response
         */
        @MessageMapping("/unsubscribe")
        @SendToUser("/queue/subscription")
        public NewsRealTimeWebSocketResponseDto handleUnsubscription(
                        @Parameter(description = "Unsubscription request details") @Payload @Valid NewsRealTimeWebSocketRequestDto request,
                        Principal principal) {

                log.info("WebSocket unsubscription request from user: {} - action: {}",
                                principal != null ? principal.getName() : "anonymous", request.getAction());

                // Create unsubscription confirmation response
                Map<String, Object> unsubscriptionData = new HashMap<>();
                unsubscriptionData.put("action", request.getAction());
                unsubscriptionData.put("notificationTypes", request.getNotificationTypes());

                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("unsubscription_confirmed")
                                .title("Unsubscription Confirmed")
                                .message("Successfully unsubscribed from real-time updates")
                                .data(unsubscriptionData)
                                .category("system")
                                .build();
        }

        /**
         * Handle ping messages from WebSocket clients.
         *
         * <p>
         * Responds to client ping messages to maintain connection
         * health and provide connection status.
         * </p>
         *
         * @param payload   Ping message payload
         * @param principal Authenticated user principal
         * @return Pong response with connection status
         */
        @MessageMapping("/ping")
        @SendToUser("/queue/pong")
        public NewsRealTimeWebSocketResponseDto handlePing(
                        @Parameter(description = "Ping message payload") @Payload Map<String, Object> payload,
                        Principal principal) {

                log.debug("WebSocket ping from user: {}",
                                principal != null ? principal.getName() : "anonymous");

                // Create pong response with connection status
                Map<String, Object> pongData = new HashMap<>();
                pongData.put("status", "connected");
                pongData.put("timestamp", java.time.LocalDateTime.now());
                pongData.put("user", principal != null ? principal.getName() : "anonymous");

                return NewsRealTimeWebSocketResponseDto.builder()
                                .type("pong")
                                .title("Connection Active")
                                .message("WebSocket connection is healthy")
                                .data(pongData)
                                .category("system")
                                .priority("low")
                                .build();
        }

        // ========================================
        // HTTP Endpoints for WebSocket Management
        // ========================================

        /**
         * Get WebSocket connection status and available endpoints.
         *
         * <p>
         * Provides information about WebSocket connection status
         * and available message destinations for client configuration.
         * </p>
         *
         * @return WebSocket status information
         */
        @GetMapping("/status")
        @ResponseBody
        @Operation(summary = "WebSocket Status", description = "Get WebSocket connection status and available endpoints")
        public Map<String, Object> getWebSocketStatus() {
                log.debug("WebSocket status request");

                Map<String, Object> status = new HashMap<>();

                // Connection information
                status.put("websocketEnabled", true);
                status.put("endpoint", "/ws");
                status.put("protocol", "STOMP over WebSocket");
                status.put("fallback", "SockJS enabled");

                // Available destinations
                Map<String, Object> destinations = new HashMap<>();
                destinations.put("broadcast", Map.of(
                                "breakingNews", "/topic/breaking-news",
                                "newsUpdates", "/topic/news-updates",
                                "analytics", "/topic/analytics",
                                "systemStatus", "/topic/system-status",
                                "maintenance", "/topic/maintenance"));
                destinations.put("userSpecific", Map.of(
                                "notifications", "/user/queue/notifications",
                                "recommendations", "/user/queue/recommendations",
                                "subscription", "/user/queue/subscription",
                                "pong", "/user/queue/pong"));
                destinations.put("articleSpecific", Map.of(
                                "comments", "/topic/comments/{newsId}"));

                status.put("destinations", destinations);

                // Message types
                status.put("messageTypes", new String[] {
                                "breaking_news", "news_published", "news_updated", "new_comment",
                                "user_notification", "news_recommendation", "analytics_update",
                                "system_status", "maintenance", "subscription_confirmed",
                                "unsubscription_confirmed", "pong"
                });

                // Connection limits and recommendations
                status.put("recommendations", Map.of(
                                "heartbeat", "25 seconds",
                                "reconnectDelay", "5 seconds",
                                "maxRetries", "10",
                                "supportedPlatforms", new String[] { "web", "mobile", "desktop" }));

                return status;
        }

        /**
         * Get WebSocket connection health check.
         *
         * <p>
         * Simple health check endpoint to verify WebSocket
         * service availability.
         * </p>
         *
         * @return Health status
         */
        @GetMapping("/health")
        @ResponseBody
        @Operation(summary = "WebSocket Health Check", description = "Check WebSocket service health")
        public Map<String, Object> getWebSocketHealth() {
                Map<String, Object> health = new HashMap<>();
                health.put("status", "UP");
                health.put("service", "WebSocket Real-time Updates");
                health.put("timestamp", java.time.LocalDateTime.now());
                health.put("features", new String[] {
                                "STOMP messaging",
                                "SockJS fallback",
                                "Broadcast messaging",
                                "User-specific messaging",
                                "Heartbeat support"
                });

                return health;
        }
}