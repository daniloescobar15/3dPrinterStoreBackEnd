package com.printerstore.backend.configuration.webclient;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final PuntoRedProperties puntoRedProperties;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(puntoRedProperties.getConnectTimeOut()))
                .setReadTimeout(Duration.ofSeconds(puntoRedProperties.getReadTimeOut()))
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(puntoRedProperties.getConnectTimeOut() * 1000);
        factory.setReadTimeout(puntoRedProperties.getReadTimeOut() * 1000);
        return new BufferingClientHttpRequestFactory(factory);
    }
}