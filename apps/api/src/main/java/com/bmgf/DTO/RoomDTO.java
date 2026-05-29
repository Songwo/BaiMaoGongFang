package com.bmgf.DTO;

import lombok.Data;

@Data
public class RoomDTO {
    private String id;
    private String title;
    private String category;
    private boolean hasPassword;
    private int onlineCount;
    private long messageCount;

    public RoomDTO(String id, String title, String category,
                   boolean hasPassword, int onlineCount, long messageCount) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.hasPassword = hasPassword;
        this.onlineCount = onlineCount;
        this.messageCount = messageCount;
    }

}


