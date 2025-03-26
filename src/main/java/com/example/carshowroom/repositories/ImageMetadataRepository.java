package com.example.carshowroom.repositories;

import com.example.carshowroom.model.ImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, Long> {
    Page<ImageMetadata> findAll(Pageable pageable);
}
