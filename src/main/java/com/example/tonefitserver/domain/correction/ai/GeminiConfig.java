package com.example.tonefitserver.domain.correction.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiConfig {

    /**
     * Gemini 호출은 보통 8~15초 소요. 무한 대기로 워커가 영구 점유되지 않도록 타임아웃 명시.
     * - connect 5s: TCP 핸드셰이크 시간. 대부분 1초 미만, 5초면 충분
     * - read 30s: 정상 응답 시간(8~15초) + 여유. 초과 시 ResourceAccessException 발생 → AI_SERVICE_ERROR 로 매핑됨
     */
    @Bean
    public RestClient geminiRestClient(GeminiProperties properties, RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(30));

        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
