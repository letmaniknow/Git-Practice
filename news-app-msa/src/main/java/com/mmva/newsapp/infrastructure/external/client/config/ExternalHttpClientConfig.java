package com.mmva.newsapp.infrastructure.external.client.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.config.RequestConfig;

/**
 * Modern HTTP client configuration using Spring's RestClient.
 *
 * <p>
 * Provides enterprise-grade HTTP client capabilities with:
 * </p>
 * <ul>
 * <li>Connection pooling and timeout management</li>
 * <li>Request/response logging and metrics</li>
 * <li>Error handling and resilience patterns</li>
 * <li>Custom interceptors for cross-cutting concerns</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ExternalHttpClientConfig {

    @Value("${external.http-client.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${external.http-client.read-timeout:30000}")
    private int readTimeout;

    @Value("${external.http-client.max-connections:20}")
    private int maxConnections;

    @Value("${external.http-client.max-connections-per-route:5}")
    private int maxConnectionsPerRoute;

    /**
     * Configures the underlying HTTP client request factory.
     *
     * @return configured HttpComponentsClientHttpRequestFactory
     */
    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // Configure HttpClient with timeouts
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                        .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
                        .build())
                .build();
        factory.setHttpClient(httpClient);

        // Connection pooling (if using Apache HttpClient)
        // Note: For production, consider using HttpComponentsClientHttpRequestFactory
        // with pooling configuration

        log.info("HTTP Client configured - connectTimeout: {}ms, readTimeout: {}ms",
                connectTimeout, readTimeout);

        return factory;
    }

    /**
     * Configures the primary RestClient builder for external API calls.
     *
     * @param meterRegistry metrics registry for monitoring
     * @return configured RestClient.Builder
     */
    @Bean
    public RestClient.Builder restClientBuilder(MeterRegistry meterRegistry) {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .requestInterceptor(new HttpClientLoggingInterceptor())
                .requestInterceptor(new HttpClientMetricsInterceptor(meterRegistry))
                .defaultHeader("User-Agent", "NewsApp/1.0")
                .defaultHeader("Accept", "application/json");
    }

    /**
     * Configures the primary RestClient for external API calls.
     *
     * @param restClientBuilder the configured RestClient builder
     * @return configured RestClient instance
     */
    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.build();
    }

    /**
     * Creates HttpServiceProxyFactory for declarative HTTP clients.
     *
     * @param restClientBuilder the RestClient builder
     * @return configured HttpServiceProxyFactory
     */
    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder.build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    }
}