package com.bmgf.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomStatusDTO {
    private String type = "status";
    private int onlineCount;
    private long messageCount;
}

