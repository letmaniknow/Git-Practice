package com.mmva.newsapp.domain.news.enums.core;

/**
 * Enum representing the urgency level of news content.
 *
 * <p>
 * Defines the priority and handling requirements for different types of news.
 * Used for editorial workflow, push notifications, and content presentation.
 * </p>
 *
 * <h3>Urgency Levels:</h3>
 * <ul>
 * <li>{@code LOW} - Standard news, no special handling</li>
 * <li>{@code NORMAL} - Regular news with normal priority</li>
 * <li>{@code HIGH} - Important news requiring attention</li>
 * <li>{@code BREAKING} - Critical breaking news requiring immediate action</li>
 * </ul>
 *
 * <h3>System Impact:</h3>
 * <ul>
 * <li><strong>Editorial:</strong> Workflow prioritization and deadlines</li>
 * <li><strong>Distribution:</strong> Push notification triggers and
 * frequency</li>
 * <li><strong>Presentation:</strong> UI styling and placement (badges,
 * alerts)</li>
 * <li><strong>Analytics:</strong> Content performance tracking by urgency</li>
 * </ul>
 *
 * @author MMVA Team
 * @since 1.0.0
 */
public enum UrgencyLevel {

    /**
     * Low priority news.
     * Standard content that doesn't require special attention.
     * Default level for most articles.
     */
    LOW("Low priority news", 1, false),

    /**
     * Normal priority news.
     * Regular news content with standard handling.
     * Most published articles fall into this category.
     */
    NORMAL("Normal priority news", 2, false),

    /**
     * High priority news.
     * Important content requiring editorial attention.
     * May trigger special notifications or placement.
     */
    HIGH("High priority news", 3, true),

    /**
     * Breaking news.
     * Critical news requiring immediate attention and action.
     * Triggers breaking news workflows and special presentation.
     */
    BREAKING("Breaking news - immediate action required", 4, true);

    private final String description;
    private final int priority;
    private final boolean requiresImmediateAction;

    UrgencyLevel(String description, int priority, boolean requiresImmediateAction) {
        this.description = description;
        this.priority = priority;
        this.requiresImmediateAction = requiresImmediateAction;
    }

    /**
     * Gets the human-readable description of the urgency level.
     *
     * @return description of the urgency level
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the numeric priority value for sorting and comparison.
     * Higher values indicate higher priority.
     *
     * @return priority value (1-4)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Checks if this urgency level requires immediate editorial action.
     *
     * @return true if immediate action is required
     */
    public boolean requiresImmediateAction() {
        return requiresImmediateAction;
    }

    /**
     * Checks if this urgency level should trigger special notifications.
     *
     * @return true if notifications should be triggered
     */
    public boolean shouldTriggerNotifications() {
        return this == HIGH || this == BREAKING;
    }

    /**
     * Checks if this urgency level should trigger breaking news workflows.
     *
     * @return true if breaking news workflows should be activated
     */
    public boolean isBreakingNews() {
        return this == BREAKING;
    }

    /**
     * Gets the default urgency level for new content.
     *
     * @return default urgency level
     */
    public static UrgencyLevel getDefault() {
        return NORMAL;
    }

    /**
     * Gets all urgency level names as a list for validation.
     *
     * @return list of urgency level names
     */
    public static String[] getAllNames() {
        return new String[] { "LOW", "NORMAL", "HIGH", "BREAKING" };
    }
}