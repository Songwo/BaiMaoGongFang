package com.bmgf.po;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Reward")
public class Reward {
    private String id;
    private String name;
    private String context;
}
