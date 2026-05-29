package com.bmgf.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class FeedbackDTO {
    private String type;
    private String title;
    private String content;
    private String contact;
    private List<MultipartFile> files;
}