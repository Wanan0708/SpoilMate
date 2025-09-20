package com.spoilmate.controller;

import com.spoilmate.dto.AlbumDto;
import com.spoilmate.dto.PhotoDto;
import com.spoilmate.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import com.spoilmate.model.Photo;
import com.spoilmate.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;
    private final PhotoRepository photoRepository;
    
    @PostMapping
    public ResponseEntity<AlbumDto> createAlbum(@RequestParam String name, 
                                               @RequestParam(required = false) String description) {
        AlbumDto album = albumService.createAlbum(name, description);
        return ResponseEntity.ok(album);
    }
    
    @GetMapping
    public ResponseEntity<List<AlbumDto>> getUserAlbums() {
        List<AlbumDto> albums = albumService.getUserAlbums();
        return ResponseEntity.ok(albums);
    }
    
    @PostMapping("/{albumId}/photos")
    public ResponseEntity<PhotoDto> uploadPhoto(@PathVariable Long albumId,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(required = false) String description) throws IOException {
        // 添加调试日志
        System.out.println("=== Upload Photo Request ===");
        System.out.println("Album ID: " + albumId);
        System.out.println("File Name: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("File Size: " + (file != null ? file.getSize() : "null"));
        System.out.println("Description: " + description);
        
        if (file == null) {
            System.out.println("Error: File is null");
            throw new RuntimeException("File is required");
        }
        
        PhotoDto photo = albumService.uploadPhoto(albumId, file, description);
        return ResponseEntity.ok(photo);
    }
    
    @GetMapping("/{albumId}/photos")
    public ResponseEntity<Page<PhotoDto>> getAlbumPhotos(@PathVariable Long albumId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        Page<PhotoDto> photos = albumService.getAlbumPhotos(albumId, page, size);
        return ResponseEntity.ok(photos);
    }
    
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) throws IOException {
        albumService.deletePhoto(photoId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId) throws IOException {
        albumService.deleteAlbum(albumId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/photos/{photoId}/download")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Long photoId) throws IOException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        
        Path filePath = Paths.get(photo.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(photo.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "inline; filename=\"" + photo.getOriginalFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}