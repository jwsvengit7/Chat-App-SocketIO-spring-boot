package com.isds.messenging_system.controller;

import com.isds.messenging_system.domain.entity.UserChat;
import com.isds.messenging_system.dto.UserDto;
import com.isds.messenging_system.serviceImpl.UserService;
import com.isds.messenging_system.serviceImpl.WebSocketEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final WebSocketEventListener webSocketEventListener;

    @GetMapping("/online")
    public ResponseEntity<List<String>> getOnlineUsers() {
        return ResponseEntity.ok(new ArrayList<>(webSocketEventListener.getOnlineUsers().keySet()));
    }

    @MessageMapping("/addUser")
    @SendToUser("/queue/users")
    public UserChat addUser(
            @Payload UserDto user
    ) {
      return  userService.addUser(user);
    }
    @MessageMapping("/disconnectUser")
    @SendToUser("/queue/users")
    public UserDto disconnectUser(
            @Payload UserDto user
    ) {
        userService.disconnect(user);
        return user;
    }
    @GetMapping("/users")
    public ResponseEntity<List<UserChat>> findConnectedUsers() {
        return ResponseEntity.ok(userService.findConnectedUsers());
    }
}