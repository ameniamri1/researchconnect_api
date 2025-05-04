package com.researchconnect.researchconnect_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiscussionRequest {

    @NotBlank
    private String message;
}
