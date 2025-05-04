package com.researchconnect.researchconnect_api.controller;

import com.researchconnect.researchconnect_api.dto.ProgressRequest;
import com.researchconnect.researchconnect_api.dto.ProgressResponse;
import com.researchconnect.researchconnect_api.service.ProgressService;
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
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Suivi de progression", description = "API de gestion du suivi de progression des étudiants")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Obtenir la progression d'un étudiant")
    public ResponseEntity<List<ProgressResponse>> getProgressByStudent(@PathVariable Long studentId) {
        log.info("Récupération des progressions pour l'étudiant ID: {}", studentId);
        List<ProgressResponse> progress = progressService.getProgressByStudent(studentId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/topic/{topicId}")
    @Operation(summary = "Obtenir les progressions pour un sujet spécifique")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<List<ProgressResponse>> getProgressByTopic(@PathVariable Long topicId) {
        log.info("Récupération des progressions pour le sujet ID: {}", topicId);
        List<ProgressResponse> progress = progressService.getProgressByTopic(topicId);
        return ResponseEntity.ok(progress);
    }

    @PostMapping
    @Operation(summary = "Créer ou mettre à jour un suivi de progression")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ProgressResponse> createOrUpdateProgress(@Valid @RequestBody ProgressRequest request) {
        log.info("Création/mise à jour de la progression pour l'étudiant ID: {} et le sujet ID: {}",
                request.getStudentId(), request.getTopicId());
        ProgressResponse progress = progressService.createOrUpdateProgress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(progress);
    }

    @DeleteMapping("/{progressId}")
    @Operation(summary = "Supprimer un suivi de progression")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProgress(@PathVariable Long progressId) {
        log.info("Suppression de la progression ID: {}", progressId);
        progressService.deleteProgress(progressId);
        return ResponseEntity.noContent().build();
    }
}
