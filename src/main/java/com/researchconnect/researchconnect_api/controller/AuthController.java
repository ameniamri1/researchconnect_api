package com.researchconnect.researchconnect_api.controller;

import com.researchconnect.researchconnect_api.dto.AuthRequest;
import com.researchconnect.researchconnect_api.dto.JwtResponse;
import com.researchconnect.researchconnect_api.dto.SignupRequest;
import com.researchconnect.researchconnect_api.dto.UserResponse;
import com.researchconnect.researchconnect_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "API d'authentification")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authentifier un utilisateur")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("Tentative de connexion pour: {}", request.getEmail());
        JwtResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Inscrire un nouvel utilisateur")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody SignupRequest request) {
        log.info("Tentative d'inscription pour: {}", request.getEmail());
        UserResponse newUser = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir les informations de l'utilisateur connecté")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Récupération des informations de l'utilisateur connecté");
        UserResponse user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
}
