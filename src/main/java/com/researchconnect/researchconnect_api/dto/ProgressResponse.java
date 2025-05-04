package com.researchconnect.researchconnect_api.dto;

import com.researchconnect.researchconnect_api.entity.Progress;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProgressResponse {

    private Long id;
    private Long topicId;
    private String topicTitle;
    private Long studentId;
    private String studentName;
    private Progress.Status status;
    private Integer completionPercentage;
    private String notes;
    private LocalDateTime lastUpdated;

    public static ProgressResponse fromEntity(Progress progress) {
        ProgressResponse response = new ProgressResponse();
        response.setId(progress.getId());
        response.setTopicId(progress.getTopic().getId());
        response.setTopicTitle(progress.getTopic().getTitle());
        response.setStudentId(progress.getStudent().getId());
        response.setStudentName(progress.getStudent().getName());
        response.setStatus(progress.getStatus());
        response.setCompletionPercentage(progress.getCompletionPercentage());
        response.setNotes(progress.getNotes());
        response.setLastUpdated(progress.getUpdatedAt());
        return response;
    }
}
