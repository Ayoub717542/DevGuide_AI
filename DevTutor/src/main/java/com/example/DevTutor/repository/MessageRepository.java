package com.example.DevTutor.repository;

import com.example.DevTutor.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findByConversationIdOrderByCreatedAtAscIdAsc(
            Long conversationId
    );
}
