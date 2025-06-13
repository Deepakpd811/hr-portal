package com.capgemini.hrmanagement.hr_portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;



import org.springframework.beans.factory.annotation.Value;


@Configuration
public class WebClientConfig {

    @Value("${backend.url}")
    private String backendUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(backendUrl)
                .build();
    }
}
