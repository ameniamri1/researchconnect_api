package com.researchconnect.researchconnect_api.controller;

import com.researchconnect.researchconnect_api.dto.ResourceResponse;
import com.researchconnect.researchconnect_api.entity.Resource;
import com.researchconnect.researchconnect_api.repository.ResourceRepository;
import com.researchconnect.researchconnect_api.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ressources", description = "API de gestion des ressources liées aux sujets")
public class ResourceController {
    
    private final ResourceService resourceService;
    private final ResourceRepository resourceRepository;

    @GetMapping("/topics/{topicId}/resources")
    @Operation(summary = "Obtenir les ressources pour un sujet spécifique")
    public ResponseEntity<List<ResourceResponse>> getResourcesByTopic(@PathVariable Long topicId) {
        log.info("Récupération des ressources pour le sujet ID: {}", topicId);
        List<ResourceResponse> resources = resourceService.getResourcesByTopic(topicId);
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/topics/{topicId}/resources")
    @Operation(summary = "Télécharger une ressource pour un sujet")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ResourceResponse> uploadResource(
            @PathVariable Long topicId,
            @RequestParam("file") MultipartFile file) {
        log.info("Téléchargement d'une ressource pour le sujet ID: {}, nom du fichier: {}", 
                 topicId, file.getOriginalFilename());
        ResourceResponse resource = resourceService.uploadResource(topicId, file);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/resources/{resourceId}")
    @Operation(summary = "Télécharger une ressource")
    public ResponseEntity<ByteArrayResource> downloadResource(@PathVariable Long resourceId) {
        log.info("Téléchargement de la ressource ID: {}", resourceId);
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Ressource non trouvée avec l'ID: " + resourceId));
        
        ByteArrayResource fileResource = resourceService.downloadResource(resourceId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getName() + "\"")
                .contentType(MediaType.parseMediaType(resource.getFileType()))
                .body(fileResource);
    }

    @DeleteMapping("/resources/{resourceId}")
    @Operation(summary = "Supprimer une ressource")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long resourceId) {
        log.info("Suppression de la ressource ID: {}", resourceId);
        resourceService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }
}
