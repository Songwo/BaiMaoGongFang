package com.bmgf.controller;

import com.bmgf.po.Feedback;
import com.bmgf.service.impl.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/feedback")
@CrossOrigin(origins = "*")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // 获取所有反馈（分页）
    @GetMapping
    public ResponseEntity<?> getAllFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Feedback> feedbacks = feedbackService.getAllFeedback(pageRequest);
        return ResponseEntity.ok(feedbacks);
    }

    // 根据状态获取反馈
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getFeedbackByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Feedback> feedbacks = feedbackService.getFeedbackByStatus(status, pageRequest);
        return ResponseEntity.ok(feedbacks);
    }

    // 更新反馈状态
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateFeedbackStatus(
            @PathVariable String id,
            @RequestParam String status) {
        try {
            Feedback feedback = feedbackService.updateFeedbackStatus(id, status);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // 删除反馈
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable String id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "反馈删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // 获取反馈统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getFeedbackStats() {
        Map<String, Long> stats = feedbackService.getFeedbackStats();
        return ResponseEntity.ok(stats);
    }
}
