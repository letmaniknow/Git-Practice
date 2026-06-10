package com.mmva.newsapp.infrastructure.clientcontext.core.enums;

/**
 * Device type classification for client context.
 * 
 * <p>
 * Used to categorize the device type making a request:
 * </p>
 * <ul>
 * <li>MOBILE - Smartphones</li>
 * <li>TABLET - Tablet devices (iPad, Android tablets)</li>
 * <li>DESKTOP - Desktop/laptop computers</li>
 * <li>TV - Smart TVs and streaming devices</li>
 * <li>WEARABLE - Smartwatches and wearable devices</li>
 * <li>CONSOLE - Gaming consoles</li>
 * <li>BOT - Automated bots and crawlers</li>
 * <li>UNKNOWN - Unable to determine device type</li>
 * </ul>
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public enum DeviceType {
    MOBILE,
    TABLET,
    DESKTOP,
    TV,
    WEARABLE,
    CONSOLE,
    BOT,
    UNKNOWN
}
