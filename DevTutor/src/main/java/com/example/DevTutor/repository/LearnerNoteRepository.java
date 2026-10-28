package com.example.DevTutor.repository;

import com.example.DevTutor.entity.LearnerNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearnerNoteRepository
        extends JpaRepository<LearnerNote, Long> {

    List<LearnerNote> findTop10ByUserEmailOrderByCreatedAtDesc(String userEmail);
}
