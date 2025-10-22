package com.printerstore.backend.infrastructure.provider.webclient.puntored.client;

import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentResponse;

public interface PuntoRedApiClient {
    
    /**
     * Autentica con el sistema Punto Red
     * 
     * @param authRequest solicitud con username y password
     * @return respuesta con token y datos de autenticación
     */
    AuthenticationResponse authenticate(AuthenticationRequest authRequest);
    
    /**
     * Crea un pago en el sistema Punto Red
     * 
     * @param paymentRequest solicitud de pago
     * @param token token de autenticación (Authorization header)
     * @return respuesta con detalles del pago creado
     */
    PaymentResponse payment(PaymentRequest paymentRequest, String token);
    
    /**
     * Cancela un pago en el sistema Punto Red
     * 
     * @param cancelPaymentRequest solicitud de cancelación con reference, status y description
     * @param token token de autenticación (Authorization header)
     * @return respuesta con detalles de la cancelación
     */
    CancelPaymentResponse cancelPayment(CancelPaymentRequest cancelPaymentRequest, String token);
}