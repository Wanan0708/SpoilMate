package com.spoilmate.repository;

import com.spoilmate.model.ImportantDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportantDateRepository extends JpaRepository<ImportantDate, Long> {
    List<ImportantDate> findByUserIdOrderByDateAsc(Long userId);
} 