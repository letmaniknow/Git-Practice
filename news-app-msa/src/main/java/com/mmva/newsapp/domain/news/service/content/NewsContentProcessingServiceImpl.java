package com.mmva.newsapp.domain.news.service.content;

import com.mmva.newsapp.domain.news.dto.validation.NewsContentValidationResult;
import com.mmva.newsapp.domain.news.dto.internal.NewsReadingMetrics;
import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of NewsContentProcessingService for news-specific content
 * processing.
 *
 * <p>
 * Provides specialized content processing operations tailored for news
 * articles,
 * including excerpt generation, validation, and optimization for news
 * publishing.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NewsContentProcessingServiceImpl implements NewsContentProcessingService {

    // =========================
    // 1. Format Conversion
    // =========================
    // (If you need format conversion helpers, add them here)

    // =========================
    // 2. HTML/Markdown Helpers
    // =========================
    private String convertPlainTextToHtml(String text) {
        if (text == null) {
            return "";
        }
        // Split into paragraphs and wrap with <p> tags
        String[] paragraphs = text.split("\n\s*\n");
        StringBuilder html = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (!paragraph.trim().isEmpty()) {
                html.append("<p>").append(paragraph.trim()).append("</p>\n");
            }
        }
        return html.toString().trim();
    }

    // Use Jsoup to extract text content
    private String extractTextFromHtml(String htmlContent) {
        if (htmlContent == null) {
            return "";
        }
        return org.jsoup.Jsoup.parse(htmlContent).text();
    }

    // Use Jsoup to sanitize HTML, allowing only safe tags
    private String sanitizeHtml(String htmlContent) {
        if (htmlContent == null) {
            return "";
        }
        return org.jsoup.Jsoup.clean(
            htmlContent,
            org.jsoup.safety.Safelist.relaxed()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6")
                .addAttributes("a", "href", "rel", "target")
                .addAttributes("img", "src", "alt", "width", "height")
                .addAttributes("table", "th", "td", "tr", "thead", "tbody")
                .addProtocols("a", "href", "http", "https", "mailto")
                .addProtocols("img", "src", "http", "https", "data")
        );
    }

    private String convertMarkdownToHtml(String markdown) {
        if (markdown == null) {
            return "";
        }
        // Basic markdown to HTML conversion
        String html = markdown
                .replaceAll("^### (.*$)", "<h3>$1</h3>")
                .replaceAll("^## (.*$)", "<h2>$1</h2>")
                .replaceAll("^# (.*$)", "<h1>$1</h1>")
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
                .replaceAll("\\n\\s*\\n", "</p>\n<p>")
                .replaceAll("^", "<p>")
                .replaceAll("$", "</p>");
        return html;
    }

    // Basic HTML to markdown conversion
    private String convertHtmlToMarkdown(String htmlContent) {
        if (htmlContent == null) {
            return "";
        }
        return htmlContent
            .replaceAll("<strong>(.*?)</strong>", "**$1**")
            .replaceAll("<em>(.*?)</em>", "*$1*")
            .replaceAll("<h1>(.*?)</h1>", "# $1\n")
            .replaceAll("<h2>(.*?)</h2>", "## $1\n")
            .replaceAll("<h3>(.*?)</h3>", "### $1\n")
            .replaceAll("<p>(.*?)</p>", "$1\n\n")
            .replaceAll("<br>", "  \n");
    }

    // =========================
    // 3. Content Optimization
    // =========================
    @Override
    public String optimizeForWebPublishing(String content, ContentFormat format) {
        log.debug("Optimizing content for web publishing, format: {}", format);
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        String optimized = content;
        switch (format) {
            case PLAIN_TEXT:
                optimized = convertPlainTextToHtml(optimized);
                break;
            case HTML_BASIC:
            case HTML_RICH:
                optimized = ensureHtmlStructure(optimized);
                break;
            case MARKDOWN:
                optimized = convertMarkdownToHtml(optimized);
                break;
        }
        optimized = addWebOptimizations(optimized);
        log.debug("Content optimized for web publishing");
        return optimized;
    }

    // =========================
    // 4. Excerpt, Headline, Keywords
    // =========================
    @Override
    public String generateExcerpt(String content, ContentFormat format, int maxLength, boolean includeEllipsis) {
        log.debug("Generating excerpt with max length: {}, format: {}", maxLength, format);
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        String plainText = extractPlainText(content, format);
        if (plainText.length() <= maxLength) {
            return plainText;
        }
        String excerpt = extractAtSentenceBoundary(plainText, maxLength);
        if (excerpt.length() <= maxLength) {
            return includeEllipsis ? excerpt + "..." : excerpt;
        }
        excerpt = extractAtWordBoundary(plainText, maxLength);
        String result = includeEllipsis ? excerpt + "..." : excerpt;
        log.debug("Generated excerpt: {} (length: {})", result, result.length());
        return result;
    }

    @Override
    public String processHeadline(String headline) {
        if (headline == null || headline.trim().isEmpty()) {
            return "";
        }
        String processed = headline.trim();
        if (processed.length() > 70) {
            processed = processed.substring(0, 67) + "...";
        }
        if (processed.length() > 0) {
            processed = processed.substring(0, 1).toUpperCase() + processed.substring(1);
        }
        if (!processed.matches(".*[.!?]$") && processed.length() > 20) {
            if (processed.toLowerCase().startsWith("what") ||
                    processed.toLowerCase().startsWith("how") ||
                    processed.toLowerCase().startsWith("why") ||
                    processed.toLowerCase().startsWith("when") ||
                    processed.toLowerCase().startsWith("where") ||
                    processed.toLowerCase().startsWith("who")) {
                processed += "?";
            }
        }
        log.debug("Processed headline: '{}' -> '{}'", headline, processed);
        return processed;
    }

    @Override
    public String[] extractNewsKeywords(String title, String content, ContentFormat format, int maxKeywords) {
        log.debug("Extracting news keywords, max: {}", maxKeywords);
        String combinedText = (title != null ? title + " " : "") + extractPlainText(content, format);
        combinedText = combinedText.toLowerCase();
        String[] newsPriorityWords = {
                "breaking", "exclusive", "update", "alert", "urgent", "developing",
                "government", "politics", "election", "president", "minister", "official",
                "police", "crime", "accident", "incident", "investigation", "arrest",
                "economy", "market", "business", "company", "stock", "trade",
                "weather", "storm", "disaster", "earthquake", "flood", "fire",
                "health", "medical", "disease", "vaccine", "hospital", "doctor",
                "sports", "game", "match", "player", "team", "championship",
                "technology", "internet", "digital", "social media", "app", "software"
        };
        List<String> foundPriorityWords = new ArrayList<>();
        for (String word : newsPriorityWords) {
            if (combinedText.contains(word) && !foundPriorityWords.contains(word)) {
                foundPriorityWords.add(word);
                if (foundPriorityWords.size() >= maxKeywords) {
                    break;
                }
            }
        }
        if (foundPriorityWords.size() < maxKeywords) {
            String[] words = WORD_PATTERN.matcher(combinedText).results()
                    .map(match -> match.group())
                    .filter(word -> word.length() > 3)
                    .toArray(String[]::new);
            java.util.Map<String, Integer> wordFreq = new java.util.HashMap<>();
            for (String word : words) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
            wordFreq.entrySet().stream()
                    .filter(entry -> !foundPriorityWords.contains(entry.getKey()))
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(maxKeywords - foundPriorityWords.size())
                    .forEach(entry -> foundPriorityWords.add(entry.getKey()));
        }
        String[] result = foundPriorityWords.toArray(new String[0]);
        log.debug("Extracted keywords: {}", Arrays.toString(result));
        return result;
    }

    // =========================
    // 5. Validation
    // =========================
    @Override
    public NewsContentValidationResult validateNewsContent(String content, ContentFormat format) {
        log.debug("Validating news content, format: {}", format);
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) {
            errors.add("Content cannot be empty");
            return NewsContentValidationResult.builder()
                    .isValid(false)
                    .errors(errors)
                    .warnings(warnings)
                    .recommendations(recommendations)
                    .qualityScore(0.0)
                    .meetsMinimumLength(false)
                    .hasProperStructure(false)
                    .hasRelevantKeywords(false)
                    .build();
        }
        String plainText = extractPlainText(content, format);
        int contentLength = plainText.length();
        int wordCount = countWords(plainText);
        int sentenceCount = countSentences(plainText);
        int paragraphCount = countParagraphs(plainText);
        boolean meetsMinimumLength = contentLength >= MINIMUM_CONTENT_LENGTH;
        if (!meetsMinimumLength) {
            errors.add("Content is too short (minimum " + MINIMUM_CONTENT_LENGTH + " characters required)");
        }
        if (wordCount < 50) {
            warnings.add("Content word count is low (recommended: 150-300 words)");
        }
        boolean hasProperStructure = paragraphCount >= 1 && sentenceCount >= 3;
        if (!hasProperStructure) {
            warnings.add("Content lacks proper structure (paragraphs and sentences)");
        }
        String[] keywords = extractNewsKeywords(null, content, format, 5);
        boolean hasRelevantKeywords = keywords.length >= 3;
        if (!hasRelevantKeywords) {
            recommendations.add("Consider adding more specific keywords for better SEO");
        }
        double qualityScore = calculateQualityScore(content, format, wordCount, sentenceCount, paragraphCount);
        if (wordCount > RECOMMENDED_CONTENT_LENGTH * 2) {
            warnings.add("Content is very long - consider breaking into multiple parts");
        }
        if (sentenceCount > 0) {
            double avgSentenceLength = (double) wordCount / sentenceCount;
            if (avgSentenceLength > 25) {
                recommendations.add("Some sentences are very long - consider breaking them up for better readability");
            } else if (avgSentenceLength < 10) {
                recommendations.add("Some sentences are very short - consider combining related ideas");
            }
        }
        boolean isValid = errors.isEmpty();
        return NewsContentValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .warnings(warnings)
                .recommendations(recommendations)
                .qualityScore(qualityScore)
                .meetsMinimumLength(meetsMinimumLength)
                .hasProperStructure(hasProperStructure)
                .hasRelevantKeywords(hasRelevantKeywords)
                .build();
    }

    // =========================
    // 6. Metrics
    // =========================
    @Override
    public NewsReadingMetrics calculateReadingMetrics(String content, ContentFormat format) {
        if (content == null || content.trim().isEmpty()) {
            return NewsReadingMetrics.builder()
                    .estimatedReadingTimeMinutes(0)
                    .slowReaderTimeMinutes(0)
                    .fastReaderTimeMinutes(0)
                    .wordsPerMinute(DEFAULT_WORDS_PER_MINUTE)
                    .wordCount(0)
                    .difficultyLevel("Unknown")
                    .suitableForSkimReading(false)
                    .contentDensityScore(0.0)
                    .averageSentenceLength(0.0)
                    .complexWordsPercentage(0.0)
                    .build();
        }
        String plainText = extractPlainText(content, format);
        int wordCount = countWords(plainText);
        int sentenceCount = countSentences(plainText);
        int estimatedTime = (int) Math.max(1, Math.ceil((double) wordCount / DEFAULT_WORDS_PER_MINUTE));
        int slowReaderTime = (int) Math.max(1, Math.ceil((double) wordCount / SLOW_READER_WPM));
        int fastReaderTime = (int) Math.max(1, Math.ceil((double) wordCount / FAST_READER_WPM));
        String difficultyLevel = "Intermediate";
        double avgSentenceLength = sentenceCount > 0 ? (double) wordCount / sentenceCount : 0.0;
        double complexWordsPercentage = calculateComplexWordsPercentage(plainText);
        if (avgSentenceLength < 12 && complexWordsPercentage < 10) {
            difficultyLevel = "Beginner";
        } else if (avgSentenceLength > 20 || complexWordsPercentage > 20) {
            difficultyLevel = "Advanced";
        }
        double contentDensityScore = Math.min(100.0, (complexWordsPercentage * 2) + (avgSentenceLength * 1.5));
        boolean suitableForSkimReading = avgSentenceLength < 18 && contentDensityScore < 60;
        return NewsReadingMetrics.builder()
                .estimatedReadingTimeMinutes(estimatedTime)
                .slowReaderTimeMinutes(slowReaderTime)
                .fastReaderTimeMinutes(fastReaderTime)
                .wordsPerMinute(DEFAULT_WORDS_PER_MINUTE)
                .wordCount(wordCount)
                .difficultyLevel(difficultyLevel)
                .suitableForSkimReading(suitableForSkimReading)
                .contentDensityScore(contentDensityScore)
                .averageSentenceLength(avgSentenceLength)
                .complexWordsPercentage(complexWordsPercentage)
                .build();
    }

    // =========================
    // 7. Utility Methods
    // =========================
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

    private String extractAtSentenceBoundary(String text, int maxLength) {
        String[] sentences = SENTENCE_PATTERN.split(text);
        StringBuilder excerpt = new StringBuilder();
        for (String sentence : sentences) {
            if (excerpt.length() + sentence.length() + 1 <= maxLength) {
                if (excerpt.length() > 0) {
                    excerpt.append(". ");
                }
                excerpt.append(sentence.trim());
            } else {
                break;
            }
        }
        return excerpt.toString();
    }

    private String extractAtWordBoundary(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        String excerpt = text.substring(0, maxLength);
        int lastSpace = excerpt.lastIndexOf(' ');
        if (lastSpace > 0) {
            excerpt = excerpt.substring(0, lastSpace);
        }
        return excerpt;
    }

    private String ensureHtmlStructure(String html) {
        if (html == null) {
            return "";
        }
        if (!html.contains("<p>") && !html.contains("<br")) {
            return convertPlainTextToHtml(html);
        }
        return html;
    }

    private String addWebOptimizations(String content) {
        return content.replaceAll("\n\s*\n\s*\n", "\n\n");
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return WORD_PATTERN.matcher(text).results().toList().size();
    }

    private int countSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return Math.max(1, SENTENCE_PATTERN.split(text).length);
    }

    private int countParagraphs(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] paragraphs = PARAGRAPH_PATTERN.split(text);
        return Math.max(1, paragraphs.length);
    }

    private double calculateQualityScore(String content, ContentFormat format, int wordCount, int sentenceCount, int paragraphCount) {
        double score = 50.0;
        if (wordCount >= 150 && wordCount <= 800) {
            score += 20.0;
        } else if (wordCount >= 50 && wordCount <= 1500) {
            score += 10.0;
        }
        if (paragraphCount >= 3 && sentenceCount >= 5) {
            score += 15.0;
        } else if (paragraphCount >= 1 && sentenceCount >= 3) {
            score += 7.5;
        }
        String[] keywords = extractNewsKeywords(null, content, format, 3);
        if (keywords.length >= 3) {
            score += 15.0;
        }
        return Math.min(100.0, Math.max(0.0, score));
    }

    private double calculateComplexWordsPercentage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        String[] words = WORD_PATTERN.matcher(text).results()
                .map(match -> match.group().toLowerCase())
                .toArray(String[]::new);
        if (words.length == 0) {
            return 0.0;
        }
        int complexWordCount = 0;
        for (String word : words) {
            if (countSyllables(word) >= 3) {
                complexWordCount++;
            }
        }
        return (double) complexWordCount / words.length * 100.0;
    }

    private int countSyllables(String word) {
        if (word == null || word.trim().isEmpty()) {
            return 0;
        }
        word = word.toLowerCase().replaceAll("[^a-z]", "");
        if (word.length() == 0) {
            return 0;
        }
        int syllables = 0;
        boolean previousWasVowel = false;
        for (char c : word.toCharArray()) {
            boolean isVowel = "aeiouy".indexOf(c) != -1;
            if (isVowel && !previousWasVowel) {
                syllables++;
            }
            previousWasVowel = isVowel;
        }
        if (word.endsWith("e") && syllables > 1) {
            syllables--;
        }
        return Math.max(1, syllables);
    }

    // =========================
    // 8. Constants & Patterns
    // =========================
    private static final int DEFAULT_WORDS_PER_MINUTE = 200;
    private static final int SLOW_READER_WPM = 150;
    private static final int FAST_READER_WPM = 300;
    private static final int MINIMUM_CONTENT_LENGTH = 100;
    private static final int RECOMMENDED_CONTENT_LENGTH = 500;
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+\\s*");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");
}