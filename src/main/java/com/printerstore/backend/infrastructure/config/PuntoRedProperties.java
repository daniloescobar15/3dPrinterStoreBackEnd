package com.printerstore.backend.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("punto-red.webclient")
public class PuntoRedProperties {

	private String url;

	private String username;

	private String password;

	private Integer connectTimeOut;

	private Integer readTimeOut;

	private String postProcessingPricingPath;
}