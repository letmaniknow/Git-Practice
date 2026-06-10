package com.mmva.newsapp.domain.news.dto.seo;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Schema.org Organization structured data.
 *
 * <p>
 * Represents a publisher organization in structured data format
 * for Schema.org compliance.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
public class NewsStructuredDataOrganization {

    /**
     * Schema.org type (Organization).
     */
    private String type;

    /**
     * Organization name.
     */
    private String name;

    /**
     * Organization logo.
     */
    private NewsStructuredDataImageObject logo;
}