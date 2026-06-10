package com.mmva.newsapp.domain.news.dto.seo;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Schema.org WebPage structured data.
 *
 * <p>
 * Represents a web page in structured data format
 * for Schema.org compliance.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
public class NewsStructuredDataWebPage {

    /**
     * Schema.org type (WebPage).
     */
    private String type;

    /**
     * Web page ID/URL.
     */
    private String id;
}