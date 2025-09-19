package com.spoilmate.service;

import com.spoilmate.dto.PhotoAlbumRequest;
import com.spoilmate.model.PhotoAlbum;
import com.spoilmate.model.User;
import com.spoilmate.repository.PhotoAlbumRepository;
import com.spoilmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoAlbumService {
    private final PhotoAlbumRepository albumRepository;
    private final UserRepository userRepository;

    @Transactional
    public PhotoAlbum createAlbum(PhotoAlbumRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var album = PhotoAlbum.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .user(user)
                .build();

        return albumRepository.save(album);
    }

    public List<PhotoAlbum> getUserAlbums() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return albumRepository.findByUserId(user.getId());
    }
} 