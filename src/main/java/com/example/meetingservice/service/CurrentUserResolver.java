package com.example.meetingservice.service;

import com.example.meetingservice.entity.UserStatus;
import com.example.meetingservice.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserReadModelService userReadModelService;

    @Transactional(readOnly = true)
    public CurrentUser resolve(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new ForbiddenException("error.authentication.unsupported", "JWT authentication is required");
        }

        UserProfile user = userReadModelService.findUserByKeycloakSubject(jwtAuthentication.getToken().getSubject())
                .orElseThrow(() -> new ForbiddenException(
                        "error.authenticated.user.not.provisioned",
                        "Authenticated user is not provisioned in meeting-service"
                ));

        if (user.status() != UserStatus.ACTIVE) {
            throw new ForbiddenException(
                    "error.authenticated.user.inactive",
                    "Authenticated user is not active"
            );
        }

        boolean admin = jwtAuthentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return new CurrentUser(user.userId(), admin);
    }
}
