package com.isds.messenging_system.domain.repository;

import com.isds.messenging_system.domain.entity.ChatMessage;
import com.isds.messenging_system.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT i FROM ChatMessage i WHERE (i.chatId = :chat) OR (i.chatId = :chat2)")
    List<ChatMessage> findByChatId(
            @Param("chat") String chat,
            @Param("chat2") String chat2
    );
}
