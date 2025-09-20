package com.spoilmate.service;

import com.spoilmate.dto.DashboardStats;
import com.spoilmate.model.ImportantDate;
import com.spoilmate.model.Relationship;
import com.spoilmate.model.RelationshipRecord;
import com.spoilmate.model.RelationshipStatus;
import com.spoilmate.model.User;
import com.spoilmate.repository.ImportantDateRepository;
import com.spoilmate.repository.RelationshipRecordRepository;
import com.spoilmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final RelationshipRecordRepository recordRepository;
    private final ImportantDateRepository dateRepository;
    private final RelationshipService relationshipService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DashboardStats getDashboardStats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RelationshipRecord> records = recordRepository.findByUserIdOrderByDateDesc(user.getId());
        List<ImportantDate> dates = dateRepository.findByUserIdOrderByDateAsc(user.getId());
        
        // 获取当前关系信息
        Relationship relationship = relationshipService.getCurrentRelationship();

        return DashboardStats.builder()
                .relationshipStats(calculateRelationshipStats(records, relationship))
                .nextEvent(findNextEvent(dates))
                .recentEvents(getRecentEvents(records, dates))
                .build();
    }

    private DashboardStats.RelationshipStats calculateRelationshipStats(List<RelationshipRecord> records, Relationship relationship) {
        // 如果没有关系或关系不是活跃状态
        if (relationship == null || !relationship.getStatus().equals(RelationshipStatus.ACTIVE)) {
            return DashboardStats.RelationshipStats.builder()
                    .totalDays(0)
                    .recordsCount(records.size())
                    .startDate("尚未开始")
                    .build();
        }

        // 使用关系的startDate来计算恋爱天数
        LocalDate startDate;
        if (relationship.getStartDate() != null) {
            startDate = relationship.getStartDate().toLocalDate();
        } else {
            // 如果没有startDate，使用关系的创建日期
            startDate = relationship.getCreatedAt().toLocalDate();
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        // 确保天数不为负数，至少为1天
        totalDays = Math.max(1, totalDays);

        return DashboardStats.RelationshipStats.builder()
                .totalDays((int) totalDays)
                .recordsCount(records.size())
                .startDate(startDate.format(DATE_FORMATTER))
                .build();
    }

    private DashboardStats.NextEvent findNextEvent(List<ImportantDate> dates) {
        LocalDate today = LocalDate.now();
        
        ImportantDate nextDate = dates.stream()
                .filter(date -> {
                    LocalDate eventDate = LocalDate.parse(date.getDate());
                    return eventDate.isAfter(today) || (date.isRecurring() && 
                           eventDate.withYear(today.getYear()).isAfter(today));
                })
                .min((a, b) -> {
                    LocalDate dateA = LocalDate.parse(a.getDate());
                    LocalDate dateB = LocalDate.parse(b.getDate());
                    if (a.isRecurring()) {
                        dateA = dateA.withYear(today.getYear());
                    }
                    if (b.isRecurring()) {
                        dateB = dateB.withYear(today.getYear());
                    }
                    return dateA.compareTo(dateB);
                })
                .orElse(null);

        if (nextDate == null) {
            return DashboardStats.NextEvent.builder()
                    .type("none")
                    .content("暂无upcoming事件")
                    .date(today.format(DATE_FORMATTER))
                    .daysUntil(0)
                    .build();
        }

        LocalDate eventDate = LocalDate.parse(nextDate.getDate());
        if (nextDate.isRecurring()) {
            eventDate = eventDate.withYear(today.getYear());
            if (eventDate.isBefore(today)) {
                eventDate = eventDate.plusYears(1);
            }
        }

        long daysUntil = ChronoUnit.DAYS.between(today, eventDate);

        return DashboardStats.NextEvent.builder()
                .type(nextDate.getType())
                .content(nextDate.getContent())
                .date(eventDate.format(DATE_FORMATTER))
                .daysUntil((int) daysUntil)
                .build();
    }

    private List<DashboardStats.TimelineEvent> getRecentEvents(
            List<RelationshipRecord> records,
            List<ImportantDate> dates
    ) {
        List<DashboardStats.TimelineEvent> events = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 添加最近的记录
        records.stream()
                .limit(5)
                .forEach(record -> {
                    LocalDate recordDate = LocalDate.parse(record.getDate());
                    long daysAgo = ChronoUnit.DAYS.between(recordDate, LocalDate.now());
                    String timeAgo = formatTimeAgo(daysAgo);

                    events.add(DashboardStats.TimelineEvent.builder()
                            .type("record")
                            .content(record.getContent())
                            .date(record.getDate())
                            .timeAgo(timeAgo)
                            .build());
                });

        // 添加最近的重要日期
        dates.stream()
                .filter(date -> {
                    LocalDate eventDate = LocalDate.parse(date.getDate());
                    return !eventDate.isBefore(LocalDate.now().minusDays(7));
                })
                .limit(3)
                .forEach(date -> {
                    LocalDate dateDate = LocalDate.parse(date.getDate());
                    long daysAgo = ChronoUnit.DAYS.between(dateDate, LocalDate.now());
                    String timeAgo = formatTimeAgo(daysAgo);

                    events.add(DashboardStats.TimelineEvent.builder()
                            .type("date")
                            .content(date.getContent())
                            .date(date.getDate())
                            .timeAgo(timeAgo)
                            .build());
                });

        return events.stream()
                .sorted(Comparator.comparing(DashboardStats.TimelineEvent::getDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private String formatTimeAgo(long days) {
        if (days == 0) {
            return "今天";
        } else if (days == 1) {
            return "昨天";
        } else if (days == 2) {
            return "前天";
        } else if (days <= 7) {
            return days + "天前";
        } else {
            return (days / 7) + "周前";
        }
    }
} 