package com.mmva.newsapp.domain.news.dto.seo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for Schema.org SpeakableSpecification structured data.
 *
 * <p>
 * Defines which parts of a web page are suitable for text-to-speech
 * conversion for voice search and accessibility.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
public class NewsStructuredDataSpeakableSpec {

    /**
     * Schema.org type (SpeakableSpecification).
     */
    private String type;

    /**
     * CSS selectors for speakable content.
     */
    private List<String> cssSelector;
}