package com.recetas.backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Fuente CORS única consumida por ambas cadenas de Spring Security.
 * Los orígenes se declaran mediante RECETON_CORS_ALLOWED_ORIGINS; no se
 * mantiene una segunda lista fija dentro de la configuración OAuth.
 */
@Configuration
public class CorsConfig {

    private static final List<String> METODOS_PERMITIDOS =
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private static final List<String> ENCABEZADOS_PERMITIDOS =
            List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With");

    private final List<String> origenesPermitidos;

    public CorsConfig(
            @Value("${receton.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200,http://receton.test:4200}")
            String allowedOrigins) {
        this.origenesPermitidos = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .distinct()
                .toList();

        if (this.origenesPermitidos.isEmpty()) {
            throw new IllegalStateException(
                    "No hay orígenes CORS configurados. Revisa RECETON_CORS_ALLOWED_ORIGINS.");
        }
        if (this.origenesPermitidos.contains("*")) {
            throw new IllegalStateException(
                    "No se permite el origen CORS comodín '*'. Declara los orígenes concretos de RecetON.");
        }
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = crearConfiguracion();
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    private CorsConfiguration crearConfiguracion() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(origenesPermitidos);
        configuracion.setAllowedMethods(METODOS_PERMITIDOS);
        configuracion.setAllowedHeaders(ENCABEZADOS_PERMITIDOS);
        configuracion.setExposedHeaders(List.of("Location"));
        configuracion.setAllowCredentials(true);
        configuracion.setMaxAge(3600L);
        return configuracion;
    }
}
