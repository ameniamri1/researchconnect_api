package com.researchconnect.researchconnect_api.service;

import com.researchconnect.researchconnect_api.dto.TopicRequest;
import com.researchconnect.researchconnect_api.dto.TopicResponse;
import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.repository.TopicRepository;
import com.researchconnect.researchconnect_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public Page<TopicResponse> getAllTopics(String category, String keyword, Pageable pageable) {
        log.debug("Récupération des sujets avec filtres - catégorie: {}, mot-clé: {}", category, keyword);
        return topicRepository.findByFilters(category, keyword, pageable)
                .map(TopicResponse::fromEntity);
    }

    public TopicResponse getTopicById(Long topicId) {
        log.debug("Récupération du sujet par ID: {}", topicId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));
        return TopicResponse.fromEntity(topic);
    }

    @Transactional
    public TopicResponse createTopic(TopicRequest request) {
        log.debug("Création d'un nouveau sujet: {}", request.getTitle());

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        // Vérifier que l'utilisateur est un enseignant
        if (currentUser.getRole() != User.Role.ROLE_TEACHER &&
                currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Seuls les enseignants peuvent créer des sujets");
        }

        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setTeacher(currentUser);
        topic.setCategory(request.getCategory());
        topic.setPrerequisites(request.getPrerequisites());
        topic.setDeadline(request.getDeadline());
        topic.setContact(request.getContact());

        Topic savedTopic = topicRepository.save(topic);
        log.info("Nouveau sujet créé avec succès, ID: {}", savedTopic.getId());

        return TopicResponse.fromEntity(savedTopic);
    }

    @Transactional
    public TopicResponse updateTopic(Long topicId, TopicRequest request) {
        log.debug("Mise à jour du sujet ID: {}", topicId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        // Vérifier que l'utilisateur est le créateur du sujet ou un administrateur
        if (!topic.getTeacher().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce sujet");
        }

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setCategory(request.getCategory());
        topic.setPrerequisites(request.getPrerequisites());
        topic.setDeadline(request.getDeadline());
        topic.setContact(request.getContact());

        Topic updatedTopic = topicRepository.save(topic);
        log.info("Sujet mis à jour avec succès, ID: {}", updatedTopic.getId());

        return TopicResponse.fromEntity(updatedTopic);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        log.debug("Suppression du sujet ID: {}", topicId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        // Vérifier que l'utilisateur est le créateur du sujet ou un administrateur
        if (!topic.getTeacher().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer ce sujet");
        }

        topicRepository.delete(topic);
        log.info("Sujet supprimé avec succès, ID: {}", topicId);
    }

    public Page<TopicResponse> getTopicsByTeacher(Long teacherId, Pageable pageable) {
        log.debug("Récupération des sujets par enseignant ID: {}", teacherId);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé avec l'ID: " + teacherId));

        return topicRepository.findByTeacher(teacher, pageable)
                .map(TopicResponse::fromEntity);
    }

    public List<TopicResponse> getTopicsByCurrentTeacher() {
        log.debug("Récupération des sujets de l'enseignant actuel");

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        if (currentUser.getRole() != User.Role.ROLE_TEACHER &&
                currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Seuls les enseignants ont des sujets publiés");
        }

        return topicRepository.findByTeacher(currentUser).stream()
                .map(TopicResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
