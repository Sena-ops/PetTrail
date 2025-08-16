package com.example.pettrail.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class WebController {

    @GetMapping(value = {"/", "/web", "/web/"})
    public ResponseEntity<String> serveIndex() {
        return serveFile("web/index.html");
    }

    @GetMapping("/web/{filename}")
    public ResponseEntity<String> serveWebFile(@PathVariable String filename) {
        return serveFile("web/" + filename);
    }

    @GetMapping("/web/{subdir}/{filename}")
    public ResponseEntity<String> serveWebSubdirFile(@PathVariable String subdir, @PathVariable String filename) {
        return serveFile("web/" + subdir + "/" + filename);
    }

    private ResponseEntity<String> serveFile(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            // Determine content type based on file extension
            String contentType = determineContentType(path);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
                    
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error reading file: " + e.getMessage());
        }
    }

    private String determineContentType(String path) {
        if (path.endsWith(".html")) {
            return "text/html; charset=utf-8";
        } else if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        } else if (path.endsWith(".json")) {
            return "application/json; charset=utf-8";
        } else if (path.endsWith(".webmanifest")) {
            return "application/manifest+json; charset=utf-8";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (path.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            return "text/plain; charset=utf-8";
        }
    }
}
