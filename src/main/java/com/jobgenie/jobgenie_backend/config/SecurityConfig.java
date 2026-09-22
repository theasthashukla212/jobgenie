package com.jobgenie.jobgenie_backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                // JWT-based stateless API: CSRF protection is not used.
                .csrf(csrf -> csrf.disable())

                // Enable CORS through Spring Security.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .headers(headers ->
                        headers.frameOptions(frameOptions -> frameOptions.disable())
                )

                // Application uses JWTs, not HTTP sessions.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exceptions -> exceptions

                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":401,\"error\":\"Unauthorized\"," +
                                    "\"message\":\"Authentication is required\"," +
                                    "\"path\":\"" + request.getRequestURI() + "\"}"
                            );
                        })

                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"Forbidden\"," +
                                    "\"message\":\"You do not have permission to access this resource\"," +
                                    "\"path\":\"" + request.getRequestURI() + "\"}"
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow browser CORS preflight requests.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints.
                        .requestMatchers(
                                "/api/health",
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/github/callback",
                                "/h2-console/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Public job listing endpoints.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/jobs/**"
                        ).permitAll()

                        // Everything else requires authentication.
                        .anyRequest().authenticated()
                )

                // Preserve the existing custom AuthenticationProvider.
                .authenticationProvider(authenticationProvider)

                // Preserve the existing JWT filter.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        /*
         * app.cors.origins must contain ONLY the frontend origin.
         *
         * Correct:
         * https://jobgenie-frontend-q66b.onrender.com
         *
         * Incorrect:
         * https://jobgenie-frontend-q66b.onrender.com/api
         */
        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .toList()
        );

        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        configuration.setExposedHeaders(
                List.of("Authorization")
        );

        /*
         * Authentication uses JWT through:
         *
         * Authorization: Bearer <token>
         *
         * It does not use authentication cookies/session credentials,
         * so browser credentials are not required for CORS.
         */
        configuration.setAllowCredentials(false);

        // Cache successful preflight responses for 1 hour.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}