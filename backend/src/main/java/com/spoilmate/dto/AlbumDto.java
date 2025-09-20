package com.spoilmate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDto {
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private String username;
    private Integer photoCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}