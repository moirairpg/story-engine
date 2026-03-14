package me.moirai.storyengine.core.port.outbound.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = DiscordTokenRevocationRequest.Builder.class)
public class DiscordTokenRevocationRequest {

    @JsonProperty("client_id")
    private final String clientId;

    @JsonProperty("client_secret")
    private final String clientSecret;

    @JsonProperty("token")
    private final String token;

    @JsonProperty("token_type_hint")
    private final String tokenTypeHint;

    private DiscordTokenRevocationRequest(Builder builder) {
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.token = builder.token;
        this.tokenTypeHint = builder.tokenTypeHint;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getToken() {
        return token;
    }

    public String getTokenTypeHint() {
        return tokenTypeHint;
    }

    @JsonPOJOBuilder
    public static final class Builder {

        private String clientId;
        private String clientSecret;
        private String token;
        private String tokenTypeHint;

        private Builder() {
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder tokenTypeHint(String tokenTypeHint) {
            this.tokenTypeHint = tokenTypeHint;
            return this;
        }

        public DiscordTokenRevocationRequest build() {
            return new DiscordTokenRevocationRequest(this);
        }
    }
}
