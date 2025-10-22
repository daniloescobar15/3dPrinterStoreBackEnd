package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.gateway.PaymentGateway;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentResponseDto;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import com.printerstore.backend.application.payment.dto.CancelPaymentResponse;
import com.printerstore.backend.application.authentication.port.AuthenticationServicePort;
import com.printerstore.backend.infrastructure.config.PuntoRedProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelPaymentUseCase {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final AuthenticationServicePort authenticationService;
    private final PuntoRedProperties puntoRedProperties;

    public CancelPaymentResponse execute(String reference, String cancelDescription) {
        log.info("Iniciando cancelación de pago. Reference: {}", reference);
        
        String token = authenticationService.getAuthenticationToken(
                puntoRedProperties.getUsername(),
                puntoRedProperties.getPassword()
        );
        
        log.info("Token obtenido exitosamente para cancelación. Primeros 50 caracteres: {}...", 
                token.length() > 50 ? token.substring(0, 50) : token);
        
        CancelPaymentRequestDto cancelRequest = new CancelPaymentRequestDto();
        cancelRequest.setReference(reference);
        cancelRequest.setStatus("03"); // Status para cancelado
        cancelRequest.setUpdateDescription(cancelDescription);
        
        CancelPaymentResponseDto response = paymentGateway.cancelPayment(cancelRequest, token);
        
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
            
            return mapToCancelPaymentResponse(response);
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

    private CancelPaymentResponse mapToCancelPaymentResponse(CancelPaymentResponseDto dto) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setResponseCode(dto.getResponseCode());
        response.setResponseMessage(dto.getResponseMessage());
        
        if (dto.getData() != null) {
            CancelPaymentResponse.CancelPaymentDataResponse dataResponse = new CancelPaymentResponse.CancelPaymentDataResponse();
            dataResponse.setPaymentId(dto.getData().getPaymentId());
            dataResponse.setCreationDate(dto.getData().getCreationDate());
            dataResponse.setReference(dto.getData().getReference());
            dataResponse.setStatus(dto.getData().getStatus());
            dataResponse.setMessage(dto.getData().getMessage());
            dataResponse.setCancelDescription(dto.getData().getCancelDescription());
            dataResponse.setUpdatedAt(dto.getData().getUpdatedAt());
            response.setData(dataResponse);
        }
        
        return response;
    }
}