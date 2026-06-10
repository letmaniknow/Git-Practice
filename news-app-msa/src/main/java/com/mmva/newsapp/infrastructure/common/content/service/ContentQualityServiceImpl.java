package com.mmva.newsapp.infrastructure.common.content.service;

import com.mmva.newsapp.domain.news.enums.core.ContentFormat;
import com.mmva.newsapp.infrastructure.common.content.dto.ContentMetrics;
import com.mmva.newsapp.infrastructure.common.content.dto.ContentQualityMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Implementation of ContentQualityService for analyzing content quality
 * metrics.
 *
 * <p>
 * Provides comprehensive content analysis including readability calculations,
 * sentiment analysis, and structural metrics for news content quality
 * assessment.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class ContentQualityServiceImpl implements ContentQualityService {

    private static final int DEFAULT_WORDS_PER_MINUTE = 200;
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+\\s*");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\s*\\n");
    private static final Pattern VOWEL_PATTERN = Pattern.compile("[aeiouyAEIOUY]");

    @Override
    public ContentQualityMetrics analyzeContentQuality(String content, ContentFormat format) {
        log.debug("Analyzing content quality for format: {}", format);

        ContentMetrics metrics = calculateContentMetrics(content, format);
        double readabilityScore = calculateReadabilityScore(content, format);
        double sentimentScore = calculateSentimentScore(content, format);
        double qualityScore = calculateQualityScore(content, format);
        String readabilityLevel = determineReadabilityLevel(content, format);

        return ContentQualityMetrics.builder()
                .contentQualityScore(qualityScore)
                .readabilityScore(readabilityScore)
                .sentimentScore(sentimentScore)
                .readabilityLevel(readabilityLevel)
                .contentMetrics(metrics)
                .build();
    }

    @Override
    public double calculateReadabilityScore(String content, ContentFormat format) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }

        String plainText = extractPlainText(content, format);
        ContentMetrics metrics = calculateContentMetrics(plainText, ContentFormat.PLAIN_TEXT);

        if (metrics.getSentenceCount() == 0 || metrics.getWordCount() == 0) {
            return 0.0;
        }

        // Flesch-Kincaid Grade Level formula
        double avgWordsPerSentence = metrics.getAverageWordsPerSentence();
        double avgSyllablesPerWord = metrics.getAverageSyllablesPerWord();

        // FKGL = 0.39 * (words/sentences) + 11.8 * (syllables/words) - 15.59
        double fkgl = 0.39 * avgWordsPerSentence + 11.8 * avgSyllablesPerWord - 15.59;

        // Convert to a 0-100 scale (lower grade level = higher readability score)
        // Assuming grade levels 0-20, convert to 0-100 where 100 is easiest
        double readabilityScore = Math.max(0, Math.min(100, 100 - (fkgl * 5)));

        log.debug("Calculated readability score: {} for FKGL: {}", readabilityScore, fkgl);
        return readabilityScore;
    }

    @Override
    public double calculateSentimentScore(String content, ContentFormat format) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }

        String plainText = extractPlainText(content, format).toLowerCase();

        // Simple sentiment analysis based on positive/negative word counts
        String[] positiveWords = { "good", "great", "excellent", "amazing", "wonderful", "fantastic",
                "outstanding", "brilliant", "superb", "incredible", "perfect",
                "happy", "joy", "success", "win", "victory", "achieve", "progress" };

        String[] negativeWords = { "bad", "terrible", "awful", "horrible", "disastrous", "tragic",
                "worst", "fail", "failure", "disaster", "crisis", "problem",
                "sad", "unhappy", "angry", "frustrated", "worried", "fear" };

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            positiveCount += countOccurrences(plainText, word);
        }

        for (String word : negativeWords) {
            negativeCount += countOccurrences(plainText, word);
        }

        int totalSentimentWords = positiveCount + negativeCount;
        if (totalSentimentWords == 0) {
            return 0.0; // Neutral
        }

        // Calculate sentiment score between -1.0 and 1.0
        double sentimentScore = (double) (positiveCount - negativeCount) / totalSentimentWords;

        log.debug("Calculated sentiment score: {} (positive: {}, negative: {})",
                sentimentScore, positiveCount, negativeCount);
        return sentimentScore;
    }

    @Override
    public double calculateQualityScore(String content, ContentFormat format) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }

        double readabilityScore = calculateReadabilityScore(content, format);
        double sentimentScore = Math.abs(calculateSentimentScore(content, format)); // Absolute value for balance
        ContentMetrics metrics = calculateContentMetrics(content, format);

        // Quality factors (weighted)
        double readabilityWeight = 0.4;
        double sentimentWeight = 0.2;
        double lengthWeight = 0.2;
        double structureWeight = 0.2;

        // Length score (optimal word count around 300-800 words)
        double lengthScore = 0.0;
        if (metrics.getWordCount() >= 100 && metrics.getWordCount() <= 1000) {
            lengthScore = 100.0;
        } else if (metrics.getWordCount() >= 50 && metrics.getWordCount() <= 1500) {
            lengthScore = 80.0;
        } else {
            lengthScore = 50.0;
        }

        // Structure score based on sentence and paragraph distribution
        double structureScore = 0.0;
        if (metrics.getSentenceCount() > 0 && metrics.getParagraphCount() > 0) {
            double avgSentencesPerParagraph = (double) metrics.getSentenceCount() / metrics.getParagraphCount();
            if (avgSentencesPerParagraph >= 2 && avgSentencesPerParagraph <= 6) {
                structureScore = 100.0;
            } else if (avgSentencesPerParagraph >= 1 && avgSentencesPerParagraph <= 8) {
                structureScore = 80.0;
            } else {
                structureScore = 60.0;
            }
        }

        double qualityScore = (readabilityScore * readabilityWeight) +
                (sentimentScore * 100 * sentimentWeight) +
                (lengthScore * lengthWeight) +
                (structureScore * structureWeight);

        log.debug("Calculated quality score: {} (readability: {}, sentiment: {}, length: {}, structure: {})",
                qualityScore, readabilityScore, sentimentScore * 100, lengthScore, structureScore);
        return Math.min(100.0, Math.max(0.0, qualityScore));
    }

    @Override
    public String determineReadabilityLevel(String content, ContentFormat format) {
        double readabilityScore = calculateReadabilityScore(content, format);

        if (readabilityScore >= 80) {
            return "Elementary";
        } else if (readabilityScore >= 60) {
            return "Intermediate";
        } else if (readabilityScore >= 40) {
            return "Advanced";
        } else {
            return "Expert";
        }
    }

    @Override
    public ContentMetrics calculateContentMetrics(String content, ContentFormat format) {
        if (content == null || content.trim().isEmpty()) {
            return ContentMetrics.builder()
                    .contentLength(0)
                    .wordCount(0)
                    .sentenceCount(0)
                    .paragraphCount(0)
                    .averageWordsPerSentence(0.0)
                    .averageSyllablesPerWord(0.0)
                    .estimatedReadingTimeMinutes(0)
                    .build();
        }

        String plainText = extractPlainText(content, format);

        int contentLength = plainText.length();
        int wordCount = countWords(plainText);
        int sentenceCount = countSentences(plainText);
        int paragraphCount = countParagraphs(plainText);
        double avgWordsPerSentence = sentenceCount > 0 ? (double) wordCount / sentenceCount : 0.0;
        double avgSyllablesPerWord = wordCount > 0 ? countTotalSyllables(plainText) / (double) wordCount : 0.0;
        int readingTime = wordCount > 0 ? (int) Math.ceil(wordCount / (double) DEFAULT_WORDS_PER_MINUTE) : 0;

        return ContentMetrics.builder()
                .contentLength(contentLength)
                .wordCount(wordCount)
                .sentenceCount(sentenceCount)
                .paragraphCount(paragraphCount)
                .averageWordsPerSentence(avgWordsPerSentence)
                .averageSyllablesPerWord(avgSyllablesPerWord)
                .estimatedReadingTimeMinutes(readingTime)
                .build();
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
                // Simple HTML tag removal for plain text extraction
                return content.replaceAll("<[^>]+>", "").trim();
            case MARKDOWN:
                // Basic markdown to plain text conversion
                return content.replaceAll("[*#_`~]", "").trim();
            case PLAIN_TEXT:
            default:
                return content.trim();
        }
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
        String[] sentences = SENTENCE_PATTERN.split(text);
        return (int) sentences.length;
    }

    private int countParagraphs(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] paragraphs = PARAGRAPH_PATTERN.split(text);
        return Math.max(1, paragraphs.length); // At least one paragraph if there's content
    }

    private int countTotalSyllables(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] words = text.split("\\s+");
        int totalSyllables = 0;

        for (String word : words) {
            totalSyllables += countSyllablesInWord(word.toLowerCase());
        }

        return totalSyllables;
    }

    private int countSyllablesInWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return 0;
        }

        // Remove non-alphabetic characters
        word = word.replaceAll("[^a-z]", "");

        if (word.length() == 0) {
            return 0;
        }

        // Count vowel groups
        int syllables = 0;
        boolean previousWasVowel = false;

        for (char c : word.toCharArray()) {
            boolean isVowel = VOWEL_PATTERN.matcher(String.valueOf(c)).matches();
            if (isVowel && !previousWasVowel) {
                syllables++;
            }
            previousWasVowel = isVowel;
        }

        // Handle silent 'e'
        if (word.endsWith("e") && syllables > 1) {
            syllables--;
        }

        return Math.max(1, syllables); // At least one syllable per word
    }

    private int countOccurrences(String text, String word) {
        if (text == null || word == null || word.trim().isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
}