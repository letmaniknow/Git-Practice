package com.mmva.newsapp.infrastructure.external.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * External News API client using Spring RestClient.
 *
 * <p>
 * Demonstrates modern HTTP client patterns for external API integration:
 * </p>
 * <ul>
 * <li>Fluent API design with RestClient</li>
 * <li>Comprehensive error handling</li>
 * <li>Request/response logging via interceptors</li>
 * <li>Metrics collection</li>
 * <li>Timeout and retry configurations</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalNewsApiClient {

    private final RestClient restClient;

    @Value("${external.news-api.base-url:https://newsapi.org}")
    private String baseUrl;

    @Value("${external.news-api.api-key}")
    private String apiKey;

    @Value("${external.news-api.timeout:10000}")
    private int timeout;

    /**
     * Fetches top headlines from external news API.
     *
     * @param country  country code (e.g., "us", "gb")
     * @param category news category (optional)
     * @return list of news articles
     */
    public NewsApiResponse getTopHeadlines(String country, String category) {
        log.debug("Fetching top headlines for country: {}, category: {}", country, category);

        try {
            Map<String, Object> params = Map.of(
                    "country", country,
                    "apiKey", apiKey);

            if (category != null && !category.trim().isEmpty()) {
                params.put("category", category);
            }

            return restClient.get()
                    .uri(baseUrl + "/v2/top-headlines?country={country}&category={category}&apiKey={apiKey}",
                            country, category, apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NewsApiResponse.class);

        } catch (RestClientException e) {
            log.error("Failed to fetch top headlines for country: {}", country, e);
            throw new ExternalApiException("Failed to fetch news headlines", e);
        }
    }

    /**
     * Searches for news articles by query.
     *
     * @param query    search query
     * @param fromDate start date (optional)
     * @param toDate   end date (optional)
     * @param sortBy   sort criteria (publishedAt, relevancy, popularity)
     * @return search results
     */
    public NewsApiResponse searchNews(String query, LocalDateTime fromDate,
            LocalDateTime toDate, String sortBy) {
        log.debug("Searching news with query: {}", query);

        try {
            // Build URI with all parameters using UriComponentsBuilder
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/v2/everything")
                    .queryParam("q", query)
                    .queryParam("apiKey", apiKey);

            // Add optional parameters
            if (fromDate != null) {
                uriBuilder.queryParam("from", fromDate.toString());
            }
            if (toDate != null) {
                uriBuilder.queryParam("to", toDate.toString());
            }
            if (sortBy != null) {
                uriBuilder.queryParam("sortBy", sortBy);
            }

            return restClient.get()
                    .uri(uriBuilder.toUriString())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NewsApiResponse.class);

        } catch (RestClientException e) {
            log.error("Failed to search news with query: {}", query, e);
            throw new ExternalApiException("Failed to search news", e);
        }
    }

    /**
     * Gets news sources from external API.
     *
     * @param category filter by category (optional)
     * @param language filter by language (optional)
     * @param country  filter by country (optional)
     * @return list of news sources
     */
    public NewsSourcesResponse getSources(String category, String language, String country) {
        log.debug("Fetching news sources - category: {}, language: {}, country: {}",
                category, language, country);

        try {
            // Build URI with all parameters using UriComponentsBuilder
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .path("/v2/sources")
                    .queryParam("apiKey", apiKey);

            // Add optional filters
            if (category != null) {
                uriBuilder.queryParam("category", category);
            }
            if (language != null) {
                uriBuilder.queryParam("language", language);
            }
            if (country != null) {
                uriBuilder.queryParam("country", country);
            }

            return restClient.get()
                    .uri(uriBuilder.toUriString())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NewsSourcesResponse.class);

        } catch (RestClientException e) {
            log.error("Failed to fetch news sources", e);
            throw new ExternalApiException("Failed to fetch news sources", e);
        }
    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * News API response wrapper.
     */
    public static class NewsApiResponse {
        private String status;
        private int totalResults;
        private List<Article> articles;
        private String message; // For error responses

        // Getters and setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(int totalResults) {
            this.totalResults = totalResults;
        }

        public List<Article> getArticles() {
            return articles;
        }

        public void setArticles(List<Article> articles) {
            this.articles = articles;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * News article representation.
     */
    public static class Article {
        private String title;
        private String description;
        private String content;
        private String url;
        private String urlToImage;
        private String publishedAt;
        private Source source;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrlToImage() {
            return urlToImage;
        }

        public void setUrlToImage(String urlToImage) {
            this.urlToImage = urlToImage;
        }

        public String getPublishedAt() {
            return publishedAt;
        }

        public void setPublishedAt(String publishedAt) {
            this.publishedAt = publishedAt;
        }

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }
    }

    /**
     * News source representation.
     */
    public static class Source {
        private String id;
        private String name;
        private String description;
        private String url;
        private String category;
        private String language;
        private String country;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    /**
     * News sources response wrapper.
     */
    public static class NewsSourcesResponse {
        private String status;
        private List<Source> sources;

        // Getters and setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<Source> getSources() {
            return sources;
        }

        public void setSources(List<Source> sources) {
            this.sources = sources;
        }
    }
}