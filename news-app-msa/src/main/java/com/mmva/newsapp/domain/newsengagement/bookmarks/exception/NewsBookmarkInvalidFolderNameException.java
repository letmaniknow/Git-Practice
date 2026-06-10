package com.mmva.newsapp.domain.newsengagement.bookmarks.exception;

/**
 * Exception thrown when a bookmark folder name is invalid.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsBookmarkInvalidFolderNameException extends RuntimeException {

    public NewsBookmarkInvalidFolderNameException(String message) {
        super(message);
    }
}