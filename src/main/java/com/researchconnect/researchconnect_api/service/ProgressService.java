package com.researchconnect.researchconnect_api.service;

import com.researchconnect.researchconnect_api.dto.ProgressRequest;
import com.researchconnect.researchconnect_api.dto.ProgressResponse;
import com.researchconnect.researchconnect_api.entity.Progress;
import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.repository.ProgressRepository;
import com.researchconnect.researchconnect_api.repository.TopicRepository;
import com.researchconnect.researchconnect_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public List<ProgressResponse> getProgressByStudent(Long studentId) {
        log.debug("Récupération des progressions pour l'étudiant ID: {}", studentId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé avec l'ID: " + studentId));

        // Vérifier que l'utilisateur peut voir ces progressions
        if (currentUser.getRole() == User.Role.ROLE_STUDENT &&
                !student.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous pouvez seulement voir vos propres progressions");
        }

        return progressRepository.findByStudent(student).stream()
                .map(ProgressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProgressResponse> getProgressByTopic(Long topicId) {
        log.debug("Récupération des progressions pour le sujet ID: {}", topicId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        // Vérifier que l'utilisateur peut voir ces progressions
        if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Les étudiants ne peuvent pas voir toutes les progressions pour un sujet");
        } else if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !topic.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous pouvez seulement voir les progressions pour vos propres sujets");
        }

        return progressRepository.findByTopic(topic).stream()
                .map(ProgressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgressResponse createOrUpdateProgress(ProgressRequest request) {
        log.debug("Création/mise à jour de la progression pour l'étudiant ID: {} et le sujet ID: {}",
                request.getStudentId(), request.getTopicId());

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + request.getTopicId()));

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Étudiant non trouvé avec l'ID: " + request.getStudentId()));

        // Vérifier que l'utilisateur est l'enseignant du sujet ou un administrateur
        if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Les étudiants ne peuvent pas mettre à jour les progressions");
        } else if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !topic.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Vous pouvez seulement mettre à jour les progressions pour vos propres sujets");
        }

        Optional<Progress> existingProgress = progressRepository.findByTopicAndStudent(topic, student);
        Progress progress;

        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
        } else {
            progress = new Progress();
            progress.setTopic(topic);
            progress.setStudent(student);
        }

        progress.setStatus(request.getStatus());
        progress.setCompletionPercentage(request.getCompletionPercentage());
        progress.setNotes(request.getNotes());

        Progress savedProgress = progressRepository.save(progress);
        log.info("Progression créée/mise à jour avec succès, ID: {}", savedProgress.getId());

        return ProgressResponse.fromEntity(savedProgress);
    }

    @Transactional
    public void deleteProgress(Long progressId) {
        log.debug("Suppression de la progression ID: {}", progressId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Progress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("Progression non trouvée avec l'ID: " + progressId));

        // Vérifier que l'utilisateur est l'enseignant du sujet ou un administrateur
        if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Les étudiants ne peuvent pas supprimer les progressions");
        } else if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !progress.getTopic().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous pouvez seulement supprimer les progressions pour vos propres sujets");
        }

        progressRepository.delete(progress);
        log.info("Progression supprimée avec succès, ID: {}", progressId);
    }
}
