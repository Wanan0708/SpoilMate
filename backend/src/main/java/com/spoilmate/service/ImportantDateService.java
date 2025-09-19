package com.spoilmate.service;

import com.spoilmate.dto.ImportantDateRequest;
import com.spoilmate.model.ImportantDate;
import com.spoilmate.model.User;
import com.spoilmate.repository.ImportantDateRepository;
import com.spoilmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportantDateService {
    private final ImportantDateRepository dateRepository;
    private final UserRepository userRepository;

    @Transactional
    public ImportantDate createDate(ImportantDateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var importantDate = ImportantDate.builder()
                .date(request.getDate())
                .content(request.getContent())
                .type(request.getType())
                .recurring(request.getRecurring())
                .user(user)
                .build();

        return dateRepository.save(importantDate);
    }

    public List<ImportantDate> getUserDates() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return dateRepository.findByUserIdOrderByDateAsc(user.getId());
    }

    @Transactional
    public ImportantDate updateDate(Long dateId, ImportantDateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ImportantDate date = dateRepository.findById(dateId)
                .orElseThrow(() -> new RuntimeException("Date not found"));

        if (!date.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to date");
        }

        date.setDate(request.getDate());
        date.setContent(request.getContent());
        date.setType(request.getType());
        date.setRecurring(request.getRecurring());

        return dateRepository.save(date);
    }

    @Transactional
    public void deleteDate(Long dateId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ImportantDate date = dateRepository.findById(dateId)
                .orElseThrow(() -> new RuntimeException("Date not found"));

        if (!date.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to date");
        }

        dateRepository.delete(date);
    }
} 