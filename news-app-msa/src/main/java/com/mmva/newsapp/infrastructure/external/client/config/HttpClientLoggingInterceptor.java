package com.mmva.newsapp.infrastructure.external.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Request/Response logging interceptor for RestClient.
 * Logs HTTP requests and responses for debugging and monitoring.
 */
@Slf4j
public class HttpClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                      ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();

        // Log request
        log.debug("HTTP Request: {} {} - Body: {}",
                request.getMethod(), request.getURI(),
                body.length > 0 ? new String(body, StandardCharsets.UTF_8) : "empty");

        try {
            // Execute request
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            log.debug("HTTP Response: {} {} - Duration: {}ms - Status: {}",
                    request.getMethod(), request.getURI(), duration, response.getStatusCode());

            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("HTTP Request failed: {} {} - Duration: {}ms - Error: {}",
                    request.getMethod(), request.getURI(), duration, e.getMessage());
            throw e;
        }
    }
}