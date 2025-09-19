package com.spoilmate.service;

import com.spoilmate.dto.UpdatePasswordRequest;
import com.spoilmate.dto.UpdateSettingsRequest;
import com.spoilmate.dto.UpdateUserRequest;
import com.spoilmate.model.User;
import com.spoilmate.model.UserSettings;
import com.spoilmate.repository.UserRepository;
import com.spoilmate.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {
    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User updateUserInfo(UpdateUserRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserSettings updateSettings(UpdateSettingsRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSettings settings = settingsRepository.findByUserId(user.getId())
                .orElse(UserSettings.builder()
                        .user(user)
                        .notificationsEnabled(true)
                        .reminderDays(7)
                        .build());

        settings.setNotificationsEnabled(request.getNotificationsEnabled());
        settings.setReminderDays(request.getReminderDays());

        return settingsRepository.save(settings);
    }

    public UserSettings getUserSettings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return settingsRepository.findByUserId(user.getId())
                .orElse(UserSettings.builder()
                        .user(user)
                        .notificationsEnabled(true)
                        .reminderDays(7)
                        .build());
    }
} 