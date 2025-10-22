package com.printerstore.backend.application.authentication.port;

public interface AuthenticationServicePort {
    
    String getAuthenticationToken(String username, String password);
}