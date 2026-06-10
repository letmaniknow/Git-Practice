package com.mmva.newsapp.domain.newsengagement.bookmarks.exception;

/**
 * Exception thrown when a bookmark folder is not found for a user.
 * 
 * @author MMVA Team
 * @since 1.0.0
 */
public class NewsBookmarkFolderNotFoundException extends RuntimeException {

    public NewsBookmarkFolderNotFoundException(String message) {
        super(message);
    }

    public NewsBookmarkFolderNotFoundException(String folderName, boolean isFolder) {
        super("Bookmark folder not found: " + folderName);
    }
}
