package com.printerstore.backend.application.payment.usecase;

import com.printerstore.backend.domain.payment.entity.Payment;
import com.printerstore.backend.domain.payment.gateway.PaymentGateway;
import com.printerstore.backend.domain.payment.gateway.dto.PaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.PaymentResponseDto;
import com.printerstore.backend.domain.payment.repository.PaymentRepository;
import com.printerstore.backend.application.payment.dto.ProcessPaymentRequest;
import com.printerstore.backend.application.payment.dto.ProcessPaymentResponse;
import com.printerstore.backend.application.authentication.port.AuthenticationServicePort;
import com.printerstore.backend.infrastructure.config.PuntoRedProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final AuthenticationServicePort authenticationService;
    private final PuntoRedProperties puntoRedProperties;

    public ProcessPaymentResponse execute(ProcessPaymentRequest request, String userId) {
        log.info("Iniciando procesamiento de pago. ExternalId: {}, Amount: {}, UserId: {}",
                request.getExternalId(), request.getAmount(), userId);
        
        String token = authenticationService.getAuthenticationToken(
                puntoRedProperties.getUsername(),
                puntoRedProperties.getPassword()
        );
        
        log.info("Token obtenido exitosamente. Primeros 50 caracteres: {}...", 
                token.length() > 50 ? token.substring(0, 50) : token);
        
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                request.getExternalId(),
                request.getAmount(),
                request.getDescription(),
                request.getDueDate(),
                request.getCallbackURL()
        );
        
        PaymentResponseDto response = paymentGateway.processPayment(paymentRequestDto, token);
        
        if (response != null && response.getResponseCode() == 201) {
            log.info("Pago procesado exitosamente. PaymentId: {}, Reference: {}", 
                    response.getData().getPaymentId(), 
                    response.getData().getReference());
            
            Payment payment = Payment.builder()
                    .userId(userId)
                    .externalId(request.getExternalId())
                    .amount(BigDecimal.valueOf(request.getAmount()))
                    .description(request.getDescription())
                    .paymentId(String.valueOf(response.getData().getPaymentId()))
                    .reference(response.getData().getReference())
                    .responseCode(response.getResponseCode())
                    .responseMessage(response.getResponseMessage())
                    .status(response.getData().getStatus())
                    .build();
            
            paymentRepository.save(payment);
            log.info("Pago guardado en la base de datos. PaymentId: {}", payment.getId());
            
            return mapToProcessPaymentResponse(response);
        } else {
            Payment payment = Payment.builder()
                    .userId(userId)
                    .externalId(request.getExternalId())
                    .amount(BigDecimal.valueOf(request.getAmount()))
                    .description(request.getDescription())
                    .responseCode(response != null ? response.getResponseCode() : null)
                    .responseMessage(response != null ? response.getResponseMessage() : "Respuesta nula")
                    .status(response != null && response.getData() != null ? response.getData().getStatus() : "FAILED")
                    .build();
            
            paymentRepository.save(payment);
            log.warn("Pago fallido guardado en la base de datos. Código: {}, Mensaje: {}", 
                    response != null ? response.getResponseCode() : "desconocido",
                    response != null ? response.getResponseMessage() : "respuesta nula");
            
            throw new RuntimeException("Error al procesar pago: " + 
                    (response != null ? response.getResponseMessage() : "respuesta nula"));
        }
    }

    private ProcessPaymentResponse mapToProcessPaymentResponse(PaymentResponseDto dto) {
        ProcessPaymentResponse response = new ProcessPaymentResponse();
        response.setResponseCode(dto.getResponseCode());
        response.setResponseMessage(dto.getResponseMessage());
        
        if (dto.getData() != null) {
            ProcessPaymentResponse.PaymentDataResponse dataResponse = new ProcessPaymentResponse.PaymentDataResponse();
            dataResponse.setPaymentId(dto.getData().getPaymentId());
            dataResponse.setReference(dto.getData().getReference());
            dataResponse.setAmount(dto.getData().getAmount());
            dataResponse.setDescription(dto.getData().getDescription());
            dataResponse.setCreationDate(dto.getData().getCreationDate());
            dataResponse.setStatus(dto.getData().getStatus());
            dataResponse.setMessage(dto.getData().getMessage());
            response.setData(dataResponse);
        }
        
        return response;
    }
}