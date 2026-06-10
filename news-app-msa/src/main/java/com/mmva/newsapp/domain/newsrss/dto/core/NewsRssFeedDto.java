package com.mmva.newsapp.domain.newsrss.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing an RSS feed channel.
 * Maps to the &lt;channel&gt; element in RSS 2.0 specification.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsRssFeedDto {

    /** Title of the RSS feed */
    private String title;

    /** URL to the website */
    private String link;

    /** Description of the feed */
    private String description;

    /** Language code (e.g., "en-us", "es") */
    private String language;

    /** Last build date in RFC 822 format */
    private String lastBuildDate;

    /** Copyright notice */
    private String copyright;

    /** Feed generator identifier */
    private String generator;

    /** List of feed items */
    private List<NewsRssItemDto> items;
}
