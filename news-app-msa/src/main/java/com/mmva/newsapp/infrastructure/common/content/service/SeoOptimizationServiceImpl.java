package com.mmva.newsapp.infrastructure.common.content.service;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import com.mmva.newsapp.infrastructure.common.content.dto.OpenGraphMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of SeoOptimizationService for SEO analysis and optimization.
 *
 * <p>
 * Provides comprehensive SEO tools including meta description generation,
 * keyword extraction, slug creation, and social media metadata generation.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class SeoOptimizationServiceImpl implements SeoOptimizationService {

    private static final int META_DESCRIPTION_MAX_LENGTH = 160;
    private static final int META_DESCRIPTION_MIN_LENGTH = 120;
    private static final int TITLE_MAX_LENGTH = 60;
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}0-9\\s-]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final Pattern STOP_WORDS_PATTERN = Pattern.compile(
            "\\b(?:a|an|and|are|as|at|be|by|for|from|has|he|in|is|it|its|of|on|that|the|to|was|will|with)\\b",
            Pattern.CASE_INSENSITIVE);

    // Common stop words to exclude from keywords
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has",
            "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "will", "with",
            "but", "or", "not", "this", "these", "those", "i", "you", "he", "she", "we", "they");

    @Override
    public String generateMetaDescription(String title, String content, ContentFormat format) {
        log.debug("Generating meta description for title: {}", title);

        if (title == null || title.trim().isEmpty()) {
            return "";
        }

        String plainText = extractPlainText(content, format);
        String firstSentence = extractFirstSentence(plainText);

        // Start with title as base
        StringBuilder description = new StringBuilder(title);

        // Add first sentence if it provides value
        if (firstSentence != null && !firstSentence.isEmpty() &&
                !title.toLowerCase()
                        .contains(firstSentence.toLowerCase().substring(0, Math.min(20, firstSentence.length())))) {
            description.append(". ").append(firstSentence);
        }

        // Ensure optimal length
        String result = description.toString();
        if (result.length() > META_DESCRIPTION_MAX_LENGTH) {
            result = result.substring(0, META_DESCRIPTION_MAX_LENGTH - 3) + "...";
        } else if (result.length() < META_DESCRIPTION_MIN_LENGTH && plainText.length() > result.length()) {
            // Try to add more content if too short
            String additionalContent = extractSummary(plainText, META_DESCRIPTION_MAX_LENGTH - result.length() - 3);
            if (!additionalContent.isEmpty()) {
                result = result + ". " + additionalContent + "...";
            }
        }

        log.debug("Generated meta description: {} (length: {})", result, result.length());
        return result;
    }

    @Override
    public String[] extractKeywords(String title, String content, ContentFormat format, int maxKeywords) {
        log.debug("Extracting keywords, max: {}", maxKeywords);

        String plainText = extractPlainText(content, format);
        String combinedText = (title != null ? title + " " : "") + plainText;

        // Extract words and their frequencies
        Map<String, Integer> wordFrequency = new HashMap<>();
        String[] words = WORD_PATTERN.matcher(combinedText.toLowerCase()).results()
                .map(match -> match.group().toLowerCase())
                .filter(word -> word.length() > 2) // Skip very short words
                .filter(word -> !STOP_WORDS.contains(word))
                .toArray(String[]::new);

        for (String word : words) {
            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
        }

        // Sort by frequency and return top keywords
        return wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(maxKeywords)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }

    @Override
    public String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }

        log.debug("Generating slug for title: {}", title);

        // Normalize unicode characters
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{M}", ""); // Remove diacritics

        // Convert to lowercase and replace non-alphanumeric with hyphens
        slug = NON_ALPHANUMERIC.matcher(slug.toLowerCase()).replaceAll("-");

        // Remove multiple consecutive hyphens and trim
        slug = MULTIPLE_SPACES.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("-+", "-").trim();

        // Remove leading/trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");

        log.debug("Generated slug: {}", slug);
        return slug;
    }

    @Override
    public double calculateSeoScore(String title, String content, ContentFormat format) {
        if (title == null || title.trim().isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        int factors = 0;

        // Title length factor (30-60 characters is optimal)
        if (title.length() >= 30 && title.length() <= TITLE_MAX_LENGTH) {
            score += 25.0;
        } else if (title.length() >= 20 && title.length() <= 70) {
            score += 15.0;
        }
        factors++;

        // Meta description factor
        String metaDesc = generateMetaDescription(title, content, format);
        if (metaDesc.length() >= META_DESCRIPTION_MIN_LENGTH && metaDesc.length() <= META_DESCRIPTION_MAX_LENGTH) {
            score += 20.0;
        } else if (metaDesc.length() >= 100) {
            score += 10.0;
        }
        factors++;

        // Keyword factor - check if title contains important keywords
        String[] keywords = extractKeywords(title, content, format, 5);
        if (keywords.length > 0) {
            String titleLower = title.toLowerCase();
            int keywordMatches = 0;
            for (String keyword : keywords) {
                if (titleLower.contains(keyword.toLowerCase())) {
                    keywordMatches++;
                }
            }
            score += (keywordMatches / (double) keywords.length) * 15.0;
        }
        factors++;

        // Content length factor (optimal: 300-2000 words)
        String plainText = extractPlainText(content, format);
        int wordCount = countWords(plainText);
        if (wordCount >= 300 && wordCount <= 2000) {
            score += 20.0;
        } else if (wordCount >= 150 && wordCount <= 3000) {
            score += 10.0;
        }
        factors++;

        // Slug factor
        String slug = generateSlug(title);
        if (slug.length() >= 3 && slug.length() <= 50 && !slug.contains("--")) {
            score += 20.0;
        }
        factors++;

        double finalScore = factors > 0 ? (score / factors) * (factors / 5.0) : 0.0;
        log.debug("Calculated SEO score: {} for title: {}", finalScore, title);
        return Math.min(100.0, Math.max(0.0, finalScore));
    }

    @Override
    public OpenGraphMetadata generateOpenGraphMetadata(String title, String description, String imageUrl) {
        log.debug("Generating Open Graph metadata for title: {}", title);

        return OpenGraphMetadata.builder()
                .ogTitle(title)
                .ogDescription(description)
                .ogImage(imageUrl)
                .ogType("article")
                .ogSiteName("News App")
                .twitterTitle(title)
                .twitterDescription(description)
                .twitterImage(imageUrl)
                .build();
    }

    @Override
    public String optimizeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }

        String optimized = title.trim();

        // Ensure title is within optimal length
        if (optimized.length() > TITLE_MAX_LENGTH) {
            optimized = optimized.substring(0, TITLE_MAX_LENGTH - 3) + "...";
        }

        // Capitalize first letter of each word
        optimized = Arrays.stream(optimized.split("\\s+"))
                .map(word -> word.isEmpty() ? word
                        : word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));

        log.debug("Optimized title: {} -> {}", title, optimized);
        return optimized;
    }

    @Override
    public double analyzeKeywordDensity(String content, ContentFormat format, String keyword) {
        if (content == null || keyword == null || keyword.trim().isEmpty()) {
            return 0.0;
        }

        String plainText = extractPlainText(content, format);
        int totalWords = countWords(plainText);
        if (totalWords == 0) {
            return 0.0;
        }

        String[] keywordWords = keyword.toLowerCase().split("\\s+");
        int keywordOccurrences = 0;

        if (keywordWords.length == 1) {
            // Single word keyword
            String textLower = plainText.toLowerCase();
            int index = 0;
            while ((index = textLower.indexOf(keyword.toLowerCase(), index)) != -1) {
                keywordOccurrences++;
                index += keyword.length();
            }
        } else {
            // Multi-word keyword - count exact phrase matches
            String textLower = plainText.toLowerCase();
            String keywordPhrase = keyword.toLowerCase();
            int index = 0;
            while ((index = textLower.indexOf(keywordPhrase, index)) != -1) {
                keywordOccurrences++;
                index += keywordPhrase.length();
            }
        }

        double density = (double) keywordOccurrences / totalWords * 100.0;
        log.debug("Keyword density for '{}': {}% (occurrences: {}, total words: {})",
                keyword, density, keywordOccurrences, totalWords);
        return density;
    }

    private String extractPlainText(String content, ContentFormat format) {
        if (content == null) {
            return "";
        }
        if (format == null) {
            return content.trim();
        }

        switch (format) {
            case HTML_BASIC:
            case HTML_RICH:
                return content.replaceAll("<[^>]+>", "").trim();
            case MARKDOWN:
                return content.replaceAll("[*#_`~]", "").trim();
            case PLAIN_TEXT:
            default:
                return content.trim();
        }
    }

    private String extractFirstSentence(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String[] sentences = text.split("[.!?]+");
        return sentences.length > 0 ? sentences[0].trim() : "";
    }

    private String extractSummary(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "";
        }

        String summary = text.substring(0, maxLength);
        int lastSpace = summary.lastIndexOf(' ');
        if (lastSpace > 0) {
            summary = summary.substring(0, lastSpace);
        }

        return summary.trim();
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return WORD_PATTERN.matcher(text).results().toList().size();
    }
}