package com.mmva.newsapp.domain.news.config.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time news updates.
 *
 * <p>
 * Configures STOMP over WebSocket endpoints for real-time communication
 * between the server and connected clients. Enables broadcasting of
 * breaking news, live updates, and real-time notifications.
 * </p>
 *
 * <h3>WebSocket Endpoints:</h3>
 * <ul>
 * <li><b>/ws</b> - Main WebSocket endpoint for STOMP connections</li>
 * <li><b>/app/**</b> - Application destination prefix for client messages</li>
 * <li><b>/topic/**</b> - Broadcast destination prefix for server messages</li>
 * <li><b>/queue/**</b> - User-specific destination prefix for private
 * messages</li>
 * </ul>
 *
 * <h3>Supported Message Types:</h3>
 * <ul>
 * <li>Breaking news alerts</li>
 * <li>Live news updates</li>
 * <li>Comment notifications</li>
 * <li>Real-time analytics</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class NewsRealtimeWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for WebSocket communication.
     *
     * <p>
     * Sets up in-memory message broker for broadcasting messages to
     * connected clients. Enables simple broker for /topic and /queue destinations.
     * </p>
     *
     * @param config Message broker registry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker for broadcasting
        config.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints for WebSocket connections.
     *
     * <p>
     * Registers the main WebSocket endpoint at /ws and enables SockJS
     * fallback for browsers that don't support WebSocket natively.
     * </p>
     *
     * @param registry STOMP endpoint registry to configure
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register main WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Configure for production with specific origins
                .withSockJS();
    }
}