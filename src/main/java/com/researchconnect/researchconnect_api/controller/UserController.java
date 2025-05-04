package com.researchconnect.researchconnect_api.controller;

import com.researchconnect.researchconnect_api.dto.UserResponse;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
public class UserController {
    
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Obtenir la liste des utilisateurs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un utilisateur spécifique")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Récupération de l'utilisateur avec l'ID: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        log.info("Mise à jour de l'utilisateur avec l'ID: {}", id);
        UserResponse updatedUser = userService.updateUser(id, updates);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Suppression de l'utilisateur avec l'ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teachers")
    @Operation(summary = "Obtenir la liste des enseignants")
    public ResponseEntity<List<UserResponse>> getAllTeachers() {
        log.info("Récupération de tous les enseignants");
        List<UserResponse> teachers = userService.getUsersByRole(User.Role.ROLE_TEACHER);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/students")
    @Operation(summary = "Obtenir la liste des étudiants")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllStudents() {
        log.info("Récupération de tous les étudiants");
        List<UserResponse> students = userService.getUsersByRole(User.Role.ROLE_STUDENT);
        return ResponseEntity.ok(students);
    }
}
