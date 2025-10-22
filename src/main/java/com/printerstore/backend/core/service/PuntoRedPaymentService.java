package com.printerstore.backend.core.service;

import com.printerstore.backend.configuration.webclient.PuntoRedProperties;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.client.PuntoRedApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentResponse;
import com.printerstore.backend.persistence.entity.Payment;
import com.printerstore.backend.persistence.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PuntoRedPaymentService {

    private final PuntoRedApiClient puntoRedApiClient;
    private final PuntoRedAuthenticationService authenticationService;
    private final PuntoRedProperties puntoRedProperties;
    private final PaymentRepository paymentRepository;

    /**
     * Procesa un pago en el sistema Punto Red
     * Obtiene automáticamente el token usando las credenciales del application.yaml
     * Guarda el registro del pago en la base de datos
     *
     * @param paymentRequest solicitud de pago
     * @param userId identificador del usuario que realiza el pago
     * @return respuesta con detalles del pago creado
     */
    public PaymentResponse processPayment(PaymentRequest paymentRequest, String userId) {
        log.info("Iniciando procesamiento de pago. ExternalId: {}, Amount: {}, UserId: {}",
                paymentRequest.getExternalId(), paymentRequest.getAmount(), userId);
        
        String token = authenticationService.getAuthenticationToken(
                puntoRedProperties.getUsername(),
                puntoRedProperties.getPassword()
        );
        
        log.info("Token obtenido exitosamente. Primeros 50 caracteres: {}...", 
                token.length() > 50 ? token.substring(0, 50) : token);
        
        PaymentResponse response = puntoRedApiClient.payment(paymentRequest, token);
        
        if (response != null && response.getResponseCode() == 201) {
            log.info("Pago procesado exitosamente. PaymentId: {}, Reference: {}", 
                    response.getData().getPaymentId(), 
                    response.getData().getReference());
            
            Payment payment = Payment.builder()
                    .userId(userId)
                    .externalId(paymentRequest.getExternalId())
                    .amount(BigDecimal.valueOf(paymentRequest.getAmount()))
                    .description(paymentRequest.getDescription())
                    .paymentId(String.valueOf(response.getData().getPaymentId()))
                    .reference(response.getData().getReference())
                    .responseCode(response.getResponseCode())
                    .responseMessage(response.getResponseMessage())
                    .status(response.getData().getStatus())
                    .build();
            
            paymentRepository.save(payment);
            log.info("Pago guardado en la base de datos. PaymentId: {}", payment.getId());
            
            return response;
        } else {
            Payment payment = Payment.builder()
                    .userId(userId)
                    .externalId(paymentRequest.getExternalId())
                    .amount(BigDecimal.valueOf(paymentRequest.getAmount()))
                    .description(paymentRequest.getDescription())
                    .responseCode(response != null ? response.getResponseCode() : null)
                    .responseMessage(response != null ? response.getResponseMessage() : "Respuesta nula")
                    .status(response.getData().getStatus())
                    .build();
            
            paymentRepository.save(payment);
            log.warn("Pago fallido guardado en la base de datos. Código: {}, Mensaje: {}", 
                    response != null ? response.getResponseCode() : "desconocido",
                    response != null ? response.getResponseMessage() : "respuesta nula");
            
            throw new RuntimeException("Error al procesar pago: " + 
                    (response != null ? response.getResponseMessage() : "respuesta nula"));
        }
    }

    /**
     * Obtiene todos los pagos de un usuario ordenados por fecha de creación (más recientes primero)
     *
     * @param userId identificador del usuario
     * @return lista de pagos del usuario
     */
    public List<Payment> getUserPayments(String userId) {
        log.info("Obteniendo pagos para userId: {}", userId);
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Se encontraron {} pagos para userId: {}", payments.size(), userId);
        return payments;
    }

    /**
     * Actualiza un pago con los datos del callback de Punto Red
     * Busca el pago prioritariamente por referencia y luego por externalId
     *
     * @param paymentId identificador del pago en Punto Red
     * @param externalId identificador externo del pago
     * @param amount monto del pago
     * @param authorizationNumber número de autorización
     * @param reference referencia del pago
     * @param paymentDate fecha del pago en milisegundos
     * @param status estado del pago
     * @param message mensaje de respuesta
     */
    public void updatePaymentCallback(Long paymentId, String externalId, Double amount, 
                                       String authorizationNumber, String reference, 
                                       Long paymentDate, String status, String message) {
        log.info("Recibido callback de pago. Reference: {}, ExternalId: {}, PaymentId: {}, Status: {}",
                reference, externalId, paymentId, status);
        
        Optional<Payment> paymentOptional = paymentRepository.findByReference(reference);
        
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.setPaymentId(String.valueOf(paymentId));
            payment.setAmount(BigDecimal.valueOf(amount));
            payment.setReference(reference);
            payment.setStatus(status);
            payment.setResponseMessage(message);
            payment.setResponseCode(200);
            
            paymentRepository.save(payment);
            log.info("Pago actualizado exitosamente desde callback. PaymentId: {}, Reference: {}, Status: {}",
                    payment.getId(), reference, status);
        } else {
            log.warn("No se encontró pago con reference: {}. Buscando por externalId: {}", 
                    reference, externalId);
            
            if (externalId != null && !externalId.isEmpty()) {
                List<Payment> payments = paymentRepository.findByExternalId(externalId);
                
                if (!payments.isEmpty()) {
                    Payment payment = payments.stream()
                            .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()))
                            .orElse(payments.get(0));
                    
                    payment.setPaymentId(String.valueOf(paymentId));
                    payment.setAmount(BigDecimal.valueOf(amount));
                    payment.setReference(reference);
                    payment.setStatus(status);
                    payment.setResponseMessage(message);
                    payment.setResponseCode(200);
                    
                    paymentRepository.save(payment);
                    log.info("Pago actualizado (búsqueda por externalId) desde callback. PaymentId: {}, Reference: {}, Status: {}",
                            payment.getId(), reference, status);
                } else {
                    log.warn("No se encontró pago con reference: {} ni con externalId: {} para actualizar desde callback",
                            reference, externalId);
                }
            } else {
                log.warn("No se encontró pago con reference: {} y externalId es nulo", reference);
            }
        }
    }

    /**
     * Cancela un pago en el sistema Punto Red
     * Obtiene automáticamente el token usando las credenciales del application.yaml
     * Actualiza el registro del pago en la base de datos
     *
     * @param reference número de referencia del pago a cancelar
     * @param cancelDescription descripción de la cancelación
     * @return respuesta con detalles de la cancelación
     */
    public CancelPaymentResponse cancelPayment(String reference, String cancelDescription) {
        log.info("Iniciando cancelación de pago. Reference: {}", reference);
        
        String token = authenticationService.getAuthenticationToken(
                puntoRedProperties.getUsername(),
                puntoRedProperties.getPassword()
        );
        
        log.info("Token obtenido exitosamente para cancelación. Primeros 50 caracteres: {}...", 
                token.length() > 50 ? token.substring(0, 50) : token);
        
        CancelPaymentRequest cancelRequest = new CancelPaymentRequest();
        cancelRequest.setReference(reference);
        cancelRequest.setStatus("03"); // Status para cancelado
        cancelRequest.setUpdateDescription(cancelDescription);
        
        CancelPaymentResponse response = puntoRedApiClient.cancelPayment(cancelRequest, token);
        
        if (response != null && response.getResponseCode() == 202) {
            log.info("Pago cancelado exitosamente. PaymentId: {}, Reference: {}", 
                    response.getData().getPaymentId(), 
                    response.getData().getReference());
            
            Optional<Payment> paymentOptional = paymentRepository.findByReference(reference);
            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();
                payment.setStatus(response.getData().getStatus());
                payment.setResponseCode(response.getResponseCode());
                payment.setResponseMessage(response.getResponseMessage());
                paymentRepository.save(payment);
                log.info("Pago actualizado en la base de datos. PaymentId: {}", payment.getId());
            } else {
                log.warn("No se encontró pago con reference: {} para actualizar estado de cancelación", reference);
            }
            
            return response;
        } else if (response != null && response.getResponseCode() == 409) {
            log.warn("Error al cancelar pago. Código: {}, Mensaje: {}", 
                    response.getResponseCode(), response.getResponseMessage());
            
            Optional<Payment> paymentOptional = paymentRepository.findByReference(reference);
            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();
                payment.setResponseCode(response.getResponseCode());
                payment.setResponseMessage(response.getResponseMessage());
                paymentRepository.save(payment);
                log.info("Pago actualizado con estado de error. PaymentId: {}", payment.getId());
            }
            
            throw new RuntimeException("Error al cancelar pago: " + response.getResponseMessage());
        } else {
            log.error("Respuesta inesperada al cancelar pago. Código: {}", 
                    response != null ? response.getResponseCode() : "desconocido");
            
            throw new RuntimeException("Error inesperado al cancelar pago: " + 
                    (response != null ? response.getResponseMessage() : "respuesta nula"));
        }
    }
}