package com.pocs.jdbcchatmemoryrepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    @Query("SELECT c FROM ChatMessageEntity c WHERE c.conversationId = :conversationId ORDER BY c.createdAt ASC, c.id ASC")
    List<ChatMessageEntity> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") String conversationId);

    @Transactional
    void deleteByConversationId(String conversationId);
}
