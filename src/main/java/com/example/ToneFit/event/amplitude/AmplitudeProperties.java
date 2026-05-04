package com.example.ToneFit.event.amplitude;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "amplitude")
public record AmplitudeProperties(
        String apiKey,
        String baseUrl,
        boolean enabled
) {
}
