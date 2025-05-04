package com.researchconnect.researchconnect_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotNull
    private Long topicId;

    @NotBlank
    private String message;
}
