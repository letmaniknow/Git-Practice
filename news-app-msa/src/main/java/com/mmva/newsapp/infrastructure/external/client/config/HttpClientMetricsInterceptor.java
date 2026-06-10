package com.mmva.newsapp.infrastructure.external.client.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Metrics interceptor for RestClient HTTP requests.
 * Records request count, duration, and error metrics using Micrometer.
 */
@RequiredArgsConstructor
public class HttpClientMetricsInterceptor implements ClientHttpRequestInterceptor {

    private final MeterRegistry meterRegistry;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        String method = request.getMethod().name();
        String host = request.getURI().getHost();
        String metricName = "http.client.requests";

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            ClientHttpResponse response = execution.execute(request, body);

            // Record successful request
            sample.stop(Timer.builder(metricName)
                    .tag("method", method)
                    .tag("host", host)
                    .tag("status", String.valueOf(response.getStatusCode().value()))
                    .register(meterRegistry));

            return response;
        } catch (Exception e) {
            // Record failed request
            sample.stop(Timer.builder(metricName)
                    .tag("method", method)
                    .tag("host", host)
                    .tag("status", "ERROR")
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry));

            throw e;
        }
    }
}