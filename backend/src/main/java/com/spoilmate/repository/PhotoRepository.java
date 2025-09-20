package com.spoilmate.repository;

import com.spoilmate.model.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByAlbumIdOrderByCreatedAtDesc(Long albumId);
    
    Page<Photo> findByAlbumIdOrderByCreatedAtDesc(Long albumId, Pageable pageable);
    
    List<Photo> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Photo p WHERE p.album.id = :albumId")
    void deleteByAlbumId(@Param("albumId") Long albumId);
}