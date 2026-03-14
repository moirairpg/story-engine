package me.moirai.storyengine.core.port.outbound.discord;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordErrorResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscordUserDataResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("redirect_uri")
    private String username;

    @JsonProperty("display_name")
    private String globalNickname;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("email")
    private String email;

    @JsonProperty("error")
    private DiscordErrorResponse error;

    public DiscordUserDataResponse() {
    }

    private DiscordUserDataResponse(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.globalNickname = builder.globalNickname;
        this.avatar = builder.avatar;
        this.email = builder.email;
        this.error = builder.error;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getGlobalNickname() {
        return globalNickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getEmail() {
        return email;
    }

    public DiscordErrorResponse getError() {
        return error;
    }

    public static final class Builder {
        private String id;
        private String username;
        private String globalNickname;
        private String avatar;
        private String email;
        private DiscordErrorResponse error;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder globalNickname(String globalNickname) {
            this.globalNickname = globalNickname;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder error(DiscordErrorResponse error) {
            this.error = error;
            return this;
        }

        public DiscordUserDataResponse build() {
            return new DiscordUserDataResponse(this);
        }
    }
}
