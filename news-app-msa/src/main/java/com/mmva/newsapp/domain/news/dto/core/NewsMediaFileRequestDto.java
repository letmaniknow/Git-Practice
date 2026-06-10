package com.mmva.newsapp.domain.news.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsMediaFileRequestDto {
    @Schema(description = "Resource representing the media file", example = "<binary data>")
    private Resource resource;

    @Schema(description = "MIME type of the media file", example = "image/jpeg")
    private String contentType;
}
