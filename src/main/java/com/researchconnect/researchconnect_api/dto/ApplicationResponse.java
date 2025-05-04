package com.researchconnect.researchconnect_api.dto;

import com.researchconnect.researchconnect_api.entity.Application;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {

    private Long id;
    private Long topicId;
    private String topicTitle;
    private Long studentId;
    private String studentName;
    private String message;
    private Application.Status status;
    private LocalDateTime appliedAt;
    private LocalDateTime responseDate;
    private String teacherFeedback;

    public static ApplicationResponse fromEntity(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setTopicId(application.getTopic().getId());
        response.setTopicTitle(application.getTopic().getTitle());
        response.setStudentId(application.getStudent().getId());
        response.setStudentName(application.getStudent().getName());
        response.setMessage(application.getMessage());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getCreatedAt());
        response.setResponseDate(application.getResponseDate());
        response.setTeacherFeedback(application.getTeacherFeedback());
        return response;
    }
}