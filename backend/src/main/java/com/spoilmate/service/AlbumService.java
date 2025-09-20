package com.spoilmate.service;

import com.spoilmate.dto.AlbumDto;
import com.spoilmate.dto.PhotoDto;
import com.spoilmate.model.Album;
import com.spoilmate.model.Photo;
import com.spoilmate.model.User;
import com.spoilmate.repository.AlbumRepository;
import com.spoilmate.repository.PhotoRepository;
import com.spoilmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    
    // 照片存储路径
    private static final String PHOTO_STORAGE_PATH = "uploads/photos/";
    
    @Transactional
    public AlbumDto createAlbum(String name, String description) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Album album = Album.builder()
                .name(name)
                .description(description)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Album savedAlbum = albumRepository.save(album);
        
        return AlbumDto.builder()
                .id(savedAlbum.getId())
                .name(savedAlbum.getName())
                .description(savedAlbum.getDescription())
                .userId(savedAlbum.getUser().getId())
                .username(savedAlbum.getUser().getUsername())
                .photoCount(0)
                .createdAt(savedAlbum.getCreatedAt())
                .updatedAt(savedAlbum.getUpdatedAt())
                .build();
    }
    
    public List<AlbumDto> getUserAlbums() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Album> albums = albumRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        return albums.stream().map(album -> 
            AlbumDto.builder()
                .id(album.getId())
                .name(album.getName())
                .description(album.getDescription())
                .userId(album.getUser().getId())
                .username(album.getUser().getUsername())
                .photoCount(albumRepository.countPhotosByAlbumId(album.getId()))
                .createdAt(album.getCreatedAt())
                .updatedAt(album.getUpdatedAt())
                .build()
        ).collect(Collectors.toList());
    }
    
    @Transactional
    public PhotoDto uploadPhoto(Long albumId, MultipartFile file, String description) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with ID: " + albumId));
    
        // 添加调试日志
        System.out.println("Debug - User ID: " + user.getId() + ", Username: " + user.getUsername());
        System.out.println("Debug - Album ID: " + album.getId());
        System.out.println("Debug - Album User ID: " + album.getUser().getId() + ", Album Username: " + album.getUser().getUsername());
    
        // 确保用户有权访问此相册
        if (!album.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: User " + user.getId() + " does not have permission to access album " + albumId + 
                                 ". Album belongs to user " + album.getUser().getId());
        }
        
        // 创建存储目录
        Path storagePath = Paths.get(PHOTO_STORAGE_PATH);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件
        Path filePath = storagePath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 创建照片记录
        Photo photo = Photo.builder()
                .filename(filename)
                .originalFilename(originalFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .description(description)
                .album(album)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Photo savedPhoto = photoRepository.save(photo);
        
        // 更新照片的URL字段
        savedPhoto.setUrl("/api/albums/photos/" + savedPhoto.getId() + "/download");
        savedPhoto = photoRepository.save(savedPhoto);
        
        return PhotoDto.builder()
                .id(savedPhoto.getId())
                .filename(savedPhoto.getFilename())
                .originalFilename(savedPhoto.getOriginalFilename())
                .url("/api/albums/photos/" + savedPhoto.getId() + "/download")
                .fileSize(savedPhoto.getFileSize())
                .contentType(savedPhoto.getContentType())
                .description(savedPhoto.getDescription())
                .albumId(savedPhoto.getAlbum().getId())
                .userId(savedPhoto.getUser().getId())
                .createdAt(savedPhoto.getCreatedAt())
                .updatedAt(savedPhoto.getUpdatedAt())
                .build();
    }
    
    public Page<PhotoDto> getAlbumPhotos(Long albumId, int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with ID: " + albumId));
    
        // 添加调试日志
        System.out.println("Debug - User ID: " + user.getId() + ", Username: " + user.getUsername());
        System.out.println("Debug - Album ID: " + album.getId());
        System.out.println("Debug - Album User ID: " + album.getUser().getId() + ", Album Username: " + album.getUser().getUsername());
    
        // 确保用户有权访问此相册
        if (!album.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: User " + user.getId() + " does not have permission to access album " + albumId + 
                                 ". Album belongs to user " + album.getUser().getId());
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photos = photoRepository.findByAlbumIdOrderByCreatedAtDesc(albumId, pageable);
        
        return photos.map(photo -> 
            PhotoDto.builder()
                .id(photo.getId())
                .filename(photo.getFilename())
                .originalFilename(photo.getOriginalFilename())
                .url("/api/albums/photos/" + photo.getId() + "/download")
                .fileSize(photo.getFileSize())
                .contentType(photo.getContentType())
                .description(photo.getDescription())
                .albumId(photo.getAlbum().getId())
                .userId(photo.getUser().getId())
                .createdAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .build()
        );
    }
    
    @Transactional
    public void deletePhoto(Long photoId) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));
    
        // 添加调试日志
        System.out.println("Debug - User ID: " + user.getId() + ", Username: " + user.getUsername());
        System.out.println("Debug - Photo ID: " + photo.getId());
        System.out.println("Debug - Photo User ID: " + photo.getUser().getId() + ", Photo Username: " + photo.getUser().getUsername());
    
        // 确保用户有权删除此照片
        if (!photo.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: User " + user.getId() + " does not have permission to delete photo " + photoId + 
                                 ". Photo belongs to user " + photo.getUser().getId());
        }
        
        // 删除文件
        Path filePath = Paths.get(photo.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // 删除数据库记录
        photoRepository.deleteById(photoId);
    }
    
    @Transactional
    public void deleteAlbum(Long albumId) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Album not found with ID: " + albumId));
    
        // 添加调试日志
        System.out.println("Debug - User ID: " + user.getId() + ", Username: " + user.getUsername());
        System.out.println("Debug - Album ID: " + album.getId());
        System.out.println("Debug - Album User ID: " + album.getUser().getId() + ", Album Username: " + album.getUser().getUsername());
    
        // 确保用户有权删除此相册
        if (!album.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: User " + user.getId() + " does not have permission to delete album " + albumId + 
                                 ". Album belongs to user " + album.getUser().getId());
        }
        
        // 删除相册中的所有照片
        List<Photo> photos = photoRepository.findByAlbumIdOrderByCreatedAtDesc(albumId);
        for (Photo photo : photos) {
            Path filePath = Paths.get(photo.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
        
        // 删除照片记录
        photoRepository.deleteByAlbumId(albumId);
        
        // 删除相册
        albumRepository.deleteById(albumId);
    }
}