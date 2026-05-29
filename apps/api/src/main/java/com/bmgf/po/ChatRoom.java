package com.bmgf.po;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "title")
    private String title;
    @Column(name = "category")
    private String category;
    @Column(name = "password")
    private String password;
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "participants", columnDefinition = "jsonb")
    private List<String> participants;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
