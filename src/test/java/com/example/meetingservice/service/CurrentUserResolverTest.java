package com.example.meetingservice.service;

import com.example.meetingservice.entity.UserRole;
import com.example.meetingservice.entity.UserStatus;
import com.example.meetingservice.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserResolverTest {

    @Mock
    private UserReadModelService userReadModelService;

    @Test
    void resolvesActiveUserByJwtSubject() {
        String keycloakSubject = "85584fe7-d51c-4ec3-b894-00e5be24b78b";
        UUID userId = UUID.randomUUID();
        when(userReadModelService.findUserByKeycloakSubject(keycloakSubject)).thenReturn(Optional.of(new UserProfile(
                userId,
                keycloakSubject,
                "denis",
                "denis@meeting.local",
                UserStatus.ACTIVE,
                UserRole.USER,
                0L,
                OffsetDateTime.now()
        )));

        CurrentUser currentUser = new CurrentUserResolver(userReadModelService).resolve(authentication(keycloakSubject));

        assertEquals(userId, currentUser.userId());
    }

    @Test
    void rejectsJwtSubjectMissingFromUserReadModel() {
        String keycloakSubject = "unknown-subject";
        when(userReadModelService.findUserByKeycloakSubject(keycloakSubject)).thenReturn(Optional.empty());

        assertThrows(
                ForbiddenException.class,
                () -> new CurrentUserResolver(userReadModelService).resolve(authentication(keycloakSubject))
        );
    }

    @Test
    void marksUserAsAdminWhenJwtHasAdminAuthority() {
        String keycloakSubject = "admin-subject";
        when(userReadModelService.findUserByKeycloakSubject(keycloakSubject)).thenReturn(Optional.of(new UserProfile(
                UUID.randomUUID(), keycloakSubject, "admin", "admin@meeting.local", UserStatus.ACTIVE,
                UserRole.ADMIN, 0L, OffsetDateTime.now()
        )));

        CurrentUser currentUser = new CurrentUserResolver(userReadModelService).resolve(
                new JwtAuthenticationToken(jwt(keycloakSubject), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        assertEquals(true, currentUser.admin());
    }

    private Jwt jwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .build();
    }

    private JwtAuthenticationToken authentication(String subject) {
        return new JwtAuthenticationToken(jwt(subject));
    }
}
