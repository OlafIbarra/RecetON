package com.recetas.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AutorizacionWebConfig implements WebMvcConfigurer {

    private final AutorizacionApiInterceptor autorizacionApiInterceptor;

    public AutorizacionWebConfig(AutorizacionApiInterceptor autorizacionApiInterceptor) {
        this.autorizacionApiInterceptor = autorizacionApiInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(autorizacionApiInterceptor).addPathPatterns("/api/**");
    }
}
