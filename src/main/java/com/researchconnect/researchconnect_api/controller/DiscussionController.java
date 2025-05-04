package com.researchconnect.researchconnect_api.controller;

import com.researchconnect.researchconnect_api.dto.DiscussionRequest;
import com.researchconnect.researchconnect_api.dto.DiscussionResponse;
import com.researchconnect.researchconnect_api.service.DiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics/{topicId}/discussions")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Discussions", description = "API de gestion des discussions sur les sujets")
public class DiscussionController {
    
    private final DiscussionService discussionService;

    @GetMapping
    @Operation(summary = "Obtenir les discussions pour un sujet spécifique")
    public ResponseEntity<List<DiscussionResponse>> getDiscussionsByTopic(@PathVariable Long topicId) {
        log.info("Récupération des discussions pour le sujet ID: {}", topicId);
        List<DiscussionResponse> discussions = discussionService.getDiscussionsByTopic(topicId);
        return ResponseEntity.ok(discussions);
    }

    @PostMapping
    @Operation(summary = "Ajouter un message à la discussion d'un sujet")
    public ResponseEntity<DiscussionResponse> addDiscussion(
            @PathVariable Long topicId, 
            @Valid @RequestBody DiscussionRequest request) {
        log.info("Ajout d'une nouvelle discussion au sujet ID: {}", topicId);
        DiscussionResponse newDiscussion = discussionService.addDiscussion(topicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDiscussion);
    }

    @PutMapping("/{discussionId}")
    @Operation(summary = "Modifier un message")
    public ResponseEntity<DiscussionResponse> updateDiscussion(
            @PathVariable Long topicId,
            @PathVariable Long discussionId,
            @Valid @RequestBody DiscussionRequest request) {
        log.info("Mise à jour de la discussion ID: {} du sujet ID: {}", discussionId, topicId);
        DiscussionResponse updatedDiscussion = discussionService.updateDiscussion(discussionId, request);
        return ResponseEntity.ok(updatedDiscussion);
    }

    @DeleteMapping("/{discussionId}")
    @Operation(summary = "Supprimer un message")
    public ResponseEntity<Void> deleteDiscussion(
            @PathVariable Long topicId,
            @PathVariable Long discussionId) {
        log.info("Suppression de la discussion ID: {} du sujet ID: {}", discussionId, topicId);
        discussionService.deleteDiscussion(discussionId);
        return ResponseEntity.noContent().build();
    }
}
