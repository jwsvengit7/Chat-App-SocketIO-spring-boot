package com.isds.messenging_system.domain.entity;

import com.isds.messenging_system.domain.enums.MessageStatus;
import com.isds.messenging_system.domain.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String chatId;
    private String senderId;
    private String senderName;
    private String recieverId;
    private String content;
    private Date timestamp;
    

}
