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
        log.info("Starting payment cancellation. Reference: {}", reference);
        
        String token = authenticationService.getAuthenticationToken(
                puntoRedProperties.getUsername(),
                puntoRedProperties.getPassword()
        );
        
        log.info("Token obtained successfully for cancellation. First 50 characters: {}...", 
                token.length() > 50 ? token.substring(0, 50) : token);
        
        CancelPaymentRequestDto cancelRequest = new CancelPaymentRequestDto();
        cancelRequest.setReference(reference);
        cancelRequest.setStatus("03"); // Status for cancelled
        cancelRequest.setUpdateDescription(cancelDescription);
        
        CancelPaymentResponseDto response = paymentGateway.cancelPayment(cancelRequest, token);
        
        if (response != null && response.getResponseCode() == 202) {
            log.info("Payment cancelled successfully. PaymentId: {}, Reference: {}", 
                    response.getData().getPaymentId(), 
                    response.getData().getReference());
            
            Optional<Payment> paymentOptional = paymentRepository.findByReference(reference);
            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();
                payment.setStatus(response.getData().getStatus());
                payment.setResponseCode(response.getResponseCode());
                payment.setResponseMessage(response.getResponseMessage());
                paymentRepository.save(payment);
                log.info("Payment updated in database. PaymentId: {}", payment.getId());
            } else {
                log.warn("Payment not found with reference: {} to update cancellation status", reference);
            }
            
            return mapToCancelPaymentResponse(response);
        } else if (response != null && response.getResponseCode() == 409) {
            log.warn("Error cancelling payment. Code: {}, Message: {}", 
                    response.getResponseCode(), response.getResponseMessage());
            
            Optional<Payment> paymentOptional = paymentRepository.findByReference(reference);
            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();
                payment.setResponseCode(response.getResponseCode());
                payment.setResponseMessage(response.getResponseMessage());
                paymentRepository.save(payment);
                log.info("Payment updated with error status. PaymentId: {}", payment.getId());
            }
            
            throw new RuntimeException("Error cancelling payment: " + response.getResponseMessage());
        } else {
            log.error("Unexpected response when cancelling payment. Code: {}", 
                    response != null ? response.getResponseCode() : "unknown");
            
            throw new RuntimeException("Unexpected error cancelling payment: " + 
                    (response != null ? response.getResponseMessage() : "null response"));
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