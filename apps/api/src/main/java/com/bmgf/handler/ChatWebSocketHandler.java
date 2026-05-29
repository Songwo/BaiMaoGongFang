package com.bmgf.handler;

import com.bmgf.DTO.ChatMessageDTO;
import com.bmgf.Manger.ChatRoomManager;
import com.bmgf.po.ChatMessage;
import com.bmgf.po.ChatRoom;
import com.bmgf.service.impl.ChatMessageService;
import com.bmgf.service.impl.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatService chatService;              // 管理 ChatRoom

    @Autowired
    private ChatMessageService messageService;    // 消息服务

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor executor;

    /** 房间到期检查、倒计时推送 */
    @PostConstruct
    public void startRoomCountdown() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> {
                    for (String roomId : ChatRoomManager.getAllRoomIds()) {
                        chatService.getRoom(roomId).ifPresent(room -> {
                            Duration rem = Duration.between(LocalDateTime.now(), room.getExpireAt());
                            if (rem.isNegative()) {
                                ChatRoomManager.getSessions(roomId)
                                        .forEach(s -> closeQuietly(s, "Room expired"));
                                ChatRoomManager.getSessions(roomId).clear();
                                chatService.delete(roomId);
                            } else {
                                long mins = rem.toMinutes();
                                String timerJson = String.format("{\"type\":\"timer\",\"minutesLeft\":%d}", mins);
                                broadcastToRoom(roomId, new TextMessage(timerJson));
                            }
                        });
                    }
                }, 0, 60, TimeUnit.SECONDS);
    }

    /** 新连接 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New connection: URI = {}", session.getUri());
        String roomId = getQueryParam(session, "roomId");
        String userId = getQueryParam(session, "userId");
        if (roomId.isEmpty() || userId.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing roomId or userId"));
            return;
        }
        Optional<ChatRoom> opt = chatService.getRoom(roomId);
        if (opt.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Room not found"));
            return;
        }

        // 同一用户新连接，踢掉旧连接
        ChatRoomManager.addSession(roomId, userId, session);

        // 初始化消息计数
        if (ChatRoomManager.getMessageCount(roomId) == 0) {
            long cnt = messageService.messageRepo.countByRoomId(roomId);
            ChatRoomManager.initMessageCount(roomId, cnt);
        }

        sendRoomInfo(session, roomId);

        List<ChatMessageDTO> history = messageService.getHistory(roomId, 50);
        for (ChatMessageDTO dto : history) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(dto)));
        }
    }

    /** 收到消息 */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage text) throws Exception {
        String roomId = getQueryParam(session, "roomId");
        if (roomId.isEmpty()) return;

        String payload = text.getPayload();

        // 简单判断 type 是不是 heartbeat
        if (payload.contains("\"type\":\"heartbeat\"")) {
            // 忽略心跳包
            return;
        }

        // 如果不是 heartbeat，则继续作为聊天消息处理
        ChatMessage raw = objectMapper.readValue(payload, ChatMessage.class);
        raw.setTimestamp(LocalDateTime.now());

        ChatMessageDTO dto = messageService.saveAndConvert(raw);

        ChatRoomManager.incrementMessageCount(roomId);

        String json = objectMapper.writeValueAsString(dto);
        broadcastToRoom(roomId, new TextMessage(json));
    }


    /** 断开连接 */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = getQueryParam(session, "roomId");
        String userId = getQueryParam(session, "userId");
        if (!roomId.isEmpty() && !userId.isEmpty()) {
            ChatRoomManager.removeSession(roomId, userId);
            log.info("连接关闭: roomId={}, userId={}, sessionId={}", roomId, userId, session.getId());
            broadcastRoomInfo(roomId);
        }
    }

    //————— 辅助方法 —————//

    private String getQueryParam(WebSocketSession session, String key) {
        if (session.getUri() == null || session.getUri().getQuery() == null) return "";
        return java.util.Arrays.stream(session.getUri().getQuery().split("&"))
                .map(param -> param.split("=", 2))
                .filter(kv -> kv.length == 2 && kv[0].equals(key))
                .map(kv -> kv[1])
                .findFirst().orElse("");
    }

    private void broadcastToRoom(String roomId, TextMessage msg) {
        ChatRoomManager.getSessions(roomId).forEach(s -> {
            if (s.isOpen()) {
                executor.execute(() -> {
                    try {
                        s.sendMessage(msg);
                    } catch (Exception e) {
                        closeQuietly(s, "broadcast fail");
                    }
                });
            }
        });
    }

    private void sendRoomInfo(WebSocketSession session, String roomId) throws IOException {
        var info = java.util.Map.of(
                "type", "roomInfo",
                "onlineCount", ChatRoomManager.getOnlineCount(roomId),
                "messageCount", ChatRoomManager.getMessageCount(roomId)
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(info)));
    }

    private void broadcastRoomInfo(String roomId) {
        TextMessage infoMsg;
        try {
            var info = java.util.Map.of(
                    "type", "roomInfo",
                    "onlineCount", ChatRoomManager.getOnlineCount(roomId),
                    "messageCount", ChatRoomManager.getMessageCount(roomId)
            );
            infoMsg = new TextMessage(objectMapper.writeValueAsString(info));
        } catch (Exception e) {
            return;
        }
        broadcastToRoom(roomId, infoMsg);
    }

    private void closeQuietly(WebSocketSession s, String reason) {
        try {
            s.close(CloseStatus.GOING_AWAY.withReason(reason));
        } catch (IOException ignored) {
        }
    }
}
