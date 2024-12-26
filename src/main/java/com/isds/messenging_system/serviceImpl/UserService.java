package com.isds.messenging_system.serviceImpl;


import com.isds.messenging_system.domain.entity.UserChat;
import com.isds.messenging_system.domain.enums.Status;
import com.isds.messenging_system.domain.repository.UserRepository;
import com.isds.messenging_system.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public void saveUser(UserChat user) {
        user.setStatus(Status.ONLINE);
        repository.save(user);
    }

    public UserChat findByUserId(String user) {
        return repository.findByUserId(user).orElse(null);

    }

    public void disconnect(UserDto user) {
        var storedUser = repository.findByUserId(user.getUserId()).orElse(null);
        if (storedUser != null) {
            storedUser.setStatus(Status.OFFLINE);
            repository.save(storedUser);
        }
    }

    public List<UserChat> findConnectedUsers() {
        return repository.findAllByStatus(Status.ONLINE);
    }

    public UserChat addUser(UserDto user) {
        UserChat users = findByUserId(user.getUserId());
        if(Objects.nonNull(users)){
            users.setName(user.getFullName());
            repository.save(users);
            return users;

        }else {
            UserChat newUser = new UserChat();
            newUser.setUserId(user.getUserId());
            newUser.setName(user.getFullName());
            saveUser(newUser);
            return newUser;
        }
    }
}