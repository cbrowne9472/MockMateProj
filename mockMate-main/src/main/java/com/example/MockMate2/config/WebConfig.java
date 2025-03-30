package com.example.MockMate2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 * 
 * This configuration class handles Cross-Origin Resource Sharing (CORS)
 * settings.
 * It allows the frontend application (running on localhost:3000) to make
 * requests
 * to the backend API endpoints.
 * 
 * CORS is a security feature implemented by browsers that restricts web pages
 * from making
 * requests to a different domain than the one that served the original page.
 * 
 * This configuration:
 * 1. Enables all routes to be accessible from the frontend origin
 * 2. Allows common HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
 * 3. Enables credentials to be included in cross-origin requests
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mappings for the application
     * 
     * @param registry The CorsRegistry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all paths
                .allowedOrigins("http://localhost:3000") // Frontend application URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true); // Allow cookies and authentication headers
    }
}