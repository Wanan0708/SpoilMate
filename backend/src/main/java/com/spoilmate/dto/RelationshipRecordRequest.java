package com.spoilmate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationshipRecordRequest {
    @NotBlank(message = "日期不能为空")
    private String date;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "心情不能为空")
    private String mood;

    private List<String> photos;
} 