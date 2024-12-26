package com.isds.messenging_system.controller;

import com.isds.messenging_system.domain.entity.ChatMessage;
import com.isds.messenging_system.dto.ChatNotification;
import com.isds.messenging_system.serviceImpl.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WSController {


  private final ChatMessageService chatMessageService;

  @MessageMapping("/chat")
  public ChatMessage processMessage(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
    return chatMessageService.chat(chatMessage,headerAccessor);
  }
  @MessageMapping("/type")
  public ChatNotification typing(ChatNotification chatMessage, SimpMessageHeaderAccessor headerAccessor) {

   return chatMessageService.typing(chatMessage,headerAccessor);

  }


}