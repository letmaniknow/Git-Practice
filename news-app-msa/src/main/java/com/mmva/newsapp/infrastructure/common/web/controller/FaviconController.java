package com.mmva.newsapp.infrastructure.common.web.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to serve favicon for the application.
 * Prevents 404/500 errors when browsers request /favicon.ico
 */
@RestController
public class FaviconController {

    @GetMapping(value = "/favicon.ico", produces = "image/svg+xml")
    public ResponseEntity<Resource> favicon() {
        Resource resource = new ClassPathResource("static/favicon.svg");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(resource);
    }
}
