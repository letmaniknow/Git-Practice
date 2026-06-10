package com.mmva.newsapp.domain.news.dto.seo;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Schema.org Person structured data.
 *
 * <p>
 * Represents an author or person in structured data format
 * for Schema.org compliance.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
public class NewsStructuredDataPerson {

    /**
     * Schema.org type (Person).
     */
    private String type;

    /**
     * Person's name.
     */
    private String name;
}