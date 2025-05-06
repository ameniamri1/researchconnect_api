package com.researchconnect.researchconnect_api.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permet les requêtes depuis l'origine de votre application React
        config.addAllowedOrigin("*"); // Pour le développement. En production, spécifiez l'URL exacte
        
        // Autorise les méthodes HTTP courantes
        //config.addAllowedMethod("GET");
        //config.addAllowedMethod("POST");
        //config.addAllowedMethod("PUT");
        //config.addAllowedMethod("DELETE");
        //config.addAllowedMethod("OPTIONS");
        
        // Autorise tous les en-têtes (headers)
        //config.addAllowedHeader("*");
        
        // Permet l'envoi de cookies (important si vous utilisez l'authentification basée sur les sessions)
        ////config.setAllowCredentials(true);
        
        // Durée de mise en cache de la pré-vérification CORS (en secondes)
        //config.setMaxAge(3600L);
        
        // Applique cette configuration à toutes les routes
        //source.registerCorsConfiguration("/**", config);
        
        //return new CorsFilter(source);
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        
        // Autorise les méthodes HTTP courantes
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Autorise tous les en-têtes (headers)
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"));
        
        // Permet l'exposition des en-têtes de réponse
        config.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        
        // Permet l'envoi de cookies
        config.setAllowCredentials(true);
        
        // Durée de mise en cache de la pré-vérification CORS (en secondes)
        config.setMaxAge(3600L);
        
        // Applique cette configuration à toutes les routes
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
    
