package com.isds.messenging_system.serviceImpl;

import com.isds.messenging_system.domain.entity.ChatRoom;
import com.isds.messenging_system.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public Optional<String> getChatRoomId(
            String senderId,
            String orderId,
            boolean createNewRoomIfNotExists
    ) {
        return chatRoomRepository
                .findBySenderIdAndRecieverId(senderId, orderId)
                .map(ChatRoom::getChatId)
                .or(() -> {
                    if(createNewRoomIfNotExists) {
                        var chatId = createChatId(senderId, orderId);
                        return Optional.of(chatId);
                    }

                    return  Optional.empty();
                });
    }

    private String createChatId(String senderId, String orderId) {
        var chatId = String.format("%s_%s", senderId, orderId);

        ChatRoom senderRecipient = ChatRoom
                .builder()
                .chatId(chatId)
                .senderId(senderId)
                .recieverId(orderId)
                .build();

        chatRoomRepository.save(senderRecipient);
//        chatRoomRepository.save(recipientSender);

        return chatId;
    }
}
