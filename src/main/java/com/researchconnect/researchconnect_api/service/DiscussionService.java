package com.researchconnect.researchconnect_api.service;

import com.researchconnect.researchconnect_api.dto.DiscussionRequest;
import com.researchconnect.researchconnect_api.dto.DiscussionResponse;
import com.researchconnect.researchconnect_api.entity.Discussion;
import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.repository.DiscussionRepository;
import com.researchconnect.researchconnect_api.repository.TopicRepository;
import com.researchconnect.researchconnect_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public List<DiscussionResponse> getDiscussionsByTopic(Long topicId) {
        log.debug("Récupération des discussions pour le sujet ID: {}", topicId);

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        return discussionRepository.findByTopicOrderByCreatedAtDesc(topic).stream()
                .map(DiscussionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscussionResponse addDiscussion(Long topicId, DiscussionRequest request) {
        log.debug("Ajout d'une nouvelle discussion au sujet ID: {}", topicId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        Discussion discussion = new Discussion();
        discussion.setTopic(topic);
        discussion.setUser(currentUser);
        discussion.setMessage(request.getMessage());

        Discussion savedDiscussion = discussionRepository.save(discussion);
        log.info("Nouvelle discussion ajoutée avec succès, ID: {}", savedDiscussion.getId());

        return DiscussionResponse.fromEntity(savedDiscussion);
    }

    @Transactional
    public DiscussionResponse updateDiscussion(Long discussionId, DiscussionRequest request) {
        log.debug("Mise à jour de la discussion ID: {}", discussionId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion non trouvée avec l'ID: " + discussionId));

        // Vérifier que l'utilisateur est l'auteur de la discussion
        if (!discussion.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette discussion");
        }

        discussion.setMessage(request.getMessage());

        Discussion updatedDiscussion = discussionRepository.save(discussion);
        log.info("Discussion mise à jour avec succès, ID: {}", updatedDiscussion.getId());

        return DiscussionResponse.fromEntity(updatedDiscussion);
    }

    @Transactional
    public void deleteDiscussion(Long discussionId) {
        log.debug("Suppression de la discussion ID: {}", discussionId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion non trouvée avec l'ID: " + discussionId));

        // Vérifier que l'utilisateur est l'auteur de la discussion ou un administrateur
        if (!discussion.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer cette discussion");
        }

        discussionRepository.delete(discussion);
        log.info("Discussion supprimée avec succès, ID: {}", discussionId);
    }
}
