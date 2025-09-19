package com.spoilmate.controller;

import com.spoilmate.dto.PhotoAlbumRequest;
import com.spoilmate.model.PhotoAlbum;
import com.spoilmate.service.PhotoAlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class PhotoAlbumController {
    private final PhotoAlbumService albumService;

    @PostMapping
    public ResponseEntity<PhotoAlbum> createAlbum(@Valid @RequestBody PhotoAlbumRequest request) {
        return ResponseEntity.ok(albumService.createAlbum(request));
    }

    @GetMapping
    public ResponseEntity<List<PhotoAlbum>> getUserAlbums() {
        return ResponseEntity.ok(albumService.getUserAlbums());
    }
} 