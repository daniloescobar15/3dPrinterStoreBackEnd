package com.printerstore.backend.configuration.webclient;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("punto-red.webclient")
public class PuntoRedProperties {

	private String url;

	private String username;

	private String password;

	private Integer connectTimeOut;

	private Integer readTimeOut;

	private String postProcessingPricingPath;

}
