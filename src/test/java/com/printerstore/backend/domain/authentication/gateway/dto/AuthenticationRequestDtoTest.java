package com.printerstore.backend.domain.authentication.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AuthenticationRequestDtoTest {

    @Test
    void testAuthenticationRequestDto_AllArgsConstructor() {
        // Arrange
        String username = "testuser";
        String password = "testpass";

        // Act
        AuthenticationRequestDto dto = new AuthenticationRequestDto(username, password);

        // Assert
        assertThat(dto.getUsername()).isEqualTo(username);
        assertThat(dto.getPassword()).isEqualTo(password);
    }

    @Test
    void testAuthenticationRequestDto_NoArgsConstructor() {
        // Act
        AuthenticationRequestDto dto = new AuthenticationRequestDto();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getUsername()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void testAuthenticationRequestDto_Setters() {
        // Arrange
        AuthenticationRequestDto dto = new AuthenticationRequestDto();

        // Act
        dto.setUsername("newuser");
        dto.setPassword("newpass");

        // Assert
        assertThat(dto.getUsername()).isEqualTo("newuser");
        assertThat(dto.getPassword()).isEqualTo("newpass");
    }

    @Test
    void testAuthenticationRequestDto_Equality() {
        // Arrange
        AuthenticationRequestDto dto1 = new AuthenticationRequestDto("user1", "pass1");
        AuthenticationRequestDto dto2 = new AuthenticationRequestDto("user1", "pass1");
        AuthenticationRequestDto dto3 = new AuthenticationRequestDto("user2", "pass2");

        // Assert
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
    }
}