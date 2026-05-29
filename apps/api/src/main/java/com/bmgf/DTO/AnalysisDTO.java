package com.bmgf.DTO;

import lombok.Data;

@Data
public class AnalysisDTO {
    private Double current;
    private String totalTime;
    private Integer day;
    private Integer intotalPass;
    private String advice;
}
