package com.learningpath.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Bean
    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse errorResponse = new ErrorResponse(
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Unauthorized",
                                    authException.getMessage() != null ? authException.getMessage() : "Full authentication is required to access this resource"
                            );
                            objectMapper.writeValue(response.getOutputStream(), errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse errorResponse = new ErrorResponse(
                                    HttpStatus.FORBIDDEN.value(),
                                    "Forbidden",
                                    "You are not authorized to access or modify another user's data"
                            );
                            objectMapper.writeValue(response.getOutputStream(), errorResponse);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Public Auth Endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                        // Public Catalogs & Health
                        .requestMatchers(HttpMethod.GET, "/api/careers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/skills/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/skills/dependencies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/learning-paths/generate").permitAll()

                        // User-Specific Endpoints — protected with cross-user authorization
                        .requestMatchers("/api/users/{userId}/**").access((authentication, context) -> {
                            String userIdStr = context.getVariables().get("userId");
                            boolean authorized = false;
                            if (userIdStr != null && authentication.get() != null) {
                                try {
                                    java.util.UUID targetUserId = java.util.UUID.fromString(userIdStr);
                                    Object principal = authentication.get().getPrincipal();
                                    if (principal instanceof UserPrincipal up) {
                                        authorized = up.getId().equals(targetUserId);
                                    }
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                            return new org.springframework.security.authorization.AuthorizationDecision(authorized);
                        })
                        .requestMatchers("/api/learning-paths/users/{userId}/**").access((authentication, context) -> {
                            String userIdStr = context.getVariables().get("userId");
                            boolean authorized = false;
                            if (userIdStr != null && authentication.get() != null) {
                                try {
                                    java.util.UUID targetUserId = java.util.UUID.fromString(userIdStr);
                                    Object principal = authentication.get().getPrincipal();
                                    if (principal instanceof UserPrincipal up) {
                                        authorized = up.getId().equals(targetUserId);
                                    }
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                            return new org.springframework.security.authorization.AuthorizationDecision(authorized);
                        })

                        // Default user admin / general endpoints
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Allow onboarding user creation
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll()

                        // Any other request permit or authenticated
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
