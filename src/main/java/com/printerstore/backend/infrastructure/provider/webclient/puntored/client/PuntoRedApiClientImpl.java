package com.printerstore.backend.infrastructure.provider.webclient.puntored.client;

import com.printerstore.backend.configuration.webclient.PuntoRedProperties;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PuntoRedApiClientImpl implements PuntoRedApiClient {

    private static final String AUTHENTICATE_ENDPOINT = "/v1/authenticate";
    private static final String PAYMENT_ENDPOINT = "/v1/payment";
    private static final String CANCEL_PAYMENT_ENDPOINT = "/v1/payment/cancel";

    private final RestTemplate restTemplate;
    private final PuntoRedProperties puntoRedProperties;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authRequest) {
        try {
            String url = puntoRedProperties.getUrl() + AUTHENTICATE_ENDPOINT;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AuthenticationRequest> request = new HttpEntity<>(authRequest, headers);
            
            log.info("Enviando solicitud de autenticación a: {}", url);
            
            AuthenticationResponse response = restTemplate.postForObject(
                    url,
                    request,
                    AuthenticationResponse.class
            );
            
            log.info("Autenticación exitosa. Código de respuesta: {}", 
                    response != null ? response.getResponseCode() : null);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al autenticar con Punto Red", e);
            throw new RuntimeException("Error de autenticación con Punto Red: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentResponse payment(PaymentRequest paymentRequest, String token) {
        try {
            String url = puntoRedProperties.getUrl() + PAYMENT_ENDPOINT;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String authHeader = "Bearer " + token;
            headers.set("Authorization", authHeader);
            
            HttpEntity<PaymentRequest> request = new HttpEntity<>(paymentRequest, headers);
            
            log.info("Enviando solicitud de pago a: {}. ExternalId: {}, Amount: {}", 
                    url, paymentRequest.getExternalId(), paymentRequest.getAmount());
            log.debug("Header Authorization enviado: {}", authHeader.substring(0, Math.min(50, authHeader.length())) + "...");
            
            PaymentResponse response = restTemplate.postForObject(
                    url,
                    request,
                    PaymentResponse.class
            );
            
            log.info("Pago procesado. Código de respuesta: {}, PaymentId: {}", 
                    response != null ? response.getResponseCode() : null,
                    response != null && response.getData() != null ? response.getData().getPaymentId() : null);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al procesar pago con Punto Red", e);
            throw new RuntimeException("Error al procesar pago con Punto Red: " + e.getMessage(), e);
        }
    }

    @Override
    public CancelPaymentResponse cancelPayment(CancelPaymentRequest cancelPaymentRequest, String token) {
        try {
            String url = puntoRedProperties.getUrl() + CANCEL_PAYMENT_ENDPOINT;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String authHeader = "Bearer " + token;
            headers.set("Authorization", authHeader);
            
            HttpEntity<CancelPaymentRequest> request = new HttpEntity<>(cancelPaymentRequest, headers);
            
            log.info("Enviando solicitud de cancelación a: {}. Reference: {}", 
                    url, cancelPaymentRequest.getReference());
            log.debug("Header Authorization enviado: {}", authHeader.substring(0, Math.min(50, authHeader.length())) + "...");
            
            ResponseEntity<CancelPaymentResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    CancelPaymentResponse.class
            );
            
            CancelPaymentResponse response = responseEntity.getBody();
            
            log.info("Cancelación procesada. Código de respuesta: {}, PaymentId: {}", 
                    response != null ? response.getResponseCode() : null,
                    response != null && response.getData() != null ? response.getData().getPaymentId() : null);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error al cancelar pago con Punto Red", e);
            throw new RuntimeException("Error al cancelar pago con Punto Red: " + e.getMessage(), e);
        }
    }
}