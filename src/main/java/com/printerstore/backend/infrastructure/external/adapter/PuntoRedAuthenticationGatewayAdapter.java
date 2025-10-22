package com.printerstore.backend.infrastructure.external.adapter;

import com.printerstore.backend.domain.authentication.gateway.AuthenticationGateway;
import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationRequestDto;
import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationResponseDto;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.client.PuntoRedApiClient;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationRequest;
import com.printerstore.backend.infrastructure.provider.webclient.puntored.model.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PuntoRedAuthenticationGatewayAdapter implements AuthenticationGateway {

    private final PuntoRedApiClient puntoRedApiClient;

    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        AuthenticationRequest authRequest = new AuthenticationRequest(
                request.getUsername(),
                request.getPassword()
        );

        AuthenticationResponse response = puntoRedApiClient.authenticate(authRequest);
        return mapAuthenticationResponse(response);
    }

    private AuthenticationResponseDto mapAuthenticationResponse(AuthenticationResponse response) {
        if (response == null) {
            return null;
        }
        AuthenticationResponseDto dto = new AuthenticationResponseDto();
        dto.setResponseCode(response.getResponseCode());
        dto.setResponseMessage(response.getResponseMessage());

        if (response.getData() != null) {
            AuthenticationResponseDto.AuthenticationDataDto dataDto = new AuthenticationResponseDto.AuthenticationDataDto();
            dataDto.setToken(response.getData().getToken());
            dto.setData(dataDto);
        }

        return dto;
    }
}