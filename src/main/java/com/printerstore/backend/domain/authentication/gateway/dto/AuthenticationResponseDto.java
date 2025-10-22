package com.printerstore.backend.domain.authentication.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponseDto {
    private Integer responseCode;
    private String responseMessage;
    private AuthenticationDataDto data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticationDataDto {
        private String token;
    }
}