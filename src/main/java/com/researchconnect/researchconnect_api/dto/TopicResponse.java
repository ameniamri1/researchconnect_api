package com.researchconnect.researchconnect_api.dto;

import com.researchconnect.researchconnect_api.entity.Topic;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TopicResponse {

    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String category;
    private String prerequisites;
    private LocalDate deadline;
    private String contact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int applicationCount;

    public static TopicResponse fromEntity(Topic topic) {
        TopicResponse response = new TopicResponse();
        response.setId(topic.getId());
        response.setTitle(topic.getTitle());
        response.setDescription(topic.getDescription());
        response.setTeacherId(topic.getTeacher().getId());
        response.setTeacherName(topic.getTeacher().getName());
        response.setCategory(topic.getCategory());
        response.setPrerequisites(topic.getPrerequisites());
        response.setDeadline(topic.getDeadline());
        response.setContact(topic.getContact());
        response.setCreatedAt(topic.getCreatedAt());
        response.setUpdatedAt(topic.getUpdatedAt());
        response.setApplicationCount(topic.getApplications().size());
        return response;
    }
}
