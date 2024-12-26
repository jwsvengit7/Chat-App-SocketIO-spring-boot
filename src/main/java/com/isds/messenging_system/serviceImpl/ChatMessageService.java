package com.isds.messenging_system.serviceImpl;

import com.isds.messenging_system.domain.entity.ChatMessage;
import com.isds.messenging_system.domain.repository.ChatMessageRepository;
import com.isds.messenging_system.dto.ChatNotification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatMessageService.class);

    public ChatMessage chat(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor){
        ChatMessage savedMsg = save(chatMessage);
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        headerAccessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));

        LOGGER.info("getUser {} ", userId);
        LOGGER.info("HeaderAccessor User: {}", headerAccessor.getUser());
        LOGGER.info("HeaderAccessor Destination: {}", headerAccessor.getDestination());
        LOGGER.info("HeaderAccessor SessionId: {}", headerAccessor.getSessionId());
        if (userId != null) {
            String destination = "/user/"+userId+"/queue/messages";
            LOGGER.info("Sending message to destination: {}", destination);
            messagingTemplate.convertAndSend(destination, chatMessage);
        } else {
            LOGGER.error("User ID is null or not found in session");
        }
        new ChatNotification(
                savedMsg.getSenderId(),
                savedMsg.getRecieverId()
        );
        return savedMsg;

    }

    public ChatNotification typing(ChatNotification chatMessage, SimpMessageHeaderAccessor headerAccessor){
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        headerAccessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));

            String destination = "/user/"+userId+"/queue/typing";
            messagingTemplate.convertAndSend(destination, chatMessage);

           return chatMessage;
    }
    public ChatMessage save(ChatMessage chatMessage) {
        var chatId = chatRoomService
                .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecieverId(), true)
                .orElseThrow();
        chatMessage.setChatId(chatId);
        repository.save(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> findChatMessages(String senderId, String receiverId) {
        String chat = String.format("%s_%s", senderId, receiverId);
        String chat2 = String.format("%s_%s", receiverId,senderId);
        LOGGER.info("chat {} ",chat);
        LOGGER.info("chat2 {} ",chat2);
        List<ChatMessage> result= repository.findByChatId(chat, chat2);
        LOGGER.info("result {} ",result);
        LOGGER.info("size {} ",result.size());
        return result;
    }

}