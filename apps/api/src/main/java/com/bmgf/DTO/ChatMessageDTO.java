package com.bmgf.DTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private String id;
    private String roomId;

    private String senderId;
    private String senderName;
    private String senderAvatarUrl;

    private String content;
    private LocalDateTime timestamp;
}

