package com.isds.messenging_system.domain.repository;


import com.isds.messenging_system.domain.entity.UserChat;
import com.isds.messenging_system.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<UserChat, Long> {
    List<UserChat> findAllByStatus(Status status);
    Optional<UserChat> findByUserId(String userId);
}