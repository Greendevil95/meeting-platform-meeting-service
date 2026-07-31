package com.example.meetingservice.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("meeting-service");

    @Test
    void acceptsTokenWhenAudienceContainsMeetingService() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("keycloak-subject")
                .audience(List.of("account", "meeting-service"))
                .build();

        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenWhenAudienceDoesNotContainMeetingService() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("keycloak-subject")
                .audience(List.of("account", "user-service"))
                .build();

        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }
}
