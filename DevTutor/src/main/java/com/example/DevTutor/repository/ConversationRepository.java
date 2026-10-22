package com.example.DevTutor.repository;

import com.example.DevTutor.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation,Long> {

    Optional<Conversation> findByIdAndCreatedByEmail(Long id, String email);
}
