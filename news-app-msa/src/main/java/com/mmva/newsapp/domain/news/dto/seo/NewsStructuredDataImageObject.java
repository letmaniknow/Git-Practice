package com.mmva.newsapp.domain.news.dto.seo;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Schema.org ImageObject structured data.
 *
 * <p>
 * Represents an image in structured data format
 * for Schema.org compliance.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
public class NewsStructuredDataImageObject {

    /**
     * Schema.org type (ImageObject).
     */
    private String type;

    /**
     * Image URL.
     */
    private String url;

    /**
     * Image width in pixels.
     */
    private Integer width;

    /**
     * Image height in pixels.
     */
    private Integer height;
}