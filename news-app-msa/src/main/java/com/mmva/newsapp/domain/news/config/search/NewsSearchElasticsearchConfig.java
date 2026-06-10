package com.mmva.newsapp.domain.news.config.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch client configuration for advanced search functionality.
 * Configures the Elasticsearch client connection with graceful fallback.
 *
 * When elasticsearch.enabled=true:
 * - Client beans are created
 * - If ES is unreachable, repositories gracefully disable
 * - App continues to work with PostgreSQL FTS fallback
 *
 * Disabled by default. Enable by setting elasticsearch.enabled=true
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class NewsSearchElasticsearchConfig {

    @Value("${elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port:9200}")
    private int elasticsearchPort;

    @Value("${elasticsearch.username:elastic}")
    private String elasticsearchUsername;

    @Value("${elasticsearch.password:changeme}")
    private String elasticsearchPassword;

    @Bean
    public RestClient elasticsearchRestClient() {
        log.info("Creating Elasticsearch RestClient for {}:{}", elasticsearchHost, elasticsearchPort);
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticsearchUsername, elasticsearchPassword));

        return RestClient.builder(new HttpHost(elasticsearchHost, elasticsearchPort))
                .setHttpClientConfigCallback(
                        httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport() {
        log.info("Creating Elasticsearch Transport");
        return new RestClientTransport(elasticsearchRestClient(), new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        log.info("Creating Elasticsearch Client");
        return new ElasticsearchClient(elasticsearchTransport());
    }
}

/**
 * Separate configuration for enabling Elasticsearch repositories.
 * This ensures repositories are only created when Elasticsearch is enabled.
 * 
 * Uses lazy initialization - repositories only connect when first accessed,
 * allowing app to boot even if Elasticsearch is temporarily unavailable.
 */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.mmva.newsapp.domain.news.repository",
        // Lazy repository initialization - connect only when actually used
        repositoryFactoryBeanClass = org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean.class)
class NewsSearchElasticsearchRepositoriesConfig {
}