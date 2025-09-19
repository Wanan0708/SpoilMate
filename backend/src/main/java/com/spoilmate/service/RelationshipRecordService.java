package com.spoilmate.service;

import com.spoilmate.dto.RelationshipRecordRequest;
import com.spoilmate.model.RelationshipRecord;
import com.spoilmate.model.User;
import com.spoilmate.repository.RelationshipRecordRepository;
import com.spoilmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelationshipRecordService {
    private final RelationshipRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Transactional
    public RelationshipRecord createRecord(RelationshipRecordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var record = RelationshipRecord.builder()
                .date(request.getDate())
                .type(request.getType())
                .content(request.getContent())
                .mood(request.getMood())
                .photos(request.getPhotos())
                .user(user)
                .build();

        return recordRepository.save(record);
    }

    public List<RelationshipRecord> getUserRecords() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return recordRepository.findByUserIdOrderByDateDesc(user.getId());
    }

    @Transactional
    public RelationshipRecord updateRecord(Long recordId, RelationshipRecordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RelationshipRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if (!record.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to record");
        }

        record.setDate(request.getDate());
        record.setType(request.getType());
        record.setContent(request.getContent());
        record.setMood(request.getMood());
        record.setPhotos(request.getPhotos());

        return recordRepository.save(record);
    }

    @Transactional
    public void deleteRecord(Long recordId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RelationshipRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if (!record.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to record");
        }

        recordRepository.delete(record);
    }
} 