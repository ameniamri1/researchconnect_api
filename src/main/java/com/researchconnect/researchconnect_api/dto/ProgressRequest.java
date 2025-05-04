package com.researchconnect.researchconnect_api.dto;

import com.researchconnect.researchconnect_api.entity.Progress;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgressRequest {
    
    @NotNull
    private Long studentId;
    
    @NotNull
    private Long topicId;
    
    @NotNull
    private Progress.Status status;
    
    @Min(0)
    @Max(100)
    private Integer completionPercentage;
    
    private String notes;
}
