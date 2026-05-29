package com.bmgf.DTO;
import lombok.Data;
@Data
public class ContainerInstanceDTO {
    private String containerId;
    private String imageName;
    // 不包含 User 对象
}
