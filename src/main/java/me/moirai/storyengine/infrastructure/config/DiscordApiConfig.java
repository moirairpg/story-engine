package me.moirai.storyengine.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class DiscordApiConfig {

    private final String baseUrl;

    public DiscordApiConfig(
            @Value("${moirai.discord.api.base-url}") String baseUrl) {

        this.baseUrl = baseUrl;
    }

    @Bean
    RestClient discordClient() {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .build();
    }
}
