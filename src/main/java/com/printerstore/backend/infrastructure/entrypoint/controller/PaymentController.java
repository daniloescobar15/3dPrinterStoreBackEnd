package com.printerstore.backend.infrastructure.entrypoint.controller;

import com.printerstore.backend.core.service.PuntoRedPaymentService;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client.FusionAuthApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentCallbackRequest;
import com.printerstore.backend.infrastructure.entrypoint.controller.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.printerstore.backend.persistence.entity.Payment;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PuntoRedPaymentService paymentService;
    private final FusionAuthApiClient fusionAuthApiClient;

    /**
     * Endpoint para procesar un pago
     * Valida el token JWT desde el header Authorization antes de procesar el pago
     * 
     * @param paymentRequest solicitud de pago con: externalId, amount, description, dueDate, callbackURL
     * @param authHeader header de autorización con el token JWT (Bearer <token>)
     * @return respuesta con detalles del pago creado
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Recibida solicitud de pago para externalId: {}", paymentRequest.getExternalId());
            
            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Solicitud de pago sin header Authorization para externalId: {}", paymentRequest.getExternalId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Token requerido")
                                .error("Token requerido en header Authorization")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            
            boolean isTokenValid = fusionAuthApiClient.validateToken(token);
            if (!isTokenValid) {
                log.warn("Token inválido en solicitud de pago para externalId: {}", paymentRequest.getExternalId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Token inválido")
                                .error("Token inválido o expirado")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            log.info("Token validado exitosamente para externalId: {}", paymentRequest.getExternalId());
            
            String userId = fusionAuthApiClient.extractUserIdFromToken(token);
            if (userId == null || userId.isEmpty()) {
                log.warn("No se pudo extraer el userId del token para externalId: {}", paymentRequest.getExternalId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Usuario no identificado")
                                .error("No se pudo identificar al usuario")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            log.info("UserId extraído del token: {} para externalId: {}", userId, paymentRequest.getExternalId());
            
            PaymentResponse response = paymentService.processPayment(paymentRequest, userId);
            
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
     * Valida el token JWT desde el header Authorization
     * 
     * @param authHeader header de autorización con el token JWT (Bearer <token>)
     * @return respuesta con lista de pagos del usuario
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserPayments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Solicitud para listar pagos del usuario");
            
            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Solicitud de lista de pagos sin header Authorization");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Token requerido")
                                .error("Token requerido en header Authorization")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            
            boolean isTokenValid = fusionAuthApiClient.validateToken(token);
            if (!isTokenValid) {
                log.warn("Token inválido en solicitud de lista de pagos");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Token inválido")
                                .error("Token inválido o expirado")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            log.info("Token validado exitosamente para lista de pagos");
            
            String userId = fusionAuthApiClient.extractUserIdFromToken(token);
            if (userId == null || userId.isEmpty()) {
                log.warn("No se pudo extraer el userId del token para lista de pagos");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Usuario no identificado")
                                .error("No se pudo identificar al usuario")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            log.info("Obteniendo pagos para userId: {}", userId);
            
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
     * Valida el token JWT desde el header Authorization antes de cancelar el pago
     * 
     * @param cancelPaymentRequest solicitud de cancelación con: reference y updateDescription
     * @param authHeader header de autorización con el token JWT (Bearer <token>)
     * @return respuesta con detalles de la cancelación
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(
            @RequestBody CancelPaymentRequest cancelPaymentRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Recibida solicitud de cancelación de pago para reference: {}", cancelPaymentRequest.getReference());
            
            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Solicitud de cancelación sin header Authorization para reference: {}", cancelPaymentRequest.getReference());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Token requerido")
                                .error("Token requerido en header Authorization")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            
            boolean isTokenValid = fusionAuthApiClient.validateToken(token);
            if (!isTokenValid) {
                log.warn("Token inválido en solicitud de cancelación para reference: {}", cancelPaymentRequest.getReference());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Token inválido")
                                .error("Token inválido o expirado")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            log.info("Token validado exitosamente para reference: {}", cancelPaymentRequest.getReference());
            
            String userId = fusionAuthApiClient.extractUserIdFromToken(token);
            if (userId == null || userId.isEmpty()) {
                log.warn("No se pudo extraer el userId del token para cancelación");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.builder()
                                .responseCode(401)
                                .responseMessage("Usuario no identificado")
                                .error("No se pudo identificar al usuario")
                                .timestamp(System.currentTimeMillis())
                                .build());
            }
            
            log.info("UserId extraído del token: {} para cancelación de reference: {}", userId, cancelPaymentRequest.getReference());
            
            CancelPaymentResponse response = paymentService.cancelPayment(
                    cancelPaymentRequest.getReference(),
                    cancelPaymentRequest.getUpdateDescription()
            );
            
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
     * No requiere autenticación JWT ya que es llamado por el servidor de Punto Red
     * 
     * @param callbackRequest solicitud con los datos del callback del pago
     * @return respuesta indicando que se recibió el callback
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
            
            return ResponseEntity.ok().body(ErrorResponse.builder()
                    .responseCode(200)
                    .responseMessage("Callback recibido y procesado exitosamente")
                    .timestamp(System.currentTimeMillis())
                    .build());
            
        } catch (Exception e) {
            log.error("Error al procesar callback de pago", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .responseCode(500)
                            .responseMessage("Error al procesar el callback")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }
}