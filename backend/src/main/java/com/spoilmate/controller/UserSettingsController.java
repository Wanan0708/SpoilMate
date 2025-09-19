package com.spoilmate.controller;

import com.spoilmate.dto.UpdatePasswordRequest;
import com.spoilmate.dto.UpdateSettingsRequest;
import com.spoilmate.dto.UpdateUserRequest;
import com.spoilmate.model.User;
import com.spoilmate.model.UserSettings;
import com.spoilmate.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class UserSettingsController {
    private final UserSettingsService settingsService;

    @GetMapping
    public ResponseEntity<UserSettings> getUserSettings() {
        return ResponseEntity.ok(settingsService.getUserSettings());
    }

    @PutMapping("/user")
    public ResponseEntity<User> updateUserInfo(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(settingsService.updateUserInfo(request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        settingsService.updatePassword(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserSettings> updateSettings(@Valid @RequestBody UpdateSettingsRequest request) {
        return ResponseEntity.ok(settingsService.updateSettings(request));
    }
} 