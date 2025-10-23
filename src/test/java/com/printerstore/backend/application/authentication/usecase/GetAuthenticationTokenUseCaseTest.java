package com.printerstore.backend.application.authentication.usecase;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.printerstore.backend.domain.authentication.gateway.AuthenticationGateway;
import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationRequestDto;
import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAuthenticationTokenUseCaseTest {

    @Mock
    private AuthenticationGateway authenticationGateway;

    @InjectMocks
    private GetAuthenticationTokenUseCase getAuthenticationTokenUseCase;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String VALID_TOKEN = createValidToken();
    private static final String EXPIRED_TOKEN = createExpiredToken();

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(authenticationGateway);
    }

    @Test
    void testSuccessfulTokenCacheHit() {
        // Arrange - First call to populate cache
        AuthenticationResponseDto.AuthenticationDataDto data = new AuthenticationResponseDto.AuthenticationDataDto(VALID_TOKEN);
        AuthenticationResponseDto response = new AuthenticationResponseDto(200, "Success", data);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(response);

        // First call to populate the cache
        getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);
        
        // Reset the mock to verify second call doesn't hit the gateway
        reset(authenticationGateway);

        // Act - Second call should hit cache
        String result = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);

        // Assert
        assertThat(result).isEqualTo(VALID_TOKEN);
        verifyNoInteractions(authenticationGateway);
    }

    @Test
    void testSuccessfulTokenFetchNew() {
        // Arrange
        AuthenticationResponseDto.AuthenticationDataDto data = new AuthenticationResponseDto.AuthenticationDataDto(VALID_TOKEN);
        AuthenticationResponseDto response = new AuthenticationResponseDto(200, "Success", data);
        
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(response);

        // Act
        String result = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);

        // Assert
        assertThat(result).isEqualTo(VALID_TOKEN);
        
        ArgumentCaptor<AuthenticationRequestDto> requestCaptor = ArgumentCaptor.forClass(AuthenticationRequestDto.class);
        verify(authenticationGateway).authenticate(requestCaptor.capture());
        
        AuthenticationRequestDto capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getUsername()).isEqualTo(USERNAME);
        assertThat(capturedRequest.getPassword()).isEqualTo(PASSWORD);
    }

    @Test
    void testAuthenticationSuccessfulResponseCode200() {
        // Arrange
        String tokenValue = "test-token-value";
        AuthenticationResponseDto.AuthenticationDataDto data = new AuthenticationResponseDto.AuthenticationDataDto(tokenValue);
        AuthenticationResponseDto response = new AuthenticationResponseDto(200, "Authentication successful", data);
        
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(response);

        // Act
        String result = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);

        // Assert
        assertThat(result).isEqualTo(tokenValue);
        verify(authenticationGateway, times(1)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testAuthenticationFailedWrongCredentials() {
        // Arrange
        AuthenticationResponseDto response = new AuthenticationResponseDto(401, "Invalid credentials", null);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> getAuthenticationTokenUseCase.execute(USERNAME, "wrongpassword"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Authentication failed: Invalid credentials");

        verify(authenticationGateway, times(1)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testAuthenticationNullResponseHandling() {
        // Arrange
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Authentication failed: null response");

        verify(authenticationGateway, times(1)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testTokenValidationWithExpiry() {
        // Arrange - First populate cache with expired token
        AuthenticationResponseDto.AuthenticationDataDto expiredData = new AuthenticationResponseDto.AuthenticationDataDto(EXPIRED_TOKEN);
        AuthenticationResponseDto expiredResponse = new AuthenticationResponseDto(200, "Success", expiredData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(expiredResponse);
        
        // First call to populate cache with expired token
        String firstResult = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);
        assertThat(firstResult).isEqualTo(EXPIRED_TOKEN);
        
        // Now setup for second call with valid token
        AuthenticationResponseDto.AuthenticationDataDto validData = new AuthenticationResponseDto.AuthenticationDataDto(VALID_TOKEN);
        AuthenticationResponseDto validResponse = new AuthenticationResponseDto(200, "Success", validData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(validResponse);

        // Act - Second call should authenticate again due to expired cached token
        String result = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);

        // Assert - Should authenticate again due to expired token
        assertThat(result).isEqualTo(VALID_TOKEN);
        verify(authenticationGateway, times(2)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testTokenValidationWithMalformedJWT() {
        // Arrange - First populate cache with malformed token
        String malformedToken = "invalid.jwt.token";
        AuthenticationResponseDto.AuthenticationDataDto malformedData = new AuthenticationResponseDto.AuthenticationDataDto(malformedToken);
        AuthenticationResponseDto malformedResponse = new AuthenticationResponseDto(200, "Success", malformedData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(malformedResponse);
        
        // First call to populate cache with malformed token
        String firstResult = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);
        assertThat(firstResult).isEqualTo(malformedToken);
        
        // Now setup for second call with valid token
        AuthenticationResponseDto.AuthenticationDataDto validData = new AuthenticationResponseDto.AuthenticationDataDto(VALID_TOKEN);
        AuthenticationResponseDto validResponse = new AuthenticationResponseDto(200, "Success", validData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(validResponse);

        // Act - Second call should authenticate again due to malformed cached token
        String result = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);

        // Assert - Should authenticate again due to invalid token
        assertThat(result).isEqualTo(VALID_TOKEN);
        verify(authenticationGateway, times(2)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testCacheMissExpiredToken() {
        // Arrange - First populate cache with expired token
        AuthenticationResponseDto.AuthenticationDataDto expiredData = new AuthenticationResponseDto.AuthenticationDataDto(EXPIRED_TOKEN);
        AuthenticationResponseDto expiredResponse = new AuthenticationResponseDto(200, "Success", expiredData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(expiredResponse);
        
        // First call to populate cache with expired token
        getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);
        
        // Now setup for second call with valid token
        AuthenticationResponseDto.AuthenticationDataDto validData = new AuthenticationResponseDto.AuthenticationDataDto(VALID_TOKEN);
        AuthenticationResponseDto validResponse = new AuthenticationResponseDto(200, "Success", validData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(validResponse);

        // Act - Second call should authenticate again due to expired cached token
        String result = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);

        // Assert
        assertThat(result).isEqualTo(VALID_TOKEN);
        
        // Verify that authentication was called twice - once for initial cache population and once for expired token refresh
        verify(authenticationGateway, times(2)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testTokenValidationWithNoExpirationDate() {
        // Arrange - Create token without expiration date
        String tokenWithoutExp = JWT.create()
                .withSubject("testuser")
                .sign(Algorithm.HMAC256("secret"));
        
        // First populate cache with token without expiration
        AuthenticationResponseDto.AuthenticationDataDto noExpData = new AuthenticationResponseDto.AuthenticationDataDto(tokenWithoutExp);
        AuthenticationResponseDto noExpResponse = new AuthenticationResponseDto(200, "Success", noExpData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(noExpResponse);
        
        // First call to populate cache
        getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);
        
        // Now setup for second call with valid token
        AuthenticationResponseDto.AuthenticationDataDto validData = new AuthenticationResponseDto.AuthenticationDataDto(VALID_TOKEN);
        AuthenticationResponseDto validResponse = new AuthenticationResponseDto(200, "Success", validData);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(validResponse);

        // Act - Second call should authenticate again because cached token has no expiration
        String result = getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD);

        // Assert - Should authenticate again because token has no expiration
        assertThat(result).isEqualTo(VALID_TOKEN);
        verify(authenticationGateway, times(2)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testAuthenticationFailedWithNonSuccessCode() {
        // Arrange
        AuthenticationResponseDto response = new AuthenticationResponseDto(500, "Internal server error", null);
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class))).thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> getAuthenticationTokenUseCase.execute(USERNAME, PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Authentication failed: Internal server error");

        verify(authenticationGateway, times(1)).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void testConcurrentCacheAccess() {
        // Arrange
        String user1 = "user1";
        String user2 = "user2";
        String token1 = createValidTokenForUser(user1);
        String token2 = createValidTokenForUser(user2);

        // Setup responses for both users using any() matchers and verify manually
        AuthenticationResponseDto.AuthenticationDataDto data1 = new AuthenticationResponseDto.AuthenticationDataDto(token1);
        AuthenticationResponseDto response1 = new AuthenticationResponseDto(200, "Success", data1);
        
        AuthenticationResponseDto.AuthenticationDataDto data2 = new AuthenticationResponseDto.AuthenticationDataDto(token2);
        AuthenticationResponseDto response2 = new AuthenticationResponseDto(200, "Success", data2);
        
        // Use simpler approach - return response1 first, then response2
        when(authenticationGateway.authenticate(any(AuthenticationRequestDto.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        // First calls to populate cache
        String firstResult1 = getAuthenticationTokenUseCase.execute(user1, "password1");
        String firstResult2 = getAuthenticationTokenUseCase.execute(user2, "password2");
        
        // Verify first calls returned correct tokens
        assertThat(firstResult1).isEqualTo(token1);
        assertThat(firstResult2).isEqualTo(token2);
        
        // Reset mock to verify cache hits
        reset(authenticationGateway);

        // Act - Second calls should hit cache
        String result1 = getAuthenticationTokenUseCase.execute(user1, "password1");
        String result2 = getAuthenticationTokenUseCase.execute(user2, "password2");

        // Assert - Should return cached tokens without calling gateway
        assertThat(result1).isEqualTo(token1);
        assertThat(result2).isEqualTo(token2);
        verifyNoInteractions(authenticationGateway);
    }

    // Helper methods for creating test tokens
    private static String createValidToken() {
        return JWT.create()
                .withSubject("testuser")
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256("secret"));
    }

    private static String createExpiredToken() {
        return JWT.create()
                .withSubject("testuser")
                .withExpiresAt(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256("secret"));
    }

    private static String createValidTokenForUser(String username) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .sign(Algorithm.HMAC256("secret"));
    }
}