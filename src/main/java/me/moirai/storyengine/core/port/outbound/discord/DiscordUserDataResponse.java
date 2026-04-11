package me.moirai.storyengine.core.port.outbound.discord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordErrorResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscordUserDataResponse(
        @JsonProperty("id") String id,
        @JsonProperty("username") String username,
        @JsonProperty("global_name") String globalNickname,
        @JsonProperty("avatar") String avatar,
        @JsonProperty("avatar_id") String avatarId,
        @JsonProperty("email") String email,
        String avatarUrl,
        @JsonProperty("error") DiscordErrorResponse error) {

    private static final String AVATAR_BASE_PATH = "https://cdn.discordapp.com/avatars/%s/%s.png";

    public DiscordUserDataResponse {
        avatarUrl = String.format(AVATAR_BASE_PATH, id, avatar);
    }
}
