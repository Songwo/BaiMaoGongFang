package com.bmgf.dao;

import com.bmgf.po.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    List<ChatRoom> findByCategory(String category);

    long countByCategory(String category);
}
