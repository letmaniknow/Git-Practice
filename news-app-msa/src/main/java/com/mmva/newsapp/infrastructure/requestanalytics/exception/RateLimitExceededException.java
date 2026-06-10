package com.mmva.newsapp.infrastructure.requestanalytics.exception;

import lombok.Getter;

/**
 * Exception thrown when a rate limit is exceeded.
 */
@Getter
public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(long retryAfterSeconds) {
        super("Too many requests. Please try again in " + retryAfterSeconds + " seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
