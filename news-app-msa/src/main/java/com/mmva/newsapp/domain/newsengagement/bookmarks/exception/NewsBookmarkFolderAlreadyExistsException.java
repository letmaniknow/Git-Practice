package com.mmva.newsapp.domain.newsengagement.bookmarks.exception;

/**
 * Exception thrown when a bookmark folder already exists for a user.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsBookmarkFolderAlreadyExistsException extends RuntimeException {

    public NewsBookmarkFolderAlreadyExistsException(String message) {
        super(message);
    }

    public NewsBookmarkFolderAlreadyExistsException(String folderName, boolean isFolder) {
        super("Bookmark folder already exists: " + folderName);
    }
}
