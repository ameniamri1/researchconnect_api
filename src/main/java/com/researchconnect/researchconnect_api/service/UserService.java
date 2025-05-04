package com.researchconnect.researchconnect_api.service;

import com.researchconnect.researchconnect_api.dto.UserResponse;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        log.debug("Récupération de tous les utilisateurs");
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long userId) {
        log.debug("Récupération de l'utilisateur par ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, Map<String, Object> updates) {
        log.debug("Mise à jour de l'utilisateur ID: {}", userId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

        // Vérifier que l'utilisateur ne modifie que son propre profil ou est un
        // administrateur
        if (!currentUser.getId().equals(userId) &&
                currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cet utilisateur");
        }

        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }

        if (updates.containsKey("password")) {
            user.setPassword(passwordEncoder.encode((String) updates.get("password")));
        }

        // Seul un administrateur peut changer le rôle
        if (updates.containsKey("role") && currentUser.getRole() == User.Role.ROLE_ADMIN) {
            String role = (String) updates.get("role");
            user.setRole(User.Role.valueOf(role));
        }

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour avec succès, ID: {}", updatedUser.getId());

        return UserResponse.fromEntity(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Suppression de l'utilisateur ID: {}", userId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur actuel non trouvé"));

        // Seul un administrateur peut supprimer un compte
        if (currentUser.getRole() != User.Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Seul un administrateur peut supprimer des utilisateurs");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId);
        }

        userRepository.deleteById(userId);
        log.info("Utilisateur supprimé avec succès, ID: {}", userId);
    }

    public List<UserResponse> getUsersByRole(User.Role role) {
        log.debug("Récupération des utilisateurs par rôle: {}", role);
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
