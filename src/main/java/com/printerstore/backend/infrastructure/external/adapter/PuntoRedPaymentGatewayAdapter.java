package com.printerstore.backend.infrastructure.external.adapter;

import com.printerstore.backend.domain.payment.gateway.PaymentGateway;
import com.printerstore.backend.domain.payment.gateway.dto.PaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.PaymentResponseDto;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentRequestDto;
import com.printerstore.backend.domain.payment.gateway.dto.CancelPaymentResponseDto;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.client.PuntoRedApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.PaymentResponse;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.CancelPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PuntoRedPaymentGatewayAdapter implements PaymentGateway {

    private final PuntoRedApiClient puntoRedApiClient;

    @Override
    public PaymentResponseDto processPayment(PaymentRequestDto request, String token) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setExternalId(request.getExternalId());
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setDescription(request.getDescription());
        paymentRequest.setDueDate(request.getDueDate());
        paymentRequest.setCallbackURL(request.getCallbackURL());

        PaymentResponse response = puntoRedApiClient.payment(paymentRequest, token);
        return mapPaymentResponse(response);
    }

    @Override
    public CancelPaymentResponseDto cancelPayment(CancelPaymentRequestDto request, String token) {
        CancelPaymentRequest cancelRequest = new CancelPaymentRequest();
        cancelRequest.setReference(request.getReference());
        cancelRequest.setStatus(request.getStatus());
        cancelRequest.setUpdateDescription(request.getUpdateDescription());

        CancelPaymentResponse response = puntoRedApiClient.cancelPayment(cancelRequest, token);
        return mapCancelPaymentResponse(response);
    }

    private PaymentResponseDto mapPaymentResponse(PaymentResponse response) {
        if (response == null) {
            return null;
        }
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setResponseCode(response.getResponseCode());
        dto.setResponseMessage(response.getResponseMessage());

        if (response.getData() != null) {
            PaymentResponseDto.PaymentDataDto dataDto = new PaymentResponseDto.PaymentDataDto();
            dataDto.setPaymentId(response.getData().getPaymentId());
            dataDto.setReference(response.getData().getReference());
            dataDto.setAmount(response.getData().getAmount());
            dataDto.setDescription(response.getData().getDescription());
            dataDto.setCreationDate(response.getData().getCreationDate());
            dataDto.setStatus(response.getData().getStatus());
            dataDto.setMessage(response.getData().getMessage());
            dto.setData(dataDto);
        }

        return dto;
    }

    private CancelPaymentResponseDto mapCancelPaymentResponse(CancelPaymentResponse response) {
        if (response == null) {
            return null;
        }
        CancelPaymentResponseDto dto = new CancelPaymentResponseDto();
        dto.setResponseCode(response.getResponseCode());
        dto.setResponseMessage(response.getResponseMessage());

        if (response.getData() != null) {
            CancelPaymentResponseDto.CancelPaymentDataDto dataDto = new CancelPaymentResponseDto.CancelPaymentDataDto();
            dataDto.setPaymentId(response.getData().getPaymentId());
            dataDto.setCreationDate(response.getData().getCreationDate());
            dataDto.setReference(response.getData().getReference());
            dataDto.setStatus(response.getData().getStatus());
            dataDto.setMessage(response.getData().getMessage());
            dataDto.setCancelDescription(response.getData().getCancelDescription());
            dataDto.setUpdatedAt(response.getData().getUpdatedAt());
            dto.setData(dataDto);
        }

        return dto;
    }
}