package com.printerstore.backend.infrastructure.provider.webclient.puntored.client;

import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentResponse;

public interface PuntoRedApiClient {
    
    /**
     * Authenticates with the Punto Red system
     * 
     * @param authRequest request with username and password
     * @return response with token and authentication data
     */
    AuthenticationResponse authenticate(AuthenticationRequest authRequest);
    
    /**
     * Creates a payment in the Punto Red system
     * 
     * @param paymentRequest payment request
     * @param token authentication token (Authorization header)
     * @return response with details of the created payment
     */
    PaymentResponse payment(PaymentRequest paymentRequest, String token);
    
    /**
     * Cancels a payment in the Punto Red system
     * 
     * @param cancelPaymentRequest cancellation request with reference, status and description
     * @param token authentication token (Authorization header)
     * @return response with cancellation details
     */
    CancelPaymentResponse cancelPayment(CancelPaymentRequest cancelPaymentRequest, String token);
}