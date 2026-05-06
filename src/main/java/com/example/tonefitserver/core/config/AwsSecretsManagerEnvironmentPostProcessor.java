package com.example.tonefitserver.core.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AwsSecretsManagerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Region REGION = Region.of("ap-northeast-2");
    private static final String APP_SECRET_NAME = "tonefit/app";
    private static final String DB_SECRET_NAME = "tonefit/db";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean isPrd = Arrays.asList(environment.getActiveProfiles()).contains("prd");
        if (!isPrd) {
            return;
        }

        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(REGION)
                .build()) {

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> properties = new HashMap<>();

            JsonNode db = mapper.readTree(fetchSecret(client, DB_SECRET_NAME));
            properties.put("spring.datasource.url",
                    "jdbc:postgresql://" + db.get("host").asText()
                    + ":" + db.get("port").asText()
                    + "/" + db.get("dbname").asText());
            properties.put("spring.datasource.username", db.get("username").asText());
            properties.put("spring.datasource.password", db.get("password").asText());

            JsonNode app = mapper.readTree(fetchSecret(client, APP_SECRET_NAME));
            properties.put("jwt.secret", app.get("JWT_SECRET").asText());
            properties.put("gemini.api-key", app.get("GEMINI_API_KEY").asText());
            properties.put("gemini.model", app.get("GEMINI_MODEL").asText());

            environment.getPropertySources().addFirst(new MapPropertySource("awsSecretsManager", properties));

        } catch (Exception e) {
            throw new IllegalStateException("AWS Secrets Manager에서 설정을 불러오지 못했습니다.", e);
        }
    }

    private String fetchSecret(SecretsManagerClient client, String secretName) {
        return client.getSecretValue(
                GetSecretValueRequest.builder().secretId(secretName).build()
        ).secretString();
    }
}
