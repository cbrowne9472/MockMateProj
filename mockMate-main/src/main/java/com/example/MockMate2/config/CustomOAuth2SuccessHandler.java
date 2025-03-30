package com.example.MockMate2.config;

import com.example.MockMate2.models.AppUser;
import com.example.MockMate2.repository.AppUserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository userRepository;

    public CustomOAuth2SuccessHandler(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        String auth0Id = user.getSubject();

        // Save user if not already in MongoDB
        userRepository.findByAuth0Id(auth0Id).orElseGet(() -> {
            AppUser newUser = new AppUser(
                    auth0Id,
                    user.getFullName(),
                    user.getEmail(),
                    user.getPicture()
            );
            return userRepository.save(newUser);
        });

        // Redirect after login
        response.sendRedirect("/auth/status");
    }
}

