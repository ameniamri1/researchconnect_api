package com.researchconnect.researchconnect_api.controller;

import com.researchconnect.researchconnect_api.dto.TopicRequest;
import com.researchconnect.researchconnect_api.dto.TopicResponse;
import com.researchconnect.researchconnect_api.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sujets de recherche", description = "API de gestion des sujets de recherche")
public class TopicController {
    
    private final TopicService topicService;

    @GetMapping
    @Operation(summary = "Obtenir la liste des sujets avec filtres optionnels")
    public ResponseEntity<Page<TopicResponse>> getAllTopics(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("Récupération des sujets avec filtres - catégorie: {}, mot-clé: {}", category, keyword);
        Page<TopicResponse> topics = topicService.getAllTopics(category, keyword, pageable);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un sujet spécifique")
    public ResponseEntity<TopicResponse> getTopicById(@PathVariable Long id) {
        log.info("Récupération du sujet avec l'ID: {}", id);
        TopicResponse topic = topicService.getTopicById(id);
        return ResponseEntity.ok(topic);
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau sujet de recherche")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        log.info("Création d'un nouveau sujet: {}", request.getTitle());
        TopicResponse newTopic = topicService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTopic);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un sujet existant")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<TopicResponse> updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest request) {
        log.info("Mise à jour du sujet avec l'ID: {}", id);
        TopicResponse updatedTopic = topicService.updateTopic(id, request);
        return ResponseEntity.ok(updatedTopic);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un sujet")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        log.info("Suppression du sujet avec l'ID: {}", id);
        topicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Obtenir les sujets proposés par un enseignant spécifique")
    public ResponseEntity<Page<TopicResponse>> getTopicsByTeacher(@PathVariable Long teacherId, Pageable pageable) {
        log.info("Récupération des sujets pour l'enseignant avec l'ID: {}", teacherId);
        Page<TopicResponse> topics = topicService.getTopicsByTeacher(teacherId, pageable);
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/my-topics")
    @Operation(summary = "Obtenir les sujets proposés par l'enseignant connecté")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<List<TopicResponse>> getMyTopics() {
        log.info("Récupération des sujets pour l'enseignant connecté");
        List<TopicResponse> topics = topicService.getTopicsByCurrentTeacher();
        return ResponseEntity.ok(topics);
    }
}

