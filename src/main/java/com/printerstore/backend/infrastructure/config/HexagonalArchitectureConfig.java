package com.printerstore.backend.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de arquitectura hexagonal
 * Define los escaneos de componentes para cada layer
 */
@Configuration
@ComponentScan({
    "com.printerstore.backend.domain",
    "com.printerstore.backend.application",
    "com.printerstore.backend.infrastructure"
})
public class HexagonalArchitectureConfig {
}