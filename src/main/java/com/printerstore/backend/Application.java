package com.printerstore.backend;

import com.printerstore.backend.configuration.webclient.FusionAuthProperties;
import com.printerstore.backend.configuration.webclient.PuntoRedProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.printerstore.backend.domain",
    "com.printerstore.backend.application",
    "com.printerstore.backend.infrastructure",
    "com.printerstore.backend.configuration"
})
@EnableConfigurationProperties({PuntoRedProperties.class, FusionAuthProperties.class})
public class Application {

    public static void main(String[] args) {
        run(Application.class, args);
    }

}
