package com.example.carshowroom.controllers;

import com.example.carshowroom.model.ImageMetadata;
import com.example.carshowroom.repositories.ImageMetadataRepository;
import com.example.carshowroom.service.S3Service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;
    private final ImageMetadataRepository imageMetadataRepository;

    public ImageController(S3Service s3Service, ImageMetadataRepository imageMetadataRepository) {
        this.s3Service = s3Service;
        this.imageMetadataRepository = imageMetadataRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("description") String description) {
        try {
            // Upload to S3 and get URL
            String imageUrl = s3Service.uploadFile(file.getOriginalFilename(), file.getBytes());

            // Save image metadata in RDS
            ImageMetadata imageMetadata = new ImageMetadata(imageUrl, description);
            imageMetadataRepository.save(imageMetadata);

            return ResponseEntity.ok("Image uploaded successfully and saved to database.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image.");
        }
    }

    /* @GetMapping
    public ResponseEntity<S3Service.S3ListResponse> getImages(
            @RequestParam(required = false) Integer maxKeys,
            @RequestParam(required = false) String continuationToken) {
        return ResponseEntity.ok(s3Service.listFiles(maxKeys, continuationToken));
    } */

    @GetMapping
    public ResponseEntity<Page<ImageMetadata>> getImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ImageMetadata> imagesPage = imageMetadataRepository.findAll(pageable);
        
        return ResponseEntity.ok(imagesPage);
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
