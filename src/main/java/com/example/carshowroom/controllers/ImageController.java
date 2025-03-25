package com.example.carshowroom.controllers;

import com.example.carshowroom.service.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;

    public ImageController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String message = s3Service.uploadFile(file.getOriginalFilename(), file.getBytes());
            return ResponseEntity.ok(message);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }

    @GetMapping
    public ResponseEntity<S3Service.S3ListResponse> getImages(
            @RequestParam(required = false) Integer maxKeys,
            @RequestParam(required = false) String continuationToken) {
        return ResponseEntity.ok(s3Service.listFiles(maxKeys, continuationToken));
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteImage(@PathVariable String fileName) {
        boolean deleted = s3Service.deleteFile(fileName);
        if (deleted) {
            return ResponseEntity.ok("Image deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to delete image");
        }
    }
        
    
}
