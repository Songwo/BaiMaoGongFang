package com.bmgf.Config;

import com.bmgf.handler.ChatOverviewWebSocketHandler;
import com.bmgf.handler.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatOverviewWebSocketHandler overviewHandler;

    @Autowired
    private ChatWebSocketHandler chatHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 房间列表总览 —— 不需要 roomId 参数
        registry.addHandler(overviewHandler, "/ws/overview")
                .setAllowedOrigins("*");

        // 单个房间聊天 —— 必须带 roomId 参数
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }
}

