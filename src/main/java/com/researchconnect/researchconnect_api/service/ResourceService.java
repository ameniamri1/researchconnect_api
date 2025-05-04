package com.researchconnect.researchconnect_api.service;

import com.researchconnect.researchconnect_api.dto.ResourceResponse;
import com.researchconnect.researchconnect_api.entity.Resource;
import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.repository.ResourceRepository;
import com.researchconnect.researchconnect_api.repository.TopicRepository;
import com.researchconnect.researchconnect_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    private final Path fileStorageLocation = Paths.get("uploads/resources")
            .toAbsolutePath().normalize();

    public ResourceService() {
        this.resourceRepository = null;
        this.topicRepository = null;
        this.userRepository = null;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage des fichiers", ex);
        }
    }

    public List<ResourceResponse> getResourcesByTopic(Long topicId) {
        log.debug("Récupération des ressources pour le sujet ID: {}", topicId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        return resourceRepository.findByTopic(topic).stream()
                .map(ResourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResourceResponse uploadResource(Long topicId, MultipartFile file) {
        log.debug("Téléchargement d'une ressource pour le sujet ID: {}", topicId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        // Vérifier que l'utilisateur est l'enseignant du sujet ou un administrateur
        if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            // On pourrait aussi permettre aux étudiants de télécharger des ressources
            throw new AccessDeniedException("Les étudiants ne peuvent pas télécharger des ressources");
        }

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Resource resource = new Resource();
            resource.setTopic(topic);
            resource.setName(file.getOriginalFilename());
            resource.setFilePath(targetLocation.toString());
            resource.setFileType(file.getContentType());
            resource.setFileSize(file.getSize());
            resource.setUploadedBy(currentUser);

            Resource savedResource = resourceRepository.save(resource);
            log.info("Ressource téléchargée avec succès, ID: {}", savedResource.getId());

            return ResourceResponse.fromEntity(savedResource);
        } catch (IOException ex) {
            log.error("Erreur lors du téléchargement du fichier", ex);
            throw new RuntimeException("Impossible de stocker le fichier. Veuillez réessayer!", ex);
        }
    }

    public ByteArrayResource downloadResource(Long resourceId) {
        log.debug("Téléchargement de la ressource ID: {}", resourceId);

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Ressource non trouvée avec l'ID: " + resourceId));

        try {
            Path filePath = Paths.get(resource.getFilePath());
            ByteArrayResource byteArrayResource = new ByteArrayResource(Files.readAllBytes(filePath));
            log.info("Ressource téléchargée, ID: {}", resourceId);
            return byteArrayResource;
        } catch (IOException ex) {
            log.error("Erreur lors de la lecture du fichier", ex);
            throw new RuntimeException("Impossible de lire le fichier", ex);
        }
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        log.debug("Suppression de la ressource ID: {}", resourceId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Ressource non trouvée avec l'ID: " + resourceId));

        // Vérifier que l'utilisateur est l'enseignant du sujet ou un administrateur
        if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Les étudiants ne peuvent pas supprimer des ressources");
        } else if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !resource.getTopic().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous ne pouvez supprimer que les ressources de vos propres sujets");
        }

        try {
            Path filePath = Paths.get(resource.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier physique", e);
        }

        resourceRepository.delete(resource);
        log.info("Ressource supprimée avec succès, ID: {}", resourceId);
    }
}
