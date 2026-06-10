package com.mmva.newsapp.domain.news.service.validation;

import com.mmva.newsapp.domain.news.dto.core.NewsCreateRequestDto;
import com.mmva.newsapp.domain.news.dto.validation.NewsValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.ContentValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.GeographicValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.WorkflowValidationResult;
import com.mmva.newsapp.domain.news.dto.validation.DuplicateDetectionResult;
import com.mmva.newsapp.domain.news.dto.seo.SeoValidationResult;
import com.mmva.newsapp.infrastructure.common.content.service.ContentQualityService;
import com.mmva.newsapp.infrastructure.common.geographic.service.GeographicValidationService;
import com.mmva.newsapp.infrastructure.common.content.service.SeoOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of NewsValidationService for comprehensive news validation.
 *
 * <p>
 * Provides domain-specific validation for news articles including business
 * rules,
 * content quality checks, geographic validation, and workflow enforcement.
 * </p>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NewsValidationServiceImpl implements NewsValidationService {

    private final ContentQualityService contentQualityService;
    private final GeographicValidationService geographicValidationService;
    private final SeoOptimizationService seoOptimizationService;

    private static final int MIN_TITLE_LENGTH = 10;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MIN_CONTENT_LENGTH = 100;
    private static final int MAX_CONTENT_LENGTH = 50000;
    private static final List<String> VALID_URGENCY_LEVELS = Arrays.asList("LOW", "MEDIUM", "HIGH", "BREAKING");

    @Override
    public NewsValidationResult validateNewsCreation(NewsCreateRequestDto request) {
        log.debug("Validating news creation request for title: {}", request.getNewsTitleEn());

        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();
        List<String> allSuggestions = new ArrayList<>();

        // Basic field validation
        validateBasicFields(request, allErrors, allWarnings);

        // Geographic validation
        GeographicValidationResult geoResult = validateGeographicData(
                request.getNewsLatitude(), request.getNewsLongitude(),
                request.getNewsCity(), request.getNewsCountryCode(), request.getNewsRegion());
        if (!geoResult.isValid()) {
            allErrors.addAll(geoResult.getErrors());
        }
        allWarnings.addAll(geoResult.getWarnings());

        // Content validation
        ContentValidationResult contentResult = validateNewsContent(
                request.getNewsTitleEn(), request.getNewsContentEn(), request.getNewsExcerptEn());
        if (!contentResult.isValid()) {
            allErrors.addAll(contentResult.getErrors());
        }
        allWarnings.addAll(contentResult.getWarnings());
        allSuggestions.addAll(contentResult.getSuggestions());

        // Workflow validation - commented out as fields not in DTO
        // WorkflowValidationResult workflowResult = validatePublishingWorkflow(
        // request.getPublishDateTime(), request.getUrgencyLevel(), request.getIsDraft()
        // );
        WorkflowValidationResult workflowResult = WorkflowValidationResult.builder().isValid(true)
                .errors(new ArrayList<>()).warnings(new ArrayList<>()).build();
        if (!workflowResult.isValid()) {
            allErrors.addAll(workflowResult.getErrors());
        }
        allWarnings.addAll(workflowResult.getWarnings());

        // SEO validation
        SeoValidationResult seoResult = validateSeoMetadata(
                request.getNewsTitleEn(), request.getNewsMetaDescription(),
                request.getNewsKeywords() != null ? request.getNewsKeywords().split(",") : new String[0],
                null // slug not in DTO
        );
        if (!seoResult.isValid()) {
            allErrors.addAll(seoResult.getErrors());
        }
        allWarnings.addAll(seoResult.getWarnings());
        allSuggestions.addAll(seoResult.getSuggestions());

        // Duplicate detection - commented out as method not implemented
        // DuplicateDetectionResult duplicateResult = null;

        boolean isValid = allErrors.isEmpty();
        double overallScore = calculateOverallScore(contentResult, seoResult, geoResult, workflowResult);

        return NewsValidationResult.builder()
                .isValid(isValid)
                .errors(allErrors)
                .warnings(allWarnings)
                .suggestions(allSuggestions)
                .geographicValidation(geoResult)
                .contentValidation(contentResult)
                .workflowValidation(workflowResult)
                // .duplicateDetection(duplicateResult)
                .seoValidation(seoResult)
                .overallScore(overallScore)
                .build();
    }

    @Override
    public NewsValidationResult validateNewsUpdate(NewsCreateRequestDto request) {
        log.debug("Validating news update request for title: {}", request.getNewsTitleEn());

        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();
        List<String> allSuggestions = new ArrayList<>();

        // Basic field validation (imageVideoFile is OPTIONAL for updates)
        validateBasicFieldsForUpdate(request, allErrors, allWarnings);

        // Geographic validation
        GeographicValidationResult geoResult = validateGeographicData(
                request.getNewsLatitude(), request.getNewsLongitude(),
                request.getNewsCity(), request.getNewsCountryCode(), request.getNewsRegion());
        if (!geoResult.isValid()) {
            allErrors.addAll(geoResult.getErrors());
        }
        allWarnings.addAll(geoResult.getWarnings());

        // Content validation
        ContentValidationResult contentResult = validateNewsContent(
                request.getNewsTitleEn(), request.getNewsContentEn(), request.getNewsExcerptEn());
        if (!contentResult.isValid()) {
            allErrors.addAll(contentResult.getErrors());
        }
        allWarnings.addAll(contentResult.getWarnings());
        allSuggestions.addAll(contentResult.getSuggestions());

        // Workflow validation
        WorkflowValidationResult workflowResult = WorkflowValidationResult.builder().isValid(true)
                .errors(new ArrayList<>()).warnings(new ArrayList<>()).build();
        if (!workflowResult.isValid()) {
            allErrors.addAll(workflowResult.getErrors());
        }
        allWarnings.addAll(workflowResult.getWarnings());

        // SEO validation
        SeoValidationResult seoResult = validateSeoMetadata(
                request.getNewsTitleEn(), request.getNewsMetaDescription(),
                request.getNewsKeywords() != null ? request.getNewsKeywords().split(",") : new String[0],
                null
        );
        if (!seoResult.isValid()) {
            allErrors.addAll(seoResult.getErrors());
        }
        allWarnings.addAll(seoResult.getWarnings());
        allSuggestions.addAll(seoResult.getSuggestions());

        boolean isValid = allErrors.isEmpty();
        double overallScore = calculateOverallScore(contentResult, seoResult, geoResult, workflowResult);

        return NewsValidationResult.builder()
                .isValid(isValid)
                .errors(allErrors)
                .warnings(allWarnings)
                .suggestions(allSuggestions)
                .geographicValidation(geoResult)
                .contentValidation(contentResult)
                .workflowValidation(workflowResult)
                .seoValidation(seoResult)
                .overallScore(overallScore)
                .build();
    }

    @Override
    public GeographicValidationResult validateGeographicData(Double latitude, Double longitude,
            String locationName, String country, String state) {
        log.debug("Validating geographic data: location={}, country={}", locationName, country);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String normalizedLocation = geographicValidationService.validateLocationName(locationName);
        String normalizedCountry = geographicValidationService.validateCountry(country);
        String normalizedState = null;

        if (state != null && !state.trim().isEmpty()) {
            normalizedState = geographicValidationService.validateState(state, country);
        }

        // Coordinate validation
        boolean coordinatesValid = true;
        if (latitude != null && longitude != null) {
            coordinatesValid = geographicValidationService.validateCoordinates(latitude, longitude);
            if (!coordinatesValid) {
                errors.add("Invalid geographic coordinates");
            }
        } else if ((latitude == null && longitude != null) || (latitude != null && longitude == null)) {
            errors.add("Both latitude and longitude must be provided together");
            coordinatesValid = false;
        }

        // Country validation
        if (country != null && !country.trim().isEmpty() && normalizedCountry == null) {
            errors.add("Invalid or unrecognized country name");
        }

        // State validation
        if (state != null && !state.trim().isEmpty() && normalizedState == null) {
            warnings.add("State/province name could not be validated");
        }

        // Coordinate-country consistency check
        boolean coordinatesWithinCountry = true;
        if (coordinatesValid && latitude != null && longitude != null && normalizedCountry != null) {
            coordinatesWithinCountry = geographicValidationService.isWithinCountry(
                    latitude, longitude, normalizedCountry);
            if (!coordinatesWithinCountry) {
                warnings.add("Coordinates do not appear to be within the specified country");
            }
        }

        boolean isValid = errors.isEmpty();
        double confidenceScore = calculateGeoConfidenceScore(coordinatesValid, normalizedCountry != null,
                normalizedState != null, coordinatesWithinCountry);

        return GeographicValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .warnings(warnings)
                .normalizedLocationName(normalizedLocation)
                .normalizedCountry(normalizedCountry)
                .normalizedState(normalizedState)
                .coordinatesWithinCountry(coordinatesWithinCountry)
                .confidenceScore(confidenceScore)
                .build();
    }

    @Override
    public ContentValidationResult validateNewsContent(String title, String content, String excerpt) {
        log.debug("Validating news content for title: {}", title);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // Title validation
        if (title == null || title.trim().isEmpty()) {
            errors.add("Title is required");
        } else if (title.length() < MIN_TITLE_LENGTH) {
            errors.add("Title is too short (minimum " + MIN_TITLE_LENGTH + " characters)");
        } else if (title.length() > MAX_TITLE_LENGTH) {
            errors.add("Title is too long (maximum " + MAX_TITLE_LENGTH + " characters)");
        }

        // Content validation
        if (content == null || content.trim().isEmpty()) {
            errors.add("Content is required");
        } else if (content.length() < MIN_CONTENT_LENGTH) {
            errors.add("Content is too short (minimum " + MIN_CONTENT_LENGTH + " characters)");
        } else if (content.length() > MAX_CONTENT_LENGTH) {
            errors.add("Content is too long (maximum " + MAX_CONTENT_LENGTH + " characters)");
        }

        // Excerpt validation (if provided)
        if (excerpt != null && !excerpt.trim().isEmpty()) {
            if (excerpt.length() > 300) {
                warnings.add("Excerpt is quite long (recommended max 300 characters)");
            }
            if (!content.contains(excerpt.substring(0, Math.min(50, excerpt.length())))) {
                warnings.add("Excerpt does not appear to be from the content");
            }
        }

        // Content quality analysis
        double qualityScore = 0.0;
        double readabilityScore = 0.0;
        boolean meetsMinimumLength = content != null && content.length() >= MIN_CONTENT_LENGTH;
        boolean hasProperStructure = false;
        boolean hasRelevantKeywords = false;

        if (content != null && !content.trim().isEmpty()) {
            // Use content quality service for detailed analysis
            var qualityMetrics = contentQualityService.analyzeContentQuality(content,
                    com.mmva.newsapp.domain.news.enums.core.ContentFormat.PLAIN_TEXT);

            qualityScore = qualityMetrics.getContentQualityScore();
            readabilityScore = qualityMetrics.getReadabilityScore();

            hasProperStructure = qualityMetrics.getContentMetrics().getParagraphCount() >= 1;
            hasRelevantKeywords = qualityMetrics.getContentMetrics().getWordCount() >= 50; // Basic check

            if (qualityScore < 40) {
                suggestions.add("Consider improving content quality - aim for better structure and clarity");
            }

            if (readabilityScore < 50) {
                suggestions.add("Content may be difficult to read - consider simplifying language");
            }
        }

        boolean isValid = errors.isEmpty();

        return ContentValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .warnings(warnings)
                .suggestions(suggestions)
                .qualityScore(qualityScore)
                .meetsMinimumLength(meetsMinimumLength)
                .hasProperStructure(hasProperStructure)
                .hasRelevantKeywords(hasRelevantKeywords)
                .readabilityScore(readabilityScore)
                .detectedLanguage("en") // Simplified - would need language detection service
                .build();
    }

    @Override
    public WorkflowValidationResult validatePublishingWorkflow(LocalDateTime publishDateTime,
            String urgencyLevel, boolean isDraft) {
        log.debug("Validating publishing workflow: urgency={}, draft={}", urgencyLevel, isDraft);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        boolean isScheduledForFuture = false;
        LocalDateTime recommendedPublishDateTime = null;

        // Publish date/time validation
        if (publishDateTime != null) {
            if (publishDateTime.isBefore(now.minusMinutes(5))) { // Allow 5-minute grace period
                errors.add("Publish date/time cannot be in the past");
                recommendedPublishDateTime = now.plusMinutes(10); // Suggest publishing in 10 minutes
            } else if (publishDateTime.isAfter(now.plusMonths(6))) {
                warnings.add("Publish date is quite far in the future (more than 6 months)");
            } else {
                isScheduledForFuture = publishDateTime.isAfter(now);
            }
        } else if (!isDraft) {
            errors.add("Publish date/time is required for non-draft articles");
        }

        // Urgency level validation
        boolean urgencyLevelAppropriate = true;
        String suggestedUrgencyLevel = null;

        if (urgencyLevel != null && !urgencyLevel.trim().isEmpty()) {
            if (!VALID_URGENCY_LEVELS.contains(urgencyLevel.toUpperCase())) {
                errors.add("Invalid urgency level. Valid values: " + String.join(", ", VALID_URGENCY_LEVELS));
                urgencyLevelAppropriate = false;
            }
        } else if (!isDraft) {
            warnings.add("Urgency level is recommended for published articles");
            suggestedUrgencyLevel = "MEDIUM";
        }

        // Draft status validation
        boolean draftStatusAppropriate = true;
        if (isDraft && publishDateTime != null && publishDateTime.isBefore(now.plusHours(1))) {
            warnings.add("Draft articles are typically scheduled for publishing later");
        }

        boolean isValid = errors.isEmpty();

        return WorkflowValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .warnings(warnings)
                .recommendedPublishDateTime(recommendedPublishDateTime)
                .isScheduledForFuture(isScheduledForFuture)
                .urgencyLevelAppropriate(urgencyLevelAppropriate)
                .suggestedUrgencyLevel(suggestedUrgencyLevel)
                .draftStatusAppropriate(draftStatusAppropriate)
                .build();
    }

    @Override
    public DuplicateDetectionResult checkForDuplicates(String title, String content, Long sourceAgencyId) {
        log.debug("Checking for duplicate content, title: {}", title);

        // Simplified duplicate detection - in a real implementation, this would:
        // 1. Query the database for similar titles
        // 2. Use text similarity algorithms
        // 3. Check content hashes
        // 4. Compare with recent articles from same source

        List<Long> duplicateIds = new ArrayList<>();
        List<String> duplicateTitles = new ArrayList<>();
        List<Double> similarityScores = new ArrayList<>();

        // Mock duplicate detection logic
        boolean hasDuplicates = false;
        boolean isClearDuplicate = false;
        String recommendedAction = "none";
        double confidenceScore = 0.0;

        // This would be replaced with actual database queries and similarity algorithms
        // For now, return no duplicates found

        return DuplicateDetectionResult.builder()
                .hasDuplicates(hasDuplicates)
                .duplicateArticleIds(duplicateIds)
                .duplicateTitles(duplicateTitles)
                .similarityScores(similarityScores)
                .isClearDuplicate(isClearDuplicate)
                .recommendedAction(recommendedAction)
                .confidenceScore(confidenceScore)
                .build();
    }

    @Override
    public SeoValidationResult validateSeoMetadata(String title, String metaDescription,
            String[] keywords, String slug) {
        log.debug("Validating SEO metadata for title: {}", title);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // Title validation
        boolean titleOptimized = true;
        String suggestedTitle = null;

        if (title == null || title.trim().isEmpty()) {
            errors.add("Title is required for SEO");
            titleOptimized = false;
        } else if (title.length() < 30) {
            warnings.add("Title is shorter than recommended (30-60 characters optimal)");
            titleOptimized = false;
        } else if (title.length() > 60) {
            warnings.add("Title is longer than recommended (30-60 characters optimal)");
            titleOptimized = false;
            suggestedTitle = seoOptimizationService.optimizeTitle(title);
        }

        // Meta description validation
        boolean metaDescriptionOptimal = true;
        String suggestedMetaDescription = null;

        if (metaDescription == null || metaDescription.trim().isEmpty()) {
            warnings.add("Meta description is recommended for better SEO");
            metaDescriptionOptimal = false;
            if (title != null) {
                suggestedMetaDescription = seoOptimizationService.generateMetaDescription(title, "", null);
            }
        } else if (metaDescription.length() < 120) {
            warnings.add("Meta description is shorter than recommended (120-160 characters optimal)");
            metaDescriptionOptimal = false;
        } else if (metaDescription.length() > 160) {
            warnings.add("Meta description is longer than recommended (120-160 characters optimal)");
            metaDescriptionOptimal = false;
        }

        // Keywords validation
        boolean keywordsPresent = true;
        if (keywords == null || keywords.length == 0) {
            warnings.add("Keywords are recommended for better SEO");
            keywordsPresent = false;
        } else if (keywords.length < 3) {
            suggestions.add("Consider adding more relevant keywords (3-5 recommended)");
        }

        // Slug validation
        boolean slugOptimized = true;
        if (slug == null || slug.trim().isEmpty()) {
            warnings.add("URL slug is recommended for SEO-friendly URLs");
            slugOptimized = false;
        } else if (slug.length() > 100) {
            warnings.add("Slug is quite long - consider shortening for better UX");
            slugOptimized = false;
        } else if (!slug.matches("[a-z0-9-]+")) {
            errors.add("Slug contains invalid characters (only lowercase letters, numbers, and hyphens allowed)");
            slugOptimized = false;
        }

        // Overall SEO score calculation
        double seoScore = seoOptimizationService.calculateSeoScore(title, "", null);

        boolean isValid = errors.isEmpty();

        return SeoValidationResult.builder()
                .isValid(isValid)
                .errors(errors)
                .warnings(warnings)
                .suggestions(suggestions)
                .seoScore(seoScore)
                .titleOptimized(titleOptimized)
                .metaDescriptionOptimal(metaDescriptionOptimal)
                .keywordsPresent(keywordsPresent)
                .slugOptimized(slugOptimized)
                .suggestedTitle(suggestedTitle)
                .suggestedMetaDescription(suggestedMetaDescription)
                .build();
    }

    private void validateBasicFields(NewsCreateRequestDto request, List<String> errors, List<String> warnings) {
        // Required field validation
        if (request.getNewsTitleEn() == null || request.getNewsTitleEn().trim().isEmpty()) {
            errors.add("English title is required");
        }

        if (request.getNewsContentEn() == null || request.getNewsContentEn().trim().isEmpty()) {
            errors.add("English content is required");
        }

        if (request.getNewsNewsCategoryId() == null) {
            errors.add("Category is required");
        }

        // Media file validation - required for CREATE operations
        // (UPDATE operations may skip media if user doesn't want to change the file)
        if (request.getImageVideoFile() == null || request.getImageVideoFile().isEmpty()) {
            errors.add("imageVideoFile is required");
        }

        // Source agency is optional for MVP testing - no FK constraint
        // Any UUID or null is accepted

        // Optional field warnings
        if (request.getNewsTitleEs() == null || request.getNewsTitleEs().trim().isEmpty()) {
            warnings.add("Spanish title is recommended for bilingual support");
        }

        if (request.getNewsContentEs() == null || request.getNewsContentEs().trim().isEmpty()) {
            warnings.add("Spanish content is recommended for bilingual support");
        }
    }

    private void validateBasicFieldsForUpdate(NewsCreateRequestDto request, List<String> errors, List<String> warnings) {
        // Required field validation (same as create, but imageVideoFile is OPTIONAL)
        if (request.getNewsTitleEn() == null || request.getNewsTitleEn().trim().isEmpty()) {
            errors.add("English title is required");
        }

        if (request.getNewsContentEn() == null || request.getNewsContentEn().trim().isEmpty()) {
            errors.add("English content is required");
        }

        if (request.getNewsNewsCategoryId() == null) {
            errors.add("Category is required");
        }

        // Media file is OPTIONAL for UPDATE - user can keep existing file
        // (No validation required here)

        // Optional field warnings
        if (request.getNewsTitleEs() == null || request.getNewsTitleEs().trim().isEmpty()) {
            warnings.add("Spanish title is recommended for bilingual support");
        }

        if (request.getNewsContentEs() == null || request.getNewsContentEs().trim().isEmpty()) {
            warnings.add("Spanish content is recommended for bilingual support");
        }
    }

    private double calculateOverallScore(ContentValidationResult content, SeoValidationResult seo,
            GeographicValidationResult geo, WorkflowValidationResult workflow) {
        double score = 0.0;
        int factors = 0;

        if (content != null) {
            score += content.getQualityScore() * 0.4; // 40% weight
            factors++;
        }

        if (seo != null) {
            score += seo.getSeoScore() * 0.3; // 30% weight
            factors++;
        }

        if (geo != null) {
            score += geo.getConfidenceScore() * 0.2; // 20% weight
            factors++;
        }

        if (workflow != null && workflow.isValid()) {
            score += 10.0; // Bonus for valid workflow
        }

        return factors > 0 ? Math.min(100.0, score) : 0.0;
    }

    private double calculateGeoConfidenceScore(boolean coordinatesValid, boolean countryValid,
            boolean stateValid, boolean coordinatesWithinCountry) {
        double score = 0.0;

        if (coordinatesValid)
            score += 40.0;
        if (countryValid)
            score += 30.0;
        if (stateValid)
            score += 20.0;
        if (coordinatesWithinCountry)
            score += 10.0;

        return score;
    }
}