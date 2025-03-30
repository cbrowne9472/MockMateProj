package com.example.MockMate2;

import com.example.MockMate2.models.AppUser;
import com.example.MockMate2.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AppUserRepository userRepository;

    @GetMapping("/status")
    public Map<String, Object> status(@AuthenticationPrincipal OidcUser user) {
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            String auth0Id = user.getSubject();

            System.out.println(auth0Id);

            // Check if user already exists, if not — save them
            userRepository.findByAuth0Id(auth0Id).orElseGet(() -> {
                AppUser newUser = new AppUser(
                        auth0Id,
                        user.getFullName(),
                        user.getEmail(),
                        user.getPicture()
                );
                return userRepository.save(newUser);
            });

            response.put("authenticated", true);
            response.put("name", user.getFullName());
            response.put("email", user.getEmail());
            response.put("picture", user.getPicture());
            response.put("auth0Id", auth0Id);
        } else {
            response.put("authenticated", false);
        }

        return response;
    }

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws Exception {
        response.sendRedirect("/oauth2/authorization/auth0");
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response,
                       @RequestParam(defaultValue = "http://localhost:8080/") String returnTo) throws Exception {
        String clientId = "OyZehXTUEVfmnXh3TSXHJyxp0L8PXl9E";
        String domain = "https://dev-x36qv2ykoznt8o45.us.auth0.com";
        String logoutUrl = domain + "/v2/logout?client_id=" + clientId + "&returnTo=" + returnTo;

        response.sendRedirect(logoutUrl);
    }
}

