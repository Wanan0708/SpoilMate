package com.spoilmate.repository;

import com.spoilmate.model.RelationshipRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelationshipRecordRepository extends JpaRepository<RelationshipRecord, Long> {
    List<RelationshipRecord> findByUserIdOrderByDateDesc(Long userId);
} 