package com.isds.messenging_system.domain.entity;

import com.isds.messenging_system.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="users_chat")
public class UserChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String name;
    private String userId;
    @Enumerated(EnumType.STRING)
    private Status status;
}