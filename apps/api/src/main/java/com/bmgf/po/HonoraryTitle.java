package com.bmgf.po;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "HonoraryTitle")
@Data
public class HonoraryTitle {
    @Id
    private String id;
    private String title;
    private String description;
    private int activite = 100;
}
