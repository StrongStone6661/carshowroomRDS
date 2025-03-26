package com.example.carshowroom.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Service
public class ParameterStoreService {

    private final SsmClient ssmClient;

    public ParameterStoreService() {
        this.ssmClient = SsmClient.builder()
                .region(Region.of("eu-central-1")) // Change to your AWS region
                .build();
    }

    private String getParameter(String paramName) {
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(paramName)
                .withDecryption(true) // Needed for SecureString parameters
                .build();
        
        GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
        return parameterResponse.parameter().value();
    }

    public String getDatabaseHost() {
        return getParameter("/showroom/database/host");
    }

    public String getDatabasePort() {
        return getParameter("/showroom/database/port");
    }

    public String getDatabaseName() {
        return getParameter("/showroom/database/name");
    }

    public String getDatabaseUsername() {
        return getParameter("/showroom/database/username");
    }

    public String getDatabasePassword() {
        return getParameter("/showroom/database/password");
    }
}
