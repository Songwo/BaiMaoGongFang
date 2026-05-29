package com.bmgf.service.impl;

import com.bmgf.DTO.ChatMessageDTO;
import com.bmgf.dao.ChatMessageRepository;
import com.bmgf.dao.UserRepository;
import com.bmgf.po.ChatMessage;
import com.bmgf.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatMessageService {

    @Autowired
    public ChatMessageRepository messageRepo;

    @Autowired
    private UserRepository userRepo;

    /** 查询并组装历史消息 DTO */
    public List<ChatMessageDTO> getHistory(String roomId, int limit) {
        List<ChatMessage> list = messageRepo.findTop50ByRoomIdOrderByTimestampDesc(roomId);
        Collections.reverse(list); // 从旧到新
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** 保存并返回带用户信息的 DTO */
    public ChatMessageDTO saveAndConvert(ChatMessage msg) {
        ChatMessage saved = messageRepo.save(msg);
        return toDto(saved);
    }

    private ChatMessageDTO toDto(ChatMessage msg) {
        log.info(msg.toString());
        if (msg == null || msg.getSenderId() == null) {
            log.error("Message or senderId is null: {}", msg);
            return null; // 或者抛出自定义异常
        }
        User u = userRepo.findById(msg.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found: " + msg.getSenderId()));
        return new ChatMessageDTO(
                msg.getId(),
                msg.getRoomId(),
                msg.getSenderId(),
                u.getUsername(),
                u.getAvatar(),
                msg.getContent(),
                msg.getTimestamp()
        );
    }
}

