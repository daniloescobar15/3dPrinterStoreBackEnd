package com.printerstore.backend.domain.authentication.gateway;

import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationRequestDto;
import com.printerstore.backend.domain.authentication.gateway.dto.AuthenticationResponseDto;

public interface AuthenticationGateway {
    
    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);
}