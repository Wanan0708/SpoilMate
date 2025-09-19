package com.spoilmate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhotoAlbumRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;
} 