package com.mmva.newsapp.infrastructure.clientcontext.core.enums;

/**
 * Access channel classification for client context.
 * 
 * <p>
 * Used to identify how the client is accessing the application:
 * </p>
 * <ul>
 * <li>WEB - Desktop web browser</li>
 * <li>MOBILE_WEB - Mobile web browser</li>
 * <li>IOS_APP - Native iOS application</li>
 * <li>ANDROID_APP - Native Android application</li>
 * <li>API - Direct API access (third-party integrations)</li>
 * <li>SDK - SDK-based access</li>
 * <li>UNKNOWN - Unable to determine access channel</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum Channel {
    WEB,
    MOBILE_WEB,
    IOS_APP,
    ANDROID_APP,
    API,
    SDK,
    UNKNOWN
}
