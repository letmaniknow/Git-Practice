package com.mmva.newsapp.domain.news.service.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mmva.newsapp.domain.news.dto.core.NewsCreateResponseDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for advanced news search with admin-level filtering
 * capabilities.
 *
 * <p>
 * Provides comprehensive search functionality exclusive to admin users,
 * including filtering by workflow status, date ranges, categories, and
 * full-text search across all news articles regardless of status.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 * <li>Full-text search across title, content, and metadata</li>
 * <li>Workflow status filtering (single or multiple statuses)</li>
 * <li>Category-based filtering</li>
 * <li>Date range filtering</li>
 * <li>Combined filtering with proper pagination</li>
 * </ul>
 *
 * <h3>Admin-Only Exception:</h3>
 * This service is exclusive to admin users and provides access to:
 * <ul>
 * <li>ALL workflow statuses (DRAFT, SUBMITTED, REVIEWED, APPROVED, PUBLISHED,
 * ARCHIVED)</li>
 * <li>Unpublished/archived content</li>
 * <li>Sensitive metadata</li>
 * </ul>
 *
 * <p>
 * For public search capabilities, use {@code PublicNewsSearchService}.
 * </p>
 *
 * @author MMVA Team
 * @version 1.0
 * @since 2026-03-04
 * @see com.mmva.newsapp.domain.news.service.search.PublicNewsSearchService
 */
public interface AdminNewsSearchService {

        /**
         * Advanced search with workflow status filtering for admin users.
         *
         * <p>
         * Searches news articles across all statuses with optional filtering.
         * This is the primary method for admin search operations combining multiple
         * filters.
         * </p>
         *
         * @param query            optional text search query (searches title, content,
         *                         excerpt)
         * @param workflowStatuses optional list of workflow statuses to filter by
         *                         (e.g., PUBLISHED, DRAFT)
         * @param categoryId       optional category ID to filter by
         * @param fromDate         optional start date for date range filter
         * @param toDate           optional end date for date range filter
         * @param pageable         pagination and sorting information
         * @return paginated search results matching all specified criteria
         *
         * @example
         *          // Search for published articles in March 2026
         *          searchWithStatusFilter(
         *          null,
         *          ["PUBLISHED"],
         *          null,
         *          LocalDate.of(2026, 3, 1),
         *          LocalDate.of(2026, 3, 31),
         *          pageable
         *          )
         *
         * @example
         *          // Search for "breaking news" in draft or submitted status
         *          searchWithStatusFilter(
         *          "breaking",
         *          ["DRAFT", "SUBMITTED"],
         *          null,
         *          null,
         *          null,
         *          pageable
         *          )
         */
        Page<NewsCreateResponseDto> searchWithStatusFilter(
                        String query,
                        List<String> workflowStatuses,
                        String categoryId,
                        LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable);

        /**
         * Search only by workflow status.
         *
         * <p>
         * Convenience method for searching news by status only.
         * </p>
         *
         * @param workflowStatuses list of statuses to filter by
         * @param pageable         pagination information
         * @return paginated news articles with specified statuses
         */
        Page<NewsCreateResponseDto> searchByWorkflowStatuses(
                        List<String> workflowStatuses,
                        Pageable pageable);

        /**
         * Search by query with optional status filter.
         *
         * <p>
         * Convenience method for text search with optional status constraints.
         * </p>
         *
         * @param query            text search query
         * @param workflowStatuses optional list of statuses to further constrain search
         * @param pageable         pagination information
         * @return paginated news articles matching search criteria
         */
        Page<NewsCreateResponseDto> searchByQuery(
                        String query,
                        List<String> workflowStatuses,
                        Pageable pageable);

        /**
         * Search within a date range.
         *
         * <p>
         * Convenience method for date-range based searches.
         * </p>
         *
         * @param fromDate         start date (inclusive)
         * @param toDate           end date (inclusive)
         * @param workflowStatuses optional status filter
         * @param pageable         pagination information
         * @return paginated news articles created within the date range
         */
        Page<NewsCreateResponseDto> searchByDateRange(
                        LocalDate fromDate,
                        LocalDate toDate,
                        List<String> workflowStatuses,
                        Pageable pageable);

        /**
         * Multi-field search across multiple content fields.
         *
         * <p>
         * Searches across multiple VARCHAR/TEXT fields including:
         * <ul>
         * <li>newsSlug - article URL slug</li>
         * <li>newsTitleEn/newsTitleEs - article titles in English/Spanish</li>
         * <li>newsContentEn/newsContentEs - article content in English/Spanish</li>
         * <li>newsKeywords - SEO keywords</li>
         * <li>newsMetaDescription - SEO meta description</li>
         * </ul>
         * </p>
         *
         * <p>
         * <strong>Database Support:</strong> This method uses LIKE/LOWER operations and
         * is
         * optimized for PostgreSQL. For other databases, falls back to single-field
         * search.
         * </p>
         *
         * <p>
         * <strong>Search Strategy (ES-First/DB-Fallback):</strong>
         * <ol>
         * <li>Attempts Elasticsearch for fast full-text search (if enabled)</li>
         * <li>Falls back to database LIKE search if ES unavailable/fails</li>
         * <li>Combines results with workflow status and other filters</li>
         * </ol>
         * </p>
         *
         * @param query            text search query (searches all multi-field targets)
         * @param workflowStatuses optional list of workflow statuses to filter by
         * @param categoryId       optional category ID to filter by
         * @param fromDate         optional start date for date range filter
         * @param toDate           optional end date for date range filter
         * @param pageable         pagination and sorting information
         * @return paginated search results matching query across all fields and filters
         *
         * @example
         *          // Search for "breaking" across all fields in published status
         *          searchWithMultipleFields(
         *          "breaking",
         *          ["PUBLISHED"],
         *          null,
         *          null,
         *          null,
         *          pageable
         *          )
         *
         * @example
         *          // Search "climate" in published articles from March 2026
         *          searchWithMultipleFields(
         *          "climate",
         *          ["PUBLISHED"],
         *          null,
         *          LocalDate.of(2026, 3, 1),
         *          LocalDate.of(2026, 3, 31),
         *          pageable
         *          )
         */
        /**
         * Multi-field search across multiple content fields.
         *
         * <p>
         * Searches across multiple VARCHAR/TEXT fields including:
         * <ul>
         * <li>newsSlug - article URL slug</li>
         * <li>newsTitleEn/newsTitleEs - article titles in English/Spanish</li>
         * <li>newsContentEn/newsContentEs - article content in English/Spanish</li>
         * <li>newsKeywords - SEO keywords</li>
         * <li>newsMetaDescription - SEO meta description</li>
         * </ul>
         * </p>
         *
         * <p>
         * <strong>Database Support:</strong> This method uses LIKE/LOWER operations and
         * is
         * optimized for PostgreSQL. For other databases, falls back to single-field
         * search.
         * </p>
         *
         * <p>
         * <strong>Search Strategy (ES-First/DB-Fallback):</strong>
         * <ol>
         * <li>Attempts Elasticsearch for fast full-text search (if enabled)</li>
         * <li>Falls back to database LIKE search if ES unavailable/fails</li>
         * <li>Combines results with workflow status and other filters</li>
         * </ol>
         * </p>
         *
         * @param query            optional text search query (searches all multi-field
         *                         targets)
         * @param workflowStatuses optional list of workflow statuses to filter by
         * @param categoryId       optional category ID to filter by
         * @param fromDate         optional start date for date range filter
         * @param toDate           optional end date for date range filter
         * @param pageable         pagination and sorting information
         * @return paginated search results matching query across all fields and filters
         */
        Page<NewsCreateResponseDto> searchWithMultipleFields(
                        String query,
                        List<String> workflowStatuses,
                        String categoryId,
                        String createdBy,
                        LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable);

        /**
         * Search by article title only (exact article lookup).
         *
         * <p>
         * Searches ONLY title fields (newsTitleEn, newsTitleEs).
         * Used for finding specific articles by title for editing/deletion.
         * Combines with workflow status, category, and date filters.
         * </p>
         *
         * <p>
         * <strong>Use Case:</strong> Admin searches exact title
         * "Elon introduced new car model" → finds ONE article to edit/delete.
         * </p>
         *
         * @param query            optional text search query (title match only)
         * @param workflowStatuses optional list of workflow statuses to filter by
         * @param categoryId       optional category ID to filter by
         * @param fromDate         optional start date for date range filter
         * @param toDate           optional end date for date range filter
         * @param pageable         pagination and sorting information
         * @return paginated search results matching title query and filters
         */
        Page<NewsCreateResponseDto> searchByTitleOnly(
                        String query,
                        List<String> workflowStatuses,
                        String categoryId,
                        String createdBy,
                        LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable);

        /**
         * Search by content/metadata fields only (topic discovery).
         *
         * <p>
         * Searches content, metadata, keywords, tags, and SEO fields
         * (everything EXCEPT title). Used for finding related articles
         * by topic without requiring exact title match.
         * </p>
         *
         * <p>
         * <strong>Use Case:</strong> Admin searches "electric vehicles"
         * → finds all articles discussing the topic in content/metadata.
         * </p>
         *
         * @param query            optional text search query (content/topic match)
         * @param workflowStatuses optional list of workflow statuses to filter by
         * @param categoryId       optional category ID to filter by
         * @param fromDate         optional start date for date range filter
         * @param toDate           optional end date for date range filter
         * @param pageable         pagination and sorting information
         * @return paginated search results matching content query and filters
         */
        Page<NewsCreateResponseDto> searchByContentFieldsOnly(
                        String query,
                        List<String> workflowStatuses,
                        String categoryId,
                        String createdBy,
                        LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable);
}
