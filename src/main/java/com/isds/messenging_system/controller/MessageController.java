package com.isds.messenging_system.controller;

import com.isds.messenging_system.domain.entity.ChatMessage;
import com.isds.messenging_system.serviceImpl.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequiredArgsConstructor
public class MessageController {


    private final ChatMessageService chatMessageService;

    @CrossOrigin(origins = "${front.end.url_uat}")
    @GetMapping("/messages/{senderId}/{recieverId}")
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable String senderId,
                                                              @PathVariable String recieverId) {
        return ResponseEntity
                .ok(chatMessageService.findChatMessages(senderId, recieverId));
    }
}
