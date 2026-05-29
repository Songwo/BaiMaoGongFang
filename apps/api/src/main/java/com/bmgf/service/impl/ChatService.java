package com.bmgf.service.impl;

import com.bmgf.DTO.ChatMessageDTO;
import com.bmgf.DTO.ChatRoomDTO;
import com.bmgf.DTO.RoomDTO;
import com.bmgf.Manger.ChatRoomManager;
import com.bmgf.dao.ChatMessageRepository;
import com.bmgf.dao.ChatRoomRepository;
import com.bmgf.dao.UserRepository;
import com.bmgf.po.ChatMessage;
import com.bmgf.po.ChatRoom;
import com.bmgf.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository repository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatRoom createRoom(ChatRoom room) {
        room.setParticipants(new ArrayList<>());
        return repository.save(room);
    }

    public boolean joinRoom(String roomId, String username, String password) {
        Optional<ChatRoom> optRoom = repository.findById(roomId);
        if (optRoom.isEmpty()) return false;
        ChatRoom room = optRoom.get();
        if (!room.getPassword().equals(password)) return false;
        if (room.getExpireAt().isBefore(LocalDateTime.now())) return false;
        room.getParticipants().add(username);
        repository.save(room);
        return true;
    }

    public Optional<ChatRoom> getRoom(String id) {
        return repository.findById(id);
    }

    public ChatRoom save(ChatRoom room) {
        return repository.save(room);
    }

    public void delete(String roomId) {
        repository.deleteById(roomId);
    }

    public List<ChatRoomDTO> getAllRooms() {
        List<ChatRoom> rooms = repository.findAll();

        return rooms.stream().map(room -> {
            String roomId = room.getId();
            // 1. 在线人数
            int onlineCount = ChatRoomManager.getOnlineCount(roomId);
            // 2. 消息数量
            long messageCount = chatMessageRepository.countByRoomId(roomId);
            // 3. 封装 DTO
            return ChatRoomDTO.from(room, onlineCount, messageCount);
        }).collect(Collectors.toList());
    }

    // 根据分类返回房间数目
    public long countRoomsByCategory(String category) {
        return repository.countByCategory(category);
    }

    public long countMessagesForRoom(String roomId) {
        return chatMessageRepository.countByRoomId(roomId);
    }

    // 所有分类统计
    public List<Map<String, Object>> countRoomsByAllCategories() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(ChatRoom::getCategory, Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", e.getKey());
                    map.put("count", e.getValue().intValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // 转为 DTO
    public RoomDTO convertToDto(ChatRoom room) {
        long messageCount = countMessagesForRoom(room.getId());
        int onlineCount = ChatRoomManager.getOnlineCount(room.getId());
        boolean hasPassword = room.getPassword() != null && !room.getPassword().isEmpty();
        return new RoomDTO(
                room.getId(), room.getTitle(), room.getCategory(),
                hasPassword, onlineCount, messageCount
        );
    }

    public List<RoomDTO> getAllRoomDTOsByCategory(String category) {
        List<ChatRoom> rooms = repository.findByCategory(category);
        return rooms.stream().map(this::convertToDto).toList();
    }

    /** 把单条消息转成带用户头像、昵称的 DTO */
    public ChatMessageDTO toDto(ChatMessage msg) {
        User u = userRepository.findById(msg.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));
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

    /** 保存新消息 */
    public ChatMessageDTO saveAndConvert(ChatMessage msg) {
        ChatMessage saved = chatMessageRepository.save(msg);
        return toDto(saved);
    }
}
