package com.spoilmate.controller;

import com.spoilmate.model.Relationship;
import com.spoilmate.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relationship")
@RequiredArgsConstructor
public class RelationshipController {
    private final RelationshipService relationshipService;

    @PostMapping("/invite")
    public ResponseEntity<Relationship> createInvitation() {
        return ResponseEntity.ok(relationshipService.createInvitation());
    }

    @PostMapping("/accept/{invitationCode}")
    public ResponseEntity<Relationship> acceptInvitation(@PathVariable String invitationCode) {
        return ResponseEntity.ok(relationshipService.acceptInvitation(invitationCode));
    }

    @PostMapping("/end")
    public ResponseEntity<Void> endRelationship() {
        relationshipService.endRelationship();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current")
    public ResponseEntity<Relationship> getCurrentRelationship() {
        Relationship relationship = relationshipService.getCurrentRelationship();
        return relationship != null ? ResponseEntity.ok(relationship) : ResponseEntity.notFound().build();
    }
} 