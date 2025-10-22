package com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.interfaces.RSAPublicKey;

@Slf4j
@Component
public class JwtValidator {

    private final RestTemplate restTemplate;
    private final String jwksUrl;
    private JWKSet jwkSet;

    public JwtValidator(RestTemplate restTemplate, @Value("${fusion-auth.jwks-url:http://158.220.99.85:9011/.well-known/jwks.json}") String jwksUrl) {
        this.restTemplate = restTemplate;
        this.jwksUrl = jwksUrl;
        this.loadJwks();
    }

    /**
     * Validates a JWT token using the public keys from JWKS
     */
    public boolean validateToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);

            String keyId = decodedJWT.getKeyId();
            if (keyId == null) {
                log.warn("JWT token does not contain Key ID (kid)");
                return false;
            }

            JWK jwk = jwkSet.getKeyByKeyId(keyId);
            if (jwk == null) {
                log.warn("Key with ID {} not found in JWKS", keyId);
                return false;
            }

            if (!(jwk instanceof RSAKey)) {
                log.warn("Key is not an RSA key");
                return false;
            }

            RSAKey rsaKey = (RSAKey) jwk;
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWT.require(algorithm)
                    .build()
                    .verify(token);

            log.debug("JWT token validated successfully");
            return true;

        } catch (JWTVerificationException e) {
            log.warn("Error verifying JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error validating JWT token", e);
            return false;
        }
    }

    /**
     * Extracts the claims from a valid token
     */
    public DecodedJWT getDecodedToken(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            log.warn("Error decoding JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Loads the JWKS from the endpoint
     */
    private void loadJwks() {
        try {
            log.info("Loading JWKS from: {}", jwksUrl);
            String jwksContent = restTemplate.getForObject(jwksUrl, String.class);
            this.jwkSet = JWKSet.parse(jwksContent);
            log.info("JWKS loaded successfully with {} keys", jwkSet.getKeys().size());
        } catch (Exception e) {
            log.error("Error loading JWKS from {}: {}", jwksUrl, e.getMessage(), e);
            try {
                this.jwkSet = new JWKSet();
            } catch (Exception ex) {
                log.error("Error creating empty JWKSet", ex);
            }
        }
    }

}