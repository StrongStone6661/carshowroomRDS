package com.example.carshowroom.config;

import com.example.carshowroom.service.ParameterStoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    private final ParameterStoreService parameterStoreService;

    public ApplicationConfig(ParameterStoreService parameterStoreService) {
        this.parameterStoreService = parameterStoreService;
    }

    @Bean
    public String databaseUrl() {
        return "jdbc:postgresql://" + parameterStoreService.getDatabaseHost() + 
               ":" + parameterStoreService.getDatabasePort() + 
               "/" + parameterStoreService.getDatabaseName();
    }

    @Bean
    public String databaseUsername() {
        return parameterStoreService.getDatabaseUsername();
    }

    @Bean
    public String databasePassword() {
        return parameterStoreService.getDatabasePassword();
    }
}
