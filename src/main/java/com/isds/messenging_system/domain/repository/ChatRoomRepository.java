package com.isds.messenging_system.domain.repository;


import com.isds.messenging_system.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT i FROM ChatRoom i WHERE (i.recieverId = :recieverId AND i.senderId = :senderId) OR (i.senderId = :recieverId AND i.recieverId = :senderId)")
    Optional<ChatRoom> findBySenderIdAndRecieverId(
            @Param("senderId") String senderId,
            @Param("recieverId") String recieverId
    );


}