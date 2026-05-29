package com.bmgf.controller;

import com.bmgf.DTO.RoomDTO;
import com.bmgf.po.ChatRoom;
import com.bmgf.service.impl.ChatService;
import com.bmgf.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/bmgfChat/rooms")
public class RoomController {

    @Autowired
    private ChatService service;

    @Autowired
    private JwtUtil jwtUtil;

    //创建房间
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ChatRoom room) {
        return ResponseEntity.ok(service.createRoom(room));
    }
    //加入房间
    @PostMapping("/{id}/join")
    public ResponseEntity<?> join(@PathVariable String id, @RequestBody Map<String, String> body,
                                  @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromToken(token);
        boolean joined = service.joinRoom(id, username, body.get("password"));
        return joined ? ResponseEntity.ok("joined") : ResponseEntity.status(403).body("fail");
    }
    //更新房间标题
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable String id, @RequestBody Map<String, String> body) {
        Optional<ChatRoom> roomOpt = service.getRoom(id);
        if (roomOpt.isEmpty()) return ResponseEntity.notFound().build();
        ChatRoom room = roomOpt.get();
        room.setTitle(body.get("title"));
        return ResponseEntity.ok(service.save(room));
    }
    //删除房间
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable String id) {
        Optional<ChatRoom> roomOpt = service.getRoom(id);
        if (roomOpt.isEmpty()) return ResponseEntity.notFound().build();
        service.delete(id);
        return ResponseEntity.ok().build();
    }
    //获取所有房间
    @GetMapping("/chatAllroom")
    public ResponseEntity<?> getAllRooms() {
        return ResponseEntity.ok(service.getAllRooms());
    }
    // 每个分类的房间数量
    @GetMapping("/category/summary")
    public ResponseEntity<?> getCategorySummary() {
        return ResponseEntity.ok(service.countRoomsByAllCategories());
    }
    // 指定分类的房间数量
    @GetMapping("/category/{category}/count")
    public ResponseEntity<?> getCountByCategory(@PathVariable String category) {
        long count = service.countRoomsByCategory(category);
        return ResponseEntity.ok(Map.of("category", category, "count", count));
    }
    // 指定分类的房间列表（含历史消息数）
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getRoomsByCategory(@PathVariable String category) {
        List<RoomDTO> list = service.getAllRoomDTOsByCategory(category);
        return ResponseEntity.ok(list);
    }
    // 获取房间详情（含历史消息数）
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoomDetail(@PathVariable String roomId) {
        Optional<ChatRoom> roomOpt = service.getRoom(roomId);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.convertToDto(roomOpt.get()));
    }

}

