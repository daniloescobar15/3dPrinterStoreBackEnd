package com.printerstore.backend.infrastructure.adapter.in.web;

import com.printerstore.backend.application.payment.port.PaymentServicePort;
import com.printerstore.backend.application.payment.dto.ProcessPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.client.FusionAuthApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentRequest;
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
     * Endpoint to process a payment
     */
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(
            @RequestBody ProcessPaymentRequest paymentRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Payment request received for externalId: {}", paymentRequest.getExternalId());
            
            String userId = extractAndValidateToken(authHeader, paymentRequest.getExternalId());
            if (userId == null) {
                return buildUnauthorizedResponse();
            }
            
            var response = paymentService.processPayment(paymentRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error in payment processing", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error processing payment")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    /**
     * Endpoint to get all payments of the authenticated user
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserPayments(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Request to list user payments");
            
            String userId = extractAndValidateToken(authHeader, null);
            if (userId == null) {
                return buildUnauthorizedResponse();
            }
            
            List<Payment> payments = paymentService.getUserPayments(userId);
            log.info("Returned {} payments for userId: {}", payments.size(), userId);
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            log.error("Error obtaining payment list", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error obtaining payment list")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    /**
     * Endpoint to cancel a payment
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(
            @RequestBody CancelPaymentRequest cancelPaymentRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("Payment cancellation request received for reference: {}", cancelPaymentRequest.getReference());
            
            String userId = extractAndValidateToken(authHeader, null);
            if (userId == null) {
                return buildUnauthorizedResponse();
            }
            
            var response = paymentService.cancelPayment(cancelPaymentRequest.getReference(), cancelPaymentRequest.getUpdateDescription());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (Exception e) {
            log.error("Error in payment cancellation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error canceling payment")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    /**
     * Callback endpoint to receive payment status updates from Punto Red
     * Does not require JWT authentication
     */
    @PostMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestBody PaymentCallbackRequest callbackRequest) {
        try {
            log.info("Payment callback received. Reference: {}, ExternalId: {}, PaymentId: {}, Status: {}",
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
            
            log.info("Callback processed successfully. Reference: {}", callbackRequest.getReference());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder()
                            .responseCode(400)
                            .responseMessage("Error processing callback")
                            .error(e.getMessage())
                            .timestamp(System.currentTimeMillis())
                            .build());
        }
    }

    private String extractAndValidateToken(String authHeader, String externalId) {
        if (authHeader == null || authHeader.isEmpty()) {
            log.warn("Request without Authorization header");
            return null;
        }
        
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        if (!fusionAuthApiClient.validateToken(token)) {
            log.warn("Invalid token");
            return null;
        }
        
        String userId = fusionAuthApiClient.extractUserIdFromToken(token);
        if (userId == null || userId.isEmpty()) {
            log.warn("Could not extract userId from token");
            return null;
        }
        
        log.info("Token validated successfully for userId: {}", userId);
        return userId;
    }

    private ResponseEntity<?> buildUnauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .responseCode(401)
                        .responseMessage("Authentication required")
                        .error("Token required or invalid")
                        .timestamp(System.currentTimeMillis())
                        .build());
    }
}