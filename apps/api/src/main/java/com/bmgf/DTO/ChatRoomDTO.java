package com.bmgf.DTO;

import com.bmgf.po.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDTO {
    private String id;
    private String title;
    private String category;
    private boolean needPassword;
    private LocalDateTime expireAt;
    private int onlineCount;
    private long messageCount;

    public static ChatRoomDTO from(ChatRoom room,int onlineCount,long messageCount) {
        return new ChatRoomDTO(
                room.getId(),
                room.getTitle(),
                room.getCategory(),
                room.getPassword() != null && !room.getPassword().isEmpty(),
                room.getExpireAt(),
                onlineCount,
                messageCount
        );
    }


}

