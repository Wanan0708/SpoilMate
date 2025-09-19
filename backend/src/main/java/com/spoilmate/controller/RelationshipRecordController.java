package com.spoilmate.controller;

import com.spoilmate.dto.RelationshipRecordRequest;
import com.spoilmate.model.RelationshipRecord;
import com.spoilmate.service.RelationshipRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RelationshipRecordController {
    private final RelationshipRecordService recordService;

    @PostMapping
    public ResponseEntity<RelationshipRecord> createRecord(@Valid @RequestBody RelationshipRecordRequest request) {
        return ResponseEntity.ok(recordService.createRecord(request));
    }

    @GetMapping
    public ResponseEntity<List<RelationshipRecord>> getUserRecords() {
        return ResponseEntity.ok(recordService.getUserRecords());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RelationshipRecord> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RelationshipRecordRequest request) {
        return ResponseEntity.ok(recordService.updateRecord(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }
} 