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
     * Valida un token JWT usando las claves públicas del JWKS
     */
    public boolean validateToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);

            String keyId = decodedJWT.getKeyId();
            if (keyId == null) {
                log.warn("Token JWT no contiene Key ID (kid)");
                return false;
            }

            JWK jwk = jwkSet.getKeyByKeyId(keyId);
            if (jwk == null) {
                log.warn("Clave con ID {} no encontrada en JWKS", keyId);
                return false;
            }

            if (!(jwk instanceof RSAKey)) {
                log.warn("La clave no es una clave RSA");
                return false;
            }

            RSAKey rsaKey = (RSAKey) jwk;
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWT.require(algorithm)
                    .build()
                    .verify(token);

            log.debug("Token JWT validado exitosamente");
            return true;

        } catch (JWTVerificationException e) {
            log.warn("Error al verificar token JWT: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error inesperado al validar token JWT", e);
            return false;
        }
    }

    /**
     * Extrae los claims de un token válido
     */
    public DecodedJWT getDecodedToken(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            log.warn("Error al decodificar token JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Carga el JWKS desde el endpoint
     */
    private void loadJwks() {
        try {
            log.info("Cargando JWKS desde: {}", jwksUrl);
            String jwksContent = restTemplate.getForObject(jwksUrl, String.class);
            this.jwkSet = JWKSet.parse(jwksContent);
            log.info("JWKS cargado exitosamente con {} claves", jwkSet.getKeys().size());
        } catch (Exception e) {
            log.error("Error al cargar JWKS desde {}: {}", jwksUrl, e.getMessage(), e);
            try {
                this.jwkSet = new JWKSet();
            } catch (Exception ex) {
                log.error("Error al crear JWKSet vacío", ex);
            }
        }
    }

    /**
     * Recarga el JWKS (útil si necesitas refrescar las claves)
     */
    public void reloadJwks() {
        loadJwks();
    }
}