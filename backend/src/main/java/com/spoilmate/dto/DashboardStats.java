package com.spoilmate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {
    private RelationshipStats relationshipStats;
    private NextEvent nextEvent;
    private List<TimelineEvent> recentEvents;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RelationshipStats {
        private int totalDays;
        private int recordsCount;
        private String startDate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NextEvent {
        private String type;
        private String content;
        private String date;
        private int daysUntil;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimelineEvent {
        private String type;
        private String content;
        private String date;
        private String timeAgo;
    }
} 