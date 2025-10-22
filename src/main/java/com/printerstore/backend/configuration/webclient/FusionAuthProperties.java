package com.printerstore.backend.configuration.webclient;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("fusion-auth.webclient")
public class FusionAuthProperties {

	private String url;

	private String authorization;

	private String applicationId;

	private Integer connectTimeOut;

	private Integer readTimeOut;
}