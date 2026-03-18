package me.moirai.storyengine.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiApiConfig {

    private static final String BEARER = "Bearer ";

    private final String baseUrl;
    private final String apiToken;

    public OpenAiApiConfig(
            @Value("${moirai.openai.api.base-url}") String baseUrl,
            @Value("${moirai.openai.api.token}") String apiToken) {

        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
    }

    @Bean
    RestClient openAiClient() {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, BEARER + apiToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .build();
    }
}
