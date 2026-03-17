package me.moirai.storyengine.core.port.outbound.discord;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordTokenRevocationRequest(
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret,
        @JsonProperty("token") String token,
        @JsonProperty("token_type_hint") String tokenTypeHint) {
}
