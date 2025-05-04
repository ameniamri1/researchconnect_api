package com.researchconnect.researchconnect_api.dto;

import com.researchconnect.researchconnect_api.entity.Resource;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceResponse {

    private Long id;
    private Long topicId;
    private String name;
    private String fileType;
    private long fileSize;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;

    public static ResourceResponse fromEntity(Resource resource) {
        ResourceResponse response = new ResourceResponse();
        response.setId(resource.getId());
        response.setTopicId(resource.getTopic().getId());
        response.setName(resource.getName());
        response.setFileType(resource.getFileType());
        response.setFileSize(resource.getFileSize());
        response.setUploadedById(resource.getUploadedBy().getId());
        response.setUploadedByName(resource.getUploadedBy().getName());
        response.setUploadedAt(resource.getCreatedAt());
        return response;
    }
}
