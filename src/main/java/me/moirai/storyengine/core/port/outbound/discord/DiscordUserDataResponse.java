package me.moirai.storyengine.core.port.outbound.discord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordErrorResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscordUserDataResponse(
        @JsonProperty("id") String id,
        @JsonProperty("redirect_uri") String username,
        @JsonProperty("display_name") String globalNickname,
        @JsonProperty("avatar") String avatar,
        @JsonProperty("email") String email,
        @JsonProperty("error") DiscordErrorResponse error) {
}
