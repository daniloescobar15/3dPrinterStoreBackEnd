package com.printerstore.backend.infrastructure.adapter.in;

import com.printerstore.backend.application.authentication.port.AuthenticationServicePort;
import com.printerstore.backend.application.authentication.usecase.GetAuthenticationTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceAdapter implements AuthenticationServicePort {

    private final GetAuthenticationTokenUseCase getAuthenticationTokenUseCase;

    @Override
    public String getAuthenticationToken(String username, String password) {
        return getAuthenticationTokenUseCase.execute(username, password);
    }
}