package me.moirai.storyengine.infrastructure.outbound.adapter.discord;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;

import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordTokenRevocationRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.RefreshSessionTokenRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class DiscordAuthenticationAdapter implements DiscordAuthenticationPort {

    private static final String BEARER = "Bearer %s";
    private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

    private final String usersUri;
    private final String tokenUri;
    private final String tokenRevokeUri;
    private final JsonMapper jsonMapper;
    private final RestClient discordClient;

    public DiscordAuthenticationAdapter(
            @Value("${moirai.discord.api.users-uri}") String usersUri,
            @Value("${moirai.discord.api.token-uri}") String tokenUri,
            @Value("${moirai.discord.api.token-revoke-uri}") String tokenRevokeUri,
            RestClient discordClient,
            JsonMapper jsonMapper) {

        this.jsonMapper = jsonMapper;
        this.usersUri = usersUri;
        this.tokenUri = tokenUri;
        this.tokenRevokeUri = tokenRevokeUri;
        this.discordClient = discordClient;
    }

    @Override
    public AuthenticateUserResult authenticate(DiscordAuthRequest request) {

        var response = postForAuthentication(tokenUri, request)
                .body(DiscordAuthResponse.class);

        return toResult(response);
    }

    @Override
    public AuthenticateUserResult refreshSessionToken(RefreshSessionTokenRequest request) {

        var response = postForAuthentication(tokenUri, request)
                .body(DiscordAuthResponse.class);

        return toResult(response);
    }

    @Override
    public DiscordUserDataResponse retrieveLoggedUser(String token) {

        return discordClient.get()
                .uri(format(usersUri, "@me"))
                .headers(headers -> {
                    headers.add(AUTHORIZATION, format(BEARER, token));
                    headers.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .body(DiscordUserDataResponse.class);
    }

    @Override
    public void logout(String clientId, String clientSecret, String token, String tokenTypeHint) {

        var request = new DiscordTokenRevocationRequest(
                clientId,
                clientSecret,
                token,
                tokenTypeHint);

        postForAuthentication(tokenRevokeUri, request)
                .body(Void.class);
    }

    private AuthenticateUserResult toResult(DiscordAuthResponse response) {

        return new AuthenticateUserResult(
                response.accessToken(),
                response.expiresIn(),
                response.refreshToken(),
                response.scope(),
                response.tokenType());
    }

    private ResponseSpec postForAuthentication(String url, Object request) {

        var valueMap = new LinkedMultiValueMap<String, String>();
        var fieldMap = jsonMapper.convertValue(request, new TypeReference<Map<String, String>>() {
        });

        valueMap.setAll(fieldMap);

        return discordClient.post()
                .uri(url)
                .headers(headers -> {
                    headers.add(CONTENT_TYPE, CONTENT_TYPE_VALUE);
                    headers.add(ACCEPT, CONTENT_TYPE_VALUE);
                })
                .body(valueMap)
                .retrieve();
    }
}
