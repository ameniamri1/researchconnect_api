package com.researchconnect.researchconnect_api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.researchconnect.researchconnect_api.dto.AuthRequest;
import com.researchconnect.researchconnect_api.dto.JwtResponse;
import com.researchconnect.researchconnect_api.dto.SignupRequest;
import com.researchconnect.researchconnect_api.dto.UserResponse;
import com.researchconnect.researchconnect_api.entity.User;
import com.researchconnect.researchconnect_api.repository.UserRepository;
import com.researchconnect.researchconnect_api.security.JwtTokenProvider;
import com.researchconnect.researchconnect_api.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public JwtResponse authenticate(AuthRequest request) {
        log.info("Tentative d'authentification pour l'utilisateur: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse("");

        log.info("Utilisateur authentifié avec succès: {}", request.getEmail());
        return new JwtResponse(jwt, userDetails.getId(), userDetails.getName(), userDetails.getUsername(), role);
    }

    @Transactional
    public UserResponse register(SignupRequest request) {
        log.info("Tentative d'inscription pour l'utilisateur: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email déjà utilisé: {}", request.getEmail());
            throw new IllegalArgumentException("Cet email est déjà utilisé!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : User.Role.ROLE_STUDENT);

        User savedUser = userRepository.save(user);
        log.info("Nouvel utilisateur enregistré avec succès: {}", savedUser.getEmail());

        return UserResponse.fromEntity(savedUser);
    }

    public UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            return UserResponse.fromEntity(user);
        }

        throw new IllegalStateException("Utilisateur non authentifié");
    }
}
