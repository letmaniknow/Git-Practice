package com.mmva.newsapp.infrastructure.common.util;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.util.Optional;

public class FileContentTypeUtil {
    /**
     * Determines the MediaType for a given Resource (file), falling back to
     * APPLICATION_OCTET_STREAM if unknown.
     * 
     * @param resource the file resource
     * @return the MediaType
     */
    public static MediaType getMediaType(Resource resource) {
        if (resource == null)
            return MediaType.APPLICATION_OCTET_STREAM;
        Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(resource);
        return mediaType.orElse(MediaType.APPLICATION_OCTET_STREAM);
    }
}
