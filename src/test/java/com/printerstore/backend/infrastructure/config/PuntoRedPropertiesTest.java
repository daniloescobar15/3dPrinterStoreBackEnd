package com.printerstore.backend.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PuntoRedPropertiesTest {

    @Test
    void testPuntoRedProperties_DefaultConstructor() {
        // Act
        PuntoRedProperties properties = new PuntoRedProperties();

        // Assert
        assertThat(properties).isNotNull();
    }

    @Test
    void testPuntoRedProperties_Setters() {
        // Arrange
        PuntoRedProperties properties = new PuntoRedProperties();

        // Act
        properties.setUrl("https://api.test.com");
        properties.setUsername("testuser");
        properties.setPassword("testpass");
        properties.setConnectTimeOut(10);
        properties.setReadTimeOut(30);
        properties.setPostProcessingPricingPath("/pricing");

        // Assert
        assertThat(properties.getUrl()).isEqualTo("https://api.test.com");
        assertThat(properties.getUsername()).isEqualTo("testuser");
        assertThat(properties.getPassword()).isEqualTo("testpass");
        assertThat(properties.getConnectTimeOut()).isEqualTo(10);
        assertThat(properties.getReadTimeOut()).isEqualTo(30);
        assertThat(properties.getPostProcessingPricingPath()).isEqualTo("/pricing");
    }

    @Test
    void testPuntoRedProperties_Getters() {
        // Arrange
        PuntoRedProperties properties = new PuntoRedProperties();
        properties.setUrl("https://api.test.com");
        properties.setUsername("user123");
        properties.setPassword("pass123");
        properties.setConnectTimeOut(15);
        properties.setReadTimeOut(45);
        properties.setPostProcessingPricingPath("/v1/pricing");

        // Act & Assert
        assertThat(properties.getUrl()).isEqualTo("https://api.test.com");
        assertThat(properties.getUsername()).isEqualTo("user123");
        assertThat(properties.getPassword()).isEqualTo("pass123");
        assertThat(properties.getConnectTimeOut()).isEqualTo(15);
        assertThat(properties.getReadTimeOut()).isEqualTo(45);
        assertThat(properties.getPostProcessingPricingPath()).isEqualTo("/v1/pricing");
    }

    @Test
    void testPuntoRedProperties_MultipleUpdates() {
        // Arrange
        PuntoRedProperties properties = new PuntoRedProperties();

        // Act & Assert - First set
        properties.setUrl("https://api1.test.com");
        assertThat(properties.getUrl()).isEqualTo("https://api1.test.com");

        // Act & Assert - Update
        properties.setUrl("https://api2.test.com");
        assertThat(properties.getUrl()).isEqualTo("https://api2.test.com");
    }

    @Test
    void testPuntoRedProperties_NullValues() {
        // Arrange
        PuntoRedProperties properties = new PuntoRedProperties();

        // Assert
        assertThat(properties.getUrl()).isNull();
        assertThat(properties.getUsername()).isNull();
        assertThat(properties.getPassword()).isNull();
        assertThat(properties.getConnectTimeOut()).isNull();
        assertThat(properties.getReadTimeOut()).isNull();
        assertThat(properties.getPostProcessingPricingPath()).isNull();
    }

    @Test
    void testPuntoRedProperties_PartialConfiguration() {
        // Arrange
        PuntoRedProperties properties = new PuntoRedProperties();

        // Act
        properties.setUrl("https://api.test.com");
        properties.setUsername("testuser");
        // Not setting other properties

        // Assert
        assertThat(properties.getUrl()).isEqualTo("https://api.test.com");
        assertThat(properties.getUsername()).isEqualTo("testuser");
        assertThat(properties.getPassword()).isNull();
        assertThat(properties.getConnectTimeOut()).isNull();
    }
}