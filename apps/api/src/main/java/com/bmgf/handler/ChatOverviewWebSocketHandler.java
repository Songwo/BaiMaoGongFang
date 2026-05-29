package com.bmgf.handler;

import com.bmgf.DTO.ChatRoomDTO;
import com.bmgf.Manger.ChatRoomManager;
import com.bmgf.service.impl.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class ChatOverviewWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Overview connected: {}", session.getId());
        ChatRoomManager.addOverviewSession(session);
        sendRoomList(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ChatRoomManager.removeOverviewSession(session);
    }

    private void sendRoomList(WebSocketSession session) {
        try {
            List<Map<String, Object>> roomsInfo = new ArrayList<>();
            for (ChatRoomDTO room : chatService.getAllRooms()) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", room.getId());
                info.put("title", room.getTitle());
                info.put("category", room.getCategory());
                info.put("needPassword", room.isNeedPassword());
                info.put("expireAt", room.getExpireAt().toString());
                info.put("onlineCount", ChatRoomManager.getOnlineCount(room.getId()));
                info.put("messageCount", ChatRoomManager.getMessageCount(room.getId()));
                roomsInfo.add(info);
            }
            Map<String, Object> data = Map.of(
                    "type", "roomOverview",
                    "rooms", roomsInfo
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(data)));
        } catch (IOException e) {
            log.error("发送房间列表失败", e);
        }
    }

    // 定时任务推送最新房间在线人数和消息数
    @Scheduled(fixedRate = 10000)
    public void startRoomOverviewPush() {
        try {
            List<Map<String, Object>> roomsInfo = new ArrayList<>();
            for (ChatRoomDTO room : chatService.getAllRooms()) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", room.getId());
                info.put("onlineCount", ChatRoomManager.getOnlineCount(room.getId()));
                info.put("messageCount", ChatRoomManager.getMessageCount(room.getId()));
                roomsInfo.add(info);
            }
            Map<String, Object> data = Map.of(
                    "type", "roomOverviewUpdate",
                    "rooms", roomsInfo
            );
            String json = objectMapper.writeValueAsString(data);
            for (WebSocketSession s : ChatRoomManager.getOverviewSessions()) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.error("推送房间概览更新失败", e);
        }
    }

}

