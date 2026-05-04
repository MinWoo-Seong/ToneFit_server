package com.example.ToneFit.correction.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiConfig {

    @Bean
    public RestClient geminiRestClient(GeminiProperties properties, RestClient.Builder builder) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
