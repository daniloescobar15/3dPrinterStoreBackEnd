package com.printerstore.backend.infrastructure.adapter.in.web;

import com.printerstore.backend.application.payment.port.PaymentServicePort;
import com.printerstore.backend.application.payment.dto.ProcessPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client.FusionAuthApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentCallbackRequest;
import com.printerstore.backend.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.printerstore.backend.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentRestController {

    private final PaymentServicePort paymentService;
    private final FusionAuthApiClient fusionAuthApiClient;

    /**
     * Endpoint para procesar un pago
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(
            @RequestBody ProcessPaymentRequest paymentRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Recibida solicitud de pago para externalId: {}", paymentRequest.getExternalId());
            
            String userId = extractAndValidateToken(authHeader, paymentRequest.getExternalId());
            if (userId == null) {
                return buildUnauthorizedResponse();
            }
            
            var response = paymentService.processPayment(paymentRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error en procesamiento de pago", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error al procesar el pago")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    /**
     * Endpoint para obtener todos los pagos del usuario autenticado
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserPayments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Solicitud para listar pagos del usuario");
            
            String userId = extractAndValidateToken(authHeader, null);
            if (userId == null) {
                return buildUnauthorizedResponse();
            }
            
            List<Payment> payments = paymentService.getUserPayments(userId);
            log.info("Se retornaron {} pagos para userId: {}", payments.size(), userId);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error al obtener lista de pagos", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error al obtener la lista de pagos")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    /**
     * Endpoint para cancelar un pago
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(
            @RequestParam String reference,
            @RequestParam String cancelDescription,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Recibida solicitud de cancelación de pago para reference: {}", reference);
            
            String userId = extractAndValidateToken(authHeader, null);
            if (userId == null) {
                return buildUnauthorizedResponse();
            }
            
            var response = paymentService.cancelPayment(reference, cancelDescription);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (Exception e) {
            log.error("Error en cancelación de pago", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error al cancelar el pago")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    /**
     * Endpoint callback para recibir actualizaciones de estado de pagos desde Punto Red
     * No requiere autenticación JWT
     */
    @PostMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestBody PaymentCallbackRequest callbackRequest) {
        try {
            log.info("Recibido callback de pago. Reference: {}, ExternalId: {}, PaymentId: {}, Status: {}",
                    callbackRequest.getReference(), callbackRequest.getExternalId(), 
                    callbackRequest.getPaymentId(), callbackRequest.getStatus());
            
            paymentService.updatePaymentCallback(
                    callbackRequest.getPaymentId(),
                    callbackRequest.getExternalId(),
                    callbackRequest.getAmount(),
                    callbackRequest.getAuthorizationNumber(),
                    callbackRequest.getReference(),
                    callbackRequest.getPaymentDate(),
                    callbackRequest.getStatus(),
                    callbackRequest.getMessage()
            );
            
            log.info("Callback procesado exitosamente. Reference: {}", callbackRequest.getReference());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error al procesar callback de pago", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error al procesar el callback")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    private String extractAndValidateToken(String authHeader, String externalId) {
        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("Solicitud sin header Authorization");
            return null;
        }
        
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        if (!fusionAuthApiClient.validateToken(token)) {
            log.warn("Token inválido");
            return null;
        }
        
        String userId = fusionAuthApiClient.extractUserIdFromToken(token);
        if (userId == null || userId.isEmpty()) {
            log.warn("No se pudo extraer el userId del token");
            return null;
        }
        
        log.info("Token validado exitosamente para userId: {}", userId);
        return userId;
    }

    private ResponseEntity<?> buildUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .responseCode(401)
                        .responseMessage("Autenticación requerida")
                        .error("Token requerido o inválido")
                        .timestamp(System.currentTimeMillis())
                        .build());
    }
}