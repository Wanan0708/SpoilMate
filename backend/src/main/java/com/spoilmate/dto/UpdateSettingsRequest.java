package com.spoilmate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSettingsRequest {
    @NotNull(message = "通知开关不能为空")
    private Boolean notificationsEnabled;

    @NotNull(message = "提醒天数不能为空")
    @Min(value = 1, message = "提醒天数最小为1天")
    @Max(value = 30, message = "提醒天数最大为30天")
    private Integer reminderDays;
} 