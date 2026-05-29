package com.bmgf.Manger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomManager {

    // roomId -> (userId -> WebSocketSession)
    private static final Map<String, Map<String, WebSocketSession>> roomUserSessions = new ConcurrentHashMap<>();

    private static final Map<String, Long> messageCountMap = new ConcurrentHashMap<>();

    // 用于维护所有监听房间概览信息的session（比如页面首页）
    private static final Set<WebSocketSession> overviewSessions = ConcurrentHashMap.newKeySet();

    public static synchronized void addSession(String roomId, String userId, WebSocketSession session) {
        Map<String, WebSocketSession> userSessionMap =
                roomUserSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

        // 踢掉旧连接（如果有）
        WebSocketSession oldSession = userSessionMap.put(userId, session);
        if (oldSession != null && oldSession.isOpen() && !oldSession.getId().equals(session.getId())) {
            try {
                oldSession.close(CloseStatus.NORMAL.withReason("Another connection established"));
            } catch (IOException ignored) {
            }
        }
    }

    public static synchronized void removeSession(String roomId, String userId) {
        Map<String, WebSocketSession> userSessionMap = roomUserSessions.get(roomId);
        if (userSessionMap != null) {
            userSessionMap.remove(userId);
            if (userSessionMap.isEmpty()) {
                roomUserSessions.remove(roomId);
            }
        }
    }

    public static int getOnlineCount(String roomId) {
        Map<String, WebSocketSession> userSessionMap = roomUserSessions.get(roomId);
        return userSessionMap == null ? 0 : userSessionMap.size();
    }

    public static long getMessageCount(String roomId) {
        return messageCountMap.getOrDefault(roomId, 0L);
    }

    public static void initMessageCount(String roomId, long count) {
        messageCountMap.put(roomId, count);
    }

    public static void incrementMessageCount(String roomId) {
        messageCountMap.merge(roomId, 1L, Long::sum);
    }

    public static Set<WebSocketSession> getSessions(String roomId) {
        Map<String, WebSocketSession> userSessionMap = roomUserSessions.get(roomId);
        if (userSessionMap == null) return Collections.emptySet();
        return Set.copyOf(userSessionMap.values());
    }

    // 下面是用于概览推送的管理

    public static void addOverviewSession(WebSocketSession session) {
        overviewSessions.add(session);
    }

    public static void removeOverviewSession(WebSocketSession session) {
        overviewSessions.remove(session);
    }

    public static Set<WebSocketSession> getOverviewSessions() {
        return overviewSessions;
    }

    // 返回所有房间ID
    public static Set<String> getAllRoomIds() {
        return roomUserSessions.keySet();
    }
}
