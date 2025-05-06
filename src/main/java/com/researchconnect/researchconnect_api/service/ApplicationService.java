package com.researchconnect.researchconnect_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.researchconnect.researchconnect_api.dto.ApplicationRequest;
import com.researchconnect.researchconnect_api.dto.ApplicationResponse;
import com.researchconnect.researchconnect_api.dto.ApplicationStatusUpdateRequest;
import com.researchconnect.researchconnect_api.entity.Application;
import com.researchconnect.researchconnect_api.entity.Topic;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.repository.ApplicationRepository;
import com.researchconnect.researchconnect_api.repository.TopicRepository;
import com.researchconnect.researchconnect_api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public List<ApplicationResponse> getAllApplications() {
        log.debug("Récupération de toutes les candidatures");
    
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));
    
        return switch (currentUser.getRole()) {
            case ROLE_ADMIN -> applicationRepository.findAll().stream()
                    .map(ApplicationResponse::fromEntity)
                    .collect(Collectors.toList());
    
            case ROLE_TEACHER -> applicationRepository.findAll().stream()
                    .filter(app -> app.getTopic().getTeacher().getId().equals(currentUser.getId()))
                    .map(ApplicationResponse::fromEntity)
                    .collect(Collectors.toList());
    
            case ROLE_STUDENT -> applicationRepository.findByStudent(currentUser).stream()
                    .map(ApplicationResponse::fromEntity)
                    .collect(Collectors.toList());
    
            default -> throw new IllegalStateException("Rôle utilisateur non supporté : " + currentUser.getRole());
        };
    }
    

    public ApplicationResponse getApplicationById(Long applicationId) {
        log.debug("Récupération de la candidature par ID: {}", applicationId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature non trouvée avec l'ID: " + applicationId));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        // Vérifier que l'utilisateur peut voir cette candidature
        if (currentUser.getRole() == User.Role.ROLE_STUDENT &&
                !application.getStudent().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à voir cette candidature");
        } else if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !application.getTopic().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à voir cette candidature");
        }

        return ApplicationResponse.fromEntity(application);
    }

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        log.debug("Création d'une nouvelle candidature pour le sujet ID: {}", request.getTopicId());

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        // Vérifier que l'utilisateur est un étudiant
        if (currentUser.getRole() != User.Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Seuls les étudiants peuvent postuler à des sujets");
        }

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + request.getTopicId()));

        // Vérifier si l'étudiant a déjà postulé à ce sujet
        if (applicationRepository.findByTopicAndStudent(topic, currentUser).isPresent()) {
            throw new IllegalArgumentException("Vous avez déjà postulé à ce sujet");
        }

        Application application = new Application();
        application.setTopic(topic);
        application.setStudent(currentUser);
        application.setMessage(request.getMessage());
        application.setStatus(Application.Status.PENDING);

        Application savedApplication = applicationRepository.save(application);
        log.info("Nouvelle candidature créée avec succès, ID: {}", savedApplication.getId());

        return ApplicationResponse.fromEntity(savedApplication);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequest request) {
        log.debug("Mise à jour du statut de la candidature ID: {} à {}", applicationId, request.getStatus());

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature non trouvée avec l'ID: " + applicationId));

        // Vérifier que l'utilisateur est l'enseignant du sujet ou un administrateur
        if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !application.getTopic().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette candidature");
        } else if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Les étudiants ne peuvent pas modifier le statut des candidatures");
        }

        application.setStatus(request.getStatus());
        application.setTeacherFeedback(request.getFeedback());
        application.setResponseDate(LocalDateTime.now());

        Application updatedApplication = applicationRepository.save(application);
        log.info("Statut de candidature mis à jour avec succès, ID: {}", updatedApplication.getId());

        return ApplicationResponse.fromEntity(updatedApplication);
    }

    @Transactional
    public void deleteApplication(Long applicationId) {
        log.debug("Suppression de la candidature ID: {}", applicationId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Candidature non trouvée avec l'ID: " + applicationId));

        // Vérifier que l'utilisateur peut supprimer cette candidature
        if (currentUser.getRole() == User.Role.ROLE_STUDENT &&
                !application.getStudent().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer cette candidature");
        } else if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !application.getTopic().getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer cette candidature");
        }

        applicationRepository.delete(application);
        log.info("Candidature supprimée avec succès, ID: {}", applicationId);
    }

    public List<ApplicationResponse> getApplicationsByTopic(Long topicId) {
        log.debug("Récupération des candidatures pour le sujet ID: {}", topicId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Sujet non trouvé avec l'ID: " + topicId));

        // Vérifier que l'utilisateur est l'enseignant du sujet ou un administrateur
        if (currentUser.getRole() == User.Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Les étudiants ne peuvent pas voir toutes les candidatures pour un sujet");
        } else if (currentUser.getRole() == User.Role.ROLE_TEACHER &&
                !topic.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous pouvez seulement voir les candidatures pour vos propres sujets");
        }

        return applicationRepository.findByTopic(topic).stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsByStudent(Long studentId) {
        log.debug("Récupération des candidatures pour l'étudiant ID: {}", studentId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant non trouvé avec l'ID: " + studentId));

        // Vérifier que l'utilisateur peut voir ces candidatures
        if (currentUser.getRole() == User.Role.ROLE_STUDENT &&
                !student.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous pouvez seulement voir vos propres candidatures");
        }

        return applicationRepository.findByStudent(student).stream()
                .map(ApplicationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
