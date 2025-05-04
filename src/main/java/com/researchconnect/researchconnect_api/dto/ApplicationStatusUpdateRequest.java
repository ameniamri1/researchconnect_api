package com.researchconnect.researchconnect_api.dto;

import com.researchconnect.researchconnect_api.entity.Application;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {

    @NotNull
    private Application.Status status;

    private String feedback;
}
