package me.moirai.discordbot.infrastructure.outbound.adapter.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = RefreshSessionTokenRequest.Builder.class)
public class RefreshSessionTokenRequest {

    @JsonProperty("client_id")
    private final String clientId;

    @JsonProperty("client_secret")
    private final String clientSecret;

    @JsonProperty("refresh_token")
    private final String refreshToken;

    @JsonProperty("grant_type")
    private final String grantType;

    private RefreshSessionTokenRequest(Builder builder) {
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.refreshToken = builder.refreshToken;
        this.grantType = builder.grantType;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getGrantType() {
        return grantType;
    }

    @JsonPOJOBuilder
    public static final class Builder {

        private String clientId;
        private String clientSecret;
        private String refreshToken;
        private String grantType;

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

        public Builder refreshToken(String clientId) {
            this.refreshToken = clientId;
            return this;
        }

        public Builder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public RefreshSessionTokenRequest build() {
            return new RefreshSessionTokenRequest(this);
        }
    }
}
