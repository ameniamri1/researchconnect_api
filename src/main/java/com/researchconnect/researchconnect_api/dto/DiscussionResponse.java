package com.researchconnect.researchconnect_api.dto;

import com.researchconnect.researchconnect_api.entity.Discussion;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiscussionResponse {

    private Long id;
    private Long topicId;
    private Long userId;
    private String userName;
    private String userRole;
    private String message;
    private LocalDateTime createdAt;

    public static DiscussionResponse fromEntity(Discussion discussion) {
        DiscussionResponse response = new DiscussionResponse();
        response.setId(discussion.getId());
        response.setTopicId(discussion.getTopic().getId());
        response.setUserId(discussion.getUser().getId());
        response.setUserName(discussion.getUser().getName());
        response.setUserRole(discussion.getUser().getRole().name());
        response.setMessage(discussion.getMessage());
        response.setCreatedAt(discussion.getCreatedAt());
        return response;
    }
}