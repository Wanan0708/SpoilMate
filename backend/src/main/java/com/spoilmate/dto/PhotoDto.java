package com.spoilmate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    private Long id;
    private String filename;
    private String originalFilename;
    private String url;
    private Long fileSize;
    private String contentType;
    private String description;
    private Long albumId;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}