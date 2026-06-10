/**
 * Push notification feature exceptions.
 * 
 * <p>
 * Contains feature-specific exceptions for the push notification module.
 * Per PROJECT_PRINCIPLES.md §5.3: Feature packages should contain their
 * own exception folder.
 * </p>
 * 
 * <h2>Exceptions:</h2>
 * <ul>
 * <li>{@link com.mmva.newsapp.infrastructure.push.exception.PushDeviceNotFoundException}
 * - Device not found by ID or token</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.exception.PushNotificationNotFoundException}
 * - Notification not found by ID</li>
 * <li>{@link com.mmva.newsapp.infrastructure.push.exception.PushTopicSubscriptionNotFoundException}
 * - Topic subscription not found</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
package com.mmva.newsapp.infrastructure.push.exception;
