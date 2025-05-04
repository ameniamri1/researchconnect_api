package com.researchconnect.researchconnect_api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TopicRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String description;

    private String category;

    private String prerequisites;

    @NotNull
    @Future
    private LocalDate deadline;

    @Size(max = 200)
    private String contact;
}