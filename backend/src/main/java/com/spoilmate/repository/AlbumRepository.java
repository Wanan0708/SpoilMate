package com.spoilmate.repository;

import com.spoilmate.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(p) FROM Photo p WHERE p.album.id = :albumId")
    Integer countPhotosByAlbumId(@Param("albumId") Long albumId);
}