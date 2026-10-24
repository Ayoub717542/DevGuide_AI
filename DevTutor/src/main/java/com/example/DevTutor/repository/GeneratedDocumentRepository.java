package com.example.DevTutor.repository;

import com.example.DevTutor.entity.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GeneratedDocumentRepository
        extends JpaRepository<GeneratedDocument, Long> {

    Optional<GeneratedDocument> findByIdAndConversationCreatedByEmail(Long id, String email);

    List<GeneratedDocument> findByConversationIdAndConversationCreatedByEmailOrderByCreatedAtAsc(
            Long conversationId, String email);

    // Documents the AI created during the current reply.
    List<GeneratedDocument> findByConversationIdAndCreatedAtGreaterThanEqual(
            Long conversationId, LocalDateTime since);

    void deleteByConversationId(Long conversationId);
}
