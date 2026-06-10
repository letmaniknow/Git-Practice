package com.mmva.newsapp.infrastructure.push.enums;

/**
 * Enum representing the infrastructure/OS type of a push device.
 * 
 * <p>
 * Used to track device platforms for targeted notifications
 * and infrastructure-specific notification formatting.
 * </p>
 * 
 * <h3>Naming Convention:</h3>
 * <p>
 * Prefixed with {@code PushDevice} per PROJECT_PRINCIPLES.md §6.1
 * Feature-Contextual Naming to clearly indicate entity ownership.
 * </p>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum PushDevicePlatform {

    /**
     * Android devices using Firebase Cloud Messaging.
     */
    ANDROID,

    /**
     * iOS devices using APNs via Firebase Cloud Messaging.
     */
    IOS,

    /**
     * Web browsers using Web Push via Firebase Cloud Messaging.
     */
    WEB
}
