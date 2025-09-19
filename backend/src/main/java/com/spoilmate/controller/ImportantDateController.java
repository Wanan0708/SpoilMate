package com.spoilmate.controller;

import com.spoilmate.dto.ImportantDateRequest;
import com.spoilmate.model.ImportantDate;
import com.spoilmate.service.ImportantDateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dates")
@RequiredArgsConstructor
public class ImportantDateController {
    private final ImportantDateService dateService;

    @PostMapping
    public ResponseEntity<ImportantDate> createDate(@Valid @RequestBody ImportantDateRequest request) {
        return ResponseEntity.ok(dateService.createDate(request));
    }

    @GetMapping
    public ResponseEntity<List<ImportantDate>> getUserDates() {
        return ResponseEntity.ok(dateService.getUserDates());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImportantDate> updateDate(
            @PathVariable Long id,
            @Valid @RequestBody ImportantDateRequest request) {
        return ResponseEntity.ok(dateService.updateDate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDate(@PathVariable Long id) {
        dateService.deleteDate(id);
        return ResponseEntity.ok().build();
    }
} 