package com.printerstore.backend.domain.authentication.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AuthenticationResponseDtoTest {

    @Test
    void testAuthenticationResponseDto_AllArgsConstructor() {
        // Arrange
        Integer responseCode = 200;
        String responseMessage = "Authentication successful";
        AuthenticationResponseDto.AuthenticationDataDto data = new AuthenticationResponseDto.AuthenticationDataDto("test-token");

        // Act
        AuthenticationResponseDto dto = new AuthenticationResponseDto(responseCode, responseMessage, data);

        // Assert
        assertThat(dto.getResponseCode()).isEqualTo(responseCode);
        assertThat(dto.getResponseMessage()).isEqualTo(responseMessage);
        assertThat(dto.getData()).isNotNull();
        assertThat(dto.getData().getToken()).isEqualTo("test-token");
    }

    @Test
    void testAuthenticationResponseDto_NoArgsConstructor() {
        // Act
        AuthenticationResponseDto dto = new AuthenticationResponseDto();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getResponseCode()).isNull();
        assertThat(dto.getResponseMessage()).isNull();
        assertThat(dto.getData()).isNull();
    }

    @Test
    void testAuthenticationResponseDto_Setters() {
        // Arrange
        AuthenticationResponseDto dto = new AuthenticationResponseDto();
        AuthenticationResponseDto.AuthenticationDataDto data = new AuthenticationResponseDto.AuthenticationDataDto();

        // Act
        dto.setResponseCode(201);
        dto.setResponseMessage("Created");
        data.setToken("new-token");
        dto.setData(data);

        // Assert
        assertThat(dto.getResponseCode()).isEqualTo(201);
        assertThat(dto.getResponseMessage()).isEqualTo("Created");
        assertThat(dto.getData().getToken()).isEqualTo("new-token");
    }

    @Test
    void testAuthenticationDataDto_AllArgsConstructor() {
        // Act
        AuthenticationResponseDto.AuthenticationDataDto data = 
                new AuthenticationResponseDto.AuthenticationDataDto("token-123");

        // Assert
        assertThat(data.getToken()).isEqualTo("token-123");
    }

    @Test
    void testAuthenticationDataDto_NoArgsConstructor() {
        // Act
        AuthenticationResponseDto.AuthenticationDataDto data = new AuthenticationResponseDto.AuthenticationDataDto();

        // Assert
        assertThat(data).isNotNull();
        assertThat(data.getToken()).isNull();
    }

    @Test
    void testAuthenticationDataDto_Setter() {
        // Arrange
        AuthenticationResponseDto.AuthenticationDataDto data = new AuthenticationResponseDto.AuthenticationDataDto();

        // Act
        data.setToken("updated-token");

        // Assert
        assertThat(data.getToken()).isEqualTo("updated-token");
    }

    @Test
    void testAuthenticationResponseDto_Equality() {
        // Arrange
        AuthenticationResponseDto.AuthenticationDataDto data1 = new AuthenticationResponseDto.AuthenticationDataDto("token");
        AuthenticationResponseDto dto1 = new AuthenticationResponseDto(200, "Success", data1);

        AuthenticationResponseDto.AuthenticationDataDto data2 = new AuthenticationResponseDto.AuthenticationDataDto("token");
        AuthenticationResponseDto dto2 = new AuthenticationResponseDto(200, "Success", data2);

        // Assert
        assertThat(dto1).isEqualTo(dto2);
    }
}