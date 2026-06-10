package com.mmva.newsapp.domain.newsrss.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single RSS feed item.
 * Maps to an &lt;item&gt; element in RSS 2.0 specification.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsRssItemDto {

    /** Title of the newsapp newsapp */
    private String title;

    /** Full URL to the newsapp */
    private String link;

    /** Short description/summary of the newsapp */
    private String description;

    /** Publication date in RFC 822 format */
    private String pubDate;

    /** Unique identifier (GUID) for the item */
    private String guid;

    /** Author/creator of the newsapp */
    private String author;

    /** Category of the newsapp */
    private String category;

    /** URL to thumbnail/media image */
    private String thumbnailUrl;
}
