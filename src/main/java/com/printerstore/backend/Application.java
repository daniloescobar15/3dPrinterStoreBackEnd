package com.printerstore.backend;

import com.printerstore.backend.configuration.webclient.FusionAuthProperties;
import com.printerstore.backend.configuration.webclient.PuntoRedProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableConfigurationProperties({PuntoRedProperties.class, FusionAuthProperties.class})
public class Application {

    public static void main(String[] args) {
        run(Application.class, args);
    }

}
