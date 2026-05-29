package com.bmgf.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "room_id")
    private String roomId;
    @Column(name = "sender_id")
    private String senderId;
    @Column(name = "content", columnDefinition = "text")
    private String content;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
