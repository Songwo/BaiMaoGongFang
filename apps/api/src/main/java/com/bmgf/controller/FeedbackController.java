package com.bmgf.controller;

import com.bmgf.DTO.FeedbackDTO;
import com.bmgf.po.Feedback;
import com.bmgf.service.impl.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @RequestParam("type") String type,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("contact") String contact,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            FeedbackDTO feedbackDTO = new FeedbackDTO();
            feedbackDTO.setType(type);
            feedbackDTO.setTitle(title);
            feedbackDTO.setContent(content);
            feedbackDTO.setContact(contact);
            if (files != null) {
                feedbackDTO.setFiles(List.of(files));
            }

            Feedback feedback = feedbackService.submitFeedback(feedbackDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "反馈提交成功");
            response.put("data", feedback);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "反馈提交失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
