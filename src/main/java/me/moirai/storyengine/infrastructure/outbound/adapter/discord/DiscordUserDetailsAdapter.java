package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;

@Component
public class DiscordUserDetailsAdapter implements DiscordUserDetailsPort {

    private static final String BEARER = "Bearer %s";

    private final String usersUri;
    private final RestClient discordClient;

    public DiscordUserDetailsAdapter(
            @Value("${moirai.discord.api.users-uri}") String usersUri,
            RestClient discordClient) {

        this.usersUri = usersUri;
        this.discordClient = discordClient;
    }

    @Override
    public Optional<DiscordUserDataResponse> getUserById(String userDiscordId, String token) {

        return Optional.ofNullable(discordClient.get()
                .uri(format(usersUri, userDiscordId))
                .headers(headers -> {
                    headers.add(AUTHORIZATION, format(BEARER, token));
                    headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .body(DiscordUserDataResponse.class));
    }
}
