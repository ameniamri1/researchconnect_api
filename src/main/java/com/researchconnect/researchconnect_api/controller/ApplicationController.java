package com.researchconnect.researchconnect_api.controller;

import com.researchconnect.researchconnect_api.dto.ApplicationRequest;
import com.researchconnect.researchconnect_api.dto.ApplicationResponse;
import com.researchconnect.researchconnect_api.dto.ApplicationStatusUpdateRequest;
import com.researchconnect.researchconnect_api.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Candidatures", description = "API de gestion des candidatures aux sujets")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "Obtenir la liste des candidatures")
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        log.info("Récupération de toutes les candidatures");
        List<ApplicationResponse> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'une candidature spécifique")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        log.info("Récupération de la candidature avec l'ID: {}", id);
        ApplicationResponse application = applicationService.getApplicationById(id);
        return ResponseEntity.ok(application);
    }

    @PostMapping
    @Operation(summary = "Soumettre une nouvelle candidature")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request) {
        log.info("Création d'une nouvelle candidature pour le sujet ID: {}", request.getTopicId());
        ApplicationResponse newApplication = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newApplication);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une candidature")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        log.info("Mise à jour du statut de la candidature ID: {} à {}", id, request.getStatus());
        ApplicationResponse updatedApplication = applicationService.updateApplicationStatus(id, request);
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une candidature")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        log.info("Suppression de la candidature avec l'ID: {}", id);
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/topic/{topicId}")
    @Operation(summary = "Obtenir les candidatures pour un sujet spécifique")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByTopic(@PathVariable Long topicId) {
        log.info("Récupération des candidatures pour le sujet ID: {}", topicId);
        List<ApplicationResponse> applications = applicationService.getApplicationsByTopic(topicId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Obtenir les candidatures d'un étudiant spécifique")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByStudent(@PathVariable Long studentId) {
        log.info("Récupération des candidatures pour l'étudiant ID: {}", studentId);
        List<ApplicationResponse> applications = applicationService.getApplicationsByStudent(studentId);
        return ResponseEntity.ok(applications);
    }
}
