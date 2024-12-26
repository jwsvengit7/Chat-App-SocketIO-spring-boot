package com.isds.messenging_system.serviceImpl;

import com.isds.messenging_system.domain.entity.UserChat;
import com.isds.messenging_system.domain.enums.Status;
import com.isds.messenging_system.domain.repository.UserRepository;
import com.isds.messenging_system.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final UserRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final ConcurrentHashMap<String, String> onlineUsers = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate,UserService userService,UserRepository repository) {
        this.messagingTemplate = messagingTemplate;
        this.userService=userService;
        this.repository=repository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String userId = extractUserId(event);
        onlineUsers.put(userId, "ONLINE");
        messagingTemplate.convertAndSend("/topic/user-status", new UserDto(userId, "", Status.ONLINE));

    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String userId = extractUserId(event);
        onlineUsers.remove(userId);

        UserChat user =userService.findByUserId(userId);
        if(Objects.nonNull(user)) {
            user.setStatus(Status.OFFLINE);

            repository.save(user);
        }
        logger.info("User disconnected: " + userId);

        messagingTemplate.convertAndSend("/topic/user-status", new UserDto(userId, user.getName(), Status.OFFLINE));
    }

    public Map<String, String> getOnlineUsers() {
        return Collections.unmodifiableMap(onlineUsers);
    }

    private String extractUserId(AbstractSubProtocolEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        logger.info("headerAccessor {}",headerAccessor);
        logger.info("userId {}",headerAccessor.getSessionAttributes().get("userId") );
        return headerAccessor.getSessionAttributes() != null 
               ? (String) headerAccessor.getSessionAttributes().get("userId") 
               : "UnknownUser";
    }
}
