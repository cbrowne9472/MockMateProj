package com.example.MockMate2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 * 
 * This class configures Spring Security for the application.
 * It defines:
 * 1. Which endpoints require authentication and which are public
 * 2. OAuth2 login configuration
 * 3. CSRF protection settings
 * 4. Logout behavior
 * 
 * The security configuration balances protection of sensitive endpoints
 * while allowing public access to specific API endpoints needed by the
 * frontend.
 */
@Configuration
public class SecurityConfig {

    private final CustomOAuth2SuccessHandler successHandler;

    /**
     * Constructor injection of the custom OAuth2 success handler
     * 
     * @param successHandler Handler for successful OAuth2 authentication
     */
    public SecurityConfig(CustomOAuth2SuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    /**
     * Configures the security filter chain
     * 
     * @param http The HttpSecurity to configure
     * @return The configured SecurityFilterChain
     * @throws Exception If configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection for API endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/error", "/interview/**", "/transcribe").permitAll() // Public
                                                                                                           // endpoints
                        .anyRequest().authenticated() // All other endpoints require authentication
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler) // Custom handler for successful login
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // Redirect to home page after logout
                        .permitAll() // Allow anyone to access logout
                );

        return http.build();
    }
}
