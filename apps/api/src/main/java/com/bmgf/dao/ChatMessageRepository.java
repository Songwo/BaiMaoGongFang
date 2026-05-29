package com.bmgf.dao;

import com.bmgf.po.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findTop50ByRoomIdOrderByTimestampDesc(String roomId);

    long countByRoomId(String roomId);
}
